package com.flix.music.core.data.di

import com.flix.music.core.data.repository.FavoritesRepository
import com.flix.music.core.data.repository.FavoritesRepositoryImpl
import com.flix.music.core.data.repository.HistoryRepository
import com.flix.music.core.data.repository.HistoryRepositoryImpl
import com.flix.music.core.data.repository.MusicRepository
import com.flix.music.core.data.repository.MusicRepositoryImpl
import com.flix.music.core.data.repository.RecentSearchesRepository
import com.flix.music.core.data.repository.RecentSearchesRepositoryImpl
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
}
