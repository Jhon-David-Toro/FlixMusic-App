package com.flix.music.core.player.stream

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import okhttp3.OkHttpClient
import java.io.IOException

/**
 * Reads a song's audio for playback and downloads. Each queue item and download points
 * at the song's YouTube page; right before loading it, [streamResolver] swaps that for
 * a direct googlevideo URL resolved on the phone.
 */
@OptIn(UnstableApi::class)
fun youTubeDataSourceFactory(okHttpClient: OkHttpClient, streamResolver: StreamResolver): DataSource.Factory =
    ResolvingDataSource.Factory(OkHttpDataSource.Factory(okHttpClient)) { dataSpec -> dataSpec.toDirectStream(streamResolver) }

/**
 * A request from the start with no end gets `Range: bytes=0-`, since googlevideo
 * throttles requests without a Range to about 30 KB/s.
 */
@OptIn(UnstableApi::class)
private fun DataSpec.toDirectStream(streamResolver: StreamResolver): DataSpec {
    val trackId = trackIdOfWatchUrl(uri.toString()) ?: return this
    val directUrl = streamResolver.resolve(trackId) ?: throw IOException("YouTube gave no audio for $trackId")
    val direct = withUri(directUrl.toUri())
    return if (position == 0L && length == C.LENGTH_UNSET.toLong()) {
        direct.withAdditionalHeaders(mapOf(RANGE_HEADER to FULL_RANGE))
    } else {
        direct
    }
}

private const val RANGE_HEADER = "Range"
private const val FULL_RANGE = "bytes=0-"
