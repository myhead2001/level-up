package com.sololeveling.systemfit.domain.model

import java.time.Instant
import java.time.ZoneOffset


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
    val workoutDaysOfWeek: String = "2,3,4,5,6",
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

fun User.hasMissedWorkoutDay(currentTimeMillis: Long): Boolean {
    if (lastWorkoutTimestamp <= 0L) return false

    val selectedDays = workoutDaysOfWeek.split(",")
        .filter { it.isNotEmpty() }
        .mapNotNull { it.toIntOrNull() }
        .toSet()

    if (selectedDays.isEmpty()) return false

    val lastLocalDate = Instant.ofEpochMilli(lastWorkoutTimestamp)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
    val currentLocalDate = Instant.ofEpochMilli(currentTimeMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()

    var checkDate = lastLocalDate.plusDays(1)
    while (checkDate.isBefore(currentLocalDate)) {
        val dayOfWeekInt = when (checkDate.dayOfWeek) {
            java.time.DayOfWeek.SUNDAY -> 1
            java.time.DayOfWeek.MONDAY -> 2
            java.time.DayOfWeek.TUESDAY -> 3
            java.time.DayOfWeek.WEDNESDAY -> 4
            java.time.DayOfWeek.THURSDAY -> 5
            java.time.DayOfWeek.FRIDAY -> 6
            java.time.DayOfWeek.SATURDAY -> 7
        }

        if (selectedDays.contains(dayOfWeekInt)) {
            return true
        }
        checkDate = checkDate.plusDays(1)
    }
    return false
}
