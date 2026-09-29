package com.flix.music.core.data.repository

import com.flix.music.core.model.AppTheme
import com.flix.music.core.model.PlaybackModes
import com.flix.music.core.model.UserSettings
import kotlinx.coroutines.flow.Flow

/** User settings stored in Preferences DataStore. */
interface SettingsRepository {

    val settings: Flow<UserSettings>

    suspend fun setTheme(theme: AppTheme)

    suspend fun setRememberPlaybackModes(remember: Boolean)

    suspend fun savePlaybackModes(modes: PlaybackModes)
}
