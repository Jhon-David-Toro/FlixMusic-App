package com.flix.music.feature.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.R
import com.flix.music.core.designsystem.component.ErrorStateView
import com.flix.music.core.designsystem.component.SearchField
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.component.util.toClock
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.designsystem.theme.ThemeMode
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.FlixError
import com.flix.music.core.model.Track
import kotlin.time.Duration.Companion.seconds

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchContent(
        query = viewModel.query,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onClear = viewModel::onClear,
        onRetry = viewModel::onRetry,
        onTrackClick = { },
        modifier = modifier,
    )
}

@Composable
fun SearchContent(
    query: String,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
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

        if (uiState is SearchUiState.Loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FlixTheme.spacing.gutter)
                    .height(2.dp),
                color = FlixTheme.colors.primary,
                trackColor = Color.Transparent,
            )
        } else {
            Spacer(Modifier.height(2.dp))
        }

        when (uiState) {
            SearchUiState.Idle -> StateView(
                icon = R.drawable.ic_search,
                title = stringResource(R.string.search_idle_title),
                body = stringResource(R.string.search_idle_body),
            )
            SearchUiState.Loading -> Unit
            is SearchUiState.Empty -> StateView(
                icon = R.drawable.ic_search_off,
                title = stringResource(R.string.search_empty_title, uiState.query),
                body = stringResource(R.string.search_empty_body),
            )
            is SearchUiState.Error -> ErrorStateView(error = uiState.error, onRetry = onRetry)
            is SearchUiState.Results -> LazyColumn {
                items(uiState.tracks, key = { it.id }) { track ->
                    TrackRow(
                        title = track.title,
                        subtitle = track.artistNames,
                        artworkUrl = track.artworkUrl,
                        duration = track.duration?.toClock(),
                        onClick = { onTrackClick(track) },
                    )
                }
            }
        }
    }
}

private val previewTracks = listOf(
    Track("1", "Fade To Black", listOf(ArtistRef(null, "Metallica")), null, 417.seconds, null),
    Track("2", "Fade to Black", listOf(ArtistRef(null, "Apocalyptica")), null, 281.seconds, null),
)

@Preview(name = "Resultados", widthDp = 360, heightDp = 640)
@Composable
private fun SearchResultsPreview() {
    FlixPreview {
        SearchContent("fade to black", SearchUiState.Results(previewTracks), {}, {}, {}, {})
    }
}

@Preview(name = "Error", widthDp = 360, heightDp = 640)
@Composable
private fun SearchErrorPreview() {
    FlixPreview {
        SearchContent("fade to black", SearchUiState.Error(FlixError.ServerUnreachable), {}, {}, {}, {})
    }
}
@Preview(name = "Resultados · claro", widthDp = 360, heightDp = 640)
@Composable
private fun SearchResultsLightPreview() {
    FlixPreview(ThemeMode.Light) {
        SearchContent("fade to black", SearchUiState.Results(previewTracks), {}, {}, {}, {})
    }
}
