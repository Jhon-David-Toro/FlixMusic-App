package com.flix.music.feature.playlist

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.R
import com.flix.music.core.designsystem.component.ConfirmDialog
import com.flix.music.core.designsystem.component.FlixBottomSheet
import com.flix.music.core.designsystem.component.FlixTopAppBar
import com.flix.music.core.designsystem.component.NowPlayingMarker
import com.flix.music.core.designsystem.component.PlayPauseButton
import com.flix.music.core.designsystem.component.PlaylistArtwork
import com.flix.music.core.designsystem.component.SheetQuickAction
import com.flix.music.core.designsystem.component.SheetQuickActions
import com.flix.music.core.designsystem.component.SheetRow
import com.flix.music.core.designsystem.component.SheetTitle
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.component.util.toReadableLength
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Playlist
import com.flix.music.core.model.Track
import com.flix.music.core.model.totalDuration
import com.flix.music.core.ui.LocalTrackMenu
import com.flix.music.core.ui.open
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

/** Every user action on the Playlist screen. Defaults do nothing, which keeps previews short. */
data class PlaylistActions(
    val onBack: () -> Unit = {},
    val onEdit: () -> Unit = {},
    val onBrowseMusic: () -> Unit = {},
    val onPlay: () -> Unit = {},
    val onShuffle: () -> Unit = {},
    val onTrackClick: (Track) -> Unit = {},
    val onTrackMore: (Track) -> Unit = {},
    val onShare: () -> Unit = {},
    val onDelete: () -> Unit = {},
)

@Composable
fun PlaylistScreen(
    onBack: () -> Unit,
    onEdit: (playlistId: Long) -> Unit,
    onBrowseMusic: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val trackMenu = LocalTrackMenu.current
    val context = LocalContext.current
    val playlist = (uiState as? PlaylistUiState.Content)?.playlist

    LaunchedEffect(uiState) {
        if (uiState == PlaylistUiState.Missing) onBack()
    }

    val actions = PlaylistActions(
        onBack = onBack,
        onEdit = { playlist?.let { onEdit(it.id) } },
        onBrowseMusic = onBrowseMusic,
        onPlay = viewModel::onPlay,
        onShuffle = viewModel::onShuffle,
        onTrackClick = viewModel::onTrackClick,
        onTrackMore = { track -> trackMenu.open(track) },
        onShare = { playlist?.let(context::sharePlaylist) },
        onDelete = viewModel::onDelete,
    )
    PlaylistContent(
        uiState = uiState,
        actions = actions,
        nowPlaying = NowPlayingMarker(playerState.current?.id, playerState.isActive),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistContent(
    uiState: PlaylistUiState,
    actions: PlaylistActions,
    modifier: Modifier = Modifier,
    nowPlaying: NowPlayingMarker = NowPlayingMarker(),
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    val content = uiState as? PlaylistUiState.Content

    Column(modifier.fillMaxSize()) {
        FlixTopAppBar(
            title = "",
            onBack = actions.onBack,
            actions = {
                if (content != null) {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.action_more_options),
                            tint = FlixTheme.colors.textPrimary,
                        )
                    }
                }
            },
        )
        if (content == null) return@Column
        val playlist = content.playlist

        LazyColumn(contentPadding = PaddingValues(bottom = FlixTheme.spacing.xxl)) {
            item(key = "header") { PlaylistHeader(content, actions) }
            if (playlist.tracks.isEmpty()) {
                item(key = "empty") {
                    StateView(
                        icon = R.drawable.ic_playlist_add,
                        title = stringResource(R.string.playlist_empty_title),
                        body = stringResource(R.string.playlist_empty_body),
                        actionLabel = stringResource(R.string.action_browse_music),
                        onAction = actions.onBrowseMusic,
                    )
                }
            }
            items(playlist.tracks, key = { track -> track.id }) { track ->
                TrackRow(
                    title = track.title,
                    subtitle = track.artistNames,
                    artworkUrl = track.artworkUrl,
                    onClick = { actions.onTrackClick(track) },
                    isCurrent = track.id == nowPlaying.trackId,
                    isPlaying = nowPlaying.isPlaying,
                    onMoreClick = { actions.onTrackMore(track) },
                    modifier = Modifier.animateItem(),
                )
            }
        }

        if (menuOpen) {
            PlaylistMenu(
                playlist = playlist,
                actions = actions,
                onRequestDelete = { confirmingDelete = true },
                onDismiss = { menuOpen = false },
            )
        }
        if (confirmingDelete) {
            ConfirmDialog(
                title = stringResource(R.string.playlist_delete_title, playlist.name),
                message = stringResource(R.string.playlist_delete_message),
                confirmLabel = stringResource(R.string.playlist_delete_confirm),
                dismissLabel = stringResource(R.string.action_cancel),
                destructive = true,
                onConfirm = {
                    confirmingDelete = false
                    actions.onDelete()
                },
                onDismiss = { confirmingDelete = false },
            )
        }
    }
}

