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

@Composable
fun getThemeColorScheme(themeName: String, isDarkMode: Boolean) = if (isDarkMode) {
    when (themeName.uppercase()) {
        "MONARCH_RED" -> darkColorScheme(
            primary = MonarchRed,
            secondary = AlertGold,
            tertiary = MonarchRed,
            background = AbsoluteBlack,
            surface = MonarchRedDark,
            onPrimary = AbsoluteBlack,
            onSecondary = AbsoluteBlack,
            onTertiary = AbsoluteBlack,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        )
        "SHADOW_PURPLE" -> darkColorScheme(
            primary = ShadowPurple,
            secondary = AlertGold,
            tertiary = ShadowPurple,
            background = AbsoluteBlack,
            surface = ShadowPurpleDark,
            onPrimary = AbsoluteBlack,
            onSecondary = AbsoluteBlack,
            onTertiary = AbsoluteBlack,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        )
        "GATEKEEPER_GREEN" -> darkColorScheme(
            primary = GatekeeperGreen,
            secondary = AlertGold,
            tertiary = GatekeeperGreen,
            background = AbsoluteBlack,
            surface = GatekeeperGreenDark,
            onPrimary = AbsoluteBlack,
            onSecondary = AbsoluteBlack,
            onTertiary = AbsoluteBlack,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        )
        else -> darkColorScheme(
            primary = SystemBlue,
            secondary = AlertGold,
            tertiary = SystemBlue,
            background = AbsoluteBlack,
            surface = DarkSurface,
            onPrimary = AbsoluteBlack,
            onSecondary = AbsoluteBlack,
            onTertiary = AbsoluteBlack,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
        )
    }
} else {
    // Light Mode! Sleek white/slate backgrounds with high-contrast vibrant neon primary/secondary colors
    when (themeName.uppercase()) {
        "MONARCH_RED" -> lightColorScheme(
            primary = MonarchRed,
            secondary = AlertGold,
            tertiary = MonarchRed,
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFE2E8F0),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.White,
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF1E293B),
        )
        "SHADOW_PURPLE" -> lightColorScheme(
            primary = ShadowPurple,
            secondary = AlertGold,
            tertiary = ShadowPurple,
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFE2E8F0),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.White,
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF1E293B),
        )
        "GATEKEEPER_GREEN" -> lightColorScheme(
            primary = GatekeeperGreen,
            secondary = AlertGold,
            tertiary = GatekeeperGreen,
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFE2E8F0),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.White,
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF1E293B),
        )
        else -> lightColorScheme(
            primary = SystemBlue,
            secondary = AlertGold,
            tertiary = SystemBlue,
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFE2E8F0),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.White,
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF1E293B),
        )
    }
}

@Composable
fun SystemFitTheme(
    themeName: String = "SOLO_BLUE",
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(themeName, isDarkMode)
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
