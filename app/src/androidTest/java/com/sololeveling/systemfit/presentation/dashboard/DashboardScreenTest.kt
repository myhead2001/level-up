package com.sololeveling.systemfit.presentation.dashboard

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sololeveling.systemfit.domain.model.User
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Ignore("Fails on Android 15 emulator due to Espresso reflection compatibility issue with InputManager")
    @Test
    fun dashboard_renders_correctly_with_user_data() {
        val user = User(
            id = "player_1",
            name = "player_1",
            level = 10,
            currentXp = 50,
            str = 15,
            vit = 12,
            agi = 10,
            availableStatPoints = 3
        )
        var navigateCalled = false

        composeTestRule.setContent {
            HomeTabContent(
                user = user,
                onRenameClick = {},
                onInfoClick = {},
                onAllocateStat = {},
                onNavigateToWorkout = { navigateCalled = true }
            )
        }

        // Verify Level and Title
        composeTestRule.onNodeWithText("LEVEL: 10").assertExists()
        composeTestRule.onNodeWithText("PLAYER: player_1").assertExists()
        
        // Verify Stats Values
        composeTestRule.onNodeWithText("15").assertExists()
        composeTestRule.onNodeWithText("12").assertExists()
        composeTestRule.onNodeWithText("10").assertExists()
        
        // Verify available points text
        composeTestRule.onNodeWithText("AVAILABLE STAT POINTS").assertExists()

        // Test Enter Dungeon button
        composeTestRule.onNodeWithText("ENTER DUNGEON (ACCEPT DAILY QUEST)").performClick()
        assert(navigateCalled)
    }
}
