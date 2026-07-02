package com.sololeveling.systemfit.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUserDto(
    val id: String = "",
    val name: String = "Player",
    val role: String = "player",
    val level: Int = 1,
    val currentXp: Int = 0,
    val str: Int = 10,
    val vit: Int = 10,
    val agi: Int = 10,
    val availableStatPoints: Int = 0,
    val currentStreak: Int = 0
)
