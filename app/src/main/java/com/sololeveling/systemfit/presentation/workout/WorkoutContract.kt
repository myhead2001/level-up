package com.sololeveling.systemfit.presentation.workout

import com.sololeveling.systemfit.domain.model.Exercise

interface WorkoutContract {
    sealed interface UiState {
        object Loading : UiState
        data class Setup(val totalDurationMinutes: Int, val rounds: Int) : UiState
        data class ActiveCombat(
            val currentRound: Int,
            val totalRounds: Int,
            val currentExercise: Exercise,
            val nextExerciseName: String?,
            val isRestPeriod: Boolean,
            val timeLeftSeconds: Int,
            val totalTimeLeftSeconds: Int,
            val isPaused: Boolean = false
        ) : UiState
        data class PenaltyZone(val timeLeftSeconds: Int) : UiState
        data class ControlledRecovery(val timeLeftSeconds: Int) : UiState
        data class Victory(val xpEarned: Int, val levelUp: Boolean, val playerLevel: Int) : UiState
    }

    sealed interface UiEvent {
        object StartQuest : UiEvent
        object SkipRest : UiEvent
        object TriggerPanicButton : UiEvent // Immediate Emergency Halt
        object SkipRecovery : UiEvent
        object ClaimRewards : UiEvent
        object TogglePause : UiEvent
        object NextExercise : UiEvent
        object PrevExercise : UiEvent
        object ExitWorkout : UiEvent
    }

    sealed interface SideEffect {
        object PlaySystemChime : SideEffect
        object TriggerHapticAlert : SideEffect
        data class ShowToast(val message: String) : SideEffect
    }
}
