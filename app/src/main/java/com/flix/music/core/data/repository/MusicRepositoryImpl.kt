package com.flix.music.core.data.repository

import com.flix.music.core.data.mapper.toTrack
import com.flix.music.core.data.network.FlixApi
import com.flix.music.core.data.network.apiCall
import com.flix.music.core.model.Track
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val api: FlixApi,
) : MusicRepository {

    override suspend fun search(query: String): List<Track> = apiCall {
        api.search(query).songs.map { it.toTrack() }
    }
}