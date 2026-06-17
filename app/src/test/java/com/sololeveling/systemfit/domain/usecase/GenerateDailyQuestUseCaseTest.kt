package com.sololeveling.systemfit.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.sololeveling.systemfit.domain.model.Exercise
import com.sololeveling.systemfit.domain.model.ExerciseType
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.ExerciseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GenerateDailyQuestUseCaseTest {

    private lateinit var useCase: GenerateDailyQuestUseCase
    private val exerciseRepository: ExerciseRepository = mockk()

    @Before
    fun setUp() {
        val safeExercises = listOf(
            Exercise("1", "Pushups", ExerciseType.STRENGTH, "", "url", true, requiredStr = 10),
            Exercise("2", "Situps", ExerciseType.STRENGTH, "", "url", true, requiredStr = 10),
            Exercise("3", "Squats", ExerciseType.STRENGTH, "", "url", true, requiredStr = 10),
            Exercise("4", "Plank", ExerciseType.STRENGTH, "", "url", true, requiredStr = 10),
            Exercise("5", "Lunges", ExerciseType.STRENGTH, "", "url", true, requiredStr = 10)
        )
        coEvery { exerciseRepository.getHtnSafeExercises() } returns safeExercises
        coEvery { exerciseRepository.getAllExercises() } returns safeExercises
        useCase = GenerateDailyQuestUseCase(exerciseRepository)
    }

    @Test
    fun `standard mode - level 1 user gets correct intervals and rounds`() = runTest {
        val user = User("1", level = 1, str = 10, vit = 10, agi = 10, bpModeActive = false)
        val quest = useCase(user)

        // activeInterval = 20 + floor(40 * (1 - e^(-0.04 * 10))) = 20 + floor(40 * 0.3297) = 33
        assertThat(quest.activeIntervalSeconds).isEqualTo(33)
        // restInterval = 30 + floor(60 * e^(-0.04 * 10)) = 30 + floor(60 * 0.6703) = 70
        assertThat(quest.restIntervalSeconds).isEqualTo(70)
        // totalRounds = min(2 + 1/5, 5) = 2
        assertThat(quest.totalTargetRounds).isEqualTo(2)
        // max 4 distinct exercises
        assertThat(quest.exercises.size).isEqualTo(4)
    }

    @Test
    fun `bp safe mode - level 1 user gets correct intervals and rounds`() = runTest {
        val user = User("1", level = 1, str = 10, vit = 10, agi = 10, bpModeActive = true)
        val quest = useCase(user)

        // activeInterval = 20 + floor(25 * (1 - e^(-0.04 * 10))) = 20 + floor(25 * 0.3297) = 28
        assertThat(quest.activeIntervalSeconds).isEqualTo(28)
        // restInterval = 45 + floor(45 * e^(-0.04 * 10)) = 45 + floor(45 * 0.6703) = 75
        assertThat(quest.restIntervalSeconds).isEqualTo(75)
        // totalRounds = min(2 + 1/8, 4) = 2
        assertThat(quest.totalTargetRounds).isEqualTo(2)
        assertThat(quest.exercises.size).isEqualTo(4)
    }

    @Test
    fun `standard mode - level 50 user caps at max bounds`() = runTest {
        val user = User("1", level = 50, str = 100, vit = 100, agi = 100, bpModeActive = false)
        val quest = useCase(user)

        // activeInterval = 20 + floor(40 * (1 - e^(-0.04 * 100))) = 20 + floor(40 * 0.9817) = 59
        assertThat(quest.activeIntervalSeconds).isEqualTo(59)
        // restInterval = 30 + floor(60 * e^(-0.04 * 100)) = 30 + floor(60 * 0.0183) = 31
        assertThat(quest.restIntervalSeconds).isEqualTo(31)
        // totalRounds = min(2 + 50/5, 5) = 5
        assertThat(quest.totalTargetRounds).isEqualTo(5)
    }

    @Test
    fun `bp safe mode - level 50 user caps at max bounds`() = runTest {
        val user = User("1", level = 50, str = 100, vit = 100, agi = 100, bpModeActive = true)
        val quest = useCase(user)

        // activeInterval = 20 + floor(25 * (1 - e^(-0.04 * 100))) = 20 + floor(25 * 0.9817) = 44
        assertThat(quest.activeIntervalSeconds).isEqualTo(44)
        // restInterval = 45 + floor(45 * e^(-0.04 * 100)) = 45 + floor(45 * 0.0183) = 45
        assertThat(quest.restIntervalSeconds).isEqualTo(45)
        // totalRounds = min(2 + 50/8, 4) = 4
        assertThat(quest.totalTargetRounds).isEqualTo(4)
    }

    @Test
    fun `custom duration settings override calculated durations`() = runTest {
        val user = User(
            "1",
            level = 1,
            str = 10,
            vit = 10,
            agi = 10,
            bpModeActive = false,
            customActiveDurationSeconds = 15,
            customRestDurationSeconds = 25
        )
        val quest = useCase(user)

        assertThat(quest.activeIntervalSeconds).isEqualTo(15)
        assertThat(quest.restIntervalSeconds).isEqualTo(25)
    }

    @Test
    fun `exercises are filtered based on player strength`() = runTest {
        val exercises = listOf(
            Exercise("1", "Pushups", ExerciseType.STRENGTH, "", "url", true, requiredStr = 10),
            Exercise("2", "Situps", ExerciseType.STRENGTH, "", "url", true, requiredStr = 20),
            Exercise("3", "Squats", ExerciseType.STRENGTH, "", "url", true, requiredStr = 30)
        )
        coEvery { exerciseRepository.getAllExercises() } returns exercises
        coEvery { exerciseRepository.getHtnSafeExercises() } returns exercises

        val user = User("1", level = 1, str = 25, vit = 10, agi = 10, bpModeActive = false)
        val quest = useCase(user)

        // The quest should only contain exercises with requiredStr <= 25 (i.e. Pushups and Situps)
        assertThat(quest.exercises).isNotEmpty()
        quest.exercises.forEach {
            assertThat(it.requiredStr).isAtMost(25)
            assertThat(it.id).isNotEqualTo("3")
        }
    }
}
