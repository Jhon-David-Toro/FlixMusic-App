package com.flix.music.feature.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.flix.music.R
import com.flix.music.core.designsystem.component.ErrorStateView
import com.flix.music.core.designsystem.component.PlayPauseButton
import com.flix.music.core.designsystem.component.SeekBar
import com.flix.music.core.designsystem.component.SkeletonBox
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.util.rememberArtworkTint
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Lyrics
import com.flix.music.core.model.Track
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Letra (design `lyricsnone` / `lyricssync`), a layer inside the player. Synced lyrics
 * highlight the line being sung, scroll by themselves and seek when a line is tapped;
 * plain lyrics scroll freely. The minimum controls stay usable below: seek bar, times,
 * previous · play/pause · next. Swipe down or Back returns to the player.
 */
@Composable
fun LyricsScreen(
    state: PlayerState,
    actions: FullPlayerActions,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LyricsViewModel = hiltViewModel(),
) {
    val lyricsState by viewModel.uiState.collectAsStateWithLifecycle()
    LyricsContent(
        state = state,
        lyricsState = lyricsState,
        actions = actions,
        onClose = onClose,
        onLineClick = viewModel::onLineClick,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@Composable
fun LyricsContent(
    state: PlayerState,
    lyricsState: LyricsUiState,
    actions: FullPlayerActions,
    onClose: () -> Unit,
    onLineClick: (Duration) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            when (lyricsState) {
                LyricsUiState.Loading -> LyricsSkeleton()
                LyricsUiState.Unavailable -> StateView(
                    icon = R.drawable.ic_lyrics,
                    title = stringResource(R.string.lyrics_unavailable_title),
                    body = stringResource(R.string.lyrics_unavailable_body),
                )
                is LyricsUiState.Failed -> ErrorStateView(error = lyricsState.error, onRetry = onRetry)
                is LyricsUiState.Ready -> LyricsLines(lyricsState.lyrics, state.position, onLineClick)
            }
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

/**
 * Lyric lines in Bricolage 21sp bold. Synced: the current line in textPrimary, past
 * lines in textTertiary and upcoming ones fading out; the current line is kept about
 * a third from the top.
 */
@Composable
private fun LyricsLines(lyrics: Lyrics, position: Duration, onLineClick: (Duration) -> Unit) {
    val activeIndex = lyrics.activeLineIndex(position)
    val listState = rememberLazyListState()
    LaunchedEffect(activeIndex) {
        if (activeIndex != null) {
            listState.animateScrollToItem(activeIndex, scrollOffset = -listState.layoutInfo.viewportSize.height / 3)
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = FlixTheme.spacing.gutter, vertical = 20.dp),
    ) {
        itemsIndexed(lyrics.lines) { index, line ->
            val emphasis = lineEmphasis(index, activeIndex, lyrics.isSynced)
            val color by animateColorAsState(emphasis.color(), label = "lyricColor")
            Text(
                text = line.text,
                style = LYRIC_STYLE,
                color = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(emphasis.alpha)
                    .then(
                        if (line.start != null) {
                            Modifier.clickable { onLineClick(line.start) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(bottom = 14.dp),
            )
        }
    }
}

/** How a lyric line stands out relative to the one being sung. */
private enum class LineEmphasis(val alpha: Float) {
    Current(1f),
    Past(1f),
    Next(NEXT_LINE_ALPHA),
    Later(LATER_LINES_ALPHA),
    Plain(1f),
}

private fun lineEmphasis(index: Int, activeIndex: Int?, isSynced: Boolean): LineEmphasis = when {
    !isSynced -> LineEmphasis.Plain
    activeIndex == null -> if (index == 0) LineEmphasis.Next else LineEmphasis.Later
    index == activeIndex -> LineEmphasis.Current
    index < activeIndex -> LineEmphasis.Past
    index == activeIndex + 1 -> LineEmphasis.Next
    else -> LineEmphasis.Later
}

@Composable
private fun LineEmphasis.color(): Color = when (this) {
    LineEmphasis.Current, LineEmphasis.Plain -> FlixTheme.colors.textPrimary
    LineEmphasis.Past -> FlixTheme.colors.textTertiary
    LineEmphasis.Next, LineEmphasis.Later -> FlixTheme.colors.textSecondary
}

/** Four placeholder lines at 80 / 60 / 70 / 40 % (design: loading lyrics). */
@Composable
private fun LyricsSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FlixTheme.spacing.gutter, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SKELETON_LINE_WIDTHS.forEach { width ->
            SkeletonBox(
                Modifier
                    .fillMaxWidth(width)
                    .height(22.dp),
            )
        }
    }
}

private val LYRIC_STYLE
    @Composable get() = FlixTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 28.sp)
private val SKELETON_LINE_WIDTHS = listOf(0.8f, 0.6f, 0.7f, 0.4f)
private const val NEXT_LINE_ALPHA = 0.55f
private const val LATER_LINES_ALPHA = 0.4f
private const val GRADIENT_END = 0.78f
private val CLOSE_DRAG_THRESHOLD = 120.dp

@Preview(name = "Letra · no disponible", widthDp = 360, heightDp = 760)
@Composable
private fun LyricsPreview() {
    FlixPreview {
        LyricsContent(
            state = PlayerState(
                status = PlaybackStatus.Playing,
                current = Track("1", "Fade To Black", listOf(ArtistRef(null, "Metallica")), null, null, null),
                position = 161.seconds,
                duration = 417.seconds,
                hasNext = true,
            ),
            lyricsState = LyricsUiState.Unavailable,
            actions = FullPlayerActions(),
            onClose = {},
            onLineClick = {},
            onRetry = {},
        )
    }
}

@Preview(name = "Letra · sincronizada", widthDp = 360, heightDp = 760)
@Composable
private fun LyricsSyncedPreview() {
    FlixPreview {
        LyricsContent(
            state = PlayerState(
                status = PlaybackStatus.Playing,
                current = Track("1", "Sala vacía", listOf(ArtistRef(null, "Proyecto Lumen")), null, null, null),
                position = 100.seconds,
                duration = 228.seconds,
            ),
            lyricsState = LyricsUiState.Ready(
                Lyrics.fromLrc("[01:20]Apagaron la última luz\n[01:30]y el eco se quedó\n[01:38]la pantalla sigue encendida\n[01:45]aunque ya no quede nadie\n[01:52]sentado en la fila siete"),
            ),
            actions = FullPlayerActions(),
            onClose = {},
            onLineClick = {},
            onRetry = {},
        )
    }
}
