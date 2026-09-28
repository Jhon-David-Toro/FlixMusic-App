package com.flix.music.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.flix.music.core.designsystem.theme.FlixTheme

/**
 * Snackbar from the design (`.snack`): inverted neutral surface (light on dark theme,
 * dark on light theme), radius md, and a violet action readable on that surface.
 */
@Composable
fun FlixSnackbar(snackbarData: SnackbarData, modifier: Modifier = Modifier) {
    val actionColor = if (FlixTheme.isDark) FlixTheme.colors.primary else ActionOnDarkSnackbar
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier.padding(horizontal = FlixTheme.spacing.sm),
        shape = FlixTheme.shapes.md,
        containerColor = FlixTheme.colors.textPrimary,
        contentColor = FlixTheme.colors.background,
        actionColor = actionColor,
        actionContentColor = actionColor,
    )
}

/** Light violet used for actions on the dark snackbar of the light theme (`.snack .act`). */
private val ActionOnDarkSnackbar = Color(0xFFA78BFA)
