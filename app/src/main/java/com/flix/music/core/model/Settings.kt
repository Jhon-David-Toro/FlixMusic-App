package com.flix.music.core.model

/** Appearance chosen in Ajustes (design: Sistema · Claro · Oscuro). */
enum class AppTheme { System, Light, Dark }

/** Shuffle and repeat as the player last had them, restored when "Recordar" is on. */
data class PlaybackModes(val shuffle: Boolean, val repeatMode: String)

data class UserSettings(
    val theme: AppTheme = AppTheme.System,
    val rememberPlaybackModes: Boolean = false,
    val playbackModes: PlaybackModes? = null,
)
