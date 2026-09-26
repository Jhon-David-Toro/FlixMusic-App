package com.flix.music.core.data.repository

import com.flix.music.core.model.Track

interface MusicRepository {

    suspend fun search(query: String): List<Track>
}