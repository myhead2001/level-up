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

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val generateDailyQuestUseCase: GenerateDailyQuestUseCase,
    private val processWorkoutResultUseCase: ProcessWorkoutResultUseCase,
    private val emergencyHaltUseCase: EmergencyHaltUseCase
) : ViewModel() {

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

    init {
        loadSetup()
    }

    private fun loadSetup() {
        viewModelScope.launch {
            val user = userRepository.getUser("player_1") ?: User(id = "player_1")
            playerLevel = user.level

            // 1. Check for daily skipped workouts (if last workout was >24 hours ago, trigger penalty)
            val now = System.currentTimeMillis()
            val lastWorkout = user.lastWorkoutTimestamp
            var activeUser = user

            if (lastWorkout > 0L) {
                val daysDiff = ((now - lastWorkout) / (1000 * 60 * 60 * 24)).toInt()
                if (daysDiff >= 2) {
                    // Missed an entire calendar day!
                    activeUser = user.copy(penaltyActive = true, currentStreak = 0)
                    userRepository.saveUser(activeUser)
                }
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
        totalWorkoutSeconds = quest.totalTargetRounds * quest.exercises.size * (quest.activeIntervalSeconds + quest.restIntervalSeconds)
        currentRoundIndex = 1
        currentExerciseIndex = 0
        isResting = false
        isPaused = false
        startNextInterval()
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
                userId = "player_1",
                isPartialCompletion = true,
                baseXpEarned = 100
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
            val user = userRepository.getUser("player_1")
            if (user != null) {
                val updatedUser = user.copy(penaltyActive = false)
                userRepository.saveUser(updatedUser)
                // Log penalty survival
                userRepository.logWorkout(
                    WorkoutLogEntity(
                        userId = "player_1",
                        timestamp = System.currentTimeMillis(),
                        xpEarned = 0,
                        isCompleted = true,
                        isPenaltyZone = true
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
        }
    }

    private fun nextExercise() {
        timerJob?.cancel()
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
        isResting = false
        if (currentExerciseIndex > 0) {
            currentExerciseIndex--
        } else if (currentRoundIndex > 1) {
            currentRoundIndex--
            currentExerciseIndex = (dailyQuest?.exercises?.size ?: 1) - 1
        }
        startNextInterval()
    }

    private fun exitWorkout() {
        timerJob?.cancel()
        penaltyTimerJob?.cancel()
        loadSetup()
    }

    private fun claimRewards() {
        // Clear screen and exit
        exitWorkout()
    }

    private fun startNextInterval() {
        val quest = dailyQuest ?: return
        
        if (currentRoundIndex > quest.totalTargetRounds) {
            finishQuest()
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

        _uiState.value = WorkoutContract.UiState.ActiveCombat(
            currentRound = currentRoundIndex,
            totalRounds = quest.totalTargetRounds,
            currentExercise = currentEx,
            nextExerciseName = nextEx,
            isRestPeriod = isResting,
            timeLeftSeconds = duration,
            totalTimeLeftSeconds = totalWorkoutSeconds,
            isPaused = isPaused
        )
        
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
            val userBefore = userRepository.getUser("player_1")
            val updatedUser = processWorkoutResultUseCase(
                userId = "player_1",
                isPartialCompletion = false,
                baseXpEarned = 200
            )
            val levelUp = if (userBefore != null && updatedUser != null) {
                updatedUser.level > userBefore.level
            } else false

            if (updatedUser != null) {
                playerLevel = updatedUser.level
            }

            if (levelUp) {
                SoundManager.playLevelUp()
            }

            _uiState.value = WorkoutContract.UiState.Victory(
                xpEarned = 200,
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
}
