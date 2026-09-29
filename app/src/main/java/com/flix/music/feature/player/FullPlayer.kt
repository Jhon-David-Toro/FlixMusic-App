package com.flix.music.feature.player

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.flix.music.R
import com.flix.music.core.designsystem.component.FavoriteButton
import com.flix.music.core.designsystem.component.PlayPauseButton
import com.flix.music.core.designsystem.component.SeekBar
import com.flix.music.core.designsystem.component.util.rememberArtworkTint
import com.flix.music.core.designsystem.component.util.rememberDelayedFlag
import com.flix.music.core.designsystem.component.util.toClock
import com.flix.music.core.designsystem.theme.FlixMotion
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.designsystem.theme.ThemeMode
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track
import com.flix.music.core.player.PlaybackSource
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerState
import com.flix.music.core.player.RepeatMode
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Every action of the Full Player. Defaults do nothing, which keeps previews short. */
data class FullPlayerActions(
    val onCollapse: () -> Unit = {},
    val onPlayPause: () -> Unit = {},
    val onSeek: (Duration) -> Unit = {},
    val onNext: () -> Unit = {},
    val onPrevious: () -> Unit = {},
    val onToggleShuffle: () -> Unit = {},
    val onCycleRepeat: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onToggleFavorite: () -> Unit = {},
    val onMore: () -> Unit = {},
    val onOpenQueue: () -> Unit = {},
    val onOpenLyrics: () -> Unit = {},
    val onOpenSong: (Track) -> Unit = {},
    val onOpenArtist: (Track) -> Unit = {},
)

/**
 * Full Player, design composition A: dominant artwork, controls anchored at the thumb
 * zone and an artwork-tinted gradient. Paused shrinks the artwork to 94 %; errors show
 * a banner with "Reintentar" (and "Saltar" when there is a next track). Swiping down
 * collapses it back to the mini player.
 */
@Composable
fun FullPlayer(
    state: PlayerState,
    actions: FullPlayerActions,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    artworkModifier: Modifier = Modifier,
) {
    val nowPlaying = state.current ?: return
    val tint = rememberArtworkTint(nowPlaying.artworkUrl)
    val background = FlixTheme.colors.background
    val collapseThreshold = with(LocalDensity.current) { COLLAPSE_DRAG_THRESHOLD.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .background(Brush.verticalGradient(0f to tint, GRADIENT_END to background))
            .pointerInput(Unit) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { _, dragAmount -> dragDistance += dragAmount },
                    onDragEnd = { if (dragDistance > collapseThreshold) actions.onCollapse() },
                )
            }
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Column(Modifier.fillMaxSize()) {
            PlayerTopBar(source = state.source, onCollapse = actions.onCollapse, onMore = actions.onMore)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = FlixTheme.spacing.gutter),
            ) {
                PlayerArtwork(
                    nowPlaying = nowPlaying,
                    status = state.status,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = FlixTheme.spacing.sm)
                        .padding(top = FlixTheme.spacing.md)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                        .then(artworkModifier),
                )
                TrackTitle(
                    track = nowPlaying,
                    isFavorite = isFavorite,
                    onToggleFavorite = actions.onToggleFavorite,
                    onOpenSong = { actions.onOpenSong(nowPlaying) },
                    onOpenArtist = { actions.onOpenArtist(nowPlaying) },
                    modifier = Modifier.padding(top = FlixTheme.spacing.xxl),
                )
                if (state.status == PlaybackStatus.Error) {
                    ErrorBanner(
                        canSkip = state.hasNext,
                        onRetry = actions.onRetry,
                        onSkip = actions.onNext,
                        modifier = Modifier.padding(top = FlixTheme.spacing.lg),
                    )
                }
                SeekBar(
                    position = state.position,
                    bufferedPosition = state.bufferedPosition,
                    duration = state.duration.takeIf { state.status != PlaybackStatus.Loading },
                    onSeek = actions.onSeek,
                    contentDescription = stringResource(R.string.player_seek),
                    bufferPulsing = state.status == PlaybackStatus.Buffering || state.status == PlaybackStatus.Loading,
                    modifier = Modifier.padding(top = FlixTheme.spacing.sm),
                )
                ElapsedAndRemaining(
                    position = state.position,
                    duration = state.duration,
                    modifier = Modifier.offset(y = TIMES_OFFSET),
                )
                PlayerControls(state = state, actions = actions)
            }
            UpNext(
                nextTrack = state.next,
                onOpenQueue = actions.onOpenQueue,
                onOpenLyrics = actions.onOpenLyrics,
                modifier = Modifier
                    .padding(horizontal = FlixTheme.spacing.gutter)
                    .padding(top = FlixTheme.spacing.lg, bottom = FlixTheme.spacing.lg),
            )
        }
    }
}

