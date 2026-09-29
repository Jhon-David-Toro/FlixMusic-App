package com.flix.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flix.music.core.data.connectivity.ConnectivityMonitor
import com.flix.music.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** App-wide state of the shell: connectivity and whether to show the first-run screens. */
@HiltViewModel
class AppViewModel @Inject constructor(
    connectivityMonitor: ConnectivityMonitor,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = connectivityMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), true)

    /** Null until the settings load, so neither the app nor the first-run screens flash. */
    val showOnboarding: StateFlow<Boolean?> = settingsRepository.settings
        .map { settings -> !settings.onboardingCompleted }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    fun onOnboardingFinished() {
        viewModelScope.launch { settingsRepository.completeOnboarding() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
