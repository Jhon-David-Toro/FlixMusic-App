package com.flix.music.feature.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flix.music.core.data.repository.FavoritesRepository
import com.flix.music.core.model.Track
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerController
import com.flix.music.core.player.PlayerEvent
import com.flix.music.core.player.PlayerState
import com.flix.music.core.player.QueueItem
import com.flix.music.core.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

/** Layers inside the Full Player (design Navigation: Cola and Letra open inside the player). */
enum class PlayerLayer { NowPlaying, Queue, Lyrics }

/**
 * App-level player UI: the mini player, the Full Player, whether it is expanded and
 * which layer it shows, plus the queue actions of the Cola layer.
 */
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
        }.stateIn(viewModelScope, WhileUiSubscribed, false)

    var isExpanded by mutableStateOf(false)
        private set

    fun expand() {
        if (state.value.status != PlaybackStatus.Idle) isExpanded = true
    }

    var layer by mutableStateOf(PlayerLayer.NowPlaying)
        private set

    fun collapse() {
        isExpanded = false
        layer = PlayerLayer.NowPlaying
    }

    fun openQueue() {
        layer = PlayerLayer.Queue
    }

    fun openLyrics() {
        layer = PlayerLayer.Lyrics
    }

    /** Back and swipe down from Cola or Letra return to the player, not to the screen below. */
    fun closeLayer() {
        layer = PlayerLayer.NowPlaying
    }

    fun onQueueItemClick(item: QueueItem) = playerController.skipToQueueItem(item.index)

    fun onRemoveQueueItem(index: Int) = playerController.removeQueueItem(index)

    fun onRestoreQueueItem(index: Int, track: Track) = playerController.restoreQueueItem(index, track)

    fun onMoveQueueItem(fromIndex: Int, toIndex: Int) = playerController.moveQueueItem(fromIndex, toIndex)

    fun onClearQueue() = playerController.clearUpNext()

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
}