@Composable
private fun PlayerTopBar(source: PlaybackSource?, onCollapse: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(TOP_BAR_HEIGHT)
            .padding(horizontal = FlixTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                contentDescription = stringResource(R.string.player_close),
                tint = FlixTheme.colors.textPrimary,
                modifier = Modifier.size(TOP_BAR_ICON_SIZE),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (source != null) {
                Text(
                    text = stringResource(R.string.player_playing_from),
                    style = FlixTheme.typography.caption,
                    color = FlixTheme.colors.textSecondary,
                )
                Text(
                    text = playbackSourceLabel(source),
                    style = FlixTheme.typography.label,
                    color = FlixTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
        IconButton(onClick = onMore) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.action_more_options),
                tint = FlixTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun PlayerArtwork(nowPlaying: Track, status: PlaybackStatus, modifier: Modifier = Modifier) {
    val artworkScale by animateFloatAsState(
        targetValue = if (status == PlaybackStatus.Paused) PAUSED_ARTWORK_SCALE else 1f,
        animationSpec = tween(FlixMotion.NORMAL, easing = FlixMotion.Standard),
        label = "artworkScale",
    )
    val shape = FlixTheme.shapes.lg
    AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(nowPlaying.artworkUrl)
            .crossfade(FlixMotion.SLOW)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .graphicsLayer {
                scaleX = artworkScale
                scaleY = artworkScale
            }
            .shadow(ARTWORK_ELEVATION, shape)
            .clip(shape)
            .background(FlixTheme.colors.surfaceVariant),
    )
}

@Composable
private fun TrackTitle(
    track: Track,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenSong: () -> Unit,
    onOpenArtist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            TrackTitleText(track, onOpenSong, onOpenArtist)
        }
        FavoriteButton(
            isFavorite = isFavorite,
            onToggle = onToggleFavorite,
            trackTitle = track.title,
            iconSize = PLAYER_FAVORITE_ICON_SIZE,
        )
    }
}

@Composable
private fun TrackTitleText(track: Track, onOpenSong: () -> Unit, onOpenArtist: () -> Unit) {
    Column {
        Text(
            text = track.title,
            style = FlixTheme.typography.titleLarge,
            color = FlixTheme.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable(onClickLabel = stringResource(R.string.player_open_song), onClick = onOpenSong),
        )
        Text(
            text = track.artistNames,
            style = FlixTheme.typography.body.copy(fontWeight = FontWeight.Medium),
            color = FlixTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = FlixTheme.spacing.xs)
                .clickable(
                    enabled = track.artists.isNotEmpty(),
                    onClickLabel = stringResource(R.string.player_open_artist),
                    onClick = onOpenArtist,
                ),
        )
    }
}

