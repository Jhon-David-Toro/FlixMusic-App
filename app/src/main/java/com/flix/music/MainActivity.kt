package com.flix.music

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.PathInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flix.music.core.data.repository.SettingsRepository
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.designsystem.theme.ThemeMode
import com.flix.music.core.model.AppTheme
import com.flix.music.core.model.UserSettings
import com.flix.music.ui.FlixApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.hypot

/**
 * Hosts the whole app and applies the theme chosen in Ajustes. The splash (Lumen
 * lighting up next to the "f") stays until the settings are read, so the app never
 * flashes the wrong theme, and then closes into the violet dot.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var settingsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !settingsLoaded }
        splashScreen.setOnExitAnimationListener(::closeIntoLumen)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle<UserSettings?>(initialValue = null)
            settings?.let { loaded ->
                SideEffect { settingsLoaded = true }
                FlixTheme(themeMode = loaded.theme.toThemeMode()) {
                    FlixApp()
                }
            }
        }
    }

    /**
     * The icon animation's start comes as wall-clock time (epoch millis), so the wait
     * is measured against [System.currentTimeMillis] and never exceeds its duration.
     * Once the icon animation has played, the dark screen shrinks into a circle around
     * the Lumen dot while the mark fades and shrinks toward it, both in step, and the
     * app shows through. Without system animations it just goes.
     */
    private fun closeIntoLumen(provider: SplashScreenViewProvider) {
        val splashView = provider.view
        if (!ValueAnimator.areAnimatorsEnabled() || !splashView.isAttachedToWindow) {
            provider.remove()
            return
        }
        val iconView = provider.iconView
        val iconLocation = IntArray(2).also(iconView::getLocationInWindow)
        val splashLocation = IntArray(2).also(splashView::getLocationInWindow)
        val dotX = iconLocation[0] - splashLocation[0] + iconView.width * LUMEN_DOT_X
        val dotY = iconLocation[1] - splashLocation[1] + iconView.height * LUMEN_DOT_Y
        val iconAnimationEnd = provider.iconAnimationStartMillis + provider.iconAnimationDurationMillis
        val remainingIconAnimation = (iconAnimationEnd - System.currentTimeMillis())
            .coerceIn(0L, provider.iconAnimationDurationMillis)
        val closingCircle = ViewAnimationUtils.createCircularReveal(
            splashView,
            dotX.toInt(),
            dotY.toInt(),
            hypot(splashView.width.toFloat(), splashView.height.toFloat()),
            0f,
        )
        iconView.pivotX = iconView.width * LUMEN_DOT_X
        iconView.pivotY = iconView.height * LUMEN_DOT_Y
        val fadingMark = ObjectAnimator.ofPropertyValuesHolder(
            iconView,
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, CLOSED_MARK_SCALE),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, CLOSED_MARK_SCALE),
        )
        AnimatorSet().apply {
            playTogether(closingCircle, fadingMark)
            duration = CLOSE_DURATION_MILLIS
            startDelay = remainingIconAnimation
            interpolator = PathInterpolator(0.3f, 0f, 0f, 1f)
            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) = provider.remove()
                },
            )
            start()
        }
    }

    private companion object {
        /** Where the dot sits in `splash_icon_animated` (108 viewport, scaled 1.1 around its center). */
        const val LUMEN_DOT_X = 66.96f / 108f
        const val LUMEN_DOT_Y = 70.36f / 108f
        const val CLOSE_DURATION_MILLIS = 450L
        const val CLOSED_MARK_SCALE = 0.8f
    }
}

private fun AppTheme.toThemeMode(): ThemeMode = when (this) {
    AppTheme.System -> ThemeMode.System
    AppTheme.Light -> ThemeMode.Light
    AppTheme.Dark -> ThemeMode.Dark
}
