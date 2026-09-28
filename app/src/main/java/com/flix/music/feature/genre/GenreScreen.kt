package com.flix.music.feature.genre

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.R
import com.flix.music.core.designsystem.component.AlbumCarousel
import com.flix.music.core.designsystem.component.ArtistCarousel
import com.flix.music.core.designsystem.component.ErrorStateView
import com.flix.music.core.designsystem.component.FlixPrimaryButton
import com.flix.music.core.designsystem.component.FlixSecondaryButton
import com.flix.music.core.designsystem.component.SectionHeader
import com.flix.music.core.designsystem.component.SkeletonBox
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.component.TrackRowSkeleton
import com.flix.music.core.designsystem.component.util.toClock
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.Album
import com.flix.music.core.model.AlbumRef
import com.flix.music.core.model.Artist
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Genre
import com.flix.music.core.model.Genres
import com.flix.music.core.model.Track
import com.flix.music.core.model.distinctAlbums
import com.flix.music.core.model.distinctArtists
import kotlin.time.Duration.Companion.seconds

/** Every user action on the Genre screen. Defaults do nothing, which keeps previews short. */
data class GenreActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onPlay: () -> Unit = {},
    val onShuffle: () -> Unit = {},
    val onTrackClick: (Track) -> Unit = {},
    val onArtistClick: (Artist) -> Unit = {},
    val onAlbumClick: (Album) -> Unit = {},
)

@Composable
fun GenreScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GenreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack) {
        GenreActions(onBack = onBack, onRetry = viewModel::onRetry)
    }
    GenreContent(
        genre = viewModel.genre,
        uiState = uiState,
        actions = actions,
        modifier = modifier,
    )
}

@Composable
fun GenreContent(
    genre: Genre,
    uiState: GenreUiState,
    actions: GenreActions,
    modifier: Modifier = Modifier,
) {
    var showAllSongs by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = FlixTheme.spacing.xxl),
    ) {
        item(key = "hero") { GenreHero(genre = genre, onBack = actions.onBack) }
        item(key = "actions") {
            Row(
                modifier = Modifier
                    .padding(horizontal = FlixTheme.spacing.gutter)
                    .padding(top = FlixTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.md),
            ) {
                val hasSongs = uiState is GenreUiState.Content
                FlixPrimaryButton(
                    text = stringResource(R.string.genre_play),
                    onClick = actions.onPlay,
                    icon = R.drawable.ic_play_arrow_filled,
                    enabled = hasSongs,
                )
                FlixSecondaryButton(
                    text = stringResource(R.string.genre_shuffle),
                    onClick = actions.onShuffle,
                    icon = R.drawable.ic_shuffle,
                    enabled = hasSongs,
                )
            }
        }
        when (uiState) {
            GenreUiState.Loading -> item(key = "loading") { GenreLoading() }
            GenreUiState.Empty -> item(key = "empty") {
                StateView(
                    icon = R.drawable.ic_search_off,
                    title = stringResource(R.string.genre_empty_title, genre.name),
                    body = stringResource(R.string.genre_empty_body),
                )
            }
            is GenreUiState.Error -> item(key = "error") {
                ErrorStateView(error = uiState.error, onRetry = actions.onRetry)
            }
            is GenreUiState.Content -> genreSections(
                content = uiState,
                showAllSongs = showAllSongs,
                onShowAllSongs = { showAllSongs = true },
                actions = actions,
            )
        }
    }
}

/** Gradient hero with the genre's fixed colors, white overline and display title. */
@Composable
private fun GenreHero(genre: Genre, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HERO_HEIGHT)
            .background(Brush.linearGradient(listOf(Color(genre.gradientStart), Color(genre.gradientEnd)))),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(FlixTheme.spacing.xs),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.action_back),
                tint = HeroContentColor,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(FlixTheme.spacing.gutter),
        ) {
            Text(
                text = stringResource(R.string.genre_overline).uppercase(),
                style = FlixTheme.typography.overline,
                color = HeroOverlineColor,
            )
            Text(
                text = genre.name,
                style = FlixTheme.typography.display,
                color = HeroContentColor,
                modifier = Modifier
                    .padding(top = FlixTheme.spacing.sm)
                    .semantics { heading() },
            )
        }
    }
}

