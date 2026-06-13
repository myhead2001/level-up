package com.sololeveling.systemfit.domain.usecase

import com.sololeveling.systemfit.domain.model.Exercise
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.ExerciseRepository
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class GenerateDailyQuestUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    data class DailyQuest(
        val exercises: List<Exercise>,
        val activeIntervalSeconds: Int,
        val restIntervalSeconds: Int,
        val totalTargetRounds: Int
    )

    suspend operator fun invoke(user: User): DailyQuest {
        // Active Interval Duration: respect custom timer if set, otherwise fallback to formula
        val activeInterval = if (user.customActiveDurationSeconds > 0) {
            user.customActiveDurationSeconds
        } else {
            min(20 + (user.agi * 2), 60)
        }
        
        // Rest Interval Duration: respect custom timer if set, otherwise fallback to formula
        val restInterval = if (user.customRestDurationSeconds > 0) {
            user.customRestDurationSeconds
        } else {
            max(90 - (user.vit * 3), 30)
        }
        
        // Total Target Rounds: min(2 + floor(Level / 3), 5)
        val totalRounds = min(2 + (user.level / 3), 5)
        
        // Exclude intense isometric / head-below-heart positions if BP safe mode is enabled
        val exercises = if (user.bpModeActive) {
            exerciseRepository.getHtnSafeExercises()
        } else {
            exerciseRepository.getAllExercises()
        }
        
        // Form a day-based seed so quest generation is deterministic for any given day
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val seed = (year * 366 + dayOfYear).toLong()
        val random = java.util.Random(seed)
        
        val selectedExercises = exercises.shuffled(random).take(4)

        return DailyQuest(
            exercises = selectedExercises,
            activeIntervalSeconds = activeInterval,
            restIntervalSeconds = restInterval,
            totalTargetRounds = totalRounds
        )
    }
}
