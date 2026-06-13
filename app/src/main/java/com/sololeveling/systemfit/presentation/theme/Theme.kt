package com.sololeveling.systemfit.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun getRankPrimaryColor(themeName: String, rank: String): Color {
    val cleanRank = rank.uppercase().substringBefore("-").trim()
    return when (themeName.uppercase()) {
        "MONARCH_RED" -> when (cleanRank) {
            "S" -> Color(0xFFFF3333)
            "A" -> Color(0xFFBE123C)
            "B" -> Color(0xFFFF0055)
            "C" -> Color(0xFFE11D48)
            "D" -> Color(0xFFF43F5E)
            else -> Color(0xFF884444)
        }
        "SHADOW_PURPLE" -> when (cleanRank) {
            "S" -> Color(0xFFE879F9)
            "A" -> Color(0xFF6D28D9)
            "B" -> Color(0xFF8B5CF6)
            "C" -> Color(0xFFD946EF)
            "D" -> Color(0xFFA855F7)
            else -> Color(0xFF8F70A3)
        }
        "GATEKEEPER_GREEN" -> when (cleanRank) {
            "S" -> Color(0xFF00FF88)
            "A" -> Color(0xFF059669)
            "B" -> Color(0xFF10B981)
            "C" -> Color(0xFF10B981)
            "D" -> Color(0xFF34D399)
            else -> Color(0xFF658070)
        }
        else -> when (cleanRank) { // SOLO_BLUE / Default
            "S" -> Color(0xFF00F0FF)
            "A" -> Color(0xFF6366F1)
            "B" -> Color(0xFF0088FF)
            "C" -> Color(0xFF06B6D4)
            "D" -> Color(0xFF38BDF8)
            else -> Color(0xFF64748B)
        }
    }
}

fun getRankSurfaceColor(primaryColor: Color, isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        // Deep blend of black and the primary color for a subtle cyber glow
        Color(
            red = (primaryColor.red * 0.08f + 0.05f).coerceIn(0f, 1f),
            green = (primaryColor.green * 0.08f + 0.06f).coerceIn(0f, 1f),
            blue = (primaryColor.blue * 0.08f + 0.09f).coerceIn(0f, 1f)
        )
    } else {
        // Light theme card background with a tiny tint of the primary color
        Color(
            red = (primaryColor.red * 0.05f + 0.93f).coerceIn(0f, 1f),
            green = (primaryColor.green * 0.05f + 0.94f).coerceIn(0f, 1f),
            blue = (primaryColor.blue * 0.05f + 0.97f).coerceIn(0f, 1f)
        )
    }
}

@Composable
fun getThemeColorScheme(themeName: String, isDarkMode: Boolean, rank: String) = if (isDarkMode) {
    val primaryColor = getRankPrimaryColor(themeName, rank)
    val surfaceColor = getRankSurfaceColor(primaryColor, true)
    darkColorScheme(
        primary = primaryColor,
        secondary = AlertGold,
        tertiary = primaryColor,
        background = AbsoluteBlack,
        surface = surfaceColor,
        onPrimary = AbsoluteBlack,
        onSecondary = AbsoluteBlack,
        onTertiary = AbsoluteBlack,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
    )
} else {
    val primaryColor = getRankPrimaryColor(themeName, rank)
    val surfaceColor = getRankSurfaceColor(primaryColor, false)
    lightColorScheme(
        primary = primaryColor,
        secondary = AlertGold,
        tertiary = primaryColor,
        background = Color(0xFFF8FAFC),
        surface = surfaceColor,
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onTertiary = Color.White,
        onBackground = Color(0xFF0F172A),
        onSurface = Color(0xFF1E293B),
    )
}

@Composable
fun SystemFitTheme(
    themeName: String = "SOLO_BLUE",
    isDarkMode: Boolean = true,
    rank: String = "E-Rank",
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(themeName, isDarkMode, rank)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var context = view.context
            while (context is android.content.ContextWrapper) {
                if (context is Activity) break
                context = context.baseContext
            }
            val window = (context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
