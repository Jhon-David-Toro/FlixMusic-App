package com.flix.music.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flix.music.core.data.repository.MusicRepository
import com.flix.music.core.model.FlixException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MusicRepository,
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()

        if (newQuery.trim().length < MIN_QUERY_LENGTH) {
            _uiState.value = SearchUiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS.milliseconds)
            search(newQuery.trim())
        }
    }

    fun onClear() = onQueryChange("")

    fun onRetry() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { search(query.trim()) }
    }

    private suspend fun search(searchQuery: String) {
        _uiState.value = SearchUiState.Loading
        _uiState.value = try {
            val tracks = repository.search(searchQuery)
            if (tracks.isEmpty()) SearchUiState.Empty(searchQuery) else SearchUiState.Results(tracks)
        } catch (exception: FlixException) {
            SearchUiState.Error(exception.error)
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val DEBOUNCE_MS = 300L
    }
}