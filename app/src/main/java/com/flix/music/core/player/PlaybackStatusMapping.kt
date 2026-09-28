package com.flix.music.core.player

import androidx.media3.common.Player

/**
 * Maps Media3's player state to the design's [PlaybackStatus].
 *
 * Buffering before the current track has ever been ready is [PlaybackStatus.Loading]
 * (the server is still resolving the audio); buffering after that is
 * [PlaybackStatus.Buffering].
 */
internal fun playbackStatusOf(
    hasMedia: Boolean,
    playbackState: Int,
    playWhenReady: Boolean,
    hasError: Boolean,
    currentTrackWasReady: Boolean,
): PlaybackStatus = when {
    !hasMedia -> PlaybackStatus.Idle
    hasError -> PlaybackStatus.Error
    playbackState == Player.STATE_ENDED -> PlaybackStatus.Ended
    playbackState == Player.STATE_READY ->
        if (playWhenReady) PlaybackStatus.Playing else PlaybackStatus.Paused
    playbackState == Player.STATE_BUFFERING ->
        if (currentTrackWasReady) PlaybackStatus.Buffering else PlaybackStatus.Loading
    playWhenReady -> PlaybackStatus.Loading
    else -> PlaybackStatus.Paused
}
