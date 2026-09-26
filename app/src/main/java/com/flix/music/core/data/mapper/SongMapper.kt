package com.flix.music.core.data.mapper

import com.flix.music.core.data.network.AlbumDto
import com.flix.music.core.data.network.ArtistDto
import com.flix.music.core.data.network.SongDto
import com.flix.music.core.model.AlbumRef
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track
import kotlin.time.Duration.Companion.seconds

fun SongDto.toTrack(): Track = Track(
    id = id,
    title = title,
    artists = artists.map { it.toArtistRef() },
    album = album?.toAlbumRef(),
    duration = durationSeconds?.seconds,
    artworkUrl = thumbnail,
)

fun ArtistDto.toArtistRef() = ArtistRef(id = id, name = name)

fun AlbumDto.toAlbumRef() = AlbumRef(id = id, name = name)