@Composable
private fun ErrorBanner(canSkip: Boolean, onRetry: () -> Unit, onSkip: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FlixTheme.shapes.md)
            .background(FlixTheme.colors.surfaceVariant)
            .padding(start = FlixTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.sm),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = FlixTheme.colors.error,
            modifier = Modifier.size(BANNER_ICON_SIZE),
        )
        Text(
            text = stringResource(R.string.player_error),
            style = FlixTheme.typography.bodySmall,
            color = FlixTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry), style = FlixTheme.typography.label, color = FlixTheme.colors.primaryContent)
        }
        if (canSkip) {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.player_skip), style = FlixTheme.typography.label, color = FlixTheme.colors.primaryContent)
            }
        }
    }
}

@Composable
internal fun ElapsedAndRemaining(position: Duration, duration: Duration?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(position.toClock(), style = FlixTheme.typography.caption, color = FlixTheme.colors.textSecondary)
        Text(
            text = duration?.let { "−${(it - position).coerceAtLeast(Duration.ZERO).toClock()}" }.orEmpty(),
            style = FlixTheme.typography.caption,
            color = FlixTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun PlayerControls(state: PlayerState, actions: FullPlayerActions) {
    val isWaiting = state.status == PlaybackStatus.Loading || state.status == PlaybackStatus.Buffering
    val showRing = rememberDelayedFlag(isWaiting)
    val playPause = playPauseAction(state.status, isMiniPlayer = false)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FlixTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModeToggle(
            icon = R.drawable.ic_shuffle,
            label = stringResource(R.string.player_shuffle),
            stateLabel = stringResource(if (state.shuffleEnabled) R.string.state_on else R.string.state_off),
            active = state.shuffleEnabled,
            onClick = actions.onToggleShuffle,
        )
        TransportButton(R.drawable.ic_skip_previous_filled, stringResource(R.string.player_previous), actions.onPrevious)
        PlayPauseButton(
            icon = playPause.icon,
            contentDescription = stringResource(playPause.label),
            onClick = if (state.status == PlaybackStatus.Error) actions.onRetry else actions.onPlayPause,
            showLoadingRing = showRing,
        )
        TransportButton(R.drawable.ic_skip_next_filled, stringResource(R.string.player_next), actions.onNext)
        ModeToggle(
            icon = if (state.repeatMode == RepeatMode.One) R.drawable.ic_repeat_one else R.drawable.ic_repeat,
            label = stringResource(R.string.player_repeat),
            stateLabel = stringResource(state.repeatMode.label()),
            active = state.repeatMode != RepeatMode.Off,
            onClick = actions.onCycleRepeat,
        )
    }
}

@Composable
private fun TransportButton(@DrawableRes icon: Int, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(TRANSPORT_BUTTON_SIZE)) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = FlixTheme.colors.textPrimary,
            modifier = Modifier.size(TRANSPORT_ICON_SIZE),
        )
    }
}

/**
 * Shuffle / repeat toggle (design `.dot-on`): active shows the icon in primaryContent
 * with a 4 dp dot underneath, so the state never depends on color alone.
 */
@Composable
private fun ModeToggle(
    @DrawableRes icon: Int,
    label: String,
    stateLabel: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = FlixTheme.colors.primaryContent
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .semantics {
                contentDescription = label
                stateDescription = stateLabel
                role = Role.Switch
            }
            .drawBehind {
                if (active) {
                    drawCircle(
                        color = activeColor,
                        radius = ACTIVE_DOT_RADIUS.toPx(),
                        center = Offset(size.width / 2, size.height - ACTIVE_DOT_BOTTOM.toPx()),
                    )
                }
            },
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (active) activeColor else FlixTheme.colors.textPrimary,
            modifier = Modifier.size(MODE_ICON_SIZE),
        )
    }
}

/**
 * "A continuación" strip (design `.peek`): the next track plus the Letra and Cola
 * buttons. It is the quick way to the Cola: tap it or swipe it up.
 */
