package com.flix.music.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode { System, Light, Dark }

private val LocalFlixColors = staticCompositionLocalOf<FlixColors> {
    error("FlixColors no provisto: envuelve la UI en FlixTheme { ... }")
}

private fun FlixColors.toMaterial(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()

    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        secondary = secondary,
        error = error,
        background = background,
        surface = surface,
        surfaceVariant = surfaceVariant,
        onBackground = textPrimary,
        onSurface = textPrimary,
        onSurfaceVariant = textSecondary,
        outline = textTertiary,
        outlineVariant = divider,
        scrim = overlay,
    )
}

@Composable
fun FlixTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colors = if (dark) DarkFlixColors else LightFlixColors

    CompositionLocalProvider(LocalFlixColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterial(dark),
            typography = Typography,
            content = content
        )
    }
}

object FlixTheme {
    val colors: FlixColors
    @Composable @ReadOnlyComposable
    get() = LocalFlixColors.current
}