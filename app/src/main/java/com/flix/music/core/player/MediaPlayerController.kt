package com.flix.music.core.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.flix.music.core.data.repository.HistoryRepository
import com.flix.music.core.data.repository.MusicRepository
import com.flix.music.core.model.Track
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * [PlayerController] backed by a Media3 [MediaController] connected to [PlaybackService].
 *
 * Error handling follows the design: a failed track is retried once silently; if it
 * fails again it is skipped with a [PlayerEvent.TrackSkipped] snackbar, and the last
 * track of the queue stays in [PlaybackStatus.Error] with a retry action.
 * The next track is prefetched through `/stream` so skips start instantly, and every
 * track is added to the history the first time it actually plays.
 */
@Singleton
class MediaPlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository,
    private val historyRepository: HistoryRepository,
) : PlayerController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlayerEvent>(extraBufferCapacity = 1)
    override val events: Flow<PlayerEvent> = _events.asSharedFlow()

    private var connectedController: MediaController? = null
    private val controller: Deferred<MediaController> = scope.async { connect() }

    private var source: PlaybackSource? = null
    private var queuedTracks: Map<String, Track> = emptyMap()
    private var recordedCurrentTrack = false
    private var currentTrackWasReady = false
    private var retriedCurrentTrack = false
    private var recoveringFromError = false
    private var positionUpdates: Job? = null

    private val listener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            currentTrackWasReady = false
            retriedCurrentTrack = false
            recordedCurrentTrack = false
            prefetchNextTrack()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                currentTrackWasReady = true
                recoveringFromError = false
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            recoverFromError()
        }

        override fun onEvents(player: Player, events: Player.Events) {
            publishState(player)
            updatePositionTicker(player)
            recordInHistoryOnFirstPlay()
        }
    }

    override fun play(tracks: List<Track>, startIndex: Int, source: PlaybackSource, shuffle: Boolean) {
        if (tracks.isEmpty()) return
        withController { mediaController -> startQueue(mediaController, tracks, startIndex, source, shuffle) }
    }

    override fun playNext(track: Track) = enqueue(track) { mediaController ->
        mediaController.currentMediaItemIndex + 1
    }

    override fun addToQueue(track: Track) = enqueue(track) { mediaController -> mediaController.mediaItemCount }

    private fun enqueue(track: Track, insertIndex: (MediaController) -> Int) = withController { mediaController ->
        if (mediaController.mediaItemCount == 0) {
            startQueue(mediaController, listOf(track), startIndex = 0, source = null, shuffle = false)
            return@withController
        }
        queuedTracks = queuedTracks + (track.id to track)
        mediaController.addMediaItem(insertIndex(mediaController), track.toMediaItem())
    }

    override fun skipToQueueItem(index: Int) = withController { mediaController ->
        if (index in 0 until mediaController.mediaItemCount) {
            mediaController.seekTo(index, 0L)
            mediaController.play()
        }
    }

    override fun removeQueueItem(index: Int) = withController { mediaController ->
        if (index in 0 until mediaController.mediaItemCount && index != mediaController.currentMediaItemIndex) {
            mediaController.removeMediaItem(index)
        }
    }

    override fun restoreQueueItem(index: Int, track: Track) = withController { mediaController ->
        queuedTracks = queuedTracks + (track.id to track)
        mediaController.addMediaItem(index.coerceIn(0, mediaController.mediaItemCount), track.toMediaItem())
    }

    override fun moveQueueItem(fromIndex: Int, toIndex: Int) = withController { mediaController ->
        val indices = 0 until mediaController.mediaItemCount
        if (fromIndex in indices && toIndex in indices) mediaController.moveMediaItem(fromIndex, toIndex)
    }

    override fun clearUpNext() = withController { mediaController ->
        val currentIndex = mediaController.currentMediaItemIndex
        mediaController.removeMediaItems(currentIndex + 1, mediaController.mediaItemCount)
        mediaController.removeMediaItems(0, currentIndex)
    }

    override fun togglePlayPause() = withController { mediaController ->
        when {
            mediaController.playbackState == Player.STATE_ENDED || mediaController.playerError != null -> retry()
            mediaController.playWhenReady -> mediaController.pause()
            else -> mediaController.play()
        }
    }

    override fun seekTo(position: Duration) = withController { mediaController ->
        mediaController.seekTo(position.inWholeMilliseconds)
    }

    override fun skipToNext() = withController { mediaController -> mediaController.seekToNext() }

    override fun skipToPrevious() = withController { mediaController -> mediaController.seekToPrevious() }

    override fun toggleShuffle() = withController { mediaController ->
        mediaController.shuffleModeEnabled = !mediaController.shuffleModeEnabled
    }

    override fun cycleRepeatMode() = withController { mediaController ->
        mediaController.repeatMode = when (mediaController.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    override fun retry() = withController { mediaController ->
        if (mediaController.playbackState == Player.STATE_ENDED) {
            mediaController.seekTo(0, 0L)
        }
        retriedCurrentTrack = false
        mediaController.prepare()
        mediaController.play()
    }

    private fun startQueue(
        mediaController: MediaController,
        tracks: List<Track>,
        startIndex: Int,
        source: PlaybackSource?,
        shuffle: Boolean,
    ) {
        this.source = source
        queuedTracks = tracks.associateBy { it.id }
        mediaController.shuffleModeEnabled = shuffle
        mediaController.setMediaItems(tracks.map { it.toMediaItem() }, startIndex.coerceIn(tracks.indices), 0L)
        mediaController.prepare()
        mediaController.play()
    }

    private suspend fun connect(): MediaController {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val mediaController = MediaController.Builder(context, sessionToken).buildAsync().await()
        mediaController.addListener(listener)
        connectedController = mediaController
        publishState(mediaController)
        updatePositionTicker(mediaController)
        return mediaController
    }

    private fun withController(action: (MediaController) -> Unit) {
        scope.launch {
            runCatching { controller.await() }.onSuccess(action)
        }
    }

    private fun recoverFromError() {
        val mediaController = connectedController ?: return
        when {
            !retriedCurrentTrack -> {
                retriedCurrentTrack = true
                recoveringFromError = true
                mediaController.prepare()
                mediaController.play()
            }
            mediaController.hasNextMediaItem() -> {
                recoveringFromError = true
                val skippedTitle = mediaController.currentMediaItem?.mediaMetadata?.title?.toString().orEmpty()
                _events.tryEmit(PlayerEvent.TrackSkipped(skippedTitle))
                mediaController.seekToNextMediaItem()
                mediaController.prepare()
                mediaController.play()
            }
            else -> recoveringFromError = false
        }
    }

    private fun prefetchNextTrack() {
        val mediaController = connectedController ?: return
        if (!mediaController.hasNextMediaItem()) return
        val nextTrackId = mediaController.getMediaItemAt(mediaController.nextMediaItemIndex).mediaId
        scope.launch {
            runCatching { repository.prefetch(nextTrackId) }
        }
    }

    private fun recordInHistoryOnFirstPlay() {
        val currentState = _state.value
        val track = currentState.current ?: return
        if (recordedCurrentTrack || currentState.status != PlaybackStatus.Playing) return
        recordedCurrentTrack = true
        scope.launch { runCatching { historyRepository.record(track) } }
    }

    private fun publishState(player: Player) {
        val currentItem = player.currentMediaItem
        val nextItem = if (player.hasNextMediaItem()) player.getMediaItemAt(player.nextMediaItemIndex) else null
        _state.value = PlayerState(
            status = playbackStatusOf(
                hasMedia = currentItem != null,
                playbackState = player.playbackState,
                playWhenReady = player.playWhenReady,
                hasError = player.playerError != null && !recoveringFromError,
                currentTrackWasReady = currentTrackWasReady,
            ),
            current = currentItem?.toTrack(queuedTracks),
            next = nextItem?.toTrack(queuedTracks),
            upNext = upNextOf(player),
            source = source,
            position = player.currentPosition.coerceAtLeast(0L).milliseconds,
            bufferedPosition = player.bufferedPosition.coerceAtLeast(0L).milliseconds,
            duration = player.duration.takeIf { it != C.TIME_UNSET && it > 0 }?.milliseconds,
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = when (player.repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.All
                Player.REPEAT_MODE_ONE -> RepeatMode.One
                else -> RepeatMode.Off
            },
            hasPrevious = player.hasPreviousMediaItem(),
            hasNext = nextItem != null,
        )
    }

    /** Walks the timeline in play order from the current item, so shuffle is respected. */
    private fun upNextOf(player: Player): List<QueueItem> {
        val timeline = player.currentTimeline
        if (timeline.isEmpty) return emptyList()
        val upNext = mutableListOf<QueueItem>()
        var index = timeline.getNextWindowIndex(player.currentMediaItemIndex, Player.REPEAT_MODE_OFF, player.shuffleModeEnabled)
        while (index != C.INDEX_UNSET) {
            upNext += QueueItem(index, player.getMediaItemAt(index).toTrack(queuedTracks))
            index = timeline.getNextWindowIndex(index, Player.REPEAT_MODE_OFF, player.shuffleModeEnabled)
        }
        return upNext
    }

    /** Media3 has no position callback, so the position is polled while audio plays. */
    private fun updatePositionTicker(player: Player) {
        if (player.isPlaying) {
            if (positionUpdates?.isActive == true) return
            positionUpdates = scope.launch {
                while (isActive) {
                    publishState(player)
                    delay(POSITION_UPDATE_INTERVAL)
                }
            }
        } else {
            positionUpdates?.cancel()
            positionUpdates = null
        }
    }

    private suspend fun <Result> ListenableFuture<Result>.await(): Result =
        suspendCancellableCoroutine { continuation ->
            addListener(
                {
                    runCatching { get() }
                        .onSuccess { result -> continuation.resume(result) }
                        .onFailure { failure -> continuation.resumeWithException(failure) }
                },
                MoreExecutors.directExecutor(),
            )
        }

    private companion object {
        val POSITION_UPDATE_INTERVAL = 250.milliseconds
    }
}
