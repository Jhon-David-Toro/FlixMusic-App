package com.flix.music.core.player

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import com.flix.music.BuildConfig
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track

/**
 * Builds the queue item for [this] track. Audio comes from the backend's `/audio`
 * proxy (m4a with Range support), the only URL that works from any network.
 */
internal fun Track.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri("${BuildConfig.SERVER_URL}api/music/songs/$id/audio")
        .setMimeType(MimeTypes.AUDIO_MP4)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artistNames)
                .setAlbumTitle(album?.name)
                .setArtworkUri(artworkUrl?.toUri())
                .build(),
        )
        .build()

/**
 * The [Track] behind this queue item: the one that was queued when known, otherwise
 * rebuilt from the metadata (e.g. a session restored after the app was closed).
 */
internal fun MediaItem.toTrack(queuedTracks: Map<String, Track>): Track =
    queuedTracks[mediaId] ?: Track(
        id = mediaId,
        title = mediaMetadata.title?.toString().orEmpty(),
        artists = mediaMetadata.artist?.toString()?.let { artistName -> listOf(ArtistRef(null, artistName)) }.orEmpty(),
        album = null,
        duration = null,
        artworkUrl = mediaMetadata.artworkUri?.toString(),
    )
