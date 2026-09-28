package com.flix.music.core.player

import com.flix.music.core.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * The app's single entry point to playback. Screens and ViewModels depend on this
 * interface, never on Media3, so they can be tested with a fake.
 */
interface PlayerController {

    val state: StateFlow<PlayerState>

    val events: Flow<PlayerEvent>

    /** Replaces the queue with [tracks] and starts playing [tracks]`[startIndex]`. */
    fun play(
        tracks: List<Track>,
        startIndex: Int,
        source: PlaybackSource,
        shuffle: Boolean = false,
    )

    fun togglePlayPause()

    fun seekTo(position: Duration)

    fun skipToNext()

    fun skipToPrevious()

    fun toggleShuffle()

    /** Off → All → One → Off. */
    fun cycleRepeatMode()

    /** Retries the current track after an error, or restarts the queue once it ended. */
    fun retry()
}
