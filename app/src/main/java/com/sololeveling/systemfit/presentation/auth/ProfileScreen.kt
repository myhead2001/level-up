package com.sololeveling.systemfit.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.systemfit.domain.model.User

@Composable
fun ProfileScreen(
    user: User?,
    userEmail: String?,
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Back", color = Color.White)
            }
            Text(
                text = "PLAYER STATUS",
                color = Color(0xFF38BDF8),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(64.dp)) // balance layout
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (userEmail != null) {
            Text(
                text = "Account: $userEmail",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (user != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Level: ${user.level}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("XP: ${user.currentXp} / 100", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("STR: ${user.str}", color = Color(0xFFF87171))
                    Text("AGI: ${user.agi}", color = Color(0xFF34D399))
                    Text("VIT: ${user.vit}", color = Color(0xFF60A5FA))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Streak: ${user.currentStreak} Days", color = Color(0xFFFBBF24))
                }
            }
        } else {
            CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