@Composable
private fun UpNext(
    nextTrack: Track?,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openThreshold = with(LocalDensity.current) { PEEK_SWIPE_THRESHOLD.toPx() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FlixTheme.shapes.md)
            .background(FlixTheme.colors.textPrimary.copy(alpha = PEEK_BACKGROUND_ALPHA))
            .clickable(onClickLabel = stringResource(R.string.queue_open), onClick = onOpenQueue)
            .pointerInput(Unit) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragDistance += dragAmount
                    },
                    onDragEnd = { if (dragDistance < -openThreshold) onOpenQueue() },
                )
            }
            .padding(start = FlixTheme.spacing.md, end = FlixTheme.spacing.xs, top = FlixTheme.spacing.sm, bottom = FlixTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.md),
    ) {
        AsyncImage(
            model = nextTrack?.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(PEEK_ARTWORK_SIZE)
                .clip(FlixTheme.shapes.sm)
                .background(FlixTheme.colors.surfaceVariant),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.player_up_next),
                style = FlixTheme.typography.caption,
                color = FlixTheme.colors.textSecondary,
            )
            Text(
                text = nextTrack?.let { "${it.title} · ${it.artistNames}" } ?: stringResource(R.string.queue_empty_title),
                style = FlixTheme.typography.label,
                color = FlixTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onOpenLyrics) {
            Icon(
                painter = painterResource(R.drawable.ic_lyrics),
                contentDescription = stringResource(R.string.lyrics_open),
                tint = FlixTheme.colors.textSecondary,
            )
        }
        IconButton(onClick = onOpenQueue) {
            Icon(
                painter = painterResource(R.drawable.ic_queue_music),
                contentDescription = stringResource(R.string.queue_open),
                tint = FlixTheme.colors.textSecondary,
            )
        }
    }
}

private const val GRADIENT_END = 0.78f
private const val PAUSED_ARTWORK_SCALE = 0.94f
private const val PEEK_BACKGROUND_ALPHA = 0.07f
private val TOP_BAR_HEIGHT = 56.dp
private val TOP_BAR_ICON_SIZE = 28.dp
private val ARTWORK_ELEVATION = 8.dp
private val TIMES_OFFSET = (-10).dp
private val TRANSPORT_BUTTON_SIZE = 56.dp
private val TRANSPORT_ICON_SIZE = 40.dp
private val MODE_ICON_SIZE = 28.dp
private val ACTIVE_DOT_RADIUS = 2.dp
private val ACTIVE_DOT_BOTTOM = 6.dp
private val BANNER_ICON_SIZE = 20.dp
private val PEEK_ARTWORK_SIZE = 36.dp
private val PEEK_SWIPE_THRESHOLD = 48.dp
private val PLAYER_FAVORITE_ICON_SIZE = 28.dp
private val COLLAPSE_DRAG_THRESHOLD = 120.dp

private val previewState = PlayerState(
    status = PlaybackStatus.Playing,
    current = Track("1", "Fade To Black", listOf(ArtistRef(null, "Metallica")), null, null, null),
    next = Track("2", "Fade to Black", listOf(ArtistRef(null, "Apocalyptica")), null, null, null),
    source = PlaybackSource.Search("fade to black"),
    position = 161.seconds,
    bufferedPosition = 260.seconds,
    duration = 417.seconds,
    shuffleEnabled = true,
    hasNext = true,
)

@Preview(name = "Full Player", widthDp = 360, heightDp = 760)
@Composable
private fun FullPlayerPreview() {
    FlixPreview { FullPlayer(state = previewState, actions = FullPlayerActions(), isFavorite = true) }
}

@Preview(name = "Full Player · error", widthDp = 360, heightDp = 760)
@Composable
private fun FullPlayerErrorPreview() {
    FlixPreview { FullPlayer(state = previewState.copy(status = PlaybackStatus.Error), actions = FullPlayerActions()) }
}

@Preview(name = "Full Player · claro", widthDp = 360, heightDp = 760)
@Composable
private fun FullPlayerLightPreview() {
    FlixPreview(ThemeMode.Light) { FullPlayer(state = previewState, actions = FullPlayerActions()) }
}
