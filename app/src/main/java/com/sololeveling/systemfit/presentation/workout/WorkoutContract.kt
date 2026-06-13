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
            val totalTimeLeftSeconds: Int
        ) : UiState
        data class PenaltyZone(val penaltyDurationMinutes: Int) : UiState
        data class Victory(val xpEarned: Int, val levelUp: Boolean) : UiState
    }

    sealed interface UiEvent {
        object StartQuest : UiEvent
        object SkipRest : UiEvent
        object TriggerPanicButton : UiEvent // Immediate Emergency Halt
        object ClaimRewards : UiEvent
    }

    sealed interface SideEffect {
        object PlaySystemChime : SideEffect
        object TriggerHapticAlert : SideEffect
        data class ShowToast(val message: String) : SideEffect
    }
}
