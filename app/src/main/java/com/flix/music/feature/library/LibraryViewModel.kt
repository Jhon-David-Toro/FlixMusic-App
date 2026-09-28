package com.flix.music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flix.music.core.data.repository.FavoritesRepository
import com.flix.music.core.data.repository.HistoryRepository
import com.flix.music.core.model.FavoriteTrack
import com.flix.music.core.model.SortOrder
import com.flix.music.core.model.Track
import com.flix.music.core.model.countPlayedOn
import com.flix.music.core.model.sortedFor
import com.flix.music.core.player.PlaybackSource
import com.flix.music.core.player.PlayerController
import com.flix.music.core.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Library filters available today. The design also lists Álbumes (saved) and
 * Artistas (followed); they arrive with the album and artist screens.
 */
enum class LibraryFilter { Playlists, Songs }

data class LibraryUiState(
    val favoriteCount: Int = 0,
    val playedTodayCount: Int = 0,
    val hasHistory: Boolean = false,
    val filter: LibraryFilter = LibraryFilter.Songs,
    val sortOrder: SortOrder = SortOrder.Recent,
    val songs: List<FavoriteTrack> = emptyList(),
)

/** Library tab: pinned Favorites and History, filter chips and the saved songs (favorites). */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    favoritesRepository: FavoritesRepository,
    historyRepository: HistoryRepository,
    private val playerController: PlayerController,
    private val clock: Clock,
) : ViewModel() {

    private val filter = MutableStateFlow(LibraryFilter.Songs)
    private val sortOrder = MutableStateFlow(SortOrder.Recent)

    val playerState: StateFlow<PlayerState> = playerController.state

    val uiState: StateFlow<LibraryUiState> = combine(
        favoritesRepository.favorites,
        historyRepository.history,
        filter,
        sortOrder,
    ) { favorites, history, selectedFilter, order ->
        LibraryUiState(
            favoriteCount = favorites.size,
            playedTodayCount = history.countPlayedOn(LocalDate.now(clock), clock.zone),
            hasHistory = history.isNotEmpty(),
            filter = selectedFilter,
            sortOrder = order,
            songs = favorites.sortedFor(order),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LibraryUiState())

    fun onFilterChange(newFilter: LibraryFilter) {
        filter.value = newFilter
    }

    fun onSortOrderChange(order: SortOrder) {
        sortOrder.value = order
    }

    /** "Canciones = favoritas" (design): plays the saved songs from [track] in the shown order. */
    fun onSongClick(track: Track) {
        val songs = uiState.value.songs.map { it.track }
        val startIndex = songs.indexOfFirst { it.id == track.id }
        if (startIndex >= 0) playerController.play(songs, startIndex, PlaybackSource.Favorites)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
