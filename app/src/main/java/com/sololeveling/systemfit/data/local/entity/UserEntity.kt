package com.sololeveling.systemfit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val level: Int,
    val currentXp: Int,
    val str: Int,
    val vit: Int,
    val agi: Int,
    val availableStatPoints: Int,
    val currentStreak: Int
)
