package com.flix.music.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.R
import com.flix.music.core.designsystem.component.ConfirmDialog
import com.flix.music.core.designsystem.component.FlixTopAppBar
import com.flix.music.core.designsystem.component.LocalSnackbarHostState
import com.flix.music.core.designsystem.component.NowPlayingMarker
import com.flix.music.core.designsystem.component.StateView
import com.flix.music.core.designsystem.component.TrackRow
import com.flix.music.core.designsystem.component.showUndo
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.HistoryDay
import com.flix.music.core.model.HistoryDayLabel
import com.flix.music.core.model.HistoryEntry
import com.flix.music.core.model.Track
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Every user action on the History screen. Defaults do nothing, which keeps previews short. */
data class HistoryActions(
    val onBack: () -> Unit = {},
    val onBrowseMusic: () -> Unit = {},
    val onEntryClick: (HistoryEntry) -> Unit = {},
    val onRemove: (HistoryEntry) -> Unit = {},
    val onUndoRemove: (HistoryEntry) -> Unit = {},
    val onClearHistory: () -> Unit = {},
)

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onBrowseMusic: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack, onBrowseMusic) {
        HistoryActions(
            onBack = onBack,
            onBrowseMusic = onBrowseMusic,
            onEntryClick = viewModel::onEntryClick,
            onRemove = viewModel::onRemove,
            onUndoRemove = viewModel::onUndoRemove,
            onClearHistory = viewModel::onClearHistory,
        )
    }
    HistoryContent(
        uiState = uiState,
        actions = actions,
        nowPlaying = NowPlayingMarker(playerState.current?.id, playerState.isActive),
        modifier = modifier,
    )
}

@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    actions: HistoryActions,
    modifier: Modifier = Modifier,
    nowPlaying: NowPlayingMarker = NowPlayingMarker(),
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmingClear by rememberSaveable { mutableStateOf(false) }
    val days = (uiState as? HistoryUiState.Content)?.days
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val removedMessage = stringResource(R.string.history_entry_removed)
    val undoLabel = stringResource(R.string.action_undo)
    val removeLabel = stringResource(R.string.history_remove_entry)

    val removeWithUndo: (HistoryEntry) -> Unit = { entry ->
        actions.onRemove(entry)
        coroutineScope.launch {
            snackbarHostState.showUndo(removedMessage, undoLabel) { actions.onUndoRemove(entry) }
        }
    }

    Column(modifier.fillMaxSize()) {
        FlixTopAppBar(
            title = stringResource(R.string.history_title),
            onBack = actions.onBack,
            actions = {
                if (!days.isNullOrEmpty()) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
                                contentDescription = stringResource(R.string.action_more_options),
                                tint = FlixTheme.colors.textPrimary,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = FlixTheme.colors.surfaceElevated,
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.history_clear), color = FlixTheme.colors.textPrimary)
                                },
                                onClick = {
                                    menuExpanded = false
                                    confirmingClear = true
                                },
                            )
                        }
                    }
                }
            },
        )

        when {
            days == null -> Unit
            days.isEmpty() -> StateView(
                icon = R.drawable.ic_history,
                title = stringResource(R.string.history_empty_title),
                body = stringResource(R.string.history_empty_body),
                actionLabel = stringResource(R.string.action_explore_genres),
                onAction = actions.onBrowseMusic,
            )
            else -> LazyColumn(contentPadding = PaddingValues(bottom = FlixTheme.spacing.xxl)) {
                days.forEach { day ->
                    stickyHeader(key = "day-${day.entries.first().id}") { DayHeader(day.label) }
                    items(day.entries, key = { it.id }) { entry ->
                        SwipeToRemove(
                            onRemove = { removeWithUndo(entry) },
                            modifier = Modifier.animateItem(),
                        ) {
                            TrackRow(
                                title = entry.track.title,
                                subtitle = entry.track.artistNames,
                                artworkUrl = entry.track.artworkUrl,
                                onClick = { actions.onEntryClick(entry) },
                                isCurrent = entry.track.id == nowPlaying.trackId,
                                isPlaying = nowPlaying.isPlaying,
                                modifier = Modifier
                                    .background(FlixTheme.colors.background)
                                    .semantics {
                                        customActions = listOf(
                                            CustomAccessibilityAction(removeLabel) {
                                                removeWithUndo(entry)
                                                true
                                            },
                                        )
                                    },
                                trailing = {
                                    Text(
                                        text = entry.playedAt.formatTime(),
                                        style = FlixTheme.typography.caption,
                                        color = FlixTheme.colors.textTertiary,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmingClear) {
        ConfirmDialog(
            title = stringResource(R.string.history_clear_title),
            message = stringResource(R.string.history_clear_message),
            confirmLabel = stringResource(R.string.history_clear_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = {
                confirmingClear = false
                actions.onClearHistory()
            },
            onDismiss = { confirmingClear = false },
        )
    }
}

/** Sticky day header (design: overline "HOY", "AYER", "LUNES, 21 SEP"). */
@Composable
private fun DayHeader(label: HistoryDayLabel) {
    val text = when (label) {
        HistoryDayLabel.Today -> stringResource(R.string.history_today)
        HistoryDayLabel.Yesterday -> stringResource(R.string.history_yesterday)
        is HistoryDayLabel.Date -> label.date.format(DAY_FORMATTER)
    }
    Text(
        text = text.uppercase(),
        style = FlixTheme.typography.overline,
        color = FlixTheme.colors.textSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .background(FlixTheme.colors.background)
            .padding(horizontal = FlixTheme.spacing.gutter)
            .padding(top = FlixTheme.spacing.lg, bottom = FlixTheme.spacing.xs)
            .semantics { heading() },
    )
}

/**
 * Swipe end-to-start to remove (design Gestures): red error background with a delete
 * icon. TalkBack users get the same action through the row's custom actions.
 */
@Composable
private fun SwipeToRemove(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        onDismiss = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) onRemove()
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FlixTheme.colors.error)
                    .padding(horizontal = FlixTheme.spacing.xxl),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        },
    ) {
        content()
    }
}

private fun Instant.formatTime(): String =
    atZone(ZoneId.systemDefault()).toLocalTime().format(TIME_FORMATTER)

private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

private val DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault())

private fun previewEntry(id: Long, title: String, artist: String) =
    HistoryEntry(id, Track(id.toString(), title, listOf(ArtistRef(null, artist)), null, null, null), Instant.EPOCH)

@Preview(name = "Historial", widthDp = 360, heightDp = 700)
@Composable
private fun HistoryPreview() {
    FlixPreview {
        HistoryContent(
            uiState = HistoryUiState.Content(
                listOf(
                    HistoryDay(
                        HistoryDayLabel.Today,
                        listOf(previewEntry(1, "Fade To Black", "Metallica"), previewEntry(2, "Killshot", "Eminem")),
                    ),
                    HistoryDay(HistoryDayLabel.Yesterday, listOf(previewEntry(3, "Tití Me Preguntó", "Bad Bunny"))),
                    HistoryDay(HistoryDayLabel.Date(LocalDate.of(2026, 9, 21)), listOf(previewEntry(4, "Everlong", "Foo Fighters"))),
                ),
            ),
            actions = HistoryActions(),
        )
    }
}

@Preview(name = "Historial · vacío", widthDp = 360, heightDp = 640)
@Composable
private fun HistoryEmptyPreview() {
    FlixPreview { HistoryContent(uiState = HistoryUiState.Content(emptyList()), actions = HistoryActions()) }
}
