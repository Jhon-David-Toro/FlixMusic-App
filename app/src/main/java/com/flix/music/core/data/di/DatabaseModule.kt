package com.flix.music.core.data.di

import android.content.Context
import androidx.room.Room
import com.flix.music.core.data.database.FavoritesDao
import com.flix.music.core.data.database.FlixDatabase
import com.flix.music.core.data.database.FollowedArtistsDao
import com.flix.music.core.data.database.HistoryDao
import com.flix.music.core.data.database.PlaylistsDao
import com.flix.music.core.data.database.SavedAlbumsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FlixDatabase =
        Room.databaseBuilder(context, FlixDatabase::class.java, "flix.db").build()

    @Provides
    fun provideFavoritesDao(database: FlixDatabase): FavoritesDao = database.favoritesDao()

    @Provides
    fun provideHistoryDao(database: FlixDatabase): HistoryDao = database.historyDao()

    @Provides
    fun providePlaylistsDao(database: FlixDatabase): PlaylistsDao = database.playlistsDao()

    @Provides
    fun provideFollowedArtistsDao(database: FlixDatabase): FollowedArtistsDao = database.followedArtistsDao()

    @Provides
    fun provideSavedAlbumsDao(database: FlixDatabase): SavedAlbumsDao = database.savedAlbumsDao()
}
