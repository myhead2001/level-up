package com.sololeveling.systemfit.domain.usecase

import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import java.time.Instant
import java.time.ZoneOffset
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.pow

class ProcessWorkoutResultUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        isPartialCompletion: Boolean,
        isPenaltyZone: Boolean = false,
        baseXpEarned: Int? = null
    ): User? {
        val user = userRepository.getUser(userId) ?: return null

        val currentLevel = user.level

        // Dynamic XP Payout formula or test override
        val finalXpEarned = if (baseXpEarned != null) {
            if (isPartialCompletion) baseXpEarned / 2 else baseXpEarned
        } else {
            val completionMultiplier = if (isPenaltyZone) 0.2 else if (isPartialCompletion) 0.5 else 1.0
            (50.0 * (1.0 + (currentLevel.toDouble() * 0.15)) * completionMultiplier).toInt()
        }
        
        var currentXp = user.currentXp + finalXpEarned
        var updatedLevel = user.level
        var availableStatPoints = user.availableStatPoints
        
        var requiredXpForNextLevel = (100 * updatedLevel.toDouble().pow(1.5)).toInt()
        
        // Handle potential multiple level ups
        while (currentXp >= requiredXpForNextLevel) {
            currentXp -= requiredXpForNextLevel
            updatedLevel++
            availableStatPoints += 3 // +3 stat points per level up
            requiredXpForNextLevel = (100 * updatedLevel.toDouble().pow(1.5)).toInt()
        }

        val now = System.currentTimeMillis()
        var currentStreak = user.currentStreak
        var bestStreak = user.bestStreak

        if (!isPartialCompletion && !isPenaltyZone) {
            val lastWorkoutTime = user.lastWorkoutTimestamp
            if (lastWorkoutTime == 0L) {
                currentStreak = 1
            } else {
                // Timezone-resilient UTC epoch day comparison
                val lastDay = Instant.ofEpochMilli(lastWorkoutTime).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                val currentDay = Instant.ofEpochMilli(now).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                val daysDiff = (currentDay - lastDay).toInt()
                
                if (daysDiff == 1) {
                    currentStreak++
                } else if (daysDiff > 1) {
                    currentStreak = 1
                }
                // If daysDiff == 0 (same day), streak is unchanged
            }
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak
            }
        }

        val updatedUser = user.copy(
            level = updatedLevel,
            currentXp = currentXp,
            availableStatPoints = availableStatPoints,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            lastWorkoutTimestamp = now,
            penaltyActive = false // Clear any active penalty upon completion
        )

        userRepository.logWorkout(
            WorkoutLogEntity(
                userId = userId,
                timestamp = now,
                xpEarned = finalXpEarned,
                isCompleted = !isPartialCompletion && !isPenaltyZone,
                isPenaltyZone = isPenaltyZone,
                durationMinutes = if (isPenaltyZone || isPartialCompletion) 5 else 15
            )
        )

        userRepository.saveUser(updatedUser)
        return updatedUser
    }
}
