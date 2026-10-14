package com.flix.music.core.data.youtube

import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.Localization
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configures NewPipeExtractor once for the whole app (song details and audio
 * streams). Call [ensureInitialized] before any NewPipe call, off the main thread.
 */
@Singleton
class NewPipeSetup @Inject constructor(private val okHttpClient: OkHttpClient) {

    private val initialization = lazy {
        NewPipe.init(NewPipeDownloader(okHttpClient), Localization(LANGUAGE, COUNTRY))
    }

    fun ensureInitialized() {
        initialization.value
    }

    private companion object {
        const val LANGUAGE = "en"
        const val COUNTRY = "US"
    }
}
