package com.flix.music.core.data.network

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Sends the server's API key (`X-Flix-Key`) on every request to the Flix server, both
 * API calls and the audio the player streams. Requests to other hosts never get it,
 * and an empty key (the home setup) sends nothing.
 */
class ServerApiKeyInterceptor(
    private val serverUrl: HttpUrl,
    private val apiKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isFlixServer = request.url.host == serverUrl.host && request.url.port == serverUrl.port
        if (apiKey.isEmpty() || !isFlixServer) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header(API_KEY_HEADER, apiKey).build())
    }

    private companion object {
        const val API_KEY_HEADER = "X-Flix-Key"
    }
}
