package com.sololeveling.systemfit.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun getThemeColorScheme(themeName: String) = when (themeName.uppercase()) {
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

@Composable
fun SystemFitTheme(
    themeName: String = "SOLO_BLUE",
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(themeName)
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
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
