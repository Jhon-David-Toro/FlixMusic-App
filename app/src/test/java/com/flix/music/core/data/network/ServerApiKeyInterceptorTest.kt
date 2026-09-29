package com.flix.music.core.data.network

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerApiKeyInterceptorTest {

    private val serverUrl = "https://flixmusic.doxel.dev/".toHttpUrl()

    /** Sends a request to [url] through a client with the interceptor and returns what went out; no network. */
    private fun sentRequest(apiKey: String, url: String): Request {
        var sent: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(ServerApiKeyInterceptor(serverUrl, apiKey))
            .addInterceptor { chain ->
                sent = chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody())
                    .build()
            }
            .build()
        client.newCall(Request.Builder().url(url).build()).execute().close()
        return requireNotNull(sent)
    }

    @Test
    fun `la clave va en las peticiones al servidor, también en el audio`() {
        val request = sentRequest("secreta", "https://flixmusic.doxel.dev/api/music/songs/abc/audio")

        assertEquals("secreta", request.header("X-Flix-Key"))
    }

    @Test
    fun `la clave nunca sale hacia otros dominios`() {
        val request = sentRequest("secreta", "https://i.ytimg.com/vi/abc/hq.jpg")

        assertNull(request.header("X-Flix-Key"))
    }

    @Test
    fun `sin clave configurada no se envía nada`() {
        val request = sentRequest("", "https://flixmusic.doxel.dev/health")

        assertNull(request.header("X-Flix-Key"))
    }
}
