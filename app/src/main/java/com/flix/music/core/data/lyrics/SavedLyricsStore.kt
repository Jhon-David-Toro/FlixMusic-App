package com.flix.music.core.data.lyrics

import android.content.Context
import com.flix.music.core.model.LyricLine
import com.flix.music.core.model.Lyrics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Serializable
private data class StoredLyrics(val lines: List<StoredLyricLine>)

@Serializable
private data class StoredLyricLine(val startMillis: Long?, val text: String)

/** Lyrics of downloaded songs, one JSON file per song, so they show offline. Blocking. */
@Singleton
class SavedLyricsStore @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json,
) {

    private val directory = File(context.filesDir, "lyrics")

    fun read(trackId: String): Lyrics? {
        val file = fileOf(trackId).takeIf(File::exists) ?: return null
        return runCatching { json.decodeFromString<StoredLyrics>(file.readText()) }.getOrNull()?.let { stored ->
            Lyrics(stored.lines.map { line -> LyricLine(line.startMillis?.milliseconds, line.text) })
        }
    }

    fun write(trackId: String, lyrics: Lyrics) {
        val stored = StoredLyrics(lyrics.lines.map { line -> StoredLyricLine(line.start?.inWholeMilliseconds, line.text) })
        directory.mkdirs()
        fileOf(trackId).writeText(json.encodeToString(stored))
    }

    fun delete(trackId: String) {
        fileOf(trackId).delete()
    }

    private fun fileOf(trackId: String) = File(directory, "$trackId.json")
}
