package com.sololeveling.systemfit.presentation.workout

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sololeveling.systemfit.domain.model.Exercise
import com.sololeveling.systemfit.domain.model.ExerciseType
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import com.sololeveling.systemfit.domain.usecase.EmergencyHaltUseCase
import com.sololeveling.systemfit.domain.usecase.GenerateDailyQuestUseCase
import com.sololeveling.systemfit.domain.usecase.GenerateDailyQuestUseCase.DailyQuest
import com.sololeveling.systemfit.domain.usecase.ProcessWorkoutResultUseCase
import com.sololeveling.systemfit.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import android.os.SystemClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WorkoutViewModel
    private val userRepository: UserRepository = mockk()
    private val generateDailyQuestUseCase: GenerateDailyQuestUseCase = mockk()
    private val processWorkoutResultUseCase: ProcessWorkoutResultUseCase = mockk()
    private val emergencyHaltUseCase: EmergencyHaltUseCase = mockk()

    @Before
    fun setUp() {
        val dummyQuest = DailyQuest(
            exercises = listOf(
                Exercise("1", "Pushups", ExerciseType.STRENGTH, "", "pushups.gif", true),
                Exercise("2", "Situps", ExerciseType.STRENGTH, "", "situps.gif", true)
            ),
            totalTargetRounds = 2,
            activeIntervalSeconds = 30,
            restIntervalSeconds = 10
        )
        
        coEvery { userRepository.getUser(any()) } returns User(id = "player_1")
        coEvery { userRepository.saveUser(any()) } returns Unit
        coEvery { generateDailyQuestUseCase.invoke(any()) } returns dummyQuest
        coEvery { processWorkoutResultUseCase.invoke(any(), any(), any()) } returns User(id = "player_1")
        mockkStatic(SystemClock::class)
        every { SystemClock.elapsedRealtime() } answers { mainDispatcherRule.testDispatcher.scheduler.currentTime }
        every { emergencyHaltUseCase.invokeHalt() } returns mockk { 
            every { remainingCooldownSeconds } returns 1800 
        }

        viewModel = WorkoutViewModel(
            userRepository,
            generateDailyQuestUseCase,
            processWorkoutResultUseCase,
            emergencyHaltUseCase
        )
    }

    @Test
    fun `initial state is Setup with correct duration after loading`() = runTest {
        viewModel.uiState.test {
            // Because initialization happens in init {} with a coroutine, it emits Loading then Setup
            val firstState = awaitItem()
            if (firstState is WorkoutContract.UiState.Loading) {
                val setupState = awaitItem() as WorkoutContract.UiState.Setup
                assertThat(setupState.rounds).isEqualTo(2)
            } else {
                val setupState = firstState as WorkoutContract.UiState.Setup
                assertThat(setupState.rounds).isEqualTo(2)
            }
        }
    }

    @Test
    fun `start quest transitions to ActiveCombat`() = runTest {
        viewModel.onEvent(WorkoutContract.UiEvent.StartQuest)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertThat(state).isInstanceOf(WorkoutContract.UiState.ActiveCombat::class.java)
            val combatState = state as WorkoutContract.UiState.ActiveCombat
            assertThat(combatState.currentRound).isEqualTo(1)
            assertThat(combatState.isRestPeriod).isFalse()
            assertThat(combatState.timeLeftSeconds).isEqualTo(30)
            assertThat(combatState.currentExercise.name).isEqualTo("Pushups")
        }
    }
}
