package com.flix.music.feature.player

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.flix.music.R
import com.flix.music.core.player.PlaybackSource
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.RepeatMode

internal data class PlayPauseAction(@DrawableRes val icon: Int, @StringRes val label: Int)

/**
 * Icon and label of the play/pause control for each state (design "Estados de
 * reproducción"): while loading, the mini player shows play with a ring; the Full
 * Player already shows pause, because the intent is to play.
 */
internal fun playPauseAction(status: PlaybackStatus, isMiniPlayer: Boolean): PlayPauseAction = when (status) {
    PlaybackStatus.Playing, PlaybackStatus.Buffering ->
        PlayPauseAction(R.drawable.ic_pause_filled, R.string.player_pause)
    PlaybackStatus.Loading ->
        if (isMiniPlayer) PlayPauseAction(R.drawable.ic_play_arrow_filled, R.string.player_play)
        else PlayPauseAction(R.drawable.ic_pause_filled, R.string.player_pause)
    PlaybackStatus.Ended -> PlayPauseAction(R.drawable.ic_replay, R.string.player_replay)
    else -> PlayPauseAction(R.drawable.ic_play_arrow_filled, R.string.player_play)
}

@Composable
internal fun playbackSourceLabel(source: PlaybackSource): String = when (source) {
    is PlaybackSource.Search -> stringResource(R.string.player_source_search, source.query)
    is PlaybackSource.Genre -> stringResource(R.string.player_source_genre, source.name)
    PlaybackSource.Favorites -> stringResource(R.string.player_source_favorites)
    PlaybackSource.History -> stringResource(R.string.player_source_history)
    is PlaybackSource.Playlist -> source.name
}

@StringRes
internal fun RepeatMode.label(): Int = when (this) {
    RepeatMode.Off -> R.string.player_repeat_off
    RepeatMode.All -> R.string.player_repeat_all
    RepeatMode.One -> R.string.player_repeat_one
}
