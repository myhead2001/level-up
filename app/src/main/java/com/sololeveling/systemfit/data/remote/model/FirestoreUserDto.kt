package com.sololeveling.systemfit.data.remote.model

data class FirestoreUserDto(
    val id: String = "",
    val level: Int = 1,
    val currentXp: Int = 0,
    val str: Int = 10,
    val vit: Int = 10,
    val agi: Int = 10,
    val availableStatPoints: Int = 0,
    val currentStreak: Int = 0
)
