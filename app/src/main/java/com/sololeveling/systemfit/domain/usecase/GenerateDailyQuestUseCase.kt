package com.sololeveling.systemfit.domain.usecase

import com.sololeveling.systemfit.domain.model.Exercise
import com.sololeveling.systemfit.domain.model.ExerciseType
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.ExerciseRepository
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.exp

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
        // Calculate Active Interval, Rest Interval, and Rounds based on BP mode or standard mode
        val activeInterval: Int
        val restInterval: Int
        val totalRounds: Int

        if (user.bpModeActive) {
            // BP Safe formulas
            restInterval = if (user.customRestDurationSeconds > 0) {
                user.customRestDurationSeconds
            } else {
                45 + (45 * exp(-0.04 * user.vit)).toInt()
            }

            activeInterval = if (user.customActiveDurationSeconds > 0) {
                user.customActiveDurationSeconds
            } else {
                20 + (25 * (1.0 - exp(-0.04 * user.agi))).toInt()
            }

            totalRounds = min(2 + (user.level / 8), 4)
        } else {
            // Standard formulas
            restInterval = if (user.customRestDurationSeconds > 0) {
                user.customRestDurationSeconds
            } else {
                30 + (60 * exp(-0.04 * user.vit)).toInt()
            }

            activeInterval = if (user.customActiveDurationSeconds > 0) {
                user.customActiveDurationSeconds
            } else {
                20 + (40 * (1.0 - exp(-0.04 * user.agi))).toInt()
            }

            totalRounds = min(2 + (user.level / 5), 5)
        }

        // Fetch exercises from repository
        val exercises = if (user.bpModeActive) {
            exerciseRepository.getHtnSafeExercises()
        } else {
            exerciseRepository.getAllExercises()
        }

        // Filter available exercises based on player STR
        val availableExercises = exercises.filter { it.requiredStr <= user.str }

        // Form a day-based seed so quest generation is deterministic for any given day
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val seed = (year * 366 + dayOfYear).toLong()
        val random = java.util.Random(seed)

        val selectedExercises = mutableListOf<Exercise>()

        if (user.bpModeActive) {
            // BP Slots:
            // 1. Warm-up Cardio (isHtnSafe=true, type=CARDIO)
            val slot1 = availableExercises.filter { it.type == ExerciseType.CARDIO }.shuffled(random).firstOrNull()
            if (slot1 != null) selectedExercises.add(slot1)

            // 2. Strength Push (isHtnSafe=true, type=STRENGTH)
            val slot2 = availableExercises.filter { it.type == ExerciseType.STRENGTH }.shuffled(random).firstOrNull()
            if (slot2 != null) selectedExercises.add(slot2)

            // 3. Core (isHtnSafe=true, type=FLEXIBILITY) - dead bugs, bird-dog, leg raises are FLEXIBILITY
            val slot3 = availableExercises.filter { it.type == ExerciseType.FLEXIBILITY && it.name in listOf("Bird-Dog", "Dead Bugs", "Lying Leg Raises (Single leg)") }.shuffled(random).firstOrNull()
                ?: availableExercises.filter { it.type == ExerciseType.FLEXIBILITY }.shuffled(random).firstOrNull()
            if (slot3 != null) selectedExercises.add(slot3)

            // 4. Active Recovery (isHtnSafe=true, type=FLEXIBILITY or CARDIO, distinct from slot1)
            val slot4 = availableExercises.filter { 
                (it.type == ExerciseType.FLEXIBILITY || it.type == ExerciseType.CARDIO) && 
                !selectedExercises.contains(it) 
            }.shuffled(random).firstOrNull()
            if (slot4 != null) selectedExercises.add(slot4)
        } else {
            // Standard Slots:
            // 1. Cardio (type=CARDIO)
            val slot1 = availableExercises.filter { it.type == ExerciseType.CARDIO }.shuffled(random).firstOrNull()
            if (slot1 != null) selectedExercises.add(slot1)

            // 2. Strength (type=STRENGTH)
            val slot2 = availableExercises.filter { it.type == ExerciseType.STRENGTH }.shuffled(random).firstOrNull()
            if (slot2 != null) selectedExercises.add(slot2)

            // 3. Cardio (type=CARDIO, distinct)
            val slot3 = availableExercises.filter { it.type == ExerciseType.CARDIO && !selectedExercises.contains(it) }.shuffled(random).firstOrNull()
            if (slot3 != null) selectedExercises.add(slot3)

            // 4. Flexibility/Isometric (type=FLEXIBILITY or type=ISOMETRIC)
            val slot4 = availableExercises.filter { it.type == ExerciseType.FLEXIBILITY || it.type == ExerciseType.ISOMETRIC }.shuffled(random).firstOrNull()
            if (slot4 != null) selectedExercises.add(slot4)
        }

        // Fallback in case list is empty or we couldn't select enough exercises
        while (selectedExercises.size < 4 && availableExercises.isNotEmpty()) {
            val nextEx = availableExercises.filter { !selectedExercises.contains(it) }.shuffled(random).firstOrNull() ?: break
            selectedExercises.add(nextEx)
        }

        return DailyQuest(
            exercises = selectedExercises.take(4),
            activeIntervalSeconds = activeInterval,
            restIntervalSeconds = restInterval,
            totalTargetRounds = totalRounds
        )
    }
}
