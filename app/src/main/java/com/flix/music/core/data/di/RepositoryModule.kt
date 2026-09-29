package com.flix.music.core.data.di

import com.flix.music.core.data.repository.CacheRepository
import com.flix.music.core.data.repository.CacheRepositoryImpl
import com.flix.music.core.data.repository.FavoritesRepository
import com.flix.music.core.data.repository.FavoritesRepositoryImpl
import com.flix.music.core.data.repository.FollowedArtistsRepository
import com.flix.music.core.data.repository.FollowedArtistsRepositoryImpl
import com.flix.music.core.data.repository.HistoryRepository
import com.flix.music.core.data.repository.HistoryRepositoryImpl
import com.flix.music.core.data.repository.LibraryLayoutRepository
import com.flix.music.core.data.repository.LibraryLayoutRepositoryImpl
import com.flix.music.core.data.repository.MusicRepository
import com.flix.music.core.data.repository.MusicRepositoryImpl
import com.flix.music.core.data.repository.PlaylistsRepository
import com.flix.music.core.data.repository.PlaylistsRepositoryImpl
import com.flix.music.core.data.repository.RecentSearchesRepository
import com.flix.music.core.data.repository.RecentSearchesRepositoryImpl
import com.flix.music.core.data.repository.SavedAlbumsRepository
import com.flix.music.core.data.repository.SavedAlbumsRepositoryImpl
import com.flix.music.core.data.repository.SettingsRepository
import com.flix.music.core.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds
    @Singleton
    abstract fun bindRecentSearchesRepository(impl: RecentSearchesRepositoryImpl): RecentSearchesRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistsRepository(impl: PlaylistsRepositoryImpl): PlaylistsRepository

    @Binds
    @Singleton
    abstract fun bindFollowedArtistsRepository(impl: FollowedArtistsRepositoryImpl): FollowedArtistsRepository

    @Binds
    @Singleton
    abstract fun bindSavedAlbumsRepository(impl: SavedAlbumsRepositoryImpl): SavedAlbumsRepository

    @Binds
    @Singleton
    abstract fun bindLibraryLayoutRepository(impl: LibraryLayoutRepositoryImpl): LibraryLayoutRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindCacheRepository(impl: CacheRepositoryImpl): CacheRepository
}
