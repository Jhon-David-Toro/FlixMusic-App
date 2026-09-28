package com.flix.music.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.R
import com.flix.music.core.designsystem.component.FavoritesArtwork
import com.flix.music.core.designsystem.component.FlixCard
import com.flix.music.core.designsystem.component.FlixFilterChip
import com.flix.music.core.designsystem.component.NowPlayingMarker
import com.flix.music.core.designsystem.component.SortOrderChip
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.component.util.toClock
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.designsystem.theme.ThemeMode
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.FavoriteTrack
import com.flix.music.core.model.SortOrder
import com.flix.music.core.model.Track
import java.time.Instant

/** Every user action on the Library tab. Defaults do nothing, which keeps previews short. */
data class LibraryActions(
    val onOpenFavorites: () -> Unit = {},
    val onOpenHistory: () -> Unit = {},
    val onBrowseMusic: () -> Unit = {},
    val onFilterChange: (LibraryFilter) -> Unit = {},
    val onSortOrderChange: (SortOrder) -> Unit = {},
    val onSongClick: (Track) -> Unit = {},
)

@Composable
fun LibraryScreen(
    onOpenFavorites: () -> Unit,
    onOpenHistory: () -> Unit,
    onBrowseMusic: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onOpenFavorites, onOpenHistory, onBrowseMusic) {
        LibraryActions(
            onOpenFavorites = onOpenFavorites,
            onOpenHistory = onOpenHistory,
            onBrowseMusic = onBrowseMusic,
            onFilterChange = viewModel::onFilterChange,
            onSortOrderChange = viewModel::onSortOrderChange,
            onSongClick = viewModel::onSongClick,
        )
    }
    LibraryContent(
        uiState = uiState,
        actions = actions,
        nowPlaying = NowPlayingMarker(playerState.current?.id, playerState.isActive),
        modifier = modifier,
    )
}

