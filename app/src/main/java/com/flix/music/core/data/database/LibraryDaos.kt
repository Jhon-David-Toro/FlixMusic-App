package com.flix.music.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorite_tracks ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :trackId)")
    suspend fun exists(trackId: String): Boolean

    @Upsert
    suspend fun upsert(favorite: FavoriteTrackEntity)

    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun delete(trackId: String)
}

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY playedAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<HistoryEntryEntity>>

    /** REPLACE lets "undo" re-insert a removed entry with its original id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntryEntity): Long

    @Query("DELETE FROM history WHERE entryId = :entryId")
    suspend fun delete(entryId: Long)

    @Query("DELETE FROM history")
    suspend fun clear()
}
