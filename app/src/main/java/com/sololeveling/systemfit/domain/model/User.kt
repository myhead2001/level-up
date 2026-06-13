package com.sololeveling.systemfit.domain.model

data class User(
    val id: String = "player_1",
    val name: String = "Sung Jin-Woo",
    val level: Int = 1,
    val currentXp: Int = 0,
    val str: Int = 10,
    val vit: Int = 10,
    val agi: Int = 10,
    val availableStatPoints: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val theme: String = "SOLO_BLUE",
    val targetWorkoutDaysPerWeek: Int = 5,
    val customActiveDurationSeconds: Int = 0,
    val customRestDurationSeconds: Int = 0,
    val lastWorkoutTimestamp: Long = 0L,
    val penaltyActive: Boolean = false,
    val bpModeActive: Boolean = true,
    val isDarkMode: Boolean = true,
    val skipIntro: Boolean = false
) {
    val requiredXpForNextLevel: Int
        get() = (100 * Math.pow(level.toDouble(), 1.5)).toInt()

    val rank: String
        get() = when {
            level >= 50 -> "S-Rank"
            level >= 40 -> "A-Rank"
            level >= 30 -> "B-Rank"
            level >= 20 -> "C-Rank"
            level >= 10 -> "D-Rank"
            else -> "E-Rank"
        }
}
