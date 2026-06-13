package com.sololeveling.systemfit.domain.model

data class User(
    val id: String = "player_1",
    val level: Int = 1,
    val currentXp: Int = 0,
    val str: Int = 10,
    val vit: Int = 10,
    val agi: Int = 10,
    val availableStatPoints: Int = 0,
    val currentStreak: Int = 0
) {
    val requiredXpForNextLevel: Int
        get() = (100 * Math.pow(level.toDouble(), 1.5)).toInt()
}
