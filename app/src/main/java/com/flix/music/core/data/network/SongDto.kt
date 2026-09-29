package com.flix.music.core.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val query: String,
    val songs: List<SongDto>
)

@Serializable
data class SongDto(
    val id: String,
    val title: String,
    val artists: List<ArtistDto> = emptyList(),
    val album: AlbumDto? = null,
    @SerialName("duration_seconds")
    val durationSeconds: Int? = null,
    val thumbnail: String? = null
)

/** `songs/{id}`: a song plus YouTube's playability status (`OK`, `UNPLAYABLE`...). */
@Serializable
data class SongDetailDto(
    val id: String,
    val title: String,
    val artists: List<ArtistDto> = emptyList(),
    val album: AlbumDto? = null,
    @SerialName("duration_seconds")
    val durationSeconds: Int? = null,
    val thumbnail: String? = null,
    val playability: String,
) {
    fun toSongDto() = SongDto(id, title, artists, album, durationSeconds, thumbnail)
}

@Serializable
data class ArtistDto(
    val name: String,
    val id: String? = null
)

@Serializable
data class AlbumDto(
    val name: String,
    val id: String? = null
)