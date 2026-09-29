package com.flix.music.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.flix.music.R
import com.flix.music.core.designsystem.theme.FlixTheme

/**
 * Informative banner (design `.banner.info`): surfaceVariant, radius md, an `info`
 * icon in the info color and 12sp text. Used where content may be incomplete, such as
 * the album tracks derived from search.
 */
@Composable
fun InfoBanner(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FlixTheme.shapes.md)
            .background(FlixTheme.colors.surfaceVariant)
            .padding(horizontal = FlixTheme.spacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = null,
            tint = FlixTheme.colors.info,
            modifier = Modifier.size(18.dp),
        )
        Text(text = text, style = FlixTheme.typography.caption, color = FlixTheme.colors.textPrimary)
    }
}
