package com.sololeveling.systemfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0L,
    val userId: String,
    val timestamp: Long,
    val xpEarned: Int,
    val isCompleted: Boolean,
    val isPenaltyZone: Boolean = false,
    val durationMinutes: Int = 15
)
