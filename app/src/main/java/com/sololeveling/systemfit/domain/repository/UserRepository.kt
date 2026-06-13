package com.sololeveling.systemfit.domain.repository

import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserStream(userId: String): Flow<User?>
    suspend fun getUser(userId: String): User?
    suspend fun saveUser(user: User)
    fun getWorkoutLogsStream(userId: String): Flow<List<WorkoutLogEntity>>
    suspend fun logWorkout(log: WorkoutLogEntity)
    suspend fun resetDatabase(userId: String)
    suspend fun backupProfile(userId: String)
    suspend fun restoreProfile(userId: String): Boolean
}
