package com.flix.music.feature.album

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.flix.music.R
import com.flix.music.core.designsystem.component.ErrorStateView
import com.flix.music.core.designsystem.component.FavoriteButton
import com.flix.music.core.designsystem.component.FlixBottomSheet
import com.flix.music.core.designsystem.component.FlixTopAppBar
import com.flix.music.core.designsystem.component.InfoBanner
import com.flix.music.core.designsystem.component.NowPlayingMarker
import com.flix.music.core.designsystem.component.PlayPauseButton
import com.flix.music.core.designsystem.component.SheetQuickAction
import com.flix.music.core.designsystem.component.SheetQuickActions
import com.flix.music.core.designsystem.component.SheetRow
import com.flix.music.core.designsystem.component.SheetTitle
import com.flix.music.core.designsystem.component.SkeletonBox
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.component.TrackRowSkeleton
import com.flix.music.core.designsystem.component.util.rememberArtworkTint
import com.flix.music.core.designsystem.component.util.toClock
import com.flix.music.core.designsystem.component.util.toReadableLength
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track
import com.flix.music.core.ui.LocalDetailNavigator
import com.flix.music.core.ui.LocalTrackMenu
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/** Every user action on the album screen. Defaults do nothing, which keeps previews short. */
data class AlbumActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onPlay: () -> Unit = {},
    val onShuffle: () -> Unit = {},
    val onToggleSaved: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onOpenArtist: () -> Unit = {},
    val onTrackClick: (Track) -> Unit = {},
    val onTrackMore: (Track) -> Unit = {},
)

@Composable
fun AlbumScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val trackMenu = LocalTrackMenu.current
    val detailNavigator = LocalDetailNavigator.current
    val context = LocalContext.current
    AlbumContent(
        uiState = uiState,
        nowPlaying = NowPlayingMarker(playerState.current?.id, playerState.isActive),
        actions = AlbumActions(
            onBack = onBack,
            onRetry = viewModel::onRetry,
            onPlay = viewModel::onPlay,
            onShuffle = viewModel::onShuffle,
            onToggleSaved = viewModel::onToggleSaved,
            onShare = { context.shareAlbum(uiState.name, uiState.artistName) },
            onOpenArtist = { detailNavigator.openArtist(viewModel.artist) },
            onTrackClick = viewModel::onTrackClick,
            onTrackMore = { track -> trackMenu.open(track) },
        ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumContent(
    uiState: AlbumUiState,
    actions: AlbumActions,
    modifier: Modifier = Modifier,
    nowPlaying: NowPlayingMarker = NowPlayingMarker(),
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    val content = uiState as? AlbumUiState.Content
    val tint = rememberArtworkTint(content?.artworkUrl)
    val background = FlixTheme.colors.background

    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(TINTED_HEADER_HEIGHT)
                .background(Brush.verticalGradient(0f to tint, GRADIENT_END to background)),
        )
        Column(Modifier.fillMaxSize()) {
            FlixTopAppBar(
                title = "",
                onBack = actions.onBack,
                actions = {
                    if (content != null) {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
                                contentDescription = stringResource(R.string.action_more_options),
                                tint = FlixTheme.colors.textPrimary,
                            )
                        }
                    }
                },
            )
            when (uiState) {
                is AlbumUiState.Content -> AlbumTracks(uiState, actions, nowPlaying, onOpenMenu = { menuOpen = true })
                is AlbumUiState.Error -> ErrorStateView(error = uiState.error, onRetry = actions.onRetry)
                is AlbumUiState.Loading -> AlbumSkeleton()
            }
        }
    }

    if (menuOpen && content != null) {
        AlbumMenu(content, actions, onDismiss = { menuOpen = false })
    }
}

@Composable
private fun AlbumTracks(
    content: AlbumUiState.Content,
    actions: AlbumActions,
    nowPlaying: NowPlayingMarker,
    onOpenMenu: () -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(bottom = FlixTheme.spacing.xxl)) {
        item(key = "header") { AlbumHeader(content, actions, onOpenMenu) }
        if (content.tracks.isEmpty()) {
            item(key = "empty") {
                StateView(
                    icon = R.drawable.ic_album,
                    title = stringResource(R.string.album_empty_title),
                    body = stringResource(R.string.album_empty_body),
                )
            }
        }
        itemsIndexed(content.tracks, key = { _, track -> track.id }) { index, track ->
            TrackRow(
                title = track.title,
                subtitle = listOfNotNull(track.artistNames, track.duration?.toClock()).joinToString(" · "),
                artworkUrl = track.artworkUrl,
                onClick = { actions.onTrackClick(track) },
                isCurrent = track.id == nowPlaying.trackId,
                isPlaying = nowPlaying.isPlaying,
                onMoreClick = { actions.onTrackMore(track) },
                number = index + 1,
            )
        }
        if (content.tracks.isNotEmpty()) {
            item(key = "derived-banner") {
                InfoBanner(
                    text = stringResource(R.string.album_derived_notice),
                    modifier = Modifier
                        .padding(horizontal = FlixTheme.spacing.gutter)
                        .padding(top = FlixTheme.spacing.md, bottom = FlixTheme.spacing.lg),
                )
            }
        }
    }
}

/**
 * Hierarchy from the design: ÁLBUM (overline) → title (headline) → artist with avatar
 * (link) → "8 canciones · 51 min" → ♥ · ⋯ … shuffle · play.
 */
