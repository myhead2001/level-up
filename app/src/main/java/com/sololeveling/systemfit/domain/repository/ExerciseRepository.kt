package com.sololeveling.systemfit.domain.repository

import com.sololeveling.systemfit.domain.model.Exercise

interface ExerciseRepository {
    suspend fun getHtnSafeExercises(): List<Exercise>
}
