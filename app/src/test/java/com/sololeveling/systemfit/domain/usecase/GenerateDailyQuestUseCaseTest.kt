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
            Exercise("1", "Pushups", ExerciseType.STRENGTH, "", "url", true),
            Exercise("2", "Situps", ExerciseType.STRENGTH, "", "url", true),
            Exercise("3", "Squats", ExerciseType.STRENGTH, "", "url", true),
            Exercise("4", "Plank", ExerciseType.STRENGTH, "", "url", true),
            Exercise("5", "Lunges", ExerciseType.STRENGTH, "", "url", true)
        )
        coEvery { exerciseRepository.getHtnSafeExercises() } returns safeExercises
        useCase = GenerateDailyQuestUseCase(exerciseRepository)
    }

    @Test
    fun `level 1 user gets correct intervals and rounds`() = runTest {
        val user = User("1", level = 1, str = 10, vit = 10, agi = 10)
        val quest = useCase(user)

        // activeInterval = min(20 + (10*2), 60) = 40
        assertThat(quest.activeIntervalSeconds).isEqualTo(40)
        // restInterval = max(90 - (10*3), 30) = 60
        assertThat(quest.restIntervalSeconds).isEqualTo(60)
        // totalRounds = min(2 + 1/3, 5) = 2
        assertThat(quest.totalTargetRounds).isEqualTo(2)
        // max 4 distinct exercises
        assertThat(quest.exercises.size).isEqualTo(4)
    }

    @Test
    fun `level 50 user caps at max bounds`() = runTest {
        val user = User("1", level = 50, str = 100, vit = 100, agi = 100)
        val quest = useCase(user)

        // activeInterval = min(20 + 200, 60) = 60
        assertThat(quest.activeIntervalSeconds).isEqualTo(60)
        // restInterval = max(90 - 300, 30) = 30
        assertThat(quest.restIntervalSeconds).isEqualTo(30)
        // totalRounds = min(2 + 16, 5) = 5
        assertThat(quest.totalTargetRounds).isEqualTo(5)
    }
}
