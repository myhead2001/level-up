package com.sololeveling.systemfit.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.sololeveling.systemfit.data.local.entity.WorkoutLogEntity
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.usecase.GenerateDailyQuestUseCase
import com.sololeveling.systemfit.presentation.components.neonPanel
import com.sololeveling.systemfit.presentation.theme.AbsoluteBlack
import com.sololeveling.systemfit.presentation.theme.AlertGold
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToWorkout: () -> Unit
) {
    val user by viewModel.userState.collectAsState()
    val workoutLogs by viewModel.workoutLogsState.collectAsState()
    val dailyQuest by viewModel.dailyQuestState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AbsoluteBlack
    ) {
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
            return@Surface
        }

        val activeUser = user!!

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = AbsoluteBlack,
                    modifier = Modifier.border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.List, contentDescription = "Quests") },
                        label = { Text("Quests") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Analytics") },
                        label = { Text("Analytics") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = primaryColor,
                            selectedTextColor = primaryColor,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = primaryColor.copy(alpha = 0.15f)
                        )
                    )
                }
            },
            containerColor = AbsoluteBlack
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }.using(
                            SizeTransform(clip = false)
                        )
                    },
                    label = "tab_transitions"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> HomeTabContent(
                            user = activeUser,
                            onRenameClick = { showRenameDialog = true },
                            onInfoClick = { showInfoDialog = true },
                            onAllocateStat = { viewModel.allocateStatPoint(it) },
                            onNavigateToWorkout = onNavigateToWorkout
                        )
                        1 -> QuestsTabContent(
                            user = activeUser,
                            logs = workoutLogs,
                            dailyQuest = dailyQuest
                        )
                        2 -> AnalyticsTabContent(user = activeUser, logs = workoutLogs)
                        3 -> ProfileTabContent(
                            user = activeUser,
                            onUpdateTheme = { viewModel.updateTheme(it) },
                            onUpdateTargetDays = { viewModel.updateTargetDays(it) },
                            onUpdateCustomTimers = { act, rst -> viewModel.updateCustomTimers(act, rst) },
                            onToggleBpMode = { viewModel.toggleBpMode() },
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onResetSystemData = { viewModel.resetSystemData() }
                        )
                    }
                }
            }
        }

        if (showRenameDialog) {
            RenameDialog(
                currentName = activeUser.name,
                onDismiss = { showRenameDialog = false },
                onConfirm = { newName ->
                    viewModel.renamePlayer(newName)
                    showRenameDialog = false
                }
            )
        }

        if (showInfoDialog) {
            InfoDialog(onDismiss = { showInfoDialog = false })
        }
    }
}

