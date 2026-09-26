package com.flix.music.core.data.mapper

import com.flix.music.core.data.network.AlbumDto
import com.flix.music.core.data.network.ArtistDto
import com.flix.music.core.data.network.SongDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class SongMapperTest {

    @Test
    fun `convierte una canción completa`() {
        val dto = SongDto(
            id = "abc123def45",
            title = "Fade To Black",
            artists = listOf(ArtistDto(name = "Metallica", id = "UC1")),
            album = AlbumDto(name = "Ride the Lightning", id = "MPRE1"),
            durationSeconds = 417,
            thumbnail = "https://img/cover.jpg",
        )

        val track = dto.toTrack()

        assertEquals("Fade To Black", track.title)
        assertEquals("Metallica", track.artistNames)
        assertEquals("Ride the Lightning", track.album?.name)
        assertEquals(417.seconds, track.duration)
        assertEquals("https://img/cover.jpg", track.artworkUrl)
    }

    @Test
    fun `los campos opcionales ausentes quedan en null`() {
        val track = SongDto(id = "abc123def45", title = "Sin datos").toTrack()

        assertNull(track.album)
        assertNull(track.duration)
        assertNull(track.artworkUrl)
        assertEquals("", track.artistNames)
    }
}