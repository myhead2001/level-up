package com.sololeveling.systemfit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLogEntity)

    @Query("SELECT * FROM workout_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLogsStream(userId: String): Flow<List<WorkoutLogEntity>>

    @Query("SELECT * FROM workout_logs WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getLogs(userId: String): List<WorkoutLogEntity>

    @Query("DELETE FROM workout_logs WHERE userId = :userId")
    suspend fun clearLogs(userId: String)
}
