package com.flix.music.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FlixPreview(
    themeMode: ThemeMode = ThemeMode.Dark,
    content: @Composable () -> Unit,
) {
    FlixTheme(themeMode) {
        Box(
            Modifier
                .fillMaxSize()
                .background(FlixTheme.colors.background)
        ) {
            content()
        }
    }
}
