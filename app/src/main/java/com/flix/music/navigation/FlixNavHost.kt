package com.flix.music.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.flix.music.core.designsystem.theme.FlixTransitions
import com.flix.music.feature.genre.navigation.genreScreen
import com.flix.music.feature.genre.navigation.navigateToGenre
import com.flix.music.feature.home.navigation.HomeRoute
import com.flix.music.feature.home.navigation.homeScreen
import com.flix.music.feature.library.navigation.libraryScreen
import com.flix.music.feature.search.navigation.searchGraph

@Composable
fun FlixNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        exitTransition = { FlixTransitions.tabExit() },
        enterTransition = { FlixTransitions.tabEnter() },
        popExitTransition = { FlixTransitions.tabExit() },
        popEnterTransition = { FlixTransitions.tabEnter() },
    ) {
        homeScreen()
        searchGraph(
            onGenreClick = { genre -> navController.navigateToGenre(genre.id) },
        ) {
            genreScreen(onBack = navController::popBackStack)
        }
        libraryScreen()
    }
}

/**
 * Switches tabs keeping one back stack per tab: the tab being left is saved and
 * the target tab restores its own stack.
 */
fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
