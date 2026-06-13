package com.sololeveling.systemfit.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sololeveling.systemfit.domain.repository.UserRepository
import com.sololeveling.systemfit.presentation.theme.SystemFitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup Immersive Fullscreen Mode (Hide Status & Navigation Bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val user by userRepository.getUserStream("player_1").collectAsState(initial = null)
            val themeName = user?.theme ?: "SOLO_BLUE"
            val isDarkMode = user?.isDarkMode ?: true
            SystemFitTheme(themeName = themeName, isDarkMode = isDarkMode) {
                SystemFitNavigation()
            }
        }
    }
}
