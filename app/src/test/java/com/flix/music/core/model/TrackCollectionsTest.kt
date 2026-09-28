package com.flix.music.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackCollectionsTest {

    private fun track(id: String, artists: List<ArtistRef>, album: AlbumRef? = null) =
        Track(id, "t$id", artists, album, null, "art$id")

    @Test
    fun `artistas distintos, en orden de aparición y con la portada de su primera canción`() {
        val tracks = listOf(
            track("1", listOf(ArtistRef("m", "Metallica"))),
            track("2", listOf(ArtistRef("a", "Apocalyptica"), ArtistRef("m", "Metallica"))),
        )

        assertEquals(
            listOf(Artist("m", "Metallica", "art1"), Artist("a", "Apocalyptica", "art2")),
            tracks.distinctArtists(),
        )
    }

    @Test
    fun `los artistas sin id se descartan`() {
        val tracks = listOf(track("1", listOf(ArtistRef(null, "Desconocido"))))

        assertEquals(emptyList<Artist>(), tracks.distinctArtists())
    }

    @Test
    fun `álbumes distintos, y los que no tienen id se descartan`() {
        val tracks = listOf(
            track("1", listOf(ArtistRef("m", "Metallica")), AlbumRef("rtl", "Ride the Lightning")),
            track("2", listOf(ArtistRef("m", "Metallica")), AlbumRef("rtl", "Ride the Lightning")),
            track("3", listOf(ArtistRef("m", "Metallica")), AlbumRef(null, "Single")),
            track("4", listOf(ArtistRef("m", "Metallica"))), // sin álbum
        )

        assertEquals(
            listOf(Album("rtl", "Ride the Lightning", "Metallica", "art1")),
            tracks.distinctAlbums(),
        )
    }
}
