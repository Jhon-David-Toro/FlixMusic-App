package com.flix.music.core.player

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.flix.music.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Owns the ExoPlayer and its [MediaSession], so music keeps playing with the screen
 * off and the system shows the media notification and lock-screen controls.
 * Downloaded songs play from the phone; the rest stream from YouTube.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var youTubeDataSourceFactory: DataSource.Factory

    @Inject
    lateinit var downloadCache: Cache

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(downloadsFirstDataSourceFactory()))
            .setLoadControl(wholeSongLoadControl())
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(openAppIntent())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /** Swiping the app away stops the service unless music is playing. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    /**
     * Buffers the whole song (up to [MAX_BUFFER_MS]), also while paused, instead of
     * ExoPlayer's default 50 s, and starts playing after 1 s of audio instead of 2.5 s.
     * A 15-minute song at 128 kbps is about 14 MB, inside [TARGET_BUFFER_BYTES].
     */
    private fun wholeSongLoadControl(): LoadControl =
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(MAX_BUFFER_MS, MAX_BUFFER_MS, START_PLAYBACK_BUFFER_MS, RESUME_PLAYBACK_BUFFER_MS)
            .setTargetBufferBytes(TARGET_BUFFER_BYTES)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

    /** Reads what was downloaded and streams the rest, without filling the downloads with plays. */
    private fun downloadsFirstDataSourceFactory(): DataSource.Factory =
        CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(youTubeDataSourceFactory)
            .setCacheWriteDataSinkFactory(null)

    private fun openAppIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private companion object {
        const val MAX_BUFFER_MS = 15 * 60_000
        const val START_PLAYBACK_BUFFER_MS = 1_000
        const val RESUME_PLAYBACK_BUFFER_MS = 2_000
        const val TARGET_BUFFER_BYTES = 32 * 1024 * 1024
    }
}
