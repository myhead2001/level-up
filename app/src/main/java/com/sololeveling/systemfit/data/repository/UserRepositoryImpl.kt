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
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val workoutLogDao: WorkoutLogDao,
    private val remoteSyncSource: RemoteSyncSource,
    @ApplicationContext private val context: Context
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
        // Sync to remote in background (non-blocking)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            remoteSyncSource.syncUser(user)
        }
    }

    override fun getWorkoutLogsStream(userId: String): Flow<List<WorkoutLogEntity>> {
        return workoutLogDao.getLogsStream(userId)
    }

    override suspend fun logWorkout(log: WorkoutLogEntity) {
        workoutLogDao.insertLog(log)
    }

    override suspend fun resetDatabase(userId: String) {
        workoutLogDao.clearLogs(userId)
        userDao.deleteUser(userId)
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
                penaltyActive = false,
                bpModeActive = true,
                isDarkMode = true,
                skipIntro = false
            )
        )
    }

    override suspend fun backupProfile(userId: String) {
        val user = userDao.getUser(userId) ?: return
        val sharedPrefs = context.getSharedPreferences("system_fit_backup", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("backup_name", user.name)
            putInt("backup_level", user.level)
            putInt("backup_currentXp", user.currentXp)
            putInt("backup_str", user.str)
            putInt("backup_vit", user.vit)
            putInt("backup_agi", user.agi)
            putInt("backup_availableStatPoints", user.availableStatPoints)
            putInt("backup_currentStreak", user.currentStreak)
            putInt("backup_bestStreak", user.bestStreak)
            putString("backup_theme", user.theme)
            putInt("backup_targetWorkoutDaysPerWeek", user.targetWorkoutDaysPerWeek)
            putInt("backup_customActiveDurationSeconds", user.customActiveDurationSeconds)
            putInt("backup_customRestDurationSeconds", user.customRestDurationSeconds)
            putLong("backup_lastWorkoutTimestamp", user.lastWorkoutTimestamp)
            putBoolean("backup_penaltyActive", user.penaltyActive)
            putBoolean("backup_bpModeActive", user.bpModeActive)
            putBoolean("backup_isDarkMode", user.isDarkMode)
            putBoolean("backup_skipIntro", user.skipIntro)
            apply()
        }
    }

    override suspend fun restoreProfile(userId: String): Boolean {
        val sharedPrefs = context.getSharedPreferences("system_fit_backup", Context.MODE_PRIVATE)
        if (!sharedPrefs.contains("backup_name")) return false
        val backupUser = UserEntity(
            id = userId,
            name = sharedPrefs.getString("backup_name", "Sung Jin-Woo") ?: "Sung Jin-Woo",
            level = sharedPrefs.getInt("backup_level", 1),
            currentXp = sharedPrefs.getInt("backup_currentXp", 0),
            str = sharedPrefs.getInt("backup_str", 10),
            vit = sharedPrefs.getInt("backup_vit", 10),
            agi = sharedPrefs.getInt("backup_agi", 10),
            availableStatPoints = sharedPrefs.getInt("backup_availableStatPoints", 0),
            currentStreak = sharedPrefs.getInt("backup_currentStreak", 0),
            bestStreak = sharedPrefs.getInt("backup_bestStreak", 0),
            theme = sharedPrefs.getString("backup_theme", "SOLO_BLUE") ?: "SOLO_BLUE",
            targetWorkoutDaysPerWeek = sharedPrefs.getInt("backup_targetWorkoutDaysPerWeek", 5),
            customActiveDurationSeconds = sharedPrefs.getInt("backup_customActiveDurationSeconds", 0),
            customRestDurationSeconds = sharedPrefs.getInt("backup_customRestDurationSeconds", 0),
            lastWorkoutTimestamp = sharedPrefs.getLong("backup_lastWorkoutTimestamp", 0L),
            penaltyActive = sharedPrefs.getBoolean("backup_penaltyActive", false),
            bpModeActive = sharedPrefs.getBoolean("backup_bpModeActive", true),
            isDarkMode = sharedPrefs.getBoolean("backup_isDarkMode", true),
            skipIntro = sharedPrefs.getBoolean("backup_skipIntro", false)
        )
        userDao.insertUser(backupUser)
        return true
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
        penaltyActive = penaltyActive,
        bpModeActive = bpModeActive,
        isDarkMode = isDarkMode,
        skipIntro = skipIntro
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
        penaltyActive = penaltyActive,
        bpModeActive = bpModeActive,
        isDarkMode = isDarkMode,
        skipIntro = skipIntro
    )
}
