package com.flix.music.feature.search

import com.flix.music.core.model.FlixError
import com.flix.music.core.model.Track

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val tracks: List<Track>) : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data class Error(val error: FlixError) : SearchUiState
}