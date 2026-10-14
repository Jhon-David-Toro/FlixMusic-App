package com.flix.music.feature.settings

import android.text.format.Formatter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.R
import com.flix.music.core.designsystem.component.ConfirmDialog
import com.flix.music.core.designsystem.component.FlixSegmentedControl
import com.flix.music.core.designsystem.component.FlixSnackbarVisuals
import com.flix.music.core.designsystem.component.FlixTopAppBar
import com.flix.music.core.designsystem.component.LocalSnackbarHostState
import com.flix.music.core.designsystem.component.Segment
import com.flix.music.core.designsystem.component.SettingsChevron
import com.flix.music.core.designsystem.component.SettingsGroupTitle
import com.flix.music.core.designsystem.component.SettingsRow
import com.flix.music.core.designsystem.component.SoonTag
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.AppTheme
import com.flix.music.core.model.UserSettings
import kotlinx.coroutines.launch

/** Every user action in Ajustes. Defaults do nothing, which keeps previews short. */
data class SettingsActions(
    val onBack: () -> Unit = {},
    val onThemeChange: (AppTheme) -> Unit = {},
    val onRememberPlaybackModesChange: (Boolean) -> Unit = {},
    val onClearHistory: () -> Unit = {},
    val onClearCache: () -> Unit = {},
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        version = viewModel.version,
        actions = SettingsActions(
            onBack = onBack,
            onThemeChange = viewModel::onThemeChange,
            onRememberPlaybackModesChange = viewModel::onRememberPlaybackModesChange,
            onClearHistory = viewModel::onClearHistory,
            onClearCache = viewModel::onClearCache,
        ),
        modifier = modifier,
    )
}

/**
 * Ajustes (design `settings`). Features the design marks as coming later (autoplay,
 * crossfade, audio quality, notifications) are shown disabled with "Próximamente".
 */
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    version: String,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
) {
    var confirmingClearHistory by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val cacheClearedMessage = stringResource(R.string.settings_cache_cleared)
    val historyClearedMessage = stringResource(R.string.settings_history_cleared)

    fun confirm(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(FlixSnackbarVisuals(message, R.drawable.ic_check_circle_filled, success = true))
        }
    }

    Column(modifier.fillMaxSize()) {
        FlixTopAppBar(title = stringResource(R.string.settings_title), onBack = actions.onBack)
        LazyColumn(contentPadding = PaddingValues(bottom = FlixTheme.spacing.lg)) {
            item(key = "appearance") {
                SettingsGroupTitle(stringResource(R.string.settings_appearance))
                FlixSegmentedControl(
                    segments = listOf(
                        Segment(AppTheme.System, stringResource(R.string.settings_theme_system), R.drawable.ic_brightness_auto),
                        Segment(AppTheme.Light, stringResource(R.string.settings_theme_light), R.drawable.ic_light_mode),
                        Segment(AppTheme.Dark, stringResource(R.string.settings_theme_dark), R.drawable.ic_dark_mode),
                    ),
                    selected = uiState.settings.theme,
                    onSelect = actions.onThemeChange,
                    modifier = Modifier.padding(horizontal = FlixTheme.spacing.gutter),
                )
            }
            item(key = "playback") {
                SettingsGroupTitle(stringResource(R.string.settings_playback))
                SettingsRow(
                    title = stringResource(R.string.settings_autoplay),
                    subtitle = stringResource(R.string.settings_autoplay_body),
                    enabled = false,
                ) { SoonTag() }
                SettingsRow(
                    title = stringResource(R.string.settings_remember_modes),
                    onClick = { actions.onRememberPlaybackModesChange(!uiState.settings.rememberPlaybackModes) },
                ) {
                    FlixSwitch(
                        checked = uiState.settings.rememberPlaybackModes,
                        onCheckedChange = actions.onRememberPlaybackModesChange,
                    )
                }
                SettingsRow(
                    title = stringResource(R.string.settings_crossfade),
                    subtitle = stringResource(R.string.settings_crossfade_body),
                    enabled = false,
                ) { SoonTag() }
                SettingsRow(
                    title = stringResource(R.string.settings_audio_quality),
                    subtitle = stringResource(R.string.settings_audio_quality_automatic),
                    enabled = false,
                ) { SoonTag() }
            }
            item(key = "notifications") {
                SettingsGroupTitle(stringResource(R.string.settings_notifications))
                SettingsRow(title = stringResource(R.string.settings_new_music), enabled = false) { SoonTag() }
            }
            item(key = "data") {
                SettingsGroupTitle(stringResource(R.string.settings_data))
                SettingsRow(
                    title = stringResource(R.string.history_clear),
                    onClick = { confirmingClearHistory = true },
                ) { SettingsChevron() }
                SettingsRow(
                    title = stringResource(R.string.settings_clear_cache),
                    subtitle = uiState.cacheBytes?.let { bytes ->
                        stringResource(R.string.settings_cache_size, Formatter.formatShortFileSize(context, bytes))
                    },
                    onClick = {
                        actions.onClearCache()
                        confirm(cacheClearedMessage)
                    },
                ) { SettingsChevron() }
            }
            item(key = "about") {
                SettingsGroupTitle(stringResource(R.string.settings_about))
                SettingsRow(title = stringResource(R.string.settings_version), subtitle = version)
            }
        }
    }

    if (confirmingClearHistory) {
        ConfirmDialog(
            title = stringResource(R.string.history_clear_title),
            message = stringResource(R.string.history_clear_message),
            confirmLabel = stringResource(R.string.history_clear_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = {
                confirmingClearHistory = false
                actions.onClearHistory()
                confirm(historyClearedMessage)
            },
            onDismiss = { confirmingClearHistory = false },
        )
    }
}

/** `.toggle-sw`: on = textPrimary track with a background-colored thumb; off = outlined. */
@Composable
private fun FlixSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedTrackColor = FlixTheme.colors.textPrimary,
            checkedThumbColor = FlixTheme.colors.background,
            checkedBorderColor = FlixTheme.colors.textPrimary,
            uncheckedTrackColor = FlixTheme.colors.surfaceVariant,
            uncheckedThumbColor = FlixTheme.colors.textTertiary,
            uncheckedBorderColor = FlixTheme.colors.textTertiary,
        ),
    )
}

@Preview(name = "Ajustes", widthDp = 360, heightDp = 1000)
@Composable
private fun SettingsPreview() {
    FlixPreview {
        SettingsContent(
            uiState = SettingsUiState(UserSettings(theme = AppTheme.Dark), 142_000_000),
            version = "1.0 (1)",
            actions = SettingsActions(),
        )
    }
}
