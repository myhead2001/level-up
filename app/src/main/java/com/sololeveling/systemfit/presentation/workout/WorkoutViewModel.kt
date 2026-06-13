package com.sololeveling.systemfit.presentation.workout

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.usecase.EmergencyHaltUseCase
import com.sololeveling.systemfit.domain.usecase.GenerateDailyQuestUseCase
import com.sololeveling.systemfit.domain.usecase.ProcessWorkoutResultUseCase
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
    
    // Internal state variables for active combat
    private var dailyQuest: GenerateDailyQuestUseCase.DailyQuest? = null
    private var currentRoundIndex = 1
    private var currentExerciseIndex = 0
    private var isResting = false
    private var totalWorkoutSeconds = 0

    init {
        loadSetup()
    }

    private fun loadSetup() {
        viewModelScope.launch {
            // For now, assume a dummy user context. In a real app, this comes from a flow.
            val dummyUser = User(id = "player_1", level = 1, str = 10, vit = 10, agi = 10)
            dailyQuest = generateDailyQuestUseCase(dummyUser)
            dailyQuest?.let {
                val totalDurationMinutes = (it.totalTargetRounds * it.exercises.size * (it.activeIntervalSeconds + it.restIntervalSeconds)) / 60
                _uiState.value = WorkoutContract.UiState.Setup(totalDurationMinutes, it.totalTargetRounds)
            }
        }
    }

    fun onEvent(event: WorkoutContract.UiEvent) {
        when (event) {
            WorkoutContract.UiEvent.StartQuest -> startQuest()
            WorkoutContract.UiEvent.SkipRest -> skipRest()
            WorkoutContract.UiEvent.TriggerPanicButton -> triggerPanic()
            WorkoutContract.UiEvent.ClaimRewards -> claimRewards()
        }
    }

    private fun startQuest() {
        val quest = dailyQuest ?: return
        totalWorkoutSeconds = quest.totalTargetRounds * quest.exercises.size * (quest.activeIntervalSeconds + quest.restIntervalSeconds)
        currentRoundIndex = 1
        currentExerciseIndex = 0
        isResting = false
        startNextInterval()
    }

    private fun skipRest() {
        if (isResting) {
            timerJob?.cancel()
            onTimerComplete()
        }
    }

    private fun triggerPanic() {
        timerJob?.cancel()
        val cooldown = emergencyHaltUseCase.invokeHalt()
        _uiState.value = WorkoutContract.UiState.PenaltyZone(cooldown.remainingCooldownSeconds / 60)
        
        viewModelScope.launch {
            _sideEffects.send(WorkoutContract.SideEffect.TriggerHapticAlert)
            // System tone generation is handled by side effect logic in UI usually
            
            // Process penalty XP immediately
            processWorkoutResultUseCase(
                userId = "player_1",
                isPartialCompletion = true,
                baseXpEarned = 100 // Example base xp for the quest
            )
        }
    }

    private fun claimRewards() {
        // Handle claiming logic or navigation via side effect if needed
    }

    private fun startNextInterval() {
        val quest = dailyQuest ?: return
        
        if (currentRoundIndex > quest.totalTargetRounds) {
            finishQuest()
            return
        }

        if (currentExerciseIndex >= quest.exercises.size) {
            currentRoundIndex++
            currentExerciseIndex = 0
            if (currentRoundIndex > quest.totalTargetRounds) {
                finishQuest()
                return
            }
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
            totalTimeLeftSeconds = totalWorkoutSeconds // Decrement in actual implementation
        )
        
        startCountdown(duration)
    }

    private fun startCountdown(durationSeconds: Int) {
        timerJob?.cancel()
        expectedEndTimeMillis = SystemClock.elapsedRealtime() + (durationSeconds * 1000)
        
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (SystemClock.elapsedRealtime() < expectedEndTimeMillis) {
                val remaining = ((expectedEndTimeMillis - SystemClock.elapsedRealtime()) / 1000).toInt()
                updateTimerState(max(0, remaining))
                delay(200) // High frequency sampling keeps precision without CPU utilization spikes
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

    private fun onTimerComplete() {
        viewModelScope.launch {
            _sideEffects.send(WorkoutContract.SideEffect.PlaySystemChime)
            if (isResting) {
                isResting = false
                currentExerciseIndex++
            } else {
                isResting = true
            }
            startNextInterval()
        }
    }

    private fun finishQuest() {
        viewModelScope.launch {
            // Process standard XP
            processWorkoutResultUseCase(
                userId = "player_1",
                isPartialCompletion = false,
                baseXpEarned = 200
            )
            _uiState.value = WorkoutContract.UiState.Victory(
                xpEarned = 200,
                levelUp = true // Dummy flag, actual check depends on previous vs new level
            )
        }
    }
}
