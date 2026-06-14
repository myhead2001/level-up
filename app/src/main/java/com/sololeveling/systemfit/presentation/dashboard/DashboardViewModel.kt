package com.sololeveling.systemfit.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import com.sololeveling.systemfit.domain.usecase.GenerateDailyQuestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val generateDailyQuestUseCase: GenerateDailyQuestUseCase
) : ViewModel() {

    val activeUserId = userRepository.getActiveUserId()

    val userState: StateFlow<User?> = userRepository.getUserStream(activeUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val workoutLogsState: StateFlow<List<WorkoutLogEntity>> = userRepository.getWorkoutLogsStream(activeUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _dailyQuestState = kotlinx.coroutines.flow.MutableStateFlow<GenerateDailyQuestUseCase.DailyQuest?>(null)
    val dailyQuestState: StateFlow<GenerateDailyQuestUseCase.DailyQuest?> = _dailyQuestState.asStateFlow()

    init {
        viewModelScope.launch {
            userState.collect { user ->
                if (user != null) {
                    _dailyQuestState.value = generateDailyQuestUseCase(user)
                }
            }
        }
    }

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

    fun toggleBpMode() {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            userRepository.saveUser(user.copy(bpModeActive = !user.bpModeActive))
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            userRepository.saveUser(user.copy(isDarkMode = !user.isDarkMode))
        }
    }

    fun toggleSkipIntro() {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            userRepository.saveUser(user.copy(skipIntro = !user.skipIntro))
        }
    }

    fun changeStartRank(rank: String) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            val startingLevel = when (rank.uppercase()) {
                "S" -> 50
                "A" -> 40
                "B" -> 30
                "C" -> 20
                "D" -> 10
                else -> 1 // E-Rank
            }
            val allocatedPoints = (startingLevel - 1) * 3
            val updatedUser = user.copy(
                level = startingLevel,
                currentXp = 0,
                str = 10,
                vit = 10,
                agi = 10,
                availableStatPoints = allocatedPoints
            )
            userRepository.saveUser(updatedUser)
        }
    }

    fun deleteWorkoutLog(logId: Long) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            val logs = workoutLogsState.value
            val targetLog = logs.find { it.logId == logId } ?: return@launch
            
            // Delete log
            userRepository.deleteWorkoutLog(logId)
            
            // Recalculate level and XP
            val updatedUser = recalculateXpAndLevel(user, -targetLog.xpEarned)
            userRepository.saveUser(updatedUser)
        }
    }

    fun addManualWorkoutLog(timestamp: Long, xpEarned: Int, isCompleted: Boolean, durationMinutes: Int) {
        viewModelScope.launch {
            val user = userState.value ?: return@launch
            
            // Log manual workout
            userRepository.logWorkout(
                WorkoutLogEntity(
                    userId = activeUserId,
                    timestamp = timestamp,
                    xpEarned = xpEarned,
                    isCompleted = isCompleted,
                    durationMinutes = durationMinutes
                )
            )
            
            // Recalculate level and XP
            val updatedUser = recalculateXpAndLevel(user, xpEarned)
            userRepository.saveUser(updatedUser)
        }
    }

    private fun recalculateXpAndLevel(user: User, xpDifference: Int): User {
        var newXp = user.currentXp + xpDifference
        var newLevel = user.level
        var statPoints = user.availableStatPoints

        if (xpDifference > 0) {
            // Level up logic
            var reqXp = (100 * Math.pow(newLevel.toDouble(), 1.5)).toInt()
            while (newXp >= reqXp) {
                newXp -= reqXp
                newLevel++
                statPoints += 3
                reqXp = (100 * Math.pow(newLevel.toDouble(), 1.5)).toInt()
            }
        } else if (xpDifference < 0) {
            // Level down logic
            while (newXp < 0 && newLevel > 1) {
                newLevel--
                val prevReqXp = (100 * Math.pow(newLevel.toDouble(), 1.5)).toInt()
                newXp += prevReqXp
                statPoints = maxOf(0, statPoints - 3)
            }
            if (newXp < 0) {
                newXp = 0
            }
        }
        return user.copy(level = newLevel, currentXp = newXp, availableStatPoints = statPoints)
    }

    fun resetSystemData() {
        viewModelScope.launch {
            userRepository.resetDatabase(activeUserId)
        }
    }

    fun backupProfile() {
        viewModelScope.launch {
            userRepository.backupProfile(activeUserId)
        }
    }

    fun restoreProfile(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.restoreProfile(activeUserId)
            onResult(success)
        }
    }
}
