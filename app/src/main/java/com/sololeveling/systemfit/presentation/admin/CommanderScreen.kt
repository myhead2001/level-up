package com.sololeveling.systemfit.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.sololeveling.systemfit.data.remote.model.FeedbackWithUserDto
import com.sololeveling.systemfit.presentation.components.GlitchText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommanderScreen(
    supabase: SupabaseClient,
    onNavigateBack: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var usersList by remember { mutableStateOf<List<SupabaseUserDto>>(emptyList()) }
    var feedbackList by remember { mutableStateOf<List<FeedbackWithUserDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Fetch Users
                val usersResult = supabase.postgrest["users"].select().decodeList<SupabaseUserDto>()
                usersList = usersResult.sortedByDescending { it.level }
                
                // Fetch Feedback
                val feedbackResult = supabase.postgrest["feedbacks"]
                    .select(columns = Columns.raw("*, users(name)"))
                    .decodeList<FeedbackWithUserDto>()
                feedbackList = feedbackResult.sortedByDescending { it.createdAt }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                        .border(1.dp, if (selectedTab == 0) primaryColor else Color.DarkGray, RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) primaryColor.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { selectedTab = 0 }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("HUNTERS", color = if (selectedTab == 0) primaryColor else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .border(1.dp, if (selectedTab == 1) primaryColor else Color.DarkGray, RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) primaryColor.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { selectedTab = 1 }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("INTELLIGENCE", color = if (selectedTab == 1) primaryColor else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(color = primaryColor, modifier = Modifier.align(Alignment.Center))
                } else if (selectedTab == 0) {
                    if (usersList.isEmpty()) {
                        Text("No hunters found.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "REGISTERED HUNTERS: ${usersList.size}",
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(usersList) { user ->
                                PlayerCard(user, primaryColor)
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                } else {
                    if (feedbackList.isEmpty()) {
                        Text("No intelligence reports found.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "SUBMITTED REPORTS: ${feedbackList.size}",
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(feedbackList) { feedback ->
                                FeedbackCard(feedback, primaryColor)
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
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

@Composable
fun FeedbackCard(feedback: FeedbackWithUserDto, primaryColor: Color) {
    val categoryColor = when (feedback.category) {
        "Bug Report" -> Color(0xFFFF5C5C) // Red
        "Feature Request" -> Color(0xFF00FFB2) // Green
        "Gameplay/UI" -> Color(0xFFFFA500) // Orange
        else -> Color.LightGray
    }

    val dateStr = try {
        // Parse ISO 8601 string to a readable format if possible
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val date = parser.parse(feedback.createdAt.take(19)) // Strip fractional seconds
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        feedback.createdAt
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, categoryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = feedback.category.uppercase(),
                color = categoryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(dateStr, color = Color.Gray, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feedback.content,
            color = Color.White,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Submitted by: ${feedback.users?.name ?: "Unknown"}",
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
            if (feedback.deviceInfo.isNotBlank()) {
                Text(feedback.deviceInfo, color = Color.DarkGray, fontSize = 10.sp)
            }
        }
    }
}
