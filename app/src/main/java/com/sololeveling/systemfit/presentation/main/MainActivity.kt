package com.sololeveling.systemfit.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
            
            var hasPlayedStartup by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            androidx.compose.runtime.LaunchedEffect(user) {
                if (user != null && !hasPlayedStartup) {
                    hasPlayedStartup = true
                    if (user?.skipIntro == true) {
                        com.sololeveling.systemfit.presentation.utils.SoundManager.playStartup()
                    }
                }
            }

            val themeName = user?.theme ?: "SOLO_BLUE"
            val isDarkMode = user?.isDarkMode ?: true
            val startDestination = if (user?.skipIntro == true) "dashboard" else "splash"

            SystemFitTheme(themeName = themeName, isDarkMode = isDarkMode) {
                SystemFitNavigation(startDestination = startDestination)
            }
        }
    }
}
