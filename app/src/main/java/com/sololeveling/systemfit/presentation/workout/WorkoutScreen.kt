package com.sololeveling.systemfit.presentation.workout

import android.app.Activity
import android.media.AudioManager
import android.media.ToneGenerator
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
import androidx.compose.ui.window.Dialog
import com.sololeveling.systemfit.presentation.components.CountdownRing
import com.sololeveling.systemfit.presentation.components.neonPanel
import com.sololeveling.systemfit.presentation.components.GlitchText
import com.sololeveling.systemfit.presentation.utils.SoundManager
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

    // Lock back button during Penalty Zone survival or Controlled Recovery
    BackHandler(enabled = true) {
        if (uiState !is WorkoutContract.UiState.PenaltyZone && uiState !is WorkoutContract.UiState.ControlledRecovery) {
            viewModel.onEvent(WorkoutContract.UiEvent.ExitWorkout)
            onNavigateBack()
        }
    }

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is WorkoutContract.SideEffect.PlaySystemChime -> {
                    try {
                        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 120) // High quality chime beep
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
                is WorkoutContract.SideEffect.TriggerHapticAlert -> {
                    try {
                        val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                        toneGen.startTone(ToneGenerator.TONE_SUP_ERROR, 300) // Distinct alarm tone
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
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
        color = MaterialTheme.colorScheme.background
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
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
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(24.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ESTIMATED TARGET", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Duration: ${state.totalDurationMinutes} minutes", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Rounds: ${state.rounds} Rounds", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleMedium)
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit", tint = MaterialTheme.colorScheme.onBackground)
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
                            color = MaterialTheme.colorScheme.onBackground,
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
                        if (!state.isRestPeriod && (state.isBpModeActive || state.currentExercise.isHtnSafe)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color(0xFFFF3333), RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF3333).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Continuous Breathing - Do Not Hold Breath",
                                    color = Color(0xFFFF3333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
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
                                    color = MaterialTheme.colorScheme.onBackground
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
                                Icon(Icons.Default.ArrowBack, contentDescription = "Prev Exercise", tint = MaterialTheme.colorScheme.onBackground)
                            }

                            // Dynamic button size to fit content without line break
                            Button(
                                onClick = { viewModel.onEvent(WorkoutContract.UiEvent.TogglePause) },
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
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
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Exercise", tint = MaterialTheme.colorScheme.onBackground)
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
                        color = MaterialTheme.colorScheme.onBackground,
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
            is WorkoutContract.UiState.ControlledRecovery -> {
                val infiniteTransition = rememberInfiniteTransition(label = "recovery_breathing")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3500),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "breathing_scale"
                )

                val breathingText = if (scale > 1.0f) "EXHALE\n(NASAL)" else "INHALE\n(NASAL)"
                val recoveryColor = Color(0xFF00E5FF)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .border(2.dp, recoveryColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "CONTROLLED RECOVERY",
                            color = recoveryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, recoveryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .background(Color(0xFF04161C), RoundedCornerShape(8.dp))
                            .padding(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Breathing Protocol",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "[Nasal Breathing Activated. Inhale deeply through your nose, expanding your diaphragm. Exhale slowly through your nose. Keep your posture upright.]",
                                color = recoveryColor,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size((120 * scale).dp)
                                .background(recoveryColor.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                                .border(2.dp, recoveryColor.copy(alpha = 0.7f), RoundedCornerShape(100.dp))
                        )
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(Color.Black, RoundedCornerShape(100.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(100.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = breathingText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    val minutes = state.timeLeftSeconds / 60
                    val seconds = state.timeLeftSeconds % 60
                    val digitalTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                    Text(
                        text = digitalTime,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "REST PROTOCOL TIMER",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Small button to force bypass controlled recovery
                    TextButton(
                        onClick = { viewModel.onEvent(WorkoutContract.UiEvent.SkipRecovery) }
                    ) {
                        Text("SKIP RECOVERY / RETURN", color = recoveryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            is WorkoutContract.UiState.Victory -> {
                // Determine monster defeated and rank based on user's level
                val monster = remember(state.playerLevel) {
                    val lvl = state.playerLevel
                    val eRank = listOf("Steel-Fanged Lycan", "Goblin Scout", "Swamp Spider", "Cave Dwarf")
                    val dRank = listOf("Hell's Gate Sentinel", "Desert Centipede", "Razan's Hound", "Cave Bear")
                    val cRank = listOf("Shadow Infantry Soldier", "Dungeon Lizardman", "Magma Slime", "Gargoyle")
                    val bRank = listOf("High Orc Commander", "Ice Elf Warrior", "Giant Stone Golem", "Dark Wraith")
                    val aRank = listOf("White Walker Archer", "Twin-Headed Ogre", "Kargalgan's Mage", "Blood-Red Igris")
                    val sRank = listOf("Shadow Commander Igris", "Ant King Beru", "Kamish the Dragon", "Baran, Demon King")

                    val list = when {
                        lvl >= 50 -> sRank
                        lvl >= 40 -> aRank
                        lvl >= 30 -> bRank
                        lvl >= 20 -> cRank
                        lvl >= 10 -> dRank
                        else -> eRank
                    }
                    list.random(kotlin.random.Random(state.xpEarned.toLong() + state.playerLevel))
                }

                val rankLabel = remember(state.playerLevel) {
                    val lvl = state.playerLevel
                    when {
                        lvl >= 50 -> "S-Rank"
                        lvl >= 40 -> "A-Rank"
                        lvl >= 30 -> "B-Rank"
                        lvl >= 20 -> "C-Rank"
                        lvl >= 10 -> "D-Rank"
                        else -> "E-Rank"
                    }
                }

                var showRewardsDialog by remember { mutableStateOf(false) }

                LaunchedEffect(showRewardsDialog) {
                    if (showRewardsDialog) {
                        SoundManager.playClaimRewards()
                    }
                }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .neonPanel(color = primaryColor)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
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
                                        text = "NOTIFICATION",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "You have defeated a",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "[$monster].",
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))

                        GlitchText(
                            text = "QUEST CLEARED",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = AlertGold
                        )
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
                                showRewardsDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
                        ) {
                            Text("CLAIM REWARDS", color = AbsoluteBlack, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showRewardsDialog) {
                        Dialog(onDismissRequest = { /* Force confirmation click */ }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, primaryColor, RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                    .padding(24.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    GlitchText(
                                        text = "REWARDS RECEIVED",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = AlertGold
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                            .padding(16.dp)
                                    ) {
                                        Column {
                                            Text("Defeated: $monster", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Dungeon Rank: $rankLabel", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Rewards Earned:", color = AlertGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(" • XP Gained: +${state.xpEarned} XP", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                                            if (state.levelUp) {
                                                Text(" • +5 Stat Points (Level Up Reward)", color = primaryColor, fontSize = 13.sp)
                                                Text(" • Rank status updated", color = primaryColor, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            showRewardsDialog = false
                                            viewModel.onEvent(WorkoutContract.UiEvent.ClaimRewards)
                                            onNavigateBack()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("CONFIRM", color = AbsoluteBlack, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
