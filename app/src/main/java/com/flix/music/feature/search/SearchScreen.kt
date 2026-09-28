package com.flix.music.feature.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.R
import com.flix.music.core.designsystem.component.BestResultCard
import com.flix.music.core.designsystem.component.BestResultCardSkeleton
import com.flix.music.core.designsystem.component.ErrorStateView
import com.flix.music.core.designsystem.component.FlixFilterChip
import com.flix.music.core.designsystem.component.LoadingHairline
import com.flix.music.core.designsystem.component.MediaCard
import com.flix.music.core.designsystem.component.SearchField
import com.flix.music.core.designsystem.component.SectionHeader
import com.flix.music.core.designsystem.component.SkeletonBox
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.component.TrackRowSkeleton
import com.flix.music.core.designsystem.component.util.toClock
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.designsystem.theme.ThemeMode
import com.flix.music.core.model.Album
import com.flix.music.core.model.AlbumRef
import com.flix.music.core.model.Artist
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.FlixError
import com.flix.music.core.model.Track
import com.flix.music.core.model.distinctAlbums
import com.flix.music.core.model.distinctArtists
import kotlin.time.Duration.Companion.seconds

// Valores del mockup searchres / sloading (escala 280 px → 360 dp).
private const val SONGS_PREVIEW_COUNT = 3   // 3 filas bajo "Canciones" en "Todo"
private val ARTIST_CARD_WIDTH = 108.dp      // acard: 84 px
private val ALBUM_CARD_WIDTH = 136.dp       // card a104: 104 px (patrón "Carrusel: tarjetas 136 dp")

// Filas fantasma del mockup sloading: (ancho del título, ancho del subtítulo).
private val SKELETON_ROW_WIDTHS = listOf(0.7f to 0.45f, 0.6f to 0.4f, 0.75f to 0.35f)

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchContent(
        query = viewModel.query,
        uiState = uiState,
        filter = viewModel.filter,
        onQueryChange = viewModel::onQueryChange,
        onClear = viewModel::onClear,
        onRetry = viewModel::onRetry,
        onFilterChange = viewModel::onFilterChange,
        // Se conectan en los próximos pasos: reproducir (8) y pantallas de detalle.
        onTrackClick = {},
        onTrackOpen = {},
        onArtistClick = {},
        onAlbumClick = {},
        modifier = modifier,
    )
}

@Composable
fun SearchContent(
    query: String,
    uiState: SearchUiState,
    filter: SearchFilter,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onFilterChange: (SearchFilter) -> Unit,
    onTrackClick: (Track) -> Unit,     // reproducir
    onTrackOpen: (Track) -> Unit,      // abrir el detalle (tarjeta "Mejor resultado")
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Agrupa las acciones de la zona de resultados para pasarlas juntas.
    val callbacks = ResultCallbacks(onFilterChange, onTrackClick, onTrackOpen, onArtistClick, onAlbumClick)

    Column(modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.tab_search),
            style = FlixTheme.typography.headline,
            color = FlixTheme.colors.textPrimary,
            modifier = Modifier.padding(
                horizontal = FlixTheme.spacing.gutter,
                vertical = FlixTheme.spacing.md,
            ),
        )
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            onClear = onClear,
            placeholder = stringResource(R.string.search_placeholder),
            clearLabel = stringResource(R.string.search_clear),
            modifier = Modifier.padding(horizontal = FlixTheme.spacing.gutter),
        )

        LoadingHairline(
            visible = uiState is SearchUiState.Loading,
            modifier = Modifier
                .padding(horizontal = FlixTheme.spacing.gutter)
                // Diseño: margin-top:-2px → la barra se apoya sobre el borde inferior del campo.
                .offset(y = (-2).dp),
        )

        when (uiState) {
            SearchUiState.Idle -> StateView(
                icon = R.drawable.ic_search,
                title = stringResource(R.string.search_idle_title),
                body = stringResource(R.string.search_idle_body),
            )
            is SearchUiState.Loading -> SearchLoading(
                previous = uiState.previous,
                filter = filter,
                callbacks = callbacks,
            )
            is SearchUiState.Empty -> StateView(
                icon = R.drawable.ic_search_off,
                title = stringResource(R.string.search_empty_title, uiState.query),
                body = stringResource(R.string.search_empty_body),
            )
            is SearchUiState.Error -> ErrorStateView(
                error = uiState.error,
                onRetry = onRetry,
                // Diseño (Red y errores → "502 en búsqueda"): texto propio de Buscar.
                upstreamTitle = R.string.search_error_upstream_title,
                upstreamBody = R.string.search_error_upstream_body,
            )
            is SearchUiState.Results -> SearchResults(
                results = uiState,
                filter = filter,
                callbacks = callbacks,
            )
        }
    }
}

// Acciones de la zona de resultados, agrupadas para no pasar cinco lambdas por separado.
private data class ResultCallbacks(
    val onFilterChange: (SearchFilter) -> Unit,
    val onTrackClick: (Track) -> Unit,
    val onTrackOpen: (Track) -> Unit,
    val onArtistClick: (Artist) -> Unit,
    val onAlbumClick: (Album) -> Unit,
)

