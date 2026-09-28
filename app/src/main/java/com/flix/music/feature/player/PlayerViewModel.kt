package com.flix.music.feature.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flix.music.core.data.repository.FavoritesRepository
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerController
import com.flix.music.core.player.PlayerEvent
import com.flix.music.core.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

/** App-level player UI: the mini player, the Full Player and whether it is expanded. */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    val state: StateFlow<PlayerState> = playerController.state

    val events: Flow<PlayerEvent> = playerController.events

    val isCurrentFavorite: StateFlow<Boolean> =
        combine(playerController.state, favoritesRepository.favoriteIds) { playerState, favoriteIds ->
            playerState.current?.id in favoriteIds
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    var isExpanded by mutableStateOf(false)
        private set

    fun expand() {
        if (state.value.status != PlaybackStatus.Idle) isExpanded = true
    }

    fun collapse() {
        isExpanded = false
    }

    fun onPlayPause() = playerController.togglePlayPause()

    fun onSeek(position: Duration) = playerController.seekTo(position)

    fun onNext() = playerController.skipToNext()

    fun onPrevious() = playerController.skipToPrevious()

    fun onToggleShuffle() = playerController.toggleShuffle()

    fun onCycleRepeat() = playerController.cycleRepeatMode()

    fun onRetry() = playerController.retry()

    fun onToggleFavorite() {
        val track = state.value.current ?: return
        viewModelScope.launch { favoritesRepository.toggle(track) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
