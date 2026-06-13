package com.sololeveling.systemfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val level: Int,
    val currentXp: Int,
    val str: Int,
    val vit: Int,
    val agi: Int,
    val availableStatPoints: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val theme: String,
    val targetWorkoutDaysPerWeek: Int,
    val customActiveDurationSeconds: Int,
    val customRestDurationSeconds: Int,
    val lastWorkoutTimestamp: Long,
    val penaltyActive: Boolean,
    val bpModeActive: Boolean = true,
    val isDarkMode: Boolean = true,
    val skipIntro: Boolean = false
)
