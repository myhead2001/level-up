package com.sololeveling.systemfit.presentation.workout

import android.app.Activity
import android.os.Build.VERSION.SDK_INT
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.sololeveling.systemfit.presentation.components.CountdownRing
import com.sololeveling.systemfit.presentation.components.neonPanel
import com.sololeveling.systemfit.presentation.theme.AbsoluteBlack
import com.sololeveling.systemfit.presentation.theme.AlertGold

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var currentContext = context
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) break
        currentContext = currentContext.baseContext
    }
    val activity = currentContext as? Activity

    val uiState by viewModel.uiState.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary

    // WakeLock Hook
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Lock back button during Penalty Zone survival
    BackHandler(enabled = true) {
        if (uiState !is WorkoutContract.UiState.PenaltyZone) {
            viewModel.onEvent(WorkoutContract.UiEvent.ExitWorkout)
            onNavigateBack()
        }
    }

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is WorkoutContract.SideEffect.PlaySystemChime -> { /* Haptic/sound tone */ }
                is WorkoutContract.SideEffect.TriggerHapticAlert -> { /* Haptic tone */ }
                is WorkoutContract.SideEffect.ShowToast -> {
                    android.widget.Toast.makeText(context, effect.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AbsoluteBlack
    ) {
        when (val state = uiState) {
            is WorkoutContract.UiState.Loading -> {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
            is WorkoutContract.UiState.Setup -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(
                        onClick = {
                            viewModel.onEvent(WorkoutContract.UiEvent.ExitWorkout)
                            onNavigateBack()
                        },
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, AlertGold, RoundedCornerShape(4.dp))
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "QUEST NOTICE",
                                color = AlertGold,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "[Daily Quest: Strength Training has arrived.]",
                            style = MaterialTheme.typography.titleLarge,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neonPanel(color = primaryColor)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(24.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ESTIMATED TARGET", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Duration: ${state.totalDurationMinutes} minutes", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Rounds: ${state.rounds} Rounds", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(64.dp))
                        Button(
                            onClick = { viewModel.onEvent(WorkoutContract.UiEvent.StartQuest) },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                        ) {
                            Text("ACCEPT QUEST", color = AbsoluteBlack, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
            is WorkoutContract.UiState.ActiveCombat -> {
                val accentColor = if (state.isRestPeriod) Color(0xFF33FF99) else primaryColor

                Box(modifier = Modifier.fillMaxSize()) {
                    // Back exit trigger
                    IconButton(
                        onClick = {
                            viewModel.onEvent(WorkoutContract.UiEvent.ExitWorkout)
                            onNavigateBack()
                        },
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Round: ${state.currentRound}/${state.totalRounds}",
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Box(
                                modifier = Modifier
                                    .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (state.isRestPeriod) "RECOVERY" else "ACTIVE COMBAT",
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // Animation GIF
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .neonPanel(color = accentColor)
                                .background(Color.Black, RoundedCornerShape(12.dp))
                                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isRestPeriod) {
                                // Dynamic resting visual
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data("https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExM2o0aWF3eWZia3h5cGtqa2h5d21tZHBkbmh2ZzI2d2pxeWw3NWk1NCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9cw/X8z9Z4lT475P9Yw472/giphy.gif")
                                        .crossfade(true)
                                        .build(),
                                    imageLoader = imageLoader,
                                    contentDescription = "Rest Recovery Animation",
                                    modifier = Modifier.fillMaxSize().padding(12.dp)
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(state.currentExercise.gifUrl)
                                        .crossfade(true)
                                        .build(),
                                    imageLoader = imageLoader,
                                    contentDescription = "Exercise Animation",
                                    modifier = Modifier.fillMaxSize().padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (state.isRestPeriod) "RECOVERY BREAK" else state.currentExercise.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.isRestPeriod) "Take deep breaths. Prepare for next exercise." else state.currentExercise.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Timer Circle & Countdown
                        Box(contentAlignment = Alignment.Center) {
                            CountdownRing(
                                progress = 1f, // Static/full ring simplified
                                activeColor = accentColor
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.timeLeftSeconds.toString(),
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text("SECONDS", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Next exercise preview
                        if (state.nextExerciseName != null) {
                            Text(
                                "NEXT: ${state.nextExerciseName}",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Workout controls row (Prev, Pause/Play, Next, SkipRest)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.onEvent(WorkoutContract.UiEvent.PrevExercise) },
                                modifier = Modifier.background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Prev Exercise", tint = Color.White)
                            }

                            Button(
                                onClick = { viewModel.onEvent(WorkoutContract.UiEvent.TogglePause) },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp).width(120.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (state.isPaused) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Resume",
                                            tint = AbsoluteBlack
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (state.isPaused) "RESUME" else "PAUSE",
                                        color = AbsoluteBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (state.isRestPeriod) {
                                Button(
                                    onClick = { viewModel.onEvent(WorkoutContract.UiEvent.SkipRest) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AlertGold),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("SKIP", color = AbsoluteBlack, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                IconButton(
                                    onClick = { viewModel.onEvent(WorkoutContract.UiEvent.NextExercise) },
                                    modifier = Modifier.background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Exercise", tint = Color.White)
                                }
                            }
                        }

                        // Emergency panic button
                        Button(
                            onClick = { viewModel.onEvent(WorkoutContract.UiEvent.TriggerPanicButton) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3333)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(55.dp)
                        ) {
                            Text("EMERGENCY HALT (BP/HEART SPIKE)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is WorkoutContract.UiState.PenaltyZone -> {
                val infiniteTransition = rememberInfiniteTransition(label = "penalty_flash")
                val alphaVal by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "flashing_red"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Flashing Alert Header
                    Box(
                        modifier = Modifier
                            .alpha(alphaVal)
                            .border(2.dp, Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "PENALTY PROTOCOL ACTIVE",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // Ornate corner notification card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .background(Color(0xFF220A0A), RoundedCornerShape(8.dp))
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Notice",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "[You have failed to complete your daily quest. You will be transferred to the \"Penalty Zone\" for an allotted amount of time.]",
                                color = Color.Red,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    // Survival timer text
                    val minutes = state.timeLeftSeconds / 60
                    val seconds = state.timeLeftSeconds % 60
                    val digitalTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                    Text(
                        text = digitalTime,
                        color = Color.White,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "TASK: JOG IN PLACE TO SURVIVE",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Exiting is prohibited. Clock must reach zero to cleanse status.",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is WorkoutContract.UiState.Victory -> {
                // Determine monster defeated based on user's current level
                // We'll approximate level check or default to Lycan
                val activityContext = LocalContext.current as? Activity
                val level = remember { 1 } // Default fallback level 1
                val monster = remember(level) {
                    when {
                        level >= 30 -> "High Orc Commander"
                        level >= 20 -> "Shadow Infantry Soldier"
                        level >= 10 -> "Hell's Gate Sentinel"
                        else -> "Steel-Fanged Lycan"
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Screenshot 2 style Notification Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neonPanel(color = primaryColor)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, primaryColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 24.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "NOTIFICATION",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "You have defeated a",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "[$monster].",
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Text("QUEST CLEARED", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = AlertGold)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.xpEarned > 0) {
                        Text(
                            text = "+${state.xpEarned} XP",
                            color = primaryColor,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (state.levelUp) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "LEVEL UP!",
                            color = AlertGold,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            letterSpacing = 2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(64.dp))
                    Button(
                        onClick = {
                            viewModel.onEvent(WorkoutContract.UiEvent.ClaimRewards)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                    ) {
                        Text("CLAIM REWARDS", color = AbsoluteBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