@Composable
fun LibraryContent(
    uiState: LibraryUiState,
    actions: LibraryActions,
    modifier: Modifier = Modifier,
    nowPlaying: NowPlayingMarker = NowPlayingMarker(),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = FlixTheme.spacing.xxl),
    ) {
        item(key = "title") {
            Text(
                text = stringResource(R.string.tab_library),
                style = FlixTheme.typography.headline,
                color = FlixTheme.colors.textPrimary,
                modifier = Modifier.padding(horizontal = FlixTheme.spacing.gutter, vertical = FlixTheme.spacing.md),
            )
        }
        item(key = "pinned") { PinnedCollections(uiState, actions) }
        item(key = "filters") {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = FlixTheme.spacing.gutter)
                    .padding(top = FlixTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.sm),
            ) {
                LibraryFilter.entries.forEach { libraryFilter ->
                    FlixFilterChip(
                        label = stringResource(libraryFilter.label()),
                        selected = libraryFilter == uiState.filter,
                        onClick = { actions.onFilterChange(libraryFilter) },
                    )
                }
            }
        }
        when (uiState.filter) {
            LibraryFilter.Playlists -> item(key = "playlists-empty") {
                StateView(
                    icon = R.drawable.ic_queue_music,
                    title = stringResource(R.string.library_no_playlists_title),
                    body = stringResource(R.string.library_no_playlists_body),
                )
            }
            LibraryFilter.Songs -> if (uiState.songs.isEmpty()) {
                item(key = "songs-empty") {
                    StateView(
                        icon = R.drawable.ic_favorite,
                        title = stringResource(R.string.favorites_empty_title),
                        body = stringResource(R.string.favorites_empty_body),
                        actionLabel = stringResource(R.string.action_browse_music),
                        onAction = actions.onBrowseMusic,
                    )
                }
            } else {
                item(key = "sort") {
                    SortOrderChip(
                        order = uiState.sortOrder,
                        onOrderChange = actions.onSortOrderChange,
                        modifier = Modifier
                            .padding(horizontal = FlixTheme.spacing.gutter)
                            .padding(top = FlixTheme.spacing.md, bottom = FlixTheme.spacing.xs),
                    )
                }
                items(uiState.songs, key = { it.track.id }) { favorite ->
                    TrackRow(
                        title = favorite.track.title,
                        subtitle = favorite.track.artistNames,
                        artworkUrl = favorite.track.artworkUrl,
                        onClick = { actions.onSongClick(favorite.track) },
                        duration = favorite.track.duration?.toClock(),
                        isCurrent = favorite.track.id == nowPlaying.trackId,
                        isPlaying = nowPlaying.isPlaying,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

/** The two pinned collections (design `library`): Favoritos and Historial as `.card` tiles. */
@Composable
private fun PinnedCollections(uiState: LibraryUiState, actions: LibraryActions) {
    Row(
        modifier = Modifier.padding(horizontal = FlixTheme.spacing.gutter),
        horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.md),
    ) {
        PinnedTile(
            title = stringResource(R.string.favorites_title),
            caption = pluralStringResource(R.plurals.songs_count, uiState.favoriteCount, uiState.favoriteCount),
            onClick = actions.onOpenFavorites,
            modifier = Modifier.weight(1f),
            artwork = {
                FavoritesArtwork(size = TILE_ARTWORK_SIZE, iconSize = TILE_ICON_SIZE, shape = FlixTheme.shapes.sm)
            },
        )
        PinnedTile(
            title = stringResource(R.string.history_title),
            caption = if (uiState.hasHistory) {
                stringResource(R.string.library_history_today, uiState.playedTodayCount)
            } else {
                stringResource(R.string.library_history_empty)
            },
            onClick = actions.onOpenHistory,
            modifier = Modifier.weight(1f),
            artwork = {
                Box(
                    modifier = Modifier
                        .size(TILE_ARTWORK_SIZE)
                        .clip(FlixTheme.shapes.sm)
                        .background(FlixTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = null,
                        tint = FlixTheme.colors.textPrimary,
                        modifier = Modifier.size(TILE_ICON_SIZE),
                    )
                }
            },
        )
    }
}

@Composable
private fun PinnedTile(
    title: String,
    caption: String,
    onClick: () -> Unit,
    artwork: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlixCard(modifier = modifier, shape = FlixTheme.shapes.md, onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FlixTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.md),
        ) {
            artwork()
            Column {
                Text(title, style = FlixTheme.typography.label, color = FlixTheme.colors.textPrimary)
                Text(caption, style = FlixTheme.typography.caption, color = FlixTheme.colors.textSecondary)
            }
        }
    }
}

private fun LibraryFilter.label(): Int = when (this) {
    LibraryFilter.Playlists -> R.string.library_filter_playlists
    LibraryFilter.Songs -> R.string.library_filter_songs
}

private val TILE_ARTWORK_SIZE = 48.dp
private val TILE_ICON_SIZE = 24.dp

private val previewSongs = listOf(
    Track("1", "Fade To Black", listOf(ArtistRef(null, "Metallica")), null, null, null),
    Track("2", "Killshot", listOf(ArtistRef(null, "Eminem")), null, null, null),
).map { track -> FavoriteTrack(track, Instant.EPOCH) }

@Preview(name = "Biblioteca", widthDp = 360, heightDp = 700)
@Composable
private fun LibraryPreview() {
    FlixPreview {
        LibraryContent(
            uiState = LibraryUiState(favoriteCount = 128, playedTodayCount = 14, hasHistory = true, songs = previewSongs),
            actions = LibraryActions(),
        )
    }
}

@Preview(name = "Biblioteca · claro", widthDp = 360, heightDp = 700)
@Composable
private fun LibraryLightPreview() {
    FlixPreview(ThemeMode.Light) {
        LibraryContent(
            uiState = LibraryUiState(favoriteCount = 128, playedTodayCount = 14, hasHistory = true, songs = previewSongs),
            actions = LibraryActions(),
        )
    }
}
