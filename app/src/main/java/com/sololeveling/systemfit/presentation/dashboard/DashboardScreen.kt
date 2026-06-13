package com.sololeveling.systemfit.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.systemfit.presentation.components.neonPanel
import com.sololeveling.systemfit.presentation.theme.AbsoluteBlack
import com.sololeveling.systemfit.presentation.theme.AlertGold
import com.sololeveling.systemfit.presentation.theme.SystemBlue
import com.sololeveling.systemfit.domain.model.User

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToWorkout: () -> Unit
) {
    val user by viewModel.userState.collectAsState()

    DashboardContent(
        user = user,
        onNavigateToWorkout = onNavigateToWorkout,
        onAllocateStat = { viewModel.allocateStatPoint(it) }
    )
}

@Composable
fun DashboardContent(
    user: User?,
    onNavigateToWorkout: () -> Unit,
    onAllocateStat: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AbsoluteBlack
    ) {
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SystemBlue)
            }
            return@Surface
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "STATUS",
                style = MaterialTheme.typography.headlineMedium,
                color = SystemBlue
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(modifier = Modifier.fillMaxWidth().neonPanel().padding(16.dp)) {
                Column {
                    Text("PLAYER: ${user.id}", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("LEVEL: ${user.level}", color = AlertGold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { user.currentXp.toFloat().div(user.requiredXpForNextLevel.toFloat()) },
                        color = SystemBlue,
                        trackColor = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Text("${user.currentXp} / ${user.requiredXpForNextLevel} XP", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Allocation
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("AVAILABLE POINTS: ${user.availableStatPoints}", color = AlertGold)
                    Spacer(modifier = Modifier.height(16.dp))
                    StatRow("STR", user.str) { onAllocateStat("STR") }
                    StatRow("VIT", user.vit) { onAllocateStat("VIT") }
                    StatRow("AGI", user.agi) { onAllocateStat("AGI") }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateToWorkout,
                colors = ButtonDefaults.buttonColors(containerColor = SystemBlue),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("ENTER DUNGEON (DAILY QUEST)", color = AbsoluteBlack, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatRow(name: String, value: Int, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
        Text(value.toString(), color = SystemBlue, modifier = Modifier.width(40.dp))
        IconButton(onClick = onAdd) {
            Text("+", color = AlertGold, fontWeight = FontWeight.Bold)
        }
    }
}
