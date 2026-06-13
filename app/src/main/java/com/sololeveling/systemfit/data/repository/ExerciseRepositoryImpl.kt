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
        return exerciseDao.getHtnSafeExercises().map { it.toDomainModel() }
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
