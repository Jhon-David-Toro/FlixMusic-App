package com.flix.music.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flix.music.R
import com.flix.music.core.designsystem.component.FlixPrimaryButton
import com.flix.music.core.designsystem.theme.FlixPreview
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.Genres
import kotlinx.coroutines.launch

/** One first-run page: its message and the icon on the center cover of the illustration. */
private data class OnboardingPage(
    @param:StringRes val title: Int,
    @param:StringRes val body: Int,
    @param:DrawableRes val icon: Int,
    val genreIds: List<String>,
)

private val pages = listOf(
    OnboardingPage(R.string.onboarding_discover_title, R.string.onboarding_discover_body, R.drawable.ic_search, listOf("metal", "electronic", "rock")),
    OnboardingPage(R.string.onboarding_collections_title, R.string.onboarding_collections_body, R.drawable.ic_favorite_filled, listOf("pop", "hip-hop", "indie")),
    OnboardingPage(R.string.onboarding_player_title, R.string.onboarding_player_body, R.drawable.ic_queue_music, listOf("jazz", "latin", "rnb")),
)

/**
 * First run (design `onboarding`): three pages at most, always with "Omitir":
 * discover, your collections, and the player. The last button says "Empezar".
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { pages.size }
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FlixTheme.colors.background)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, end = 10.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            TextButton(onClick = onFinished) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = FlixTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = FlixTheme.colors.textSecondary,
                )
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pageIndex ->
            OnboardingPageContent(pages[pageIndex])
        }
        Column(
            modifier = Modifier.padding(horizontal = FlixTheme.spacing.gutter, vertical = FlixTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PageDots(count = pages.size, current = pagerState.currentPage)
            FlixPrimaryButton(
                text = stringResource(if (isLastPage) R.string.onboarding_start else R.string.onboarding_next),
                onClick = {
                    if (isLastPage) {
                        onFinished()
                    } else {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CoversIllustration(page)
        FlixLogo(Modifier.padding(top = FlixTheme.spacing.lg))
        Text(
            text = stringResource(page.title),
            style = FlixTheme.typography.headline.copy(fontSize = 25.sp),
            color = FlixTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 14.dp, start = FlixTheme.spacing.gutter, end = FlixTheme.spacing.gutter)
                .semantics { heading() },
        )
        Text(
            text = stringResource(page.body),
            style = FlixTheme.typography.bodySmall,
            color = FlixTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 10.dp)
                .widthIn(max = 260.dp),
        )
    }
}

/**
 * Three tilted covers (design: −8° left, +7° right, one larger in front). Genre
 * gradients stand in for album art so the first run works before anything loads.
 */
@Composable
private fun CoversIllustration(page: OnboardingPage) {
    val gradients = page.genreIds.mapNotNull(Genres::byId).map { genre ->
        Brush.linearGradient(listOf(Color(genre.gradientStart), Color(genre.gradientEnd)))
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(top = 6.dp),
    ) {
        Cover(gradients[0], SIDE_COVER, Modifier.offset(x = 34.dp, y = 30.dp).rotate(-8f))
        Cover(
            gradients[1],
            SIDE_COVER,
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 20.dp)
                .rotate(7f),
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = 60.dp),
            contentAlignment = Alignment.Center,
        ) {
            Cover(gradients[2], FRONT_COVER, Modifier.shadow(12.dp, COVER_SHAPE))
            Icon(
                painter = painterResource(page.icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun Cover(gradient: Brush, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(COVER_SHAPE)
            .background(gradient),
    )
}

/** "flix" wordmark (800, −0.03em) with the glowing Lumen dot. */
@Composable
private fun FlixLogo(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = "flix",
            style = FlixTheme.typography.display.copy(fontSize = 22.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.03).em),
            color = FlixTheme.colors.textPrimary,
        )
        Box(
            Modifier
                .padding(start = 2.dp, bottom = 5.dp)
                .size(6.dp)
                .shadow(6.dp, FlixTheme.shapes.circle, ambientColor = FlixTheme.colors.primary, spotColor = FlixTheme.colors.primary)
                .background(FlixTheme.colors.primary, FlixTheme.shapes.circle),
        )
    }
}

/** Current page as an 18dp textPrimary pill, the rest as 6dp tertiary dots. */
@Composable
private fun PageDots(count: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val width by animateDpAsState(if (index == current) 18.dp else 6.dp, label = "dotWidth")
            Box(
                Modifier
                    .size(width = width, height = 6.dp)
                    .clip(FlixTheme.shapes.pill)
                    .background(if (index == current) FlixTheme.colors.textPrimary else FlixTheme.colors.textTertiary),
            )
        }
    }
}

private val SIDE_COVER = 120.dp
private val FRONT_COVER = 160.dp
private val COVER_SHAPE = RoundedCornerShape(12.dp)

@Preview(name = "Primer inicio", widthDp = 360, heightDp = 760)
@Composable
private fun OnboardingPreview() {
    FlixPreview { OnboardingScreen(onFinished = {}) }
}
