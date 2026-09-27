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
        exitTransition =  { fadeOut(tween(FlixMotion.FAST)) },
        enterTransition = {
            fadeIn(tween(durationMillis = FlixMotion.NORMAL, delayMillis = FlixMotion.FAST))
        },
        popEnterTransition = {
            fadeIn(tween(durationMillis = FlixMotion.NORMAL, delayMillis = FlixMotion.FAST))
        },
        popExitTransition = { fadeOut(tween(FlixMotion.FAST))}
    ) {
        homeScreen()
        searchScreen()
        libraryScreen()
    }
}

fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}