@Composable
private fun GenreLoading() {
    val loadingLabel = stringResource(R.string.loading_results)
    Column(Modifier.clearAndSetSemantics { contentDescription = loadingLabel }) {
        SkeletonBox(
            Modifier
                .padding(horizontal = FlixTheme.spacing.gutter)
                .padding(top = FlixTheme.spacing.sectionGap, bottom = FlixTheme.spacing.md)
                .fillMaxWidth(SECTION_TITLE_SKELETON_WIDTH)
                .height(12.dp)
        )
        repeat(SONGS_PREVIEW_COUNT) { TrackRowSkeleton() }
    }
}

private fun LazyListScope.genreSections(
    content: GenreUiState.Content,
    showAllSongs: Boolean,
    onShowAllSongs: () -> Unit,
    actions: GenreActions,
) {
    val hasMoreSongs = content.tracks.size > SONGS_PREVIEW_COUNT
    item(key = "songs-header") {
        SectionHeader(
            title = stringResource(R.string.genre_popular_songs),
            actionLabel = if (hasMoreSongs && !showAllSongs) stringResource(R.string.see_all) else null,
            onAction = onShowAllSongs,
            modifier = Modifier.padding(top = FlixTheme.spacing.sectionGap),
        )
    }
    val visibleSongs = if (showAllSongs) content.tracks else content.tracks.take(SONGS_PREVIEW_COUNT)
    items(visibleSongs, key = { it.id }) { track ->
        TrackRow(
            title = track.title,
            subtitle = track.artistNames,
            artworkUrl = track.artworkUrl,
            duration = track.duration?.toClock(),
            onClick = { actions.onTrackClick(track) },
        )
    }

    if (content.artists.isNotEmpty()) {
        item(key = "artists-header") {
            SectionHeader(
                title = stringResource(R.string.genre_artists),
                modifier = Modifier.padding(top = FlixTheme.spacing.sectionGap, bottom = FlixTheme.spacing.md),
            )
        }
        item(key = "artists") {
            ArtistCarousel(artists = content.artists, onArtistClick = actions.onArtistClick)
        }
    }

    if (content.albums.isNotEmpty()) {
        item(key = "albums-header") {
            SectionHeader(
                title = stringResource(R.string.genre_albums),
                modifier = Modifier.padding(top = FlixTheme.spacing.sectionGap, bottom = FlixTheme.spacing.md),
            )
        }
        item(key = "albums") {
            AlbumCarousel(albums = content.albums, onAlbumClick = actions.onAlbumClick)
        }
    }
}

private const val SONGS_PREVIEW_COUNT = 3
private const val SECTION_TITLE_SKELETON_WIDTH = 0.35f
private val HERO_HEIGHT = 220.dp
private val HeroContentColor = Color.White
private val HeroOverlineColor = Color.White.copy(alpha = 0.75f)

private val previewTracks = listOf(
    Track("1", "Master of Puppets", listOf(ArtistRef("m", "Metallica")), AlbumRef("mop", "Master of Puppets"), 516.seconds, null),
    Track("2", "Symphony of Destruction", listOf(ArtistRef("mg", "Megadeth")), AlbumRef("cte", "Countdown to Extinction"), 242.seconds, null),
    Track("3", "Fade To Black", listOf(ArtistRef("m", "Metallica")), AlbumRef("rtl", "Ride the Lightning"), 417.seconds, null),
    Track("4", "Raining Blood", listOf(ArtistRef("s", "Slayer")), AlbumRef("rib", "Reign in Blood"), 254.seconds, null),
)

@Preview(name = "Género · Metal", widthDp = 360, heightDp = 900)
@Composable
private fun GenreContentPreview() {
    FlixPreview {
        GenreContent(
            genre = Genres.all.first { it.id == "metal" },
            uiState = GenreUiState.Content(
                tracks = previewTracks,
                artists = previewTracks.distinctArtists(),
                albums = previewTracks.distinctAlbums(),
            ),
            actions = GenreActions(),
        )
    }
}

@Preview(name = "Género · cargando", widthDp = 360, heightDp = 640)
@Composable
private fun GenreLoadingPreview() {
    FlixPreview {
        GenreContent(genre = Genres.all.first(), uiState = GenreUiState.Loading, actions = GenreActions())
    }
}
