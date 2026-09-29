package com.flix.music.core.data.repository

import android.content.Context
import coil3.SingletonImageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Everything under the app's cache directory, where Coil and the player keep their files. */
class CacheRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : CacheRepository {

    override suspend fun sizeInBytes(): Long = withContext(Dispatchers.IO) {
        context.cacheDir.walkBottomUp().filter(File::isFile).sumOf(File::length)
    }

    override suspend fun clear() {
        SingletonImageLoader.get(context).memoryCache?.clear()
        withContext(Dispatchers.IO) {
            context.cacheDir.listFiles()?.forEach { file -> file.deleteRecursively() }
        }
    }
}
