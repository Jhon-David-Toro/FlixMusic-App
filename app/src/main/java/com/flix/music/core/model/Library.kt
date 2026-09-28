package com.flix.music.core.model

import java.time.Instant

/** A favorite song and when it was added (used for the "Recientes" order and for undo). */
data class FavoriteTrack(
    val track: Track,
    val addedAt: Instant,
)

/** One play in the history. The same song appears once per play. */
data class HistoryEntry(
    val id: Long,
    val track: Track,
    val playedAt: Instant,
)
