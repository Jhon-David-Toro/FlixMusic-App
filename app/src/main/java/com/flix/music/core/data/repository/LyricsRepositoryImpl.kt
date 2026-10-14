package com.flix.music.core.data.repository

import com.flix.music.core.data.lyrics.LrclibClient
import com.flix.music.core.data.network.catalogCall
import com.flix.music.core.model.Lyrics
import com.flix.music.core.model.Track
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/** LRCLIB, remembering each answer (also "no lyrics") for the session. */
class LyricsRepositoryImpl @Inject constructor(
    private val lrclibClient: LrclibClient,
) : LyricsRepository {

    private class Answer(val lyrics: Lyrics?)

    private val answers = ConcurrentHashMap<String, Answer>()

    override suspend fun lyrics(track: Track): Lyrics? {
        answers[track.id]?.let { answer -> return answer.lyrics }
        val lyrics = catalogCall { lrclibClient.find(track) }
        answers[track.id] = Answer(lyrics)
        return lyrics
    }
}
