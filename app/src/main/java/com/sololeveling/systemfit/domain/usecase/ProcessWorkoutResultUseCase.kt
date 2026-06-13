package com.sololeveling.systemfit.domain.usecase

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

        val updatedStreak = if (isPartialCompletion) user.currentStreak else user.currentStreak + 1

        val updatedUser = user.copy(
            level = currentLevel,
            currentXp = currentXp,
            availableStatPoints = availableStatPoints,
            currentStreak = updatedStreak
        )

        userRepository.saveUser(updatedUser)
        return updatedUser
    }
}
