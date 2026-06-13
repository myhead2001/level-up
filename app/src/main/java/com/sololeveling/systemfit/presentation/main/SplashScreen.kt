package com.sololeveling.systemfit.presentation.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sololeveling.systemfit.presentation.components.GlitchText
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val darkBackground = Color(0xFF070708)

    val bootLines = listOf(
        "[SYSTEM PROTOCOL INITIALIZED]",
        "[ESTABLISHING CONNECTION TO THE NEURAL NETWORK]",
        "[MEASURING PLAYER BIOMETRICS & BLOOD PRESSURE]",
        "[UPDATING STATUS AND DAILY QUEST DIFFICULTY...]",
        "[WARNING: SKIPPING A DAILY QUEST WILL TRIGGER PENALTY PROTOCOL]",
        "[PROTOCOL VERIFIED. WELCOME, PLAYER.]"
    )

    var currentLineIndex by remember { mutableStateOf(0) }
    var displayedText by remember { mutableStateOf("") }
    
    // Progress Indicator Animation
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Trigger typewriter bootlog lines
        for (i in bootLines.indices) {
            currentLineIndex = i
            val fullText = bootLines[i]
            displayedText = ""
            for (charIndex in 0..fullText.length) {
                displayedText = fullText.substring(0, charIndex)
                delay(30)
            }
            delay(350)
        }
        
        // Finalize loading progress
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
        delay(200)
        onTimeout()
    }

    // Secondary launched effect to run the progress indicator slowly alongside log lines
    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 0.85f,
            animationSpec = tween(durationMillis = 3500, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // System Logo & Header Alert Box
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .border(2.dp, primaryColor, RoundedCornerShape(4.dp))
                    .background(Color.Black)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                GlitchText(
                    text = "THE SYSTEM",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 8.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Boot log alert screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.Top) {
                    Text(
                        text = "BOOT STATUS LOG",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Display previous boot lines in gray
                    for (prev in 0 until currentLineIndex) {
                        Text(
                            text = bootLines[prev],
                            color = if (bootLines[prev].startsWith("[WARNING")) Color(0xFFFF3333).copy(alpha = 0.6f) else Color.DarkGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // Display active line typing out in neon color
                    Text(
                        text = displayedText,
                        color = if (displayedText.startsWith("[WARNING")) Color(0xFFFF3333) else primaryColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Progress Bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            LinearProgressIndicator(
                progress = { progressAnim.value },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = primaryColor,
                trackColor = Color.DarkGray.copy(alpha = 0.3f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SYNCHRONIZING SYSTEM DATA ... ${(progressAnim.value * 100).toInt()}%",
                color = Color.Gray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
