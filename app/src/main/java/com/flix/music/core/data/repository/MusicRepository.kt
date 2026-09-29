package com.flix.music.core.data.repository

import com.flix.music.core.model.SongDetails
import com.flix.music.core.model.Track

/** Music catalog from the backend. Every failure is thrown as a `FlixException`. */
interface MusicRepository {

    suspend fun search(query: String): List<Track>

    /** A song already seen in a search this session, so its detail can open instantly. */
    fun cachedTrack(trackId: String): Track?

    suspend fun song(trackId: String): SongDetails

    /** Warms the server cache for [trackId] so its audio starts instantly when played. */
    suspend fun prefetch(trackId: String)
}
