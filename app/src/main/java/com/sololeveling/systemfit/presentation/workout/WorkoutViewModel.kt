package com.sololeveling.systemfit.presentation.workout

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import com.sololeveling.systemfit.domain.usecase.EmergencyHaltUseCase
import com.sololeveling.systemfit.domain.usecase.GenerateDailyQuestUseCase
import com.sololeveling.systemfit.domain.usecase.ProcessWorkoutResultUseCase
import com.sololeveling.systemfit.presentation.utils.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import java.time.Instant
import java.time.ZoneOffset
import java.time.LocalDate
import com.sololeveling.systemfit.domain.model.hasMissedWorkoutDay


@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val generateDailyQuestUseCase: GenerateDailyQuestUseCase,
    private val processWorkoutResultUseCase: ProcessWorkoutResultUseCase,
    private val emergencyHaltUseCase: EmergencyHaltUseCase
) : ViewModel() {

    private val activeUserId = userRepository.getActiveUserId()

    private val _uiState = MutableStateFlow<WorkoutContract.UiState>(WorkoutContract.UiState.Loading)
    val uiState: StateFlow<WorkoutContract.UiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<WorkoutContract.SideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private var expectedEndTimeMillis = 0L
    private var timerJob: Job? = null
    private var penaltyTimerJob: Job? = null
    private var recoveryTimerJob: Job? = null
    
    // Internal state variables for active combat
    private var dailyQuest: GenerateDailyQuestUseCase.DailyQuest? = null
    private var currentRoundIndex = 1
    private var currentExerciseIndex = 0
    private var isResting = false
    private var totalWorkoutSeconds = 0
    private var isPaused = false
    private var playerLevel = 1
    private var isWarmupActive = false
    private var isCooldownActive = false

    init {
        loadSetup()
    }

    private fun loadSetup() {
        viewModelScope.launch {
            val user = userRepository.getUser(activeUserId) ?: User(id = activeUserId)
            playerLevel = user.level

            // 1. Check for daily skipped workouts (use UTC day calculations for timezone safety)
            val now = System.currentTimeMillis()
            val lastWorkout = user.lastWorkoutTimestamp
            var activeUser = user

            if (!user.penaltyActive && user.hasMissedWorkoutDay(now)) {
                // Missed a scheduled workout day!
                activeUser = user.copy(penaltyActive = true, currentStreak = 0)
                userRepository.saveUser(activeUser)
            }

            // 2. If penalty is active, redirect straight to Penalty Zone
            if (activeUser.penaltyActive) {
                startPenaltyZone()
                SoundManager.playPenalty()
                return@launch
            }

            // 3. Normal setup
            dailyQuest = generateDailyQuestUseCase(activeUser)
            dailyQuest?.let {
                val totalDurationMinutes = (it.totalTargetRounds * it.exercises.size * (it.activeIntervalSeconds + it.restIntervalSeconds)) / 60
                _uiState.value = WorkoutContract.UiState.Setup(max(1, totalDurationMinutes), it.totalTargetRounds)
            }
        }
    }

    fun onEvent(event: WorkoutContract.UiEvent) {
        when (event) {
            WorkoutContract.UiEvent.StartQuest -> startQuest()
            WorkoutContract.UiEvent.SkipRest -> skipRest()
            WorkoutContract.UiEvent.TriggerPanicButton -> triggerPanic()
            WorkoutContract.UiEvent.SkipRecovery -> skipRecovery()
            WorkoutContract.UiEvent.ClaimRewards -> claimRewards()
            WorkoutContract.UiEvent.TogglePause -> togglePause()
            WorkoutContract.UiEvent.NextExercise -> nextExercise()
            WorkoutContract.UiEvent.PrevExercise -> prevExercise()
            WorkoutContract.UiEvent.ExitWorkout -> exitWorkout()
        }
    }

    private fun skipRecovery() {
        recoveryTimerJob?.cancel()
        _uiState.value = WorkoutContract.UiState.Victory(xpEarned = 0, levelUp = false, playerLevel = playerLevel)
    }

    private fun startQuest() {
        val quest = dailyQuest ?: return
        totalWorkoutSeconds = quest.totalTargetRounds * quest.exercises.size * (quest.activeIntervalSeconds + quest.restIntervalSeconds) + 120
        currentRoundIndex = 1
        currentExerciseIndex = 0
        isResting = false
        isPaused = false
        startWarmup()
    }

    private fun skipRest() {
        if (isResting) {
            timerJob?.cancel()
            viewModelScope.launch {
                onTimerComplete()
            }
        }
    }

    private fun triggerPanic() {
        timerJob?.cancel()

        // Play system penalty chime to confirm receipt of emergency command
        SoundManager.playPenalty()

        // Save workout result as partial immediately to log partial XP
        viewModelScope.launch {
            _sideEffects.send(WorkoutContract.SideEffect.TriggerHapticAlert)
            processWorkoutResultUseCase(
                userId = activeUserId,
                isPartialCompletion = true
            )
            // No penalty flag is set. Start controlled recovery directly.
            startControlledRecovery()
        }
    }

    private fun startControlledRecovery() {
        timerJob?.cancel()
        _uiState.value = WorkoutContract.UiState.ControlledRecovery(180)
        startRecoveryCountdown(180)
    }

    private fun startRecoveryCountdown(durationSeconds: Int) {
        recoveryTimerJob?.cancel()
        var remaining = durationSeconds
        recoveryTimerJob = viewModelScope.launch {
            while (remaining >= 0) {
                _uiState.value = WorkoutContract.UiState.ControlledRecovery(remaining)
                if (remaining > 0 && remaining % 6 == 0) {
                    _sideEffects.send(WorkoutContract.SideEffect.PlaySystemChime)
                }
                delay(1000)
                remaining--
            }
            _sideEffects.send(WorkoutContract.SideEffect.PlaySystemChime)
            _uiState.value = WorkoutContract.UiState.Victory(xpEarned = 0, levelUp = false, playerLevel = playerLevel)
        }
    }

    private fun startPenaltyZone() {
        timerJob?.cancel()
        _uiState.value = WorkoutContract.UiState.PenaltyZone(180) // 180 seconds countdown
        startPenaltyCountdown(180)
    }

    private fun startPenaltyCountdown(durationSeconds: Int) {
        penaltyTimerJob?.cancel()
        var remaining = durationSeconds
        penaltyTimerJob = viewModelScope.launch {
            while (remaining >= 0) {
                _uiState.value = WorkoutContract.UiState.PenaltyZone(remaining)
                delay(1000)
                remaining--
            }
            // Cleansing Completed!
            val user = userRepository.getUser(activeUserId)
            if (user != null) {
                val updatedUser = user.copy(penaltyActive = false)
                userRepository.saveUser(updatedUser)
                // Log penalty survival
                userRepository.logWorkout(
                    WorkoutLogEntity(
                        userId = activeUserId,
                        timestamp = System.currentTimeMillis(),
                        xpEarned = 0,
                        isCompleted = true,
                        isPenaltyZone = true,
                        durationMinutes = 5
                    )
                )
            }
            _uiState.value = WorkoutContract.UiState.Victory(xpEarned = 0, levelUp = false, playerLevel = playerLevel)
        }
    }

    private fun togglePause() {
        val state = _uiState.value
        if (state is WorkoutContract.UiState.ActiveCombat) {
            isPaused = !isPaused
            _uiState.value = state.copy(isPaused = isPaused)
            if (isPaused) {
                timerJob?.cancel()
            } else {
                startCountdown(state.timeLeftSeconds)
            }
        } else if (state is WorkoutContract.UiState.Warmup) {
            isPaused = !isPaused
            _uiState.value = state.copy(isPaused = isPaused)
            if (isPaused) {
                timerJob?.cancel()
            } else {
                startWarmupCountdown(state.timeLeftSeconds)
            }
        } else if (state is WorkoutContract.UiState.Cooldown) {
            isPaused = !isPaused
            _uiState.value = state.copy(isPaused = isPaused)
            if (isPaused) {
                timerJob?.cancel()
            } else {
                startCooldownCountdown(state.timeLeftSeconds)
            }
        }
    }

    private fun nextExercise() {
        timerJob?.cancel()
        if (isWarmupActive) {
            onWarmupComplete()
            return
        }
        if (isCooldownActive) {
            onCooldownComplete()
            return
        }
        val quest = dailyQuest ?: return
        isResting = false
        currentExerciseIndex++
        if (currentExerciseIndex >= quest.exercises.size) {
            currentRoundIndex++
            currentExerciseIndex = 0
        }
        startNextInterval()
    }

    private fun prevExercise() {
        timerJob?.cancel()
        if (isWarmupActive) {
            return
        }
        if (isCooldownActive) {
            isCooldownActive = false
            val quest = dailyQuest ?: return
            currentRoundIndex = quest.totalTargetRounds
            currentExerciseIndex = quest.exercises.size - 1
            isResting = false
            startNextInterval()
            return
        }
        isResting = false
        if (currentExerciseIndex > 0) {
            currentExerciseIndex--
            startNextInterval()
        } else if (currentRoundIndex > 1) {
            currentRoundIndex--
            currentExerciseIndex = (dailyQuest?.exercises?.size ?: 1) - 1
            startNextInterval()
        } else {
            startWarmup()
        }
    }

    private fun exitWorkout() {
        timerJob?.cancel()
        penaltyTimerJob?.cancel()
        recoveryTimerJob?.cancel()
        isWarmupActive = false
        isCooldownActive = false
        loadSetup()
    }

    private fun claimRewards() {
        // Clear screen and exit
        exitWorkout()
    }

    private fun startNextInterval() {
        val quest = dailyQuest ?: return
        
        if (currentRoundIndex > quest.totalTargetRounds) {
            startCooldown()
            return
        }

        val currentEx = quest.exercises[currentExerciseIndex]
        val nextEx = if (currentExerciseIndex + 1 < quest.exercises.size) {
            quest.exercises[currentExerciseIndex + 1].name
        } else if (currentRoundIndex < quest.totalTargetRounds) {
            quest.exercises[0].name
        } else {
            null
        }

        val duration = if (isResting) quest.restIntervalSeconds else quest.activeIntervalSeconds

        viewModelScope.launch {
            val user = userRepository.getUser(activeUserId)
            val isBpMode = user?.bpModeActive ?: false

            _uiState.value = WorkoutContract.UiState.ActiveCombat(
                currentRound = currentRoundIndex,
                totalRounds = quest.totalTargetRounds,
                currentExercise = currentEx,
                nextExerciseName = nextEx,
                isRestPeriod = isResting,
                timeLeftSeconds = duration,
                totalTimeLeftSeconds = totalWorkoutSeconds,
                isPaused = isPaused,
                isBpModeActive = isBpMode
            )
        }
        
        if (!isPaused) {
            startCountdown(duration)
        }
    }

    private fun startCountdown(durationSeconds: Int) {
        timerJob?.cancel()
        expectedEndTimeMillis = SystemClock.elapsedRealtime() + (durationSeconds * 1000)
        
        timerJob = viewModelScope.launch {
            while (SystemClock.elapsedRealtime() < expectedEndTimeMillis) {
                if (isPaused) return@launch
                val remaining = ((expectedEndTimeMillis - SystemClock.elapsedRealtime()) / 1000).toInt()
                updateTimerState(max(0, remaining))
                delay(200)
            }
            onTimerComplete()
        }
    }

    private fun updateTimerState(remainingSeconds: Int) {
        val currentState = _uiState.value
        if (currentState is WorkoutContract.UiState.ActiveCombat) {
            _uiState.value = currentState.copy(timeLeftSeconds = remainingSeconds)
        }
    }

    private suspend fun onTimerComplete() {
        _sideEffects.send(WorkoutContract.SideEffect.PlaySystemChime)
        if (isResting) {
            isResting = false
            currentExerciseIndex++
            if (currentExerciseIndex >= (dailyQuest?.exercises?.size ?: 0)) {
                currentRoundIndex++
                currentExerciseIndex = 0
            }
        } else {
            isResting = true
        }
        startNextInterval()
    }

    private fun finishQuest() {
        viewModelScope.launch {
            val userBefore = userRepository.getUser(activeUserId)
            val updatedUser = processWorkoutResultUseCase(
                userId = activeUserId,
                isPartialCompletion = false
            )
            val levelUp = if (userBefore != null && updatedUser != null) {
                updatedUser.level > userBefore.level
            } else false

            val xpEarned = if (userBefore != null) {
                val lvl = userBefore.level
                (50.0 * (1.0 + (lvl.toDouble() * 0.15))).toInt()
            } else {
                200
            }

            if (updatedUser != null) {
                playerLevel = updatedUser.level
            }

            if (levelUp) {
                SoundManager.playLevelUp()
            }

            _uiState.value = WorkoutContract.UiState.Victory(
                xpEarned = xpEarned,
                levelUp = levelUp,
                playerLevel = playerLevel
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        penaltyTimerJob?.cancel()
        recoveryTimerJob?.cancel()
    }

    private fun startWarmup() {
        isWarmupActive = true
        isCooldownActive = false
        isPaused = false
        _uiState.value = WorkoutContract.UiState.Warmup(60, isPaused = false)
        startWarmupCountdown(60)
    }

    private fun startWarmupCountdown(durationSeconds: Int) {
        timerJob?.cancel()
        expectedEndTimeMillis = SystemClock.elapsedRealtime() + (durationSeconds * 1000)
        timerJob = viewModelScope.launch {
            while (SystemClock.elapsedRealtime() < expectedEndTimeMillis) {
                if (isPaused) return@launch
                val remaining = ((expectedEndTimeMillis - SystemClock.elapsedRealtime()) / 1000).toInt()
                val currentState = _uiState.value
                if (currentState is WorkoutContract.UiState.Warmup) {
                    _uiState.value = currentState.copy(timeLeftSeconds = max(0, remaining))
                }
                delay(200)
            }
            onWarmupComplete()
        }
    }

    private fun onWarmupComplete() {
        isWarmupActive = false
        currentRoundIndex = 1
        currentExerciseIndex = 0
        isResting = false
        startNextInterval()
    }

    private fun startCooldown() {
        isWarmupActive = false
        isCooldownActive = true
        isPaused = false
        _uiState.value = WorkoutContract.UiState.Cooldown(60, isPaused = false)
        startCooldownCountdown(60)
    }

    private fun startCooldownCountdown(durationSeconds: Int) {
        timerJob?.cancel()
        expectedEndTimeMillis = SystemClock.elapsedRealtime() + (durationSeconds * 1000)
        timerJob = viewModelScope.launch {
            while (SystemClock.elapsedRealtime() < expectedEndTimeMillis) {
                if (isPaused) return@launch
                val remaining = ((expectedEndTimeMillis - SystemClock.elapsedRealtime()) / 1000).toInt()
                val currentState = _uiState.value
                if (currentState is WorkoutContract.UiState.Cooldown) {
                    _uiState.value = currentState.copy(timeLeftSeconds = max(0, remaining))
                }
                delay(200)
            }
            onCooldownComplete()
        }
    }

    private fun onCooldownComplete() {
        isCooldownActive = false
        finishQuest()
    }
}
