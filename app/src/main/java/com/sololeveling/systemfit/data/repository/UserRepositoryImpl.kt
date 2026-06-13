package com.sololeveling.systemfit.data.repository

import com.sololeveling.systemfit.data.local.dao.UserDao
import com.sololeveling.systemfit.data.local.dao.WorkoutLogDao
import com.sololeveling.systemfit.data.local.entity.UserEntity
import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import com.sololeveling.systemfit.data.remote.DataSource.RemoteSyncSource
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val workoutLogDao: WorkoutLogDao,
    private val remoteSyncSource: RemoteSyncSource
) : UserRepository {

    override fun getUserStream(userId: String): Flow<User?> {
        return userDao.getUserStream(userId).map { entity ->
            if (entity == null) {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    if (userDao.getUser(userId) == null) {
                        userDao.insertUser(
                            UserEntity(
                                id = userId,
                                name = "Sung Jin-Woo",
                                level = 1,
                                currentXp = 0,
                                str = 10,
                                vit = 10,
                                agi = 10,
                                availableStatPoints = 0,
                                currentStreak = 0,
                                bestStreak = 0,
                                theme = "SOLO_BLUE",
                                targetWorkoutDaysPerWeek = 5,
                                customActiveDurationSeconds = 0,
                                customRestDurationSeconds = 0,
                                lastWorkoutTimestamp = 0L,
                                penaltyActive = false
                            )
                        )
                    }
                }
            }
            entity?.toDomainModel()
        }
    }

    override suspend fun getUser(userId: String): User? {
        return userDao.getUser(userId)?.toDomainModel()
    }

    override suspend fun saveUser(user: User) {
        val entity = user.toEntity()
        userDao.insertUser(entity)
        // Sync to remote in background
        remoteSyncSource.syncUser(user)
    }

    override fun getWorkoutLogsStream(userId: String): Flow<List<WorkoutLogEntity>> {
        return workoutLogDao.getLogsStream(userId)
    }

    override suspend fun logWorkout(log: WorkoutLogEntity) {
        workoutLogDao.insertLog(log)
    }

    private fun UserEntity.toDomainModel() = User(
        id = id,
        name = name,
        level = level,
        currentXp = currentXp,
        str = str,
        vit = vit,
        agi = agi,
        availableStatPoints = availableStatPoints,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        theme = theme,
        targetWorkoutDaysPerWeek = targetWorkoutDaysPerWeek,
        customActiveDurationSeconds = customActiveDurationSeconds,
        customRestDurationSeconds = customRestDurationSeconds,
        lastWorkoutTimestamp = lastWorkoutTimestamp,
        penaltyActive = penaltyActive
    )

    private fun User.toEntity() = UserEntity(
        id = id,
        name = name,
        level = level,
        currentXp = currentXp,
        str = str,
        vit = vit,
        agi = agi,
        availableStatPoints = availableStatPoints,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        theme = theme,
        targetWorkoutDaysPerWeek = targetWorkoutDaysPerWeek,
        customActiveDurationSeconds = customActiveDurationSeconds,
        customRestDurationSeconds = customRestDurationSeconds,
        lastWorkoutTimestamp = lastWorkoutTimestamp,
        penaltyActive = penaltyActive
    )
}
