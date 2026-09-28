package com.flix.music.core.player

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import com.flix.music.BuildConfig
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

internal fun MediaItem.toNowPlaying(): NowPlaying =
    NowPlaying(
        trackId = mediaId,
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString().orEmpty(),
        artworkUrl = mediaMetadata.artworkUri?.toString(),
    )