@Composable
private fun AlbumHeader(content: AlbumUiState.Content, actions: AlbumActions, onOpenMenu: () -> Unit) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FlixTheme.spacing.xs, bottom = FlixTheme.spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = content.artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(COVER_SIZE)
                    .shadow(8.dp, COVER_SHAPE)
                    .clip(COVER_SHAPE)
                    .background(FlixTheme.colors.surfaceVariant),
            )
        }
        Column(Modifier.padding(horizontal = FlixTheme.spacing.gutter)) {
            Text(
                text = stringResource(R.string.type_album).uppercase(),
                style = FlixTheme.typography.overline,
                color = FlixTheme.colors.textSecondary,
            )
            Text(
                text = content.name,
                style = FlixTheme.typography.headline,
                color = FlixTheme.colors.textPrimary,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .semantics { heading() },
            )
            if (content.artistName.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clip(FlixTheme.shapes.pill)
                        .clickable(onClick = actions.onOpenArtist),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.sm),
                ) {
                    AsyncImage(
                        model = content.artworkUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(FlixTheme.shapes.circle)
                            .background(FlixTheme.colors.surfaceVariant),
                    )
                    Text(content.artistName, style = FlixTheme.typography.label, color = FlixTheme.colors.textPrimary)
                }
            }
            Text(
                text = pluralStringResource(
                    R.plurals.album_summary,
                    content.tracks.size,
                    content.tracks.size,
                    content.totalDuration.toReadableLength(),
                ),
                style = FlixTheme.typography.caption,
                color = FlixTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(
                modifier = Modifier.padding(top = FlixTheme.spacing.md, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.xs),
            ) {
                if (content.canSave) {
                    FavoriteButton(
                        isFavorite = content.isSaved,
                        onToggle = actions.onToggleSaved,
                        trackTitle = content.name,
                        inactiveTint = FlixTheme.colors.textPrimary,
                    )
                }
                IconButton(onClick = onOpenMenu) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.action_more_options),
                        tint = FlixTheme.colors.textPrimary,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (content.tracks.isNotEmpty()) {
                    IconButton(onClick = actions.onShuffle) {
                        Icon(
                            painter = painterResource(R.drawable.ic_shuffle),
                            contentDescription = stringResource(R.string.player_shuffle),
                            tint = FlixTheme.colors.textPrimary,
                        )
                    }
                    PlayPauseButton(
                        icon = R.drawable.ic_play_arrow_filled,
                        contentDescription = stringResource(R.string.album_play, content.name),
                        onClick = actions.onPlay,
                        size = 52.dp,
                        iconSize = 28.dp,
                    )
                }
            }
        }
    }
}

/** Album menu (design "Menú: Álbum"): Reproducir · Aleatorio · Favorito, then Compartir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumMenu(content: AlbumUiState.Content, actions: AlbumActions, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val closeThen: (() -> Unit) -> Unit = { action ->
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
            action()
        }
    }
    FlixBottomSheet(onDismiss = onDismiss, sheetState = sheetState) {
        SheetTitle(content.name)
        SheetQuickActions {
            SheetQuickAction(
                icon = R.drawable.ic_play_arrow_filled,
                label = stringResource(R.string.playlist_play),
                onClick = { closeThen(actions.onPlay) },
            )
            SheetQuickAction(
                icon = R.drawable.ic_shuffle,
                label = stringResource(R.string.player_shuffle),
                onClick = { closeThen(actions.onShuffle) },
            )
            if (content.canSave) {
                SheetQuickAction(
                    icon = if (content.isSaved) R.drawable.ic_favorite_filled else R.drawable.ic_favorite,
                    label = stringResource(R.string.track_favorite),
                    iconTint = if (content.isSaved) FlixTheme.colors.primaryContent else FlixTheme.colors.textPrimary,
                    onClick = { closeThen(actions.onToggleSaved) },
                )
            }
        }
        SheetRow(
            icon = R.drawable.ic_share,
            label = stringResource(R.string.action_share),
            onClick = { closeThen(actions.onShare) },
        )
    }
}

@Composable
private fun AlbumSkeleton() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FlixTheme.spacing.xs, bottom = FlixTheme.spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            SkeletonBox(Modifier.size(COVER_SIZE), shape = COVER_SHAPE)
        }
        repeat(SKELETON_ROWS) { TrackRowSkeleton() }
    }
}

private fun Context.shareAlbum(name: String, artistName: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, listOf(name, artistName).filter { it.isNotBlank() }.joinToString(" — "))
    }
    startActivity(Intent.createChooser(sendIntent, null))
}

private const val GRADIENT_END = 0.78f
private const val SKELETON_ROWS = 5
private val TINTED_HEADER_HEIGHT = 300.dp
private val COVER_SIZE = 184.dp
private val COVER_SHAPE = RoundedCornerShape(14.dp)

@Preview(name = "Álbum", widthDp = 360, heightDp = 900)
@Composable
private fun AlbumPreview() {
    FlixPreview {
        val tracks = listOf("Fight Fire with Fire", "Ride the Lightning", "For Whom the Bell Tolls", "Fade To Black")
            .mapIndexed { index, title ->
                Track(index.toString(), title, listOf(ArtistRef("m", "Metallica")), null, (280 + index * 30).seconds, null)
            }
        AlbumContent(
            uiState = AlbumUiState.Content(
                name = "Ride the Lightning",
                artistName = "Metallica",
                artworkUrl = null,
                tracks = tracks,
                totalDuration = tracks.mapNotNull { it.duration }.fold(0.seconds) { total, length -> total + length },
                canSave = true,
                isSaved = false,
            ),
            actions = AlbumActions(),
            nowPlaying = NowPlayingMarker(trackId = "3", isPlaying = true),
        )
    }
}
