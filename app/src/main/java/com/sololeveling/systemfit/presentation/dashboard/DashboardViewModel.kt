package com.sololeveling.systemfit.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val userState: StateFlow<User?> = userRepository.getUserStream("player_1")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val workoutLogsState: StateFlow<List<WorkoutLogEntity>> = userRepository.getWorkoutLogsStream("player_1")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun allocateStatPoint(stat: String) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            if (user.availableStatPoints > 0) {
                val updatedUser = when (stat.uppercase()) {
                    "STR" -> user.copy(str = user.str + 1, availableStatPoints = user.availableStatPoints - 1)
                    "VIT" -> user.copy(vit = user.vit + 1, availableStatPoints = user.availableStatPoints - 1)
                    "AGI" -> user.copy(agi = user.agi + 1, availableStatPoints = user.availableStatPoints - 1)
                    else -> user
                }
                userRepository.saveUser(updatedUser)
            }
        }
    }

    fun renamePlayer(newName: String) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            userRepository.saveUser(user.copy(name = newName))
        }
    }

    fun updateTheme(newTheme: String) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            userRepository.saveUser(user.copy(theme = newTheme))
        }
    }

    fun updateTargetDays(days: Int) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            userRepository.saveUser(user.copy(targetWorkoutDaysPerWeek = days))
        }
    }

    fun updateCustomTimers(activeSeconds: Int, restSeconds: Int) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            userRepository.saveUser(user.copy(
                customActiveDurationSeconds = activeSeconds,
                customRestDurationSeconds = restSeconds
            ))
        }
    }
}
