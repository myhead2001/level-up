package com.sololeveling.systemfit.presentation.workout

import android.app.Activity
import android.os.Build.VERSION.SDK_INT
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.sololeveling.systemfit.presentation.theme.SystemBlue

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
    
    // WakeLock Hook
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is WorkoutContract.SideEffect.PlaySystemChime -> { /* TODO: Play tone */ }
                is WorkoutContract.SideEffect.TriggerHapticAlert -> { /* TODO: Haptics */ }
                is WorkoutContract.SideEffect.ShowToast -> {
                    android.widget.Toast.makeText(context, effect.message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    // Coil GIF ImageLoader
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
                    CircularProgressIndicator(color = SystemBlue)
                }
            }
            is WorkoutContract.UiState.Setup -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                    Text(
                        text = "NEW QUEST GENERATED",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AlertGold
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(modifier = Modifier.neonPanel().padding(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Duration: ${state.totalDurationMinutes} mins", color = Color.White)
                            Text("Total Rounds: ${state.rounds}", color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(64.dp))
                    Button(
                        onClick = { viewModel.onEvent(WorkoutContract.UiEvent.StartQuest) },
                        colors = ButtonDefaults.buttonColors(containerColor = SystemBlue)
                    ) {
                        Text("ACCEPT QUEST", color = AbsoluteBlack, fontWeight = FontWeight.Bold)
                    }
                }
                }
            }
            is WorkoutContract.UiState.ActiveCombat -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Round: ${state.currentRound}/${state.totalRounds}", color = SystemBlue)
                        if (state.isRestPeriod) {
                            Text("REST", color = AlertGold, fontWeight = FontWeight.Bold)
                        } else {
                            Text("ACTIVE", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    // GIF Display
                    Box(modifier = Modifier.size(200.dp).neonPanel(color = if (state.isRestPeriod) AlertGold else SystemBlue)) {
                        // In actual deployment, gifUrl would be a local asset or valid remote url
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(state.currentExercise.gifUrl)
                                .crossfade(true)
                                .build(),
                            imageLoader = imageLoader,
                            contentDescription = "Exercise Animation",
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(state.currentExercise.name, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Timer
                    Box(contentAlignment = Alignment.Center) {
                        CountdownRing(
                            progress = 1f, // A simplified progress as we don't have max duration in state easily
                            activeColor = if (state.isRestPeriod) AlertGold else SystemBlue
                        )
                        Text(
                            text = "${state.timeLeftSeconds}",
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Panic Button
                    Button(
                        onClick = { viewModel.onEvent(WorkoutContract.UiEvent.TriggerPanicButton) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    ) {
                        Text("EMERGENCY HALT (BP SPIKE)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                }
            }
            is WorkoutContract.UiState.PenaltyZone -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("EMERGENCY PROTOCOL ACTIVE", color = Color.Red, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Controlled Nasal Recovery: ${state.penaltyDurationMinutes} mins", color = Color.White)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onNavigateBack) {
                        Text("RETURN TO DASHBOARD")
                    }
                }
            }
            is WorkoutContract.UiState.Victory -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("QUEST CLEARED", style = MaterialTheme.typography.headlineMedium, color = AlertGold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("+${state.xpEarned} XP", color = SystemBlue, style = MaterialTheme.typography.displayMedium)
                    if (state.levelUp) {
                        Text("LEVEL UP!", color = AlertGold, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = onNavigateBack) {
                        Text("CLAIM REWARDS")
                    }
                }
            }
        }
    }
}
