package com.flix.music.feature.player

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.flix.music.R
import com.flix.music.core.designsystem.component.AnimatedIcon
import com.flix.music.core.designsystem.component.FavoriteButton
import com.flix.music.core.designsystem.component.LoadingRing
import com.flix.music.core.designsystem.component.util.rememberDelayedFlag
import com.flix.music.core.designsystem.theme.FlixMotion
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.designsystem.theme.ThemeMode
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

/**
 * Persistent mini player (design `.mini`): floating surfaceElevated card with medium
 * elevation above the navigation bar, 2 dp progress line at the bottom and one action
 * that changes with the playback state. Tap opens the Full Player; swiping sideways
 * skips to the next / previous track.
 */
@Composable
fun MiniPlayer(
    state: PlayerState,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    artworkModifier: Modifier = Modifier,
) {
    val nowPlaying = state.current ?: return
    val isError = state.status == PlaybackStatus.Error
    val shape = FlixTheme.shapes.md
    val swipeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val swipeThreshold = with(LocalDensity.current) { SWIPE_THRESHOLD.toPx() }
    val openLabel = stringResource(R.string.player_open)

    Box(
        modifier = modifier
            .padding(start = FlixTheme.spacing.sm, end = FlixTheme.spacing.sm, bottom = FlixTheme.spacing.sm)
            .fillMaxWidth()
            .height(MINI_PLAYER_HEIGHT)
            .shadow(MINI_PLAYER_ELEVATION, shape, ambientColor = elevationShadowColor(), spotColor = elevationShadowColor())
            .clip(shape)
            .background(FlixTheme.colors.surfaceElevated)
            .then(if (isError) Modifier.border(1.dp, FlixTheme.colors.error.copy(alpha = ERROR_BORDER_ALPHA), shape) else Modifier)
            .clickable(onClickLabel = openLabel, onClick = onOpen)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        coroutineScope.launch { swipeOffset.snapTo(swipeOffset.value + dragAmount) }
                    },
                    onDragEnd = {
                        when {
                            swipeOffset.value < -swipeThreshold -> onNext()
                            swipeOffset.value > swipeThreshold -> onPrevious()
                        }
                        coroutineScope.launch { swipeOffset.animateTo(0f, FlixMotion.gesture()) }
                    },
                    onDragCancel = {
                        coroutineScope.launch { swipeOffset.animateTo(0f, FlixMotion.gesture()) }
                    },
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .graphicsLayer { translationX = swipeOffset.value * SWIPE_FOLLOW_RATIO }
                .padding(start = FlixTheme.spacing.sm, end = FlixTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.md),
        ) {
            AsyncImage(
                model = nowPlaying.artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = artworkModifier
                    .size(MINI_ARTWORK_SIZE)
                    .clip(FlixTheme.shapes.sm)
                    .background(FlixTheme.colors.surfaceVariant),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = nowPlaying.title,
                    style = FlixTheme.typography.subtitle,
                    color = FlixTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = miniSubtitle(state.status, nowPlaying),
                    style = FlixTheme.typography.bodySmall,
                    color = FlixTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!isError) {
                FavoriteButton(
                    isFavorite = isFavorite,
                    onToggle = onToggleFavorite,
                    trackTitle = nowPlaying.title,
                )
            }
            MiniPlayerAction(status = state.status, onPlayPause = onPlayPause, onRetry = onRetry)
        }
        ProgressLine(
            fraction = progressFraction(state),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = PROGRESS_INSET),
        )
    }
}

@Composable
private fun miniSubtitle(status: PlaybackStatus, nowPlaying: Track): String = when (status) {
    PlaybackStatus.Error -> stringResource(R.string.player_mini_error)
    PlaybackStatus.Ended -> stringResource(R.string.player_queue_ended)
    else -> nowPlaying.artistNames
}

@Composable
private fun MiniPlayerAction(status: PlaybackStatus, onPlayPause: () -> Unit, onRetry: () -> Unit) {
    when (status) {
        PlaybackStatus.Error -> IconButton(onClick = onRetry) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = stringResource(R.string.action_retry),
                tint = FlixTheme.colors.error,
            )
        }
        else -> {
            val isWaiting = status == PlaybackStatus.Loading || status == PlaybackStatus.Buffering
            val showRing = rememberDelayedFlag(isWaiting)
            val playPause = playPauseAction(status, isMiniPlayer = true)
            val actionLabel = stringResource(playPause.label)
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.semantics { contentDescription = actionLabel },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (showRing) LoadingRing(Modifier.matchParentSize())
                    AnimatedIcon(icon = playPause.icon, tint = FlixTheme.colors.textPrimary, iconSize = MINI_ACTION_ICON_SIZE)
                }
            }
        }
    }
}

@Composable
private fun ProgressLine(fraction: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(PROGRESS_HEIGHT)
            .clip(FlixTheme.shapes.pill)
            .background(FlixTheme.colors.divider),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(FlixTheme.colors.primary),
        )
    }
}

@Composable
private fun elevationShadowColor(): Color =
    if (FlixTheme.isDark) DarkElevationShadow else LightElevationShadow

internal fun progressFraction(state: PlayerState): Float {
    val duration = state.duration ?: return 0f
    if (!duration.isPositive()) return 0f
    return (state.position / duration).toFloat().coerceIn(0f, 1f)
}

val MINI_PLAYER_HEIGHT = 64.dp
private val MINI_ARTWORK_SIZE = 48.dp
private val MINI_ACTION_ICON_SIZE = 28.dp
private val MINI_PLAYER_ELEVATION = 8.dp
private val PROGRESS_HEIGHT = 2.dp
private val PROGRESS_INSET = 12.dp
private val SWIPE_THRESHOLD = 56.dp
private const val SWIPE_FOLLOW_RATIO = 0.5f
private const val ERROR_BORDER_ALPHA = 0.5f
private val DarkElevationShadow = Color(0x73000000)
private val LightElevationShadow = Color(0x1F101218)

private val previewNowPlaying = Track("1", "Fade To Black", listOf(ArtistRef(null, "Metallica")), null, null, null)

@Preview(name = "Mini · estados", widthDp = 360)
@Composable
private fun MiniPlayerStatesPreview() {
    FlixPreview {
        Column(Modifier.padding(vertical = FlixTheme.spacing.md)) {
            listOf(
                PlaybackStatus.Playing,
                PlaybackStatus.Paused,
                PlaybackStatus.Loading,
                PlaybackStatus.Error,
                PlaybackStatus.Ended,
            ).forEach { status ->
                MiniPlayer(
                    state = PlayerState(status = status, current = previewNowPlaying, position = 160.seconds, duration = 417.seconds),
                    onOpen = {}, onPlayPause = {}, onRetry = {}, onNext = {}, onPrevious = {},
                )
            }
        }
    }
}

@Preview(name = "Mini · claro", widthDp = 360)
@Composable
private fun MiniPlayerLightPreview() {
    FlixPreview(ThemeMode.Light) {
        MiniPlayer(
            state = PlayerState(status = PlaybackStatus.Playing, current = previewNowPlaying, position = 160.seconds, duration = 417.seconds),
            onOpen = {}, onPlayPause = {}, onRetry = {}, onNext = {}, onPrevious = {},
        )
    }
}
