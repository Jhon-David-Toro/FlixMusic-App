package com.flix.music.core.player.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import com.flix.music.core.player.stream.StreamResolver
import com.flix.music.core.player.stream.youTubeDataSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Singleton

/**
 * Media3's downloader. Downloaded audio lives in the app's private storage (removed
 * with the app) and is never evicted; the player reads it from there when present.
 */
@OptIn(UnstableApi::class)
@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {

    private const val MAX_PARALLEL_DOWNLOADS = 2

    @Provides
    @Singleton
    fun provideDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider = StandaloneDatabaseProvider(context)

    @Provides
    @Singleton
    fun provideDownloadCache(@ApplicationContext context: Context, databaseProvider: DatabaseProvider): Cache =
        SimpleCache(File(context.filesDir, "downloads"), NoOpCacheEvictor(), databaseProvider)

    @Provides
    @Singleton
    fun provideYouTubeDataSourceFactory(okHttpClient: OkHttpClient, streamResolver: StreamResolver): DataSource.Factory =
        youTubeDataSourceFactory(okHttpClient, streamResolver)

    @Provides
    @Singleton
    fun provideDownloadManager(
        @ApplicationContext context: Context,
        databaseProvider: DatabaseProvider,
        downloadCache: Cache,
        youTubeDataSourceFactory: DataSource.Factory,
    ): DownloadManager =
        DownloadManager(
            context,
            databaseProvider,
            downloadCache,
            youTubeDataSourceFactory,
            Executors.newFixedThreadPool(MAX_PARALLEL_DOWNLOADS),
        ).apply { maxParallelDownloads = MAX_PARALLEL_DOWNLOADS }
}
