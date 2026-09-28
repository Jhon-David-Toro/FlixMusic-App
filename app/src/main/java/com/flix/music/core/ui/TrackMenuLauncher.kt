package com.flix.music.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.flix.music.core.model.Track

/**
 * Opens the song menu (design "Menú de canción") from any screen. The app shell hosts
 * the sheet, so screens only need the track. [onRemoveFromHistory] adds
 * "Quitar del historial", which only applies inside Historial.
 */
fun interface TrackMenuLauncher {
    fun open(track: Track, onRemoveFromHistory: (() -> Unit)?)
}

fun TrackMenuLauncher.open(track: Track) = open(track, onRemoveFromHistory = null)

/** Defaults to doing nothing so previews and tests need no host. */
val LocalTrackMenu = staticCompositionLocalOf { TrackMenuLauncher { _, _ -> } }
