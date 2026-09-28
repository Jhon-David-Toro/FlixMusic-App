package com.flix.music.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.flix.music.core.designsystem.theme.FlixMotion
import com.flix.music.feature.home.navigation.HomeRoute
import com.flix.music.feature.home.navigation.homeScreen
import com.flix.music.feature.library.navigation.libraryScreen
import com.flix.music.feature.search.navigation.searchScreen

@Composable
fun FlixNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        exitTransition = { tabExit() },
        enterTransition = { tabEnter() },
        popExitTransition = { tabExit() },
        popEnterTransition = { tabEnter() },
    ) {
        homeScreen()
        searchScreen()
        libraryScreen()
    }
}

private fun tabExit() = fadeOut(tween(FlixMotion.TAB_EXIT, easing = FlixMotion.Accelerate))

private fun tabEnter() = fadeIn(
    tween(FlixMotion.TAB_ENTER, delayMillis = FlixMotion.TAB_EXIT, easing = FlixMotion.Decelerate)
)

fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}