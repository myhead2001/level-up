package com.sololeveling.systemfit.data.repository

import com.sololeveling.systemfit.data.local.dao.ExerciseDao
import com.sololeveling.systemfit.data.local.entity.ExerciseEntity
import com.sololeveling.systemfit.domain.model.Exercise
import com.sololeveling.systemfit.domain.repository.ExerciseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override suspend fun getHtnSafeExercises(): List<Exercise> {
        val list = exerciseDao.getHtnSafeExercises()
        if (list.isEmpty()) {
            val defaultExercises = listOf(
                ExerciseEntity("ex_1", "Wall Sit", com.sololeveling.systemfit.domain.model.ExerciseType.ISOMETRIC, "Hold a seated position against a wall.", "placeholder", true),
                ExerciseEntity("ex_2", "Glute Bridge", com.sololeveling.systemfit.domain.model.ExerciseType.ISOMETRIC, "Lift your hips off the floor and hold.", "placeholder", true),
                ExerciseEntity("ex_3", "Plank", com.sololeveling.systemfit.domain.model.ExerciseType.ISOMETRIC, "Maintain a straight body position on elbows.", "placeholder", true),
                ExerciseEntity("ex_4", "Side Plank", com.sololeveling.systemfit.domain.model.ExerciseType.ISOMETRIC, "Support your body on one side.", "placeholder", true),
                ExerciseEntity("ex_5", "Walking", com.sololeveling.systemfit.domain.model.ExerciseType.CARDIO, "Brisk walking in place.", "placeholder", true),
                ExerciseEntity("ex_6", "Light Jogging", com.sololeveling.systemfit.domain.model.ExerciseType.CARDIO, "Gentle jogging in place.", "placeholder", true),
                ExerciseEntity("ex_7", "Cycling", com.sololeveling.systemfit.domain.model.ExerciseType.CARDIO, "Air cycling movements on back.", "placeholder", true),
                ExerciseEntity("ex_8", "Arm Circles", com.sololeveling.systemfit.domain.model.ExerciseType.FLEXIBILITY, "Controlled circular arm motions.", "placeholder", true),
                ExerciseEntity("ex_9", "Neck Stretches", com.sololeveling.systemfit.domain.model.ExerciseType.FLEXIBILITY, "Gentle stretches for the neck.", "placeholder", true),
                ExerciseEntity("ex_10", "Leg Swings", com.sololeveling.systemfit.domain.model.ExerciseType.FLEXIBILITY, "Controlled leg swings.", "placeholder", true),
                ExerciseEntity("ex_11", "Bodyweight Squats", com.sololeveling.systemfit.domain.model.ExerciseType.STRENGTH, "Basic unweighted squats.", "placeholder", true),
                ExerciseEntity("ex_12", "Modified Pushups", com.sololeveling.systemfit.domain.model.ExerciseType.STRENGTH, "Pushups from the knees.", "placeholder", true),
                ExerciseEntity("ex_13", "Lunges", com.sololeveling.systemfit.domain.model.ExerciseType.STRENGTH, "Controlled forward lunges.", "placeholder", true),
                ExerciseEntity("ex_14", "Calf Raises", com.sololeveling.systemfit.domain.model.ExerciseType.STRENGTH, "Lift onto the toes.", "placeholder", true)
            )
            exerciseDao.insertExercises(defaultExercises)
            return defaultExercises.map { it.toDomainModel() }
        }
        return list.map { it.toDomainModel() }
    }

    private fun ExerciseEntity.toDomainModel() = Exercise(
        id = id,
        name = name,
        type = type,
        description = description,
        gifUrl = gifUrl,
        isHtnSafe = isHtnSafe
    )
}
