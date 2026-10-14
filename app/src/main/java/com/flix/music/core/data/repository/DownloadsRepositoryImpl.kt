package com.flix.music.core.data.repository

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.flix.music.core.data.database.StoredTrack
import com.flix.music.core.data.database.toStoredTrack
import com.flix.music.core.data.database.toTrack
import com.flix.music.core.data.lyrics.SavedLyricsStore
import com.flix.music.core.model.DownloadState
import com.flix.music.core.model.DownloadStorage
import com.flix.music.core.model.FlixException
import com.flix.music.core.model.SongDownload
import com.flix.music.core.model.Track
import com.flix.music.core.player.FlixDownloadService
import com.flix.music.core.player.stream.youTubeWatchUrl
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downloads through Media3's [DownloadManager], run by [FlixDownloadService] so they go
 * on in the background. Each request carries its [Track] as JSON, so the list renders
 * offline; lyrics are saved next to it in [SavedLyricsStore].
 */
@OptIn(UnstableApi::class)
@Singleton
class DownloadsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadManager: DownloadManager,
    private val downloadCache: Cache,
    private val lyricsRepository: LyricsRepository,
    private val savedLyricsStore: SavedLyricsStore,
    private val json: Json,
) : DownloadsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @kotlin.OptIn(ExperimentalCoroutinesApi::class)
    override val downloads: Flow<List<SongDownload>> = callbackFlow {
        val listener = object : DownloadManager.Listener {
            override fun onInitialized(downloadManager: DownloadManager) {
                trySend(Unit)
            }

            override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                trySend(Unit)
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                trySend(Unit)
            }
        }
        downloadManager.addListener(listener)
        trySend(Unit)
        awaitClose { downloadManager.removeListener(listener) }
    }
        .flowOn(Dispatchers.Main)
        .conflate()
        .transformLatest {
            do {
                val songs = snapshot()
                emit(songs)
                val anyDownloading = songs.any { song -> song.state == DownloadState.Downloading }
                if (anyDownloading) delay(PROGRESS_REFRESH_MILLIS)
            } while (anyDownloading)
        }

    override fun download(tracks: List<Track>) {
        scope.launch {
            val pending = snapshot()
                .filter { song -> song.state != DownloadState.Failed }
                .mapTo(HashSet()) { song -> song.track.id }
            val newTracks = tracks.distinctBy(Track::id).filterNot { track -> track.id in pending }
            newTracks.forEach { track ->
                DownloadService.sendAddDownload(context, FlixDownloadService::class.java, track.toDownloadRequest(), false)
            }
            newTracks.forEach { track -> saveLyrics(track) }
        }
    }

    override fun remove(trackId: String) {
        DownloadService.sendRemoveDownload(context, FlixDownloadService::class.java, trackId, false)
        scope.launch { savedLyricsStore.delete(trackId) }
    }

    override fun storage(): DownloadStorage = DownloadStorage(downloadCache.cacheSpace, context.filesDir.totalSpace)

    /** Stored downloads with the live progress of the ones in flight, which only the manager knows. */
    private suspend fun snapshot(): List<SongDownload> {
        val inFlight = withContext(Dispatchers.Main) { downloadManager.currentDownloads }.associateBy { download -> download.request.id }
        val stored = withContext(Dispatchers.IO) {
            downloadManager.downloadIndex.getDownloads().use { cursor ->
                buildList { while (cursor.moveToNext()) add(cursor.download) }
            }
        }
        return stored
            .map { download -> inFlight[download.request.id] ?: download }
            .sortedByDescending(Download::startTimeMs)
            .mapNotNull { download -> download.toSongDownload() }
    }

    private suspend fun saveLyrics(track: Track) {
        val lyrics = try {
            lyricsRepository.lyrics(track)
        } catch (exception: FlixException) {
            null
        } ?: return
        savedLyricsStore.write(track.id, lyrics)
    }

    private fun Track.toDownloadRequest(): DownloadRequest =
        DownloadRequest.Builder(id, youTubeWatchUrl(id).toUri())
            .setMimeType(MimeTypes.AUDIO_MP4)
            .setData(json.encodeToString(toStoredTrack()).encodeToByteArray())
            .build()

    private fun Download.toSongDownload(): SongDownload? {
        val songState = when (state) {
            Download.STATE_QUEUED, Download.STATE_STOPPED -> DownloadState.Queued
            Download.STATE_DOWNLOADING, Download.STATE_RESTARTING -> DownloadState.Downloading
            Download.STATE_COMPLETED -> DownloadState.Downloaded
            Download.STATE_FAILED -> DownloadState.Failed
            else -> return null
        }
        val track = runCatching { json.decodeFromString<StoredTrack>(request.data.decodeToString()).toTrack() }.getOrNull()
            ?: return null
        return SongDownload(track, songState, percentDownloaded.coerceAtLeast(0f).toInt())
    }

    private companion object {
        const val PROGRESS_REFRESH_MILLIS = 500L
    }
}
