package com.sololeveling.systemfit.presentation.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sololeveling.systemfit.presentation.dashboard.DashboardScreen
import com.sololeveling.systemfit.presentation.workout.WorkoutScreen

@Composable
fun SystemFitNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onTimeout = {
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
