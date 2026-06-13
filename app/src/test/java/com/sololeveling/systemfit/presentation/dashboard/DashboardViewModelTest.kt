package com.sololeveling.systemfit.presentation.dashboard

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import com.sololeveling.systemfit.domain.usecase.GenerateDailyQuestUseCase
import com.sololeveling.systemfit.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: DashboardViewModel
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val generateDailyQuestUseCase: GenerateDailyQuestUseCase = mockk(relaxed = true)

    @Before
    fun setUp() {
        val user = User("player_1", level = 5, str = 10, vit = 10, agi = 10, availableStatPoints = 3)
        coEvery { userRepository.getUserStream("player_1") } returns flowOf(user)
        coEvery { generateDailyQuestUseCase(any()) } returns mockk()
        viewModel = DashboardViewModel(userRepository, generateDailyQuestUseCase)
    }

    @Test
    fun `user state correctly observes repository`() = runTest {
        viewModel.userState.test {
            val user = awaitItem()
            assertThat(user).isNotNull()
            assertThat(user?.level).isEqualTo(5)
            assertThat(user?.availableStatPoints).isEqualTo(3)
        }
    }

    @Test
    fun `allocateStatPoint correctly increments stat and decrements available points`() = runTest {
        viewModel.userState.test {
            awaitItem() // Wait for initial emission
            
            viewModel.allocateStatPoint("STR")
            
            coVerify { 
                userRepository.saveUser(match { 
                    it.str == 11 && it.availableStatPoints == 2 
                }) 
            }
        }
    }
}
