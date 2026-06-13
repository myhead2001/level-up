package com.sololeveling.systemfit.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        setContent {
            val user by userRepository.getUserStream("player_1").collectAsState(initial = null)
            val themeName = user?.theme ?: "SOLO_BLUE"
            SystemFitTheme(themeName = themeName) {
                SystemFitNavigation()
            }
        }
    }
}
