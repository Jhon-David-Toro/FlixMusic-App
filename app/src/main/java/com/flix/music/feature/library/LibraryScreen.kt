package com.flix.music.feature.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flix.music.R
import com.flix.music.core.designsystem.theme.FlixTheme

@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(FlixTheme.spacing.gutter)
    ) {
        Text(
            text = stringResource(R.string.tab_library),
            style = FlixTheme.typography.headline,
            color = FlixTheme.colors.textPrimary
        )
    }
}