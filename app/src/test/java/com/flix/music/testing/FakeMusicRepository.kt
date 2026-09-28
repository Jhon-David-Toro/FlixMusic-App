package com.flix.music.testing

import com.flix.music.core.data.repository.MusicRepository
import com.flix.music.core.model.FlixError
import com.flix.music.core.model.FlixException
import com.flix.music.core.model.Track

class FakeMusicRepository : MusicRepository {

    var tracks: List<Track> = emptyList()
    var error: FlixError? = null
    var searchCount = 0
        private set

    override suspend fun search(query: String): List<Track> {
        searchCount++
        error?.let { throw FlixException(it) }
        return tracks
    }
}