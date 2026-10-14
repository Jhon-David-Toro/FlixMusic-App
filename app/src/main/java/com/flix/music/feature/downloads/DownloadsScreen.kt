package com.flix.music.feature.downloads

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.R
import com.flix.music.core.designsystem.component.ConfirmDialog
import com.flix.music.core.designsystem.component.FlixTopAppBar
import com.flix.music.core.designsystem.component.NowPlayingMarker
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.DownloadState
import com.flix.music.core.model.DownloadStorage
import com.flix.music.core.model.SongDownload
import com.flix.music.core.model.Track
import com.flix.music.core.ui.LocalTrackMenu

/** Every user action on the Descargas screen. Defaults do nothing, which keeps previews short. */
data class DownloadsActions(
    val onBack: () -> Unit = {},
    val onDownloadClick: (SongDownload) -> Unit = {},
    val onRetry: (SongDownload) -> Unit = {},
    val onRemove: (SongDownload) -> Unit = {},
)

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DownloadsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack) {
        DownloadsActions(
            onBack = onBack,
            onDownloadClick = viewModel::onDownloadClick,
            onRetry = viewModel::onRetry,
            onRemove = viewModel::onRemove,
        )
    }
    DownloadsContent(
        uiState = uiState,
        actions = actions,
        nowPlaying = nowPlaying,
        modifier = modifier,
    )
}

/**
 * Descargas (design `downloads`): one row per song with its state on the right, which is
 * also its action (remove, cancel or retry), and the space used at the bottom.
 */
@Composable
fun DownloadsContent(
    uiState: DownloadsUiState,
    actions: DownloadsActions,
    modifier: Modifier = Modifier,
    nowPlaying: NowPlayingMarker = NowPlayingMarker(),
) {
    var confirmingRemoval by rememberSaveable { mutableStateOf<String?>(null) }
    val content = uiState as? DownloadsUiState.Content

    Column(modifier.fillMaxSize()) {
        FlixTopAppBar(title = stringResource(R.string.profile_downloads), onBack = actions.onBack)
        when {
            content == null -> Unit
            content.downloads.isEmpty() -> StateView(
                icon = R.drawable.ic_download,
                title = stringResource(R.string.downloads_empty_title),
                body = stringResource(R.string.downloads_empty_body),
            )
            else -> LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = FlixTheme.spacing.xxl)) {
                items(content.downloads, key = { download -> download.track.id }) { download ->
                    DownloadRow(
                        download = download,
                        nowPlaying = nowPlaying,
                        onClick = { actions.onDownloadClick(download) },
                        onAction = {
                            when (download.state) {
                                DownloadState.Downloaded -> confirmingRemoval = download.track.id
                                DownloadState.Failed -> actions.onRetry(download)
                                DownloadState.Queued, DownloadState.Downloading -> actions.onRemove(download)
                            }
                        },
                        modifier = Modifier.animateItem(),
                    )
                }
                item(key = "storage") {
                    StorageUsage(
                        storage = content.storage,
                        modifier = Modifier
                            .padding(horizontal = FlixTheme.spacing.gutter)
                            .padding(top = 18.dp),
                    )
                }
            }
        }
    }

    val removing = content?.downloads?.firstOrNull { download -> download.track.id == confirmingRemoval }
    if (removing != null) {
        ConfirmDialog(
            title = stringResource(R.string.downloads_remove_title),
            message = stringResource(R.string.downloads_remove_message, removing.track.title),
            confirmLabel = stringResource(R.string.download_remove),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = {
                confirmingRemoval = null
                actions.onRemove(removing)
            },
            onDismiss = { confirmingRemoval = null },
        )
    }
}

@Composable
private fun DownloadRow(
    download: SongDownload,
    nowPlaying: NowPlayingMarker,
    onClick: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackMenu = LocalTrackMenu.current
    val track = download.track
    TrackRow(
        title = track.title,
        subtitle = when (download.state) {
            DownloadState.Downloaded -> stringResource(R.string.downloads_state_downloaded)
            DownloadState.Downloading -> stringResource(R.string.downloads_state_downloading, download.percent)
            DownloadState.Queued -> stringResource(R.string.downloads_state_queued)
            DownloadState.Failed -> stringResource(R.string.downloads_state_failed)
        },
        subtitleColor = if (download.state == DownloadState.Failed) FlixTheme.colors.error else FlixTheme.colors.textSecondary,
        artworkUrl = track.artworkUrl,
        onClick = onClick,
        onLongClick = { trackMenu.open(track) },
        isCurrent = track.id == nowPlaying.trackId,
        isPlaying = nowPlaying.isPlaying,
        modifier = modifier,
        trailing = { DownloadStateButton(download, onAction) },
    )
}

