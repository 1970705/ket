package com.wordland.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme =
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = SurfaceLight,
        primaryContainer = PrimaryVariant,
        onPrimaryContainer = TextPrimaryLight,
        secondary = SecondaryLight,
        onSecondary = SurfaceLight,
        secondaryContainer = SecondaryVariant,
        onSecondaryContainer = TextPrimaryLight,
        tertiary = AccentOrange,
        onTertiary = SurfaceLight,
        error = ErrorRed,
        onError = SurfaceLight,
        background = BackgroundLight,
        onBackground = TextPrimaryLight,
        surface = SurfaceLight,
        onSurface = TextPrimaryLight,
        surfaceVariant = ProgressBackground,
        onSurfaceVariant = TextSecondaryLight,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = PrimaryLight,
        onPrimary = BackgroundDark,
        primaryContainer = PrimaryDark,
        onPrimaryContainer = TextPrimaryDark,
        secondary = SecondaryLight,
        onSecondary = BackgroundDark,
        secondaryContainer = SecondaryDark,
        onSecondaryContainer = TextPrimaryDark,
        tertiary = AccentOrange,
        onTertiary = BackgroundDark,
        error = ErrorRed,
        onError = BackgroundDark,
        background = BackgroundDark,
        onBackground = TextPrimaryDark,
        surface = SurfaceDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = ProgressBackground,
        onSurfaceVariant = TextSecondaryDark,
    )

@Composable
fun WordlandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
