package com.flix.music.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.flix.music.R
import com.flix.music.core.designsystem.theme.FlixTheme

/**
 * Song row. When [isCurrent] it follows the design's "now playing" row (`.row.on`):
 * title in primaryContent and the equalizer in place of the duration, animated only
 * while [isPlaying]. The state is also announced to screen readers.
 * [trailing] replaces the duration/equalizer slot (e.g. the ♥ in Favoritos or the
 * time in Historial).
 */
@Composable
fun TrackRow(
    title: String,
    subtitle: String,
    artworkUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    duration: String? = null,
    isCurrent: Boolean = false,
    isPlaying: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val playbackStateLabel = when {
        !isCurrent -> null
        isPlaying -> stringResource(R.string.now_playing)
        else -> stringResource(R.string.now_paused)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = FlixTheme.spacing.rowHeight)
            .clickable(onClick = onClick)
            .semantics { if (playbackStateLabel != null) stateDescription = playbackStateLabel }
            .padding(horizontal = FlixTheme.spacing.gutter),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.itemGap),
    ) {
        AsyncImage(
            model = artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(FlixTheme.shapes.sm)
                .background(FlixTheme.colors.surfaceVariant),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = FlixTheme.typography.subtitle,
                color = if (isCurrent) FlixTheme.colors.primaryContent else FlixTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = FlixTheme.typography.bodySmall,
                color = FlixTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        when {
            trailing != null -> trailing()
            isCurrent -> NowPlayingEqualizer(animated = isPlaying)
            duration != null -> Text(
                text = duration,
                style = FlixTheme.typography.caption,
                color = FlixTheme.colors.textTertiary,
            )
        }
    }
}

/** Which track is playing, so lists can mark its row. */
data class NowPlayingMarker(
    val trackId: String? = null,
    val isPlaying: Boolean = false,
)