/** The state icon on the right of a row (design: the five download states). */
@Composable
private fun DownloadStateButton(download: SongDownload, onClick: () -> Unit) {
    val title = download.track.title
    IconButton(onClick = onClick) {
        when (download.state) {
            DownloadState.Downloaded -> Icon(
                painter = painterResource(R.drawable.ic_download_done_filled),
                contentDescription = stringResource(R.string.downloads_remove, title),
                tint = FlixTheme.colors.secondary,
            )
            DownloadState.Downloading -> {
                val cancelLabel = stringResource(R.string.downloads_cancel, title)
                CircularProgressIndicator(
                    progress = { download.percent / 100f },
                    modifier = Modifier
                        .size(PROGRESS_RING_SIZE)
                        .semantics { contentDescription = cancelLabel },
                    color = FlixTheme.colors.primary,
                    trackColor = FlixTheme.colors.surfaceVariant,
                    strokeWidth = PROGRESS_RING_STROKE,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                )
            }
            DownloadState.Queued -> Icon(
                painter = painterResource(R.drawable.ic_schedule),
                contentDescription = stringResource(R.string.downloads_cancel, title),
                tint = FlixTheme.colors.textSecondary,
            )
            DownloadState.Failed -> Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = stringResource(R.string.downloads_retry, title),
                tint = FlixTheme.colors.error,
            )
        }
    }
}

/** "Almacenamiento · 1,2 GB de 8 GB" over a steel beam (design `.beam`). */
@Composable
private fun StorageUsage(storage: DownloadStorage, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.downloads_storage),
                style = FlixTheme.typography.caption,
                color = FlixTheme.colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    R.string.downloads_storage_value,
                    Formatter.formatShortFileSize(context, storage.usedBytes),
                    Formatter.formatShortFileSize(context, storage.totalBytes),
                ),
                style = FlixTheme.typography.caption,
                color = FlixTheme.colors.textSecondary,
            )
        }
        Box(
            Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(BEAM_HEIGHT)
                .clip(FlixTheme.shapes.xs)
                .background(FlixTheme.colors.surfaceVariant),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(storage.usedFraction)
                    .fillMaxHeight()
                    .background(FlixTheme.colors.secondary),
            )
        }
    }
}

private val PROGRESS_RING_SIZE = 22.dp
private val PROGRESS_RING_STROKE = 2.5.dp
private val BEAM_HEIGHT = 3.dp

private fun previewDownload(id: String, title: String, artist: String, state: DownloadState, percent: Int = 0) =
    SongDownload(Track(id, title, listOf(ArtistRef(null, artist)), null, null, null), state, percent)

@Preview(name = "Descargas", widthDp = 360, heightDp = 640)
@Composable
private fun DownloadsPreview() {
    FlixPreview {
        DownloadsContent(
            uiState = DownloadsUiState.Content(
                downloads = listOf(
                    previewDownload("1", "Fade To Black", "Metallica", DownloadState.Downloaded, 100),
                    previewDownload("2", "Killshot", "Eminem", DownloadState.Downloading, 45),
                    previewDownload("3", "Tití Me Preguntó", "Bad Bunny", DownloadState.Queued),
                    previewDownload("4", "Midnight City", "M83", DownloadState.Failed),
                ),
                storage = DownloadStorage(usedBytes = 1_200_000_000, totalBytes = 8_000_000_000),
            ),
            actions = DownloadsActions(),
        )
    }
}

@Preview(name = "Descargas · vacío", widthDp = 360, heightDp = 640)
@Composable
private fun DownloadsEmptyPreview() {
    FlixPreview {
        DownloadsContent(
            uiState = DownloadsUiState.Content(emptyList(), DownloadStorage(0, 0)),
            actions = DownloadsActions(),
        )
    }
}