// Diseño (.chips): margin-top 12 px, separación 8 px, se desliza si no caben.
@Composable
private fun FilterChipsRow(
    selected: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = FlixTheme.spacing.gutter)
            .padding(top = FlixTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.sm),
    ) {
        SearchFilter.entries.forEach { searchFilter ->
            FlixFilterChip(
                label = stringResource(searchFilter.label),
                selected = searchFilter == selected,
                onClick = { onFilterChange(searchFilter) },
            )
        }
    }
}

// CARGA (mockup sloading): chips al 50 %, tarjeta fantasma, título fantasma al 35 %
// y 3 filas fantasma. Si ya había resultados, se quedan atenuados (nota del diseño).
@Composable
private fun SearchLoading(
    previous: SearchUiState.Results?,
    filter: SearchFilter,
    callbacks: ResultCallbacks,
) {
    if (previous != null) {
        Box(Modifier.alpha(0.5f)) {
            SearchResults(results = previous, filter = filter, callbacks = callbacks)
        }
        return
    }
    val loadingLabel = stringResource(R.string.loading_results)
    Column(Modifier.clearAndSetSemantics { contentDescription = loadingLabel }) {
        FilterChipsRow(
            selected = SearchFilter.All,
            onFilterChange = {},
            modifier = Modifier.alpha(0.5f),
        )
        BestResultCardSkeleton(Modifier.padding(top = FlixTheme.spacing.lg))
        SkeletonBox(
            Modifier
                .padding(horizontal = FlixTheme.spacing.gutter)
                .padding(top = FlixTheme.spacing.sectionGap, bottom = FlixTheme.spacing.md)
                .fillMaxWidth(0.35f)
                .height(12.dp)
        )
        SKELETON_ROW_WIDTHS.forEach { (titleWidth, subtitleWidth) ->
            TrackRowSkeleton(titleWidth = titleWidth, subtitleWidth = subtitleWidth)
        }
    }
}

// RESULTADOS (mockup searchres).
@Composable
private fun SearchResults(
    results: SearchUiState.Results,
    filter: SearchFilter,
    callbacks: ResultCallbacks,
) {
    Column {
        FilterChipsRow(selected = filter, onFilterChange = callbacks.onFilterChange)
        LazyColumn(
            contentPadding = PaddingValues(top = FlixTheme.spacing.lg, bottom = FlixTheme.spacing.xxl),
        ) {
            when (filter) {
                SearchFilter.All -> allResults(results, callbacks)
                SearchFilter.Songs -> trackItems(results.tracks, callbacks.onTrackClick)
                SearchFilter.Artists -> cardGrid(results.artists, key = { it.id }) { artist, cardModifier ->
                    MediaCard(
                        title = artist.name,
                        subtitle = stringResource(R.string.type_artist),
                        artworkUrl = artist.artworkUrl,
                        onClick = { callbacks.onArtistClick(artist) },
                        circular = true,
                        modifier = cardModifier,
                    )
                }
                SearchFilter.Albums -> cardGrid(results.albums, key = { it.id }) { album, cardModifier ->
                    MediaCard(
                        title = album.name,
                        subtitle = album.artistName,
                        artworkUrl = album.artworkUrl,
                        onClick = { callbacks.onAlbumClick(album) },
                        modifier = cardModifier,
                    )
                }
            }
        }
    }
}

// "Todo": mejor resultado → Canciones (Ver todo) → Artistas → Álbumes.
// Las secciones vacías no se muestran (principio de diseño).
private fun LazyListScope.allResults(results: SearchUiState.Results, callbacks: ResultCallbacks) {
    val bestTrack = results.tracks.first() // seguro: Results nunca tiene la lista vacía
    item(key = "best") {
        BestResultCard(
            overline = stringResource(R.string.search_best_result),
            title = bestTrack.title,
            subtitle = "${stringResource(R.string.type_song)} · ${bestTrack.artistNames}",
            artworkUrl = bestTrack.artworkUrl,
            playLabel = stringResource(R.string.play_item, bestTrack.title),
            onClick = { callbacks.onTrackOpen(bestTrack) },
            onPlay = { callbacks.onTrackClick(bestTrack) },
        )
    }

    val moreSongs = results.tracks.drop(1).take(SONGS_PREVIEW_COUNT)
    if (moreSongs.isNotEmpty()) {
        item(key = "songs-header") {
            SectionHeader(
                title = stringResource(R.string.search_section_songs),
                actionLabel = stringResource(R.string.see_all),
                onAction = { callbacks.onFilterChange(SearchFilter.Songs) },
                modifier = Modifier.padding(top = FlixTheme.spacing.sectionGap),
            )
        }
        trackItems(moreSongs, callbacks.onTrackClick)
    }

    if (results.artists.isNotEmpty()) {
        item(key = "artists-header") {
            // Diseño: sec('Artistas') sin "Ver todo".
            SectionHeader(
                title = stringResource(R.string.search_section_artists),
                modifier = Modifier.padding(top = FlixTheme.spacing.sectionGap, bottom = FlixTheme.spacing.md),
            )
        }
        item(key = "artists") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = FlixTheme.spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.itemGap),
            ) {
                items(results.artists, key = { it.id }) { artist ->
                    MediaCard(
                        title = artist.name,
                        subtitle = stringResource(R.string.type_artist),
                        artworkUrl = artist.artworkUrl,
                        onClick = { callbacks.onArtistClick(artist) },
                        circular = true,
                        modifier = Modifier.width(ARTIST_CARD_WIDTH),
                    )
                }
            }
        }
    }

    if (results.albums.isNotEmpty()) {
        item(key = "albums-header") {
            // Diseño: sec('Álbumes') sin "Ver todo".
            SectionHeader(
                title = stringResource(R.string.search_section_albums),
                modifier = Modifier.padding(top = FlixTheme.spacing.sectionGap, bottom = FlixTheme.spacing.md),
            )
        }
        item(key = "albums") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = FlixTheme.spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.itemGap),
            ) {
                items(results.albums, key = { it.id }) { album ->
                    MediaCard(
                        title = album.name,
                        subtitle = album.artistName,
                        artworkUrl = album.artworkUrl,
                        onClick = { callbacks.onAlbumClick(album) },
                        modifier = Modifier.width(ALBUM_CARD_WIDTH),
                    )
                }
            }
        }
    }
}

