package com.flix.music.feature.search

import com.flix.music.core.model.Album
import com.flix.music.core.model.Artist
import com.flix.music.core.model.FlixError
import com.flix.music.core.model.Track

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class Loading(val previous: Results? = null) : SearchUiState

    data class Results(
        val tracks: List<Track>,
        val artists: List<Artist>,
        val albums: List<Album>,
    ) : SearchUiState

    data class Empty(val query: String) : SearchUiState
    data class Error(val error: FlixError) : SearchUiState
}