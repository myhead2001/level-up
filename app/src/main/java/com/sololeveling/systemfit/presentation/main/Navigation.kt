package com.sololeveling.systemfit.presentation.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sololeveling.systemfit.presentation.dashboard.DashboardScreen
import com.sololeveling.systemfit.presentation.workout.WorkoutScreen

@Composable
fun SystemFitNavigation(startDestination: String = "splash") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    com.sololeveling.systemfit.presentation.utils.SoundManager.stopStartup()
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onNavigateToWorkout = { navController.navigate("workout") }
            )
        }
        composable("workout") {
            WorkoutScreen(
                onNavigateBack = {
                    navController.popBackStack("dashboard", inclusive = false)
                }
            )
        }
    }
}
