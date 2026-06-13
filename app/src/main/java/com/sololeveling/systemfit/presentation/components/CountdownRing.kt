package com.sololeveling.systemfit.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sololeveling.systemfit.presentation.theme.SystemBlue
import com.sololeveling.systemfit.presentation.theme.DarkSurface

@Composable
fun CountdownRing(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = SystemBlue,
    inactiveColor: Color = DarkSurface,
    strokeWidth: Float = 12f
) {
    Canvas(modifier = modifier.size(200.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f - strokeWidth

        // Draw inactive track
        drawCircle(
            color = inactiveColor,
            center = center,
            radius = radius,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Draw active progress
        drawArc(
            color = activeColor,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(center.x - radius, center.y - radius)
        )
    }
}
