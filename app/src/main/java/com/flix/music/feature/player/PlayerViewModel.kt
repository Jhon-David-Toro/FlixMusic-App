package com.flix.music.feature.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerController
import com.flix.music.core.player.PlayerEvent
import com.flix.music.core.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.time.Duration

/** App-level player UI: the mini player, the Full Player and whether it is expanded. */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
) : ViewModel() {

    val state: StateFlow<PlayerState> = playerController.state

    val events: Flow<PlayerEvent> = playerController.events

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
}
