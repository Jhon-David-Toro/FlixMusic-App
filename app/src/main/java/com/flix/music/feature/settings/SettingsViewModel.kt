package com.flix.music.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flix.music.BuildConfig
import com.flix.music.core.data.repository.CacheRepository
import com.flix.music.core.data.repository.HistoryRepository
import com.flix.music.core.data.repository.MusicRepository
import com.flix.music.core.data.repository.SettingsRepository
import com.flix.music.core.model.AppTheme
import com.flix.music.core.model.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Result of `/health` for Ajustes → Servidor. */
enum class ServerStatus { Checking, Connected, Unreachable }

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val serverStatus: ServerStatus = ServerStatus.Checking,
    val cacheBytes: Long? = null,
)

/** Ajustes (design `settings`): appearance, playback, server, data and about. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val musicRepository: MusicRepository,
    private val cacheRepository: CacheRepository,
) : ViewModel() {

    private val serverStatus = MutableStateFlow(ServerStatus.Checking)
    private val cacheBytes = MutableStateFlow<Long?>(null)

    /** Host and port of the Flix server, as shown under "Servidor". */
    val serverAddress: String = BuildConfig.SERVER_URL.substringAfter("://").trimEnd('/')

    val version: String = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    val uiState: StateFlow<SettingsUiState> =
        combine(settingsRepository.settings, serverStatus, cacheBytes, ::SettingsUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SettingsUiState())

    init {
        checkServer()
        measureCache()
    }

    fun onThemeChange(theme: AppTheme) {
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun onRememberPlaybackModesChange(remember: Boolean) {
        viewModelScope.launch { settingsRepository.setRememberPlaybackModes(remember) }
    }

    fun onCheckServer() = checkServer()

    fun onClearHistory() {
        viewModelScope.launch { historyRepository.clear() }
    }

    fun onClearCache() {
        viewModelScope.launch {
            cacheRepository.clear()
            cacheBytes.value = cacheRepository.sizeInBytes()
        }
    }

    private fun checkServer() {
        serverStatus.value = ServerStatus.Checking
        viewModelScope.launch {
            serverStatus.value = if (musicRepository.isServerReachable()) ServerStatus.Connected else ServerStatus.Unreachable
        }
    }

    private fun measureCache() {
        viewModelScope.launch { cacheBytes.value = cacheRepository.sizeInBytes() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
