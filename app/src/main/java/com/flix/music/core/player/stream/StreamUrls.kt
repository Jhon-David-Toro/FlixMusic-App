package com.flix.music.core.player.stream

private val SERVER_AUDIO_PATH = Regex("/api/music/songs/([A-Za-z0-9_-]{11})/audio")
private val EXPIRE_PARAMETER = Regex("[?&]expire=(\\d+)")

/** The YouTube id behind a queue item's server `/audio` URL, or null for any other URL. */
internal fun trackIdOfServerAudio(uri: String): String? = SERVER_AUDIO_PATH.find(uri)?.groupValues?.get(1)

/**
 * When a direct stream URL stops being usable. googlevideo states it in the `expire`
 * parameter (Unix seconds); a minute is kept as margin so a song never starts on a
 * URL about to die. URLs without it are trusted for [FALLBACK_LIFETIME_MILLIS].
 */
internal fun streamExpiresAtMillis(url: String, nowMillis: Long): Long {
    val expireSeconds = EXPIRE_PARAMETER.find(url)?.groupValues?.get(1)?.toLongOrNull()
    return if (expireSeconds != null) expireSeconds * 1_000 - EXPIRY_MARGIN_MILLIS else nowMillis + FALLBACK_LIFETIME_MILLIS
}

private const val EXPIRY_MARGIN_MILLIS = 60_000L
private const val FALLBACK_LIFETIME_MILLIS = 30 * 60_000L
