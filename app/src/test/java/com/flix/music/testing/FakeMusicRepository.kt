package com.flix.music.testing

import com.flix.music.core.data.repository.MusicRepository
import com.flix.music.core.model.FlixError
import com.flix.music.core.model.FlixException
import com.flix.music.core.model.Track
import kotlinx.coroutines.delay

class FakeMusicRepository : MusicRepository {

    var tracks: List<Track> = emptyList()
    var error: FlixError? = null
    var searchCount = 0
        private set

    // Simula un servidor lento (en el reloj virtual del test).
    var responseDelayMillis = 0L

    override suspend fun search(query: String): List<Track> {
        searchCount++
        delay(responseDelayMillis)
        error?.let { throw FlixException(it) }
        return tracks
    }
}