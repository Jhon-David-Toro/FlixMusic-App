package com.flix.music.core.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.flix.music.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Owns the ExoPlayer and its [MediaSession], so music keeps playing with the screen
 * off and the system shows the media notification and lock-screen controls.
 * Audio goes through the app's shared [OkHttpClient] (same timeouts, logging and,
 * later, the API key).
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(OkHttpDataSource.Factory(okHttpClient)))
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

    private fun openAppIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
