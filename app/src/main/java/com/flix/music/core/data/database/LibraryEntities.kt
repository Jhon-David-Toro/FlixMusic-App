package com.flix.music.core.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_tracks", primaryKeys = ["trackId"])
data class FavoriteTrackEntity(
    @Embedded val track: StoredTrack,
    val addedAtMillis: Long,
)

@Entity(tableName = "history", indices = [Index("playedAtMillis")])
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val entryId: Long = 0,
    @Embedded val track: StoredTrack,
    val playedAtMillis: Long,
)
