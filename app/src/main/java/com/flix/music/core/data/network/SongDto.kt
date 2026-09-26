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