/** Mosaic cover, name, description, "Creada por ti · N canciones · length" and actions. */
@Composable
private fun PlaylistHeader(content: PlaylistUiState.Content, actions: PlaylistActions) {
    val playlist = content.playlist
    val coverShape = RoundedCornerShape(COVER_RADIUS)
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FlixTheme.spacing.xs, bottom = FlixTheme.spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            PlaylistArtwork(
                coverUrls = playlist.coverUrls,
                size = COVER_SIZE,
                cornerRadius = COVER_RADIUS,
                stacked = playlist.coverUrls.isEmpty(),
                placeholderIconSize = 56.dp,
                modifier = if (playlist.coverUrls.isEmpty()) Modifier else Modifier.shadow(8.dp, coverShape),
            )
        }
        Column(Modifier.padding(horizontal = FlixTheme.spacing.gutter)) {
            Text(
                text = playlist.name,
                style = FlixTheme.typography.headline,
                color = FlixTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            playlist.description?.let { description ->
                Text(
                    text = description,
                    style = FlixTheme.typography.caption,
                    color = FlixTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Text(
                text = playlistSummary(playlist.tracks.size, content.totalDuration.toReadableLength()),
                style = FlixTheme.typography.caption,
                color = FlixTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 6.dp),
            )
            if (playlist.tracks.isNotEmpty()) {
                PlaylistActionsRow(actions)
            }
        }
    }
}

@Composable
private fun playlistSummary(trackCount: Int, length: String): String =
    if (trackCount == 0) {
        stringResource(R.string.playlist_summary_empty)
    } else {
        pluralStringResource(R.plurals.playlist_summary, trackCount, trackCount, length)
    }

@Composable
private fun PlaylistActionsRow(actions: PlaylistActions) {
    Row(
        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FlixTheme.spacing.xs),
    ) {
        IconButton(onClick = actions.onEdit) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.playlist_edit),
                tint = FlixTheme.colors.textPrimary,
            )
        }
        IconButton(onClick = actions.onShare) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = stringResource(R.string.action_share),
                tint = FlixTheme.colors.textPrimary,
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = actions.onShuffle) {
            Icon(
                painter = painterResource(R.drawable.ic_shuffle),
                contentDescription = stringResource(R.string.player_shuffle),
                tint = FlixTheme.colors.textPrimary,
            )
        }
        PlayPauseButton(
            icon = R.drawable.ic_play_arrow_filled,
            contentDescription = stringResource(R.string.playlist_play),
            onClick = actions.onPlay,
            size = PLAY_BUTTON_SIZE,
            iconSize = PLAY_ICON_SIZE,
        )
    }
}

/** Playlist menu (design "Menú: Playlist"): Reproducir · Aleatorio · Editar, then Compartir and Eliminar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistMenu(
    playlist: Playlist,
    actions: PlaylistActions,
    onRequestDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val closeThen: (() -> Unit) -> Unit = { action ->
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
            action()
        }
    }
    FlixBottomSheet(onDismiss = onDismiss, sheetState = sheetState) {
        SheetTitle(playlist.name)
        SheetQuickActions {
            SheetQuickAction(
                icon = R.drawable.ic_play_arrow_filled,
                label = stringResource(R.string.playlist_play),
                onClick = { closeThen(actions.onPlay) },
            )
            SheetQuickAction(
                icon = R.drawable.ic_shuffle,
                label = stringResource(R.string.player_shuffle),
                onClick = { closeThen(actions.onShuffle) },
            )
            SheetQuickAction(
                icon = R.drawable.ic_edit,
                label = stringResource(R.string.playlist_edit_short),
                onClick = { closeThen(actions.onEdit) },
            )
        }
        SheetRow(
            icon = R.drawable.ic_share,
            label = stringResource(R.string.action_share),
            onClick = { closeThen(actions.onShare) },
        )
        SheetRow(
            icon = R.drawable.ic_delete,
            label = stringResource(R.string.playlist_delete),
            contentColor = FlixTheme.colors.error,
            iconTint = FlixTheme.colors.error,
            onClick = { closeThen(onRequestDelete) },
        )
    }
}

/** Shares the playlist as text: its name followed by one "Title — Artists" line per song. */
private fun Context.sharePlaylist(playlist: Playlist) {
    val lines = playlist.tracks.joinToString("\n") { track -> "${track.title} — ${track.artistNames}" }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, playlist.name)
        putExtra(Intent.EXTRA_TEXT, listOf(playlist.name, lines).filter { it.isNotBlank() }.joinToString("\n\n"))
    }
    startActivity(Intent.createChooser(sendIntent, null))
}

private val COVER_SIZE = 184.dp
private val COVER_RADIUS = 14.dp
private val PLAY_BUTTON_SIZE = 52.dp
private val PLAY_ICON_SIZE = 28.dp

private val previewTracks = listOf(
    Track("1", "Midnight City", listOf(ArtistRef(null, "M83")), null, 243.seconds, null),
    Track("2", "Killshot", listOf(ArtistRef(null, "Eminem")), null, 253.seconds, null),
    Track("3", "Tití Me Preguntó", listOf(ArtistRef(null, "Bad Bunny")), null, 243.seconds, null),
)

@Preview(name = "Playlist", widthDp = 360, heightDp = 760)
@Composable
private fun PlaylistPreview() {
    FlixPreview {
        val playlist = Playlist(1, "Noches de ruta", "Para conducir cuando ya no hay tráfico.", previewTracks, Instant.EPOCH)
        PlaylistContent(
            uiState = PlaylistUiState.Content(playlist, previewTracks.totalDuration()),
            actions = PlaylistActions(),
        )
    }
}

@Preview(name = "Playlist vacía", widthDp = 360, heightDp = 760)
@Composable
private fun PlaylistEmptyPreview() {
    FlixPreview {
        val playlist = Playlist(2, "Para correr", null, emptyList(), Instant.EPOCH)
        PlaylistContent(
            uiState = PlaylistUiState.Content(playlist, 0.seconds),
            actions = PlaylistActions(),
        )
    }
}
