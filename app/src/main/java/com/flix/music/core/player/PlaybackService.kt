package com.flix.music.core.player

import android.app.PendingIntent
import android.content.Intent
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.flix.music.MainActivity
import com.flix.music.core.player.stream.StreamResolver
import com.flix.music.core.player.stream.trackIdOfWatchUrl
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject
import okhttp3.OkHttpClient

/**
 * Owns the ExoPlayer and its [MediaSession], so music keeps playing with the screen
 * off and the system shows the media notification and lock-screen controls.
 * Each queue item points at the song's YouTube page; right before loading it, the
 * [StreamResolver] swaps that for a direct googlevideo URL resolved on the phone.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var streamResolver: StreamResolver

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(directStreamDataSourceFactory()))
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

    private fun directStreamDataSourceFactory(): DataSource.Factory =
        ResolvingDataSource.Factory(OkHttpDataSource.Factory(okHttpClient)) { dataSpec -> dataSpec.toDirectStream() }

    /**
     * Points a queue item's YouTube page at the phone-resolved stream. A request from
     * the start with no end gets `Range: bytes=0-`, since googlevideo throttles requests
     * without a Range to about 30 KB/s.
     */
    private fun DataSpec.toDirectStream(): DataSpec {
        val trackId = trackIdOfWatchUrl(uri.toString()) ?: return this
        val directUrl = streamResolver.resolve(trackId) ?: throw IOException("YouTube gave no audio for $trackId")
        val direct = withUri(directUrl.toUri())
        return if (position == 0L && length == C.LENGTH_UNSET.toLong()) {
            direct.withAdditionalHeaders(mapOf(RANGE_HEADER to FULL_RANGE))
        } else {
            direct
        }
    }

    private fun openAppIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private companion object {
        const val RANGE_HEADER = "Range"
        const val FULL_RANGE = "bytes=0-"
    }
}
