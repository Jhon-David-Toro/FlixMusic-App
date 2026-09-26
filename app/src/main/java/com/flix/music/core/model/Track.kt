package com.flix.music.core.model

import kotlin.time.Duration

data class Track(
    val id: String,
    val title: String,
    val artists: List<ArtistRef>,
    val album: AlbumRef?,
    val duration: Duration?,
    val artworkUrl: String?,
) {

    val artistNames: String
        get() = artists.joinToString(", ") { it.name }
}

data class ArtistRef(
    val id: String?,
    val name: String,
)

data class AlbumRef(
    val id: String?,
    val name: String,
)