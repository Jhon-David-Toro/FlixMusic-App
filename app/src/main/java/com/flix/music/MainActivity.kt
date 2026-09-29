package com.flix.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
+import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.core.data.repository.SettingsRepository
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.designsystem.theme.ThemeMode
import com.flix.music.core.model.AppTheme
import com.flix.music.core.model.UserSettings
import com.flix.music.ui.FlixApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Hosts the whole app and applies the theme chosen in Ajustes. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = UserSettings())
            FlixTheme(themeMode = settings.theme.toThemeMode()) {
                FlixApp()
            }
        }
    }
}

private fun AppTheme.toThemeMode(): ThemeMode = when (this) {
    AppTheme.System -> ThemeMode.System
    AppTheme.Light -> ThemeMode.Light
    AppTheme.Dark -> ThemeMode.Dark
}