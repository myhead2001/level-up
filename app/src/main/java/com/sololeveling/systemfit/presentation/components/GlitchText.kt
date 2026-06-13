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

    LaunchedEffect(text) {
        while (true) {
            delay(Random.nextLong(2500, 6000)) // Random interval between glitches
            glitchActive = true
            repeat(4) {
                offset1 = Offset(Random.nextFloat() * 8f - 4f, Random.nextFloat() * 4f - 2f)
                offset2 = Offset(Random.nextFloat() * 8f - 4f, Random.nextFloat() * 4f - 2f)
                delay(60)
            }
            glitchActive = false
            offset1 = Offset.Zero
            offset2 = Offset.Zero
        }
    }

    Box(modifier = modifier) {
        if (glitchActive) {
            Text(
                text = text,
                style = style,
                color = Color(0xFF00E5FF).copy(alpha = 0.6f), // Neon cyan split shadow
                modifier = Modifier.offset(offset1.x.dp, offset1.y.dp)
            )
            Text(
                text = text,
                style = style,
                color = Color(0xFFFF3366).copy(alpha = 0.5f), // Neon magenta split shadow
                modifier = Modifier.offset(offset2.x.dp, offset2.y.dp)
            )
        }
        Text(
            text = text,
            style = style,
            color = color
        )
    }
}
