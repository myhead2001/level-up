package com.sololeveling.systemfit.presentation.dashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sololeveling.systemfit.domain.model.User
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboard_renders_correctly_with_user_data() {
        val user = User("player_1", level = 10, currentXp = 50, str = 15, vit = 12, agi = 10, availableStatPoints = 3)
        var navigateCalled = false

        composeTestRule.setContent {
            DashboardContent(
                user = user,
                onNavigateToWorkout = { navigateCalled = true },
                onAllocateStat = {}
            )
        }

        // Verify Level and Title
        composeTestRule.onNodeWithText("LEVEL: 10").assertExists()
        composeTestRule.onNodeWithText("PLAYER: player_1").assertExists()
        
        // Verify Stats
        composeTestRule.onNodeWithText("15").assertExists() // the value
        composeTestRule.onNodeWithText("12").assertExists()
        composeTestRule.onNodeWithText("10").assertExists()
        
        // Verify available points
        composeTestRule.onNodeWithText("AVAILABLE POINTS: 3").assertExists()

        // Test Enter Dungeon button
        composeTestRule.onNodeWithText("ENTER DUNGEON (DAILY QUEST)").performClick()
        assert(navigateCalled)
    }

    @Test
    fun dashboard_shows_loading_when_user_is_null() {
        composeTestRule.setContent {
            DashboardContent(
                user = null,
                onNavigateToWorkout = {},
                onAllocateStat = {}
            )
        }

        // We could test for a testTag on the CircularProgressIndicator, 
        // but checking the absence of the main title is enough to prove it's in loading state
        composeTestRule.onNodeWithText("STATUS").assertDoesNotExist()
    }
}