// Filas de canción con subtítulo "artista · álbum" (row(..., {album:1}) del diseño).
private fun LazyListScope.trackItems(tracks: List<Track>, onTrackClick: (Track) -> Unit) {
    items(tracks, key = { it.id }) { track ->
        TrackRow(
            title = track.title,
            subtitle = listOfNotNull(track.artistNames, track.album?.name)
                .filter { it.isNotBlank() }
                .joinToString(" · "),
            artworkUrl = track.artworkUrl,
            duration = track.duration?.toClock(),
            onClick = { onTrackClick(track) },
        )
    }
}

// Cuadrícula de 2 columnas DENTRO de una LazyColumn (no se puede anidar una
// LazyVerticalGrid): se agrupan los elementos de 2 en 2 y cada par es una fila.
private fun <Item> LazyListScope.cardGrid(
    gridItems: List<Item>,
    key: (Item) -> String,
    card: @Composable (item: Item, cardModifier: Modifier) -> Unit,
) {
    items(gridItems.chunked(2), key = { pair -> key(pair.first()) }) { pair ->
        Row(
            modifier = Modifier.padding(
                horizontal = FlixTheme.spacing.gutter,
                vertical = FlixTheme.spacing.sm,
            ),
            horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.itemGap),
        ) {
            pair.forEach { gridItem -> card(gridItem, Modifier.weight(1f)) }
            if (pair.size == 1) Spacer(Modifier.weight(1f)) // par incompleto: media anchura
        }
    }
}

// ─── Previews: sin ViewModel ni servidor ───

private val previewTracks = listOf(
    Track("1", "Fade To Black", listOf(ArtistRef("m", "Metallica")), AlbumRef("rtl", "Ride the Lightning"), 417.seconds, null),
    Track("2", "Fade to Black", listOf(ArtistRef("a", "Apocalyptica")), AlbumRef("pm", "Plays Metallica by Four Cellos"), 281.seconds, null),
    Track("3", "Fade to Black", listOf(ArtistRef("d", "Dire Straits")), AlbumRef("oes", "On Every Street"), 214.seconds, null),
)

private val previewResults = SearchUiState.Results(
    tracks = previewTracks,
    artists = previewTracks.distinctArtists(),
    albums = previewTracks.distinctAlbums(),
)

@Composable
private fun SearchPreviewContent(uiState: SearchUiState, filter: SearchFilter = SearchFilter.All) {
    SearchContent(
        query = "fade to black",
        uiState = uiState,
        filter = filter,
        onQueryChange = {}, onClear = {}, onRetry = {}, onFilterChange = {},
        onTrackClick = {}, onTrackOpen = {}, onArtistClick = {}, onAlbumClick = {},
    )
}

@Preview(name = "Resultados · Todo", widthDp = 360, heightDp = 900)
@Composable
private fun SearchResultsPreview() {
    FlixPreview { SearchPreviewContent(previewResults) }
}

@Preview(name = "Resultados · Todo · claro", widthDp = 360, heightDp = 900)
@Composable
private fun SearchResultsLightPreview() {
    FlixPreview(ThemeMode.Light) { SearchPreviewContent(previewResults) }
}

@Preview(name = "Resultados · Artistas", widthDp = 360, heightDp = 640)
@Composable
private fun SearchArtistsPreview() {
    FlixPreview { SearchPreviewContent(previewResults, filter = SearchFilter.Artists) }
}

@Preview(name = "Cargando", widthDp = 360, heightDp = 640)
@Composable
private fun SearchLoadingPreview() {
    FlixPreview { SearchPreviewContent(SearchUiState.Loading()) }
}

@Preview(name = "Error", widthDp = 360, heightDp = 640)
@Composable
private fun SearchErrorPreview() {
    FlixPreview { SearchPreviewContent(SearchUiState.Error(FlixError.ServerUnreachable)) }
}
