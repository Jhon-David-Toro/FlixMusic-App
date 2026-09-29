package com.flix.music.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.flix.music.R
import com.flix.music.core.designsystem.component.PlayPauseButton
import com.flix.music.core.designsystem.component.SeekBar
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerState
import kotlin.time.Duration.Companion.seconds

/**
 * Letra (design `lyricsnone`), a layer inside the player. v1 has no lyrics endpoint, so
 * it always shows "Letra no disponible" while the minimum controls stay usable: seek
 * bar, times, previous · play/pause · next. Swipe down or Back returns to the player.
 */
@Composable
fun LyricsScreen(state: PlayerState, actions: FullPlayerActions, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val nowPlaying = state.current ?: return
    val tint = rememberArtworkTint(nowPlaying.artworkUrl)
    val background = FlixTheme.colors.background
    val closeThreshold = with(LocalDensity.current) { CLOSE_DRAG_THRESHOLD.toPx() }
    val playPause = playPauseAction(state.status, isMiniPlayer = false)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .background(Brush.verticalGradient(0f to tint, GRADIENT_END to background))
            .pointerInput(Unit) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { _, dragAmount -> dragDistance += dragAmount },
                    onDragEnd = { if (dragDistance > closeThreshold) onClose() },
                )
            }
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = FlixTheme.spacing.xs, end = FlixTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                    contentDescription = stringResource(R.string.lyrics_close),
                    tint = FlixTheme.colors.textPrimary,
                    modifier = Modifier.size(26.dp),
                )
            }
            AsyncImage(
                model = nowPlaying.artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FlixTheme.colors.surfaceVariant),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = nowPlaying.title,
                    style = FlixTheme.typography.label,
                    color = FlixTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = nowPlaying.artistNames,
                    style = FlixTheme.typography.caption,
                    color = FlixTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            StateView(
                icon = R.drawable.ic_lyrics,
                title = stringResource(R.string.lyrics_unavailable_title),
                body = stringResource(R.string.lyrics_unavailable_body),
            )
        }
        Column(Modifier.padding(horizontal = FlixTheme.spacing.gutter, vertical = FlixTheme.spacing.lg)) {
            SeekBar(
                position = state.position,
                bufferedPosition = state.bufferedPosition,
                duration = state.duration.takeIf { state.status != PlaybackStatus.Loading },
                onSeek = actions.onSeek,
                contentDescription = stringResource(R.string.player_seek),
                bufferPulsing = state.status == PlaybackStatus.Buffering || state.status == PlaybackStatus.Loading,
            )
            ElapsedAndRemaining(position = state.position, duration = state.duration)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = FlixTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = actions.onPrevious, enabled = state.hasPrevious || state.position > 0.seconds) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_previous_filled),
                        contentDescription = stringResource(R.string.player_previous),
                        tint = FlixTheme.colors.textPrimary,
                    )
                }
                PlayPauseButton(
                    icon = playPause.icon,
                    contentDescription = stringResource(playPause.label),
                    onClick = actions.onPlayPause,
                    size = 52.dp,
                    iconSize = 28.dp,
                )
                IconButton(onClick = actions.onNext, enabled = state.hasNext) {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_next_filled),
                        contentDescription = stringResource(R.string.player_next),
                        tint = FlixTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}

private const val GRADIENT_END = 0.78f
private val CLOSE_DRAG_THRESHOLD = 120.dp

@Preview(name = "Letra · no disponible", widthDp = 360, heightDp = 760)
@Composable
private fun LyricsPreview() {
    FlixPreview {
        LyricsScreen(
            state = PlayerState(
                status = PlaybackStatus.Playing,
                current = Track("1", "Fade To Black", listOf(ArtistRef(null, "Metallica")), null, null, null),
                position = 161.seconds,
                duration = 417.seconds,
                hasNext = true,
            ),
            actions = FullPlayerActions(),
            onClose = {},
        )
    }
}
