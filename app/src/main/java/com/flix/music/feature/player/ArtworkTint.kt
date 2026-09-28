package com.flix.music.feature.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.flix.music.core.designsystem.theme.FlixMotion
import com.flix.music.core.designsystem.theme.FlixTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Top color of the Full Player gradient, taken from the artwork (design: "Tinte = color
 * dominante del artwork, limitado en luminancia"). The dominant color is clamped to
 * L* ≤ 35 (dark) / ≥ 85 (light), then mixed with the background at 48 % / 22 %,
 * and animated on track changes (slow).
 */
@Composable
fun rememberArtworkTint(artworkUrl: String?): Color {
    val platformContext = LocalPlatformContext.current
    val isDark = FlixTheme.isDark
    val background = FlixTheme.colors.background
    var dominantColor by remember(artworkUrl) { mutableStateOf<Int?>(null) }

    LaunchedEffect(artworkUrl) {
        dominantColor = artworkUrl?.let { url -> dominantColorOf(platformContext, url) }
    }

    val target = dominantColor?.let { color ->
        lerp(background, Color(color.clampLightness(isDark)), if (isDark) DARK_TINT_AMOUNT else LIGHT_TINT_AMOUNT)
    } ?: background

    val tint by animateColorAsState(target, tween(FlixMotion.SLOW, easing = FlixMotion.Standard), label = "artworkTint")
    return tint
}

private suspend fun dominantColorOf(platformContext: PlatformContext, artworkUrl: String): Int? {
    val request = ImageRequest.Builder(platformContext)
        .data(artworkUrl)
        .size(PALETTE_SAMPLE_SIZE)
        .allowHardware(false)
        .build()
    val result = platformContext.imageLoader.execute(request) as? SuccessResult ?: return null
    val bitmap = result.image.toBitmap()
    return withContext(Dispatchers.Default) {
        Palette.from(bitmap).generate().dominantSwatch?.rgb
    }
}

private fun Int.clampLightness(isDark: Boolean): Int {
    val lab = DoubleArray(3)
    ColorUtils.colorToLAB(this, lab)
    lab[0] = if (isDark) lab[0].coerceAtMost(DARK_MAX_LIGHTNESS) else lab[0].coerceAtLeast(LIGHT_MIN_LIGHTNESS)
    return ColorUtils.LABToColor(lab[0], lab[1], lab[2])
}

private const val DARK_TINT_AMOUNT = 0.48f
private const val LIGHT_TINT_AMOUNT = 0.22f
private const val DARK_MAX_LIGHTNESS = 35.0
private const val LIGHT_MIN_LIGHTNESS = 85.0
private const val PALETTE_SAMPLE_SIZE = 128
