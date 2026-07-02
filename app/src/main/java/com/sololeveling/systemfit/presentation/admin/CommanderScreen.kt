package com.sololeveling.systemfit.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.systemfit.data.remote.model.SupabaseUserDto
import com.sololeveling.systemfit.presentation.components.GlitchText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommanderScreen(
    supabase: SupabaseClient,
    onNavigateBack: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var usersList by remember { mutableStateOf<List<SupabaseUserDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Because of RLS, only Commander role can query all users
                val result = supabase.postgrest["users"].select().decodeList<SupabaseUserDto>()
                usersList = result.sortedByDescending { it.level }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { GlitchText("COMMANDER TERMINAL", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = primaryColor) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = primaryColor
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = primaryColor, modifier = Modifier.align(Alignment.Center))
            } else if (usersList.isEmpty()) {
                Text(
                    text = "No users found or permission denied.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "REGISTERED HUNTERS: ${usersList.size}",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(usersList) { user ->
                        PlayerCard(user, primaryColor)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerCard(user: SupabaseUserDto, primaryColor: Color) {
    val rank = when {
        user.level >= 40 -> "S-Rank"
        user.level >= 30 -> "A-Rank"
        user.level >= 20 -> "B-Rank"
        user.level >= 10 -> "C-Rank"
        user.level >= 5 -> "D-Rank"
        else -> "E-Rank"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.role == "Commander") {
                    Icon(Icons.Default.Star, contentDescription = "Commander", tint = Color.Yellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = user.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Role: ${user.role.uppercase()}", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("STR: ${user.str} | VIT: ${user.vit} | AGI: ${user.agi}", color = Color.LightGray, fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(rank, color = primaryColor, fontWeight = FontWeight.Bold)
            Text("Lv. ${user.level}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("${user.currentXp} XP", color = Color.Gray, fontSize = 12.sp)
        }
    }
}
