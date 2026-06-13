package com.sololeveling.systemfit.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.math.pow

class ProcessWorkoutResultUseCaseTest {

    private lateinit var useCase: ProcessWorkoutResultUseCase
    private val userRepository: UserRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        useCase = ProcessWorkoutResultUseCase(userRepository)
    }

    @Test
    fun `standard completion updates xp and streak without leveling up`() = runTest {
        val user = User("1", level = 1, currentXp = 0, currentStreak = 0, availableStatPoints = 0)
        coEvery { userRepository.getUser("1") } returns user

        val result = useCase(userId = "1", isPartialCompletion = false, baseXpEarned = 50)
        
        assertThat(result?.currentXp).isEqualTo(50)
        assertThat(result?.level).isEqualTo(1)
        assertThat(result?.currentStreak).isEqualTo(1)
        
        coVerify { userRepository.saveUser(any()) }
    }

    @Test
    fun `partial completion halts streak and halves XP`() = runTest {
        val user = User("1", level = 1, currentXp = 50, currentStreak = 5, availableStatPoints = 0)
        coEvery { userRepository.getUser("1") } returns user

        val result = useCase(userId = "1", isPartialCompletion = true, baseXpEarned = 100)
        
        // Halves 100 to 50. Total 50 + 50 = 100 (which is required for level 2)
        // requiredXpForNextLevel = 100 * (1)^1.5 = 100
        // So level becomes 2, currentXp becomes 0
        assertThat(result?.level).isEqualTo(2)
        assertThat(result?.currentXp).isEqualTo(0)
        assertThat(result?.currentStreak).isEqualTo(5) // streak not incremented
        assertThat(result?.availableStatPoints).isEqualTo(3) // +3 for level up
    }
}
