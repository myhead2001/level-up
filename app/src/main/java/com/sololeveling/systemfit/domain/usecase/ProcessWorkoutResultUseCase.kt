package com.sololeveling.systemfit.domain.usecase

import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import javax.inject.Inject
import kotlin.math.pow

class ProcessWorkoutResultUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        isPartialCompletion: Boolean,
        baseXpEarned: Int
    ): User? {
        val user = userRepository.getUser(userId) ?: return null

        // If partial completion (e.g. panic button used), scale down XP by half
        val finalXpEarned = if (isPartialCompletion) baseXpEarned / 2 else baseXpEarned
        
        var currentXp = user.currentXp + finalXpEarned
        var currentLevel = user.level
        var availableStatPoints = user.availableStatPoints
        
        var requiredXpForNextLevel = (100 * currentLevel.toDouble().pow(1.5)).toInt()
        
        // Handle potential multiple level ups
        while (currentXp >= requiredXpForNextLevel) {
            currentXp -= requiredXpForNextLevel
            currentLevel++
            availableStatPoints += 3 // +3 stat points per level up
            requiredXpForNextLevel = (100 * currentLevel.toDouble().pow(1.5)).toInt()
        }

        val now = System.currentTimeMillis()
        var currentStreak = user.currentStreak
        var bestStreak = user.bestStreak

        if (!isPartialCompletion) {
            val lastWorkoutTime = user.lastWorkoutTimestamp
            if (lastWorkoutTime == 0L) {
                currentStreak = 1
            } else {
                val daysDiff = ((now - lastWorkoutTime) / (1000 * 60 * 60 * 24)).toInt()
                if (daysDiff == 1) {
                    currentStreak++
                } else if (daysDiff > 1) {
                    currentStreak = 1
                }
                // If daysDiff == 0, keep currentStreak unchanged
            }
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak
            }
        }

        val updatedUser = user.copy(
            level = currentLevel,
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
                isCompleted = !isPartialCompletion,
                isPenaltyZone = false
            )
        )

        userRepository.saveUser(updatedUser)
        return updatedUser
    }
}
