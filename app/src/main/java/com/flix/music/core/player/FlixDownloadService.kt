package com.flix.music.core.player

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import com.flix.music.MainActivity
import com.flix.music.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Keeps downloads going with the app in the background, showing their progress. */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class FlixDownloadService : DownloadService(
    NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.downloads_channel_name,
    0,
) {

    @Inject
    lateinit var injectedDownloadManager: DownloadManager

    private val notificationHelper by lazy { DownloadNotificationHelper(this, CHANNEL_ID) }

    override fun getDownloadManager(): DownloadManager = injectedDownloadManager

    override fun getScheduler(): Scheduler? = null

    override fun getForegroundNotification(downloads: List<Download>, notMetRequirements: Int): Notification =
        notificationHelper.buildProgressNotification(
            this,
            R.drawable.ic_download,
            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE),
            null,
            downloads,
            notMetRequirements,
        )

    private companion object {
        const val NOTIFICATION_ID = 2
        const val CHANNEL_ID = "downloads"
    }
}
