package com.flix.music.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * On-device library. The schema is exported to `app/schemas` so future versions
 * (playlists) can migrate with Room auto-migrations instead of wiping user data.
 */
@Database(
    entities = [FavoriteTrackEntity::class, HistoryEntryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class FlixDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoritesDao

    abstract fun historyDao(): HistoryDao
}
