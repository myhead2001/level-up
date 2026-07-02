package com.sololeveling.systemfit.presentation.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sololeveling.systemfit.presentation.dashboard.DashboardScreen
import com.sololeveling.systemfit.presentation.workout.WorkoutScreen

import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.systemfit.presentation.auth.AuthViewModel
import com.sololeveling.systemfit.presentation.auth.LoginScreen
import com.sololeveling.systemfit.presentation.auth.ProfileScreen
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import io.github.jan.supabase.auth.auth

@Composable
fun SystemFitNavigation(
    supabase: SupabaseClient,
    startDestination: String = "splash"
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val sessionStatus by authViewModel.sessionStatus.collectAsState()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    com.sololeveling.systemfit.presentation.utils.SoundManager.stopStartup()
                    val nextRoute = if (sessionStatus is SessionStatus.Authenticated) "dashboard" else "login"
                    navController.navigate(nextRoute) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                supabase = supabase,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onNavigateToWorkout = { navController.navigate("workout") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        composable("workout") {
            WorkoutScreen(
                onNavigateBack = {
                    navController.popBackStack("dashboard", inclusive = false)
                }
            )
        }
        composable("profile") {
            val dashboardViewModel: com.sololeveling.systemfit.presentation.dashboard.DashboardViewModel = hiltViewModel()
            val user by dashboardViewModel.userState.collectAsState()

            ProfileScreen(
                user = user,
                userEmail = supabase.auth.currentUserOrNull()?.email,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(0) // clear entire backstack
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