@Composable
fun HomeTabContent(
    user: User,
    onRenameClick: () -> Unit,
    onInfoClick: () -> Unit,
    onAllocateStat: (String) -> Unit,
    onNavigateToWorkout: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onInfoClick) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = primaryColor)
                }
                Text(
                    text = "STATUS",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )
                IconButton(onClick = onRenameClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = primaryColor)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Player Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neonPanel(color = primaryColor)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "PLAYER: ${user.name}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("LEVEL: ${user.level}", color = AlertGold, style = MaterialTheme.typography.titleMedium)
                        Text("RANK: ${user.rank}", color = primaryColor, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val progress = if (user.requiredXpForNextLevel > 0) {
                        user.currentXp.toFloat() / user.requiredXpForNextLevel
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        color = primaryColor,
                        trackColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${user.currentXp} / ${user.requiredXpForNextLevel} XP",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Stats Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AVAILABLE STAT POINTS", color = AlertGold, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${user.availableStatPoints}",
                            color = AlertGold,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    StatRow("STR", user.str, "Increases power & damage capacity", user.availableStatPoints > 0) { onAllocateStat("STR") }
                    Divider(color = Color.DarkGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                    StatRow("VIT", user.vit, "Improves endurance & stamina recovery", user.availableStatPoints > 0) { onAllocateStat("VIT") }
                    Divider(color = Color.DarkGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                    StatRow("AGI", user.agi, "Boosts agility & exercise speed multiplier", user.availableStatPoints > 0) { onAllocateStat("AGI") }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            // Action Button
            Button(
                onClick = onNavigateToWorkout,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text(
                    text = "ENTER DUNGEON (ACCEPT DAILY QUEST)",
                    color = AbsoluteBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun StatRow(
    name: String,
    value: Int,
    description: String,
    canAllocate: Boolean,
    onAdd: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(description, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                value.toString(),
                color = primaryColor,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(
                onClick = onAdd,
                enabled = canAllocate,
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (canAllocate) primaryColor.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Text("+", color = if (canAllocate) AlertGold else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun QuestsTabContent(
    user: User,
    logs: List<WorkoutLogEntity>,
    dailyQuest: GenerateDailyQuestUseCase.DailyQuest?
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Check if a workout has been successfully logged today
    val isQuestCompleted = remember(logs) {
        val today = Calendar.getInstance()
        logs.any { log ->
            val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            log.isCompleted &&
                    logCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    logCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quest Info Header
            Box(
                modifier = Modifier
                    .border(2.dp, primaryColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            ) {
                Text(
                    "QUEST INFO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "[Daily Quest: Strength Training has arrived.]",
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Goals Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "GOALS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (dailyQuest != null) {
                        dailyQuest.exercises.forEachIndexed { index, exercise ->
                            QuestGoalRow(
                                name = exercise.name,
                                progressText = if (isQuestCompleted) {
                                    "${dailyQuest.totalTargetRounds}/${dailyQuest.totalTargetRounds} Sets"
                                } else {
                                    "0/${dailyQuest.totalTargetRounds} Sets (${dailyQuest.activeIntervalSeconds}s)"
                                },
                                completed = isQuestCompleted
                            )
                            if (index < dailyQuest.exercises.size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        Text(
                            "Generating daily quest...",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // Warning Footer
        Text(
            text = "WARNING: Failure to complete the daily quest will result in an appropriate penalty.",
            color = Color.Red.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun QuestGoalRow(name: String, progressText: String, completed: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "[$progressText]",
                color = if (completed) primaryColor else Color.Gray,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (completed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun AnalyticsTabContent(user: User, logs: List<WorkoutLogEntity>) {
    val primaryColor = MaterialTheme.colorScheme.primary

    // Group logs by day of the week for the last 7 days
    val weeklyXp = remember(logs) {
        val xpList = FloatArray(7) { 0f }
        val calendar = Calendar.getInstance()
        for (i in 0..6) {
            val targetDay = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val totalXpForDay = logs.filter { log ->
                val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                logCal.get(Calendar.YEAR) == targetDay.get(Calendar.YEAR) &&
                        logCal.get(Calendar.DAY_OF_YEAR) == targetDay.get(Calendar.DAY_OF_YEAR)
            }.sumOf { it.xpEarned }
            xpList[6 - i] = totalXpForDay.toFloat()
        }
        // Fallback mock starter values if no logs exist
        if (logs.isEmpty()) {
            floatArrayOf(90f, 92f, 100f, 48f, 50f, 52f, 10f)
        } else {
            xpList
        }
    }

    val daysOfWeek = remember {
        val format = SimpleDateFormat("E", Locale.getDefault())
        val days = mutableListOf<String>()
        for (i in 0..6) {
            val targetDay = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            days.add(format.format(targetDay.time).take(1))
        }
        days.reversed()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Overview Navigation Bar Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnalyticsCard(title = "LEVEL", value = user.level.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AnalyticsCard(title = "RANK", value = user.rank, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Today XP calculation
                val todayXp = remember(logs) {
                    val today = Calendar.getInstance()
                    logs.filter { log ->
                        val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                        logCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                logCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                    }.sumOf { it.xpEarned }
                }
                AnalyticsCard(title = "TODAY XP", value = todayXp.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AnalyticsCard(title = "TOTAL XP", value = (user.level * 1000 + user.currentXp).toString(), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnalyticsCard(title = "STREAK", value = user.currentStreak.toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AnalyticsCard(title = "BEST STREAK", value = user.bestStreak.toString(), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Weekly Activity Header
            Text(
                "WEEKLY ACTIVITY",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val maxVal = maxOf(weeklyXp.maxOrNull() ?: 100f, 100f)

                    val points = weeklyXp.mapIndexed { index, value ->
                        val x = (width / 6f) * index
                        val y = height - (value / maxVal) * (height - 40f) - 20f
                        Offset(x, y)
                    }

                    // Draw grid lines
                    drawLine(Color.DarkGray.copy(alpha = 0.2f), Offset(0f, height * 0.25f), Offset(width, height * 0.25f))
                    drawLine(Color.DarkGray.copy(alpha = 0.2f), Offset(0f, height * 0.5f), Offset(width, height * 0.5f))
                    drawLine(Color.DarkGray.copy(alpha = 0.2f), Offset(0f, height * 0.75f), Offset(width, height * 0.75f))

                    // Draw smooth curve
                    val path = Path()
                    path.moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val cpX1 = p0.x + (p1.x - p0.x) / 2f
                        val cpY1 = p0.y
                        val cpX2 = p0.x + (p1.x - p0.x) / 2f
                        val cpY2 = p1.y
                        path.cubicTo(cpX1, cpY1, cpX2, cpY2, p1.x, p1.y)
                    }

                    // Fill area under the path
                    val fillPath = Path()
                    fillPath.addPath(path)
                    fillPath.lineTo(points.last().x, height)
                    fillPath.lineTo(points.first().x, height)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )

                    // Draw glow points
                    points.forEach { pt ->
                        drawCircle(primaryColor, radius = 6f, center = pt)
                        drawCircle(Color.White, radius = 2f, center = pt)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Graph Labels (Days of Week)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { day ->
                    Text(day, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Consistency Grid Header
            Text(
                "CONSISTENCY",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Consistency Row Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val calendar = Calendar.getInstance()
                for (i in 0..6) {
                    val targetDay = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6 + i) }
                    val isDayCompleted = logs.any { log ->
                        val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                        log.isCompleted &&
                                logCal.get(Calendar.YEAR) == targetDay.get(Calendar.YEAR) &&
                                logCal.get(Calendar.DAY_OF_YEAR) == targetDay.get(Calendar.DAY_OF_YEAR)
                    }
                    val dayName = daysOfWeek[i]

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, if (isDayCompleted) primaryColor else Color.DarkGray, RoundedCornerShape(6.dp))
                                .background(
                                    if (isDayCompleted) primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .neonPanel(
                                    color = if (isDayCompleted) primaryColor else Color.Transparent,
                                    borderRadius = 6.dp,
                                    blurRadius = if (isDayCompleted) 8.dp else 0.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDayCompleted) {
                                Icon(Icons.Default.Check, contentDescription = "Done", tint = primaryColor, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(dayName, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: String, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(title, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun ProfileTabContent(
    user: User,
    onUpdateTheme: (String) -> Unit,
    onUpdateTargetDays: (Int) -> Unit,
    onUpdateCustomTimers: (Int, Int) -> Unit,
    onToggleBpMode: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onResetSystemData: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var isThemeDropdownExpanded by remember { mutableStateOf(false) }

    val themes = listOf(
        "SOLO_BLUE" to "Solo Leveling Blue",
        "MONARCH_RED" to "Monarch Red",
        "SHADOW_PURPLE" to "Shadow Purple",
        "GATEKEEPER_GREEN" to "Gatekeeper Green"
    )

    var workoutDays by remember { mutableStateOf(user.targetWorkoutDaysPerWeek) }
    var activeTimer by remember { mutableStateOf(if (user.customActiveDurationSeconds == 0) 30 else user.customActiveDurationSeconds) }
    var restTimer by remember { mutableStateOf(if (user.customRestDurationSeconds == 0) 30 else user.customRestDurationSeconds) }

    var useFormulaTimers by remember { mutableStateOf(user.customActiveDurationSeconds == 0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Text(
                "SYSTEM SETTINGS",
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Theme Selector
            Text("SYSTEM LOOK (THEME)", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .clickable { isThemeDropdownExpanded = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = themes.firstOrNull { it.first == user.theme }?.second ?: "Default Blue",
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Theme", tint = Color.Gray)
                }

                DropdownMenu(
                    expanded = isThemeDropdownExpanded,
                    onDismissRequest = { isThemeDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth().background(AbsoluteBlack).border(1.dp, Color.DarkGray)
                ) {
                    themes.forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme.second, color = Color.White) },
                            onClick = {
                                onUpdateTheme(theme.first)
                                isThemeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Workout Days Goal
            Text("TARGET WORKOUT DAYS PER WEEK", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (workoutDays > 3) {
                            workoutDays--
                            onUpdateTargetDays(workoutDays)
                        }
                    },
                    enabled = workoutDays > 3
                ) {
                    Text("-", color = if (workoutDays > 3) primaryColor else Color.Gray, fontSize = 24.sp)
                }
                Text("$workoutDays Days", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = {
                        if (workoutDays < 7) {
                            workoutDays++
                            onUpdateTargetDays(workoutDays)
                        }
                    },
                    enabled = workoutDays < 7
                ) {
                    Text("+", color = if (workoutDays < 7) primaryColor else Color.Gray, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // BP Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("BP/HYPERTENSION SAFE MODE", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Exclude intense isometric movements for user safety", color = Color.Gray, fontSize = 11.sp)
                }
                Switch(
                    checked = user.bpModeActive,
                    onCheckedChange = { onToggleBpMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = primaryColor,
                        checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Dark Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DARK MODE STYLE", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Toggle between light and dark background systems", color = Color.Gray, fontSize = 11.sp)
                }
                Switch(
                    checked = user.isDarkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = primaryColor,
                        checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Timers Customization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CUSTOM WORKOUT TIMERS", color = Color.White, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use Stats Formula", color = Color.Gray, fontSize = 12.sp)
                    Checkbox(
                        checked = useFormulaTimers,
                        onCheckedChange = { checked ->
                            useFormulaTimers = checked
                            if (checked) {
                                onUpdateCustomTimers(0, 0)
                            } else {
                                onUpdateCustomTimers(activeTimer, restTimer)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryColor,
                            uncheckedColor = Color.DarkGray
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (!useFormulaTimers) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    // Active Timer Selector
                    Text("Active Interval: $activeTimer seconds", color = Color.Gray, fontSize = 14.sp)
                    Slider(
                        value = activeTimer.toFloat(),
                        onValueChange = { activeTimer = it.toInt() },
                        onValueChangeFinished = { onUpdateCustomTimers(activeTimer, restTimer) },
                        valueRange = 10f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Rest Timer Selector
                    Text("Rest Interval: $restTimer seconds", color = Color.Gray, fontSize = 14.sp)
                    Slider(
                        value = restTimer.toFloat(),
                        onValueChange = { restTimer = it.toInt() },
                        onValueChangeFinished = { onUpdateCustomTimers(activeTimer, restTimer) },
                        valueRange = 10f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Active & Rest timers are dynamically calculated using your VIT and AGI attributes.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onResetSystemData,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3333)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
            ) {
                Text("RESET SYSTEM DATA", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, primaryColor, RoundedCornerShape(8.dp))
                .background(AbsoluteBlack, RoundedCornerShape(8.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "RENAME PLAYER",
                    style = MaterialTheme.typography.titleLarge,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text("Player Name", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Gray)
                    }
                    Button(
                        onClick = { onConfirm(text) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("CONFIRM", color = AbsoluteBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, primaryColor, RoundedCornerShape(8.dp))
                .background(AbsoluteBlack, RoundedCornerShape(8.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "SYSTEM INFORMATION",
                    style = MaterialTheme.typography.titleLarge,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("EXP BREAKDOWN", color = AlertGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "You earn 200 XP upon clearing the standard Daily Quest. Engaging the Panic Button halts the workout and transfers you to the recovery zone, granting a partial 100 XP. Level up requirement formula: 100 * Level^1.5.",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("STATS EXPLANATION", color = AlertGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "• STR (Strength): Increases physical capacity.\n• VIT (Vitality): Reduces dynamic Rest cooldown duration.\n• AGI (Agility): Increases dynamic exercise Active duration.",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("ROUTINE EXERCISES", color = AlertGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "The system generates a custom daily routine comprised of Isometric hold, Cardio, Flexibility and Strength movements. The exercise duration scales with level difficulty.",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("OK", color = AbsoluteBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
