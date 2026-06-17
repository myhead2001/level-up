package com.sololeveling.systemfit.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GlitchText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    var offset1 by remember { mutableStateOf(Offset.Zero) }
    var offset2 by remember { mutableStateOf(Offset.Zero) }
    var glitchActive by remember { mutableStateOf(false) }
    var displayedText by remember(text) { mutableStateOf(text) }

    LaunchedEffect(text) {
        while (true) {
            val nextDelay = if (Random.nextFloat() < 0.25f) 400L else Random.nextLong(1500, 5000)
            delay(nextDelay)
            
            glitchActive = true
            val burstCount = Random.nextInt(3, 7)
            repeat(burstCount) {
                offset1 = Offset(Random.nextFloat() * 12f - 6f, Random.nextFloat() * 6f - 3f)
                offset2 = Offset(Random.nextFloat() * 12f - 6f, Random.nextFloat() * 6f - 3f)
                
                if (text.isNotEmpty()) {
                    val chars = text.toCharArray()
                    val scrambleCount = Random.nextInt(1, kotlin.math.min(4, text.length + 1))
                    repeat(scrambleCount) {
                        val idx = Random.nextInt(text.length)
                        if (!chars[idx].isWhitespace()) {
                            chars[idx] = listOf('_', '*', '?', 'X', '#', '@', '1', '0', '!').random()
                        }
                    }
                    displayedText = String(chars)
                }
                delay(Random.nextLong(40, 80))
            }
            glitchActive = false
            displayedText = text
            offset1 = Offset.Zero
            offset2 = Offset.Zero
        }
    }

    Box(modifier = modifier) {
        if (glitchActive) {
            Text(
                text = displayedText,
                style = style,
                color = Color(0xFF00E5FF).copy(alpha = 0.7f), // Neon cyan split shadow
                modifier = Modifier.offset(offset1.x.dp, offset1.y.dp)
            )
            Text(
                text = displayedText,
                style = style,
                color = Color(0xFFFF3366).copy(alpha = 0.6f), // Neon magenta split shadow
                modifier = Modifier.offset(offset2.x.dp, offset2.y.dp)
            )
        }
        Text(
            text = displayedText,
            style = style,
            color = color
        )
    }
}
