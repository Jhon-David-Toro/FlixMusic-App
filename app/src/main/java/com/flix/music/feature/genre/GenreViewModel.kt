package com.flix.music.feature.genre

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flix.music.core.data.repository.MusicRepository
import com.flix.music.core.model.FlixException
import com.flix.music.core.model.Genre
import com.flix.music.core.model.Genres
import com.flix.music.core.model.distinctAlbums
import com.flix.music.core.model.distinctArtists
import com.flix.music.feature.genre.navigation.GenreRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Loads a genre's popular songs through its curated search query and derives its
 * artists and albums from them (design: "Cada género = búsqueda curada").
 */
@HiltViewModel
class GenreViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
) : ViewModel() {

    /**
     * Reads the [GenreRoute.genreId] argument by name instead of `toRoute()`, so plain
     * JVM tests can build the ViewModel with `SavedStateHandle(mapOf("genreId" to ...))`.
     */
    val genre: Genre = requireNotNull(
        Genres.byId(requireNotNull(savedStateHandle.get<String>(GENRE_ID_ARGUMENT))),
    )

    private val _uiState = MutableStateFlow<GenreUiState>(GenreUiState.Loading)
    val uiState: StateFlow<GenreUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = GenreUiState.Loading
            _uiState.value = try {
                val tracks = repository.search(genre.query)
                if (tracks.isEmpty()) {
                    GenreUiState.Empty
                } else {
                    GenreUiState.Content(
                        tracks = tracks,
                        artists = tracks.distinctArtists(),
                        albums = tracks.distinctAlbums(),
                    )
                }
            } catch (exception: FlixException) {
                GenreUiState.Error(exception.error)
            }
        }
    }

    private companion object {
        const val GENRE_ID_ARGUMENT = "genreId"
    }
}
