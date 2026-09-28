package com.flix.music.core.model

fun List<Track>.distinctArtists(): List<Artist> =
    flatMap { track ->
        track.artists.mapNotNull { artistRef ->
            artistRef.id?.let { artistId ->
                Artist(id = artistId, name = artistRef.name, artworkUrl = track.artworkUrl)
            }
        }
    }.distinctBy { it.id }

fun List<Track>.distinctAlbums(): List<Album> =
    mapNotNull { track ->
        track.album?.let { albumRef ->
            albumRef.id?.let { albumId ->
                Album(
                    id = albumId,
                    name = albumRef.name,
                    artistName = track.artistNames,
                    artworkUrl = track.artworkUrl,
                )
            }
        }
    }.distinctBy { it.id }