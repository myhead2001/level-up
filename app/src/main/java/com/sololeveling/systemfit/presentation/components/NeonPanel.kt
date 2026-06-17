package com.sololeveling.systemfit.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.toArgb
import com.sololeveling.systemfit.presentation.theme.SystemBlue

fun Modifier.neonPanel(
    color: Color = SystemBlue,
    borderRadius: Dp = 8.dp,
    blurRadius: Dp = 16.dp
) = this.drawBehind {
    val blurRadiusPx = blurRadius.toPx()
    if (blurRadiusPx > 0f && color != Color.Transparent && color != Color.Unspecified) {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = color.copy(alpha = 0.5f).toArgb()
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                blurRadiusPx,
                android.graphics.BlurMaskFilter.Blur.OUTER
            )

            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = borderRadius.toPx(),
                radiusY = borderRadius.toPx(),
                paint = paint
            )
        }
    }
}
