package com.flix.music.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flix.music.BuildConfig
import com.flix.music.core.data.repository.CacheRepository
import com.flix.music.core.data.repository.HistoryRepository
import com.flix.music.core.data.repository.SettingsRepository
import com.flix.music.core.model.AppTheme
import com.flix.music.core.model.UserSettings
import com.flix.music.core.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val cacheBytes: Long? = null,
)

/**
 * Ajustes (design `settings`): appearance, playback, data and about. The design's
 * "Servidor" group is gone: the app talks to YouTube Music directly.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val cacheRepository: CacheRepository,
) : ViewModel() {

    private val cacheBytes = MutableStateFlow<Long?>(null)

    val version: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    val uiState: StateFlow<SettingsUiState> =
        combine(settingsRepository.settings, cacheBytes, ::SettingsUiState)
            .stateIn(viewModelScope, WhileUiSubscribed, SettingsUiState())

    init {
        measureCache()
    }

    fun onThemeChange(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun onAutoplayChange(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoplay(enabled) }
    }

    fun onRememberPlaybackModesChange(remember: Boolean) {
        viewModelScope.launch { settingsRepository.setRememberPlaybackModes(remember) }
    }

    fun onClearHistory() {
        viewModelScope.launch { historyRepository.clear() }
    }

    fun onClearCache() {
        viewModelScope.launch {
            cacheRepository.clear()
            cacheBytes.value = cacheRepository.sizeInBytes()
        }
    }

    private fun measureCache() {
        viewModelScope.launch { cacheBytes.value = cacheRepository.sizeInBytes() }
    }
}
