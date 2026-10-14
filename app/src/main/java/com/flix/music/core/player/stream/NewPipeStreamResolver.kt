package com.flix.music.core.player.stream

import android.util.Log
import com.flix.music.core.data.youtube.NewPipeSetup
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.StreamInfo

/**
 * [StreamResolver] backed by NewPipeExtractor, the same approach as NewPipe and
 * Muzika: YouTube's player page is read and its signatures solved on the phone.
 * Prefers m4a/AAC (what the queue items declare) and falls back to the best other
 * progressive audio.
 */
@Singleton
class NewPipeStreamResolver @Inject constructor(
    private val newPipeSetup: NewPipeSetup,
    private val clock: Clock,
) : StreamResolver {

    private class CachedStream(val url: String, val expiresAtMillis: Long)

    private val cache = ConcurrentHashMap<String, CachedStream>()

    override fun resolve(trackId: String): String? {
        val now = clock.millis()
        cache[trackId]?.takeIf { cached -> cached.expiresAtMillis > now }?.let { cached -> return cached.url }
        val url = runCatching { extract(trackId) }
            .onFailure { failure -> Log.w(TAG, "Could not resolve $trackId on the phone: ${failure.message}") }
            .getOrNull()
            ?: return null
        cache[trackId] = CachedStream(url, streamExpiresAtMillis(url, now))
        return url
    }

    override fun forget(trackId: String) {
        cache.remove(trackId)
    }

    private fun extract(trackId: String): String? {
        newPipeSetup.ensureInitialized()
        val info = StreamInfo.getInfo(ServiceList.YouTube, youTubeWatchUrl(trackId))
        val progressive = info.audioStreams.filter { stream -> stream.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP }
        val preferred = progressive.filter { stream -> stream.format == MediaFormat.M4A }.ifEmpty { progressive }
        return preferred.maxByOrNull(AudioStream::getAverageBitrate)?.content
    }

    private companion object {
        const val TAG = "NewPipeStreamResolver"
    }
}
