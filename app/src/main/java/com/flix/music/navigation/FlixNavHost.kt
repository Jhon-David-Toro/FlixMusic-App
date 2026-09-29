package com.flix.music.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.flix.music.core.designsystem.theme.FlixTransitions
import com.flix.music.feature.album.navigation.albumScreen
import com.flix.music.feature.artist.navigation.artistScreen
import com.flix.music.feature.favorites.navigation.favoritesScreen
import com.flix.music.feature.favorites.navigation.navigateToFavorites
import com.flix.music.feature.genre.navigation.genreScreen
import com.flix.music.feature.genre.navigation.navigateToGenre
import com.flix.music.feature.history.navigation.historyScreen
import com.flix.music.feature.history.navigation.navigateToHistory
import com.flix.music.feature.home.navigation.HomeRoute
import com.flix.music.feature.home.navigation.homeScreen
import com.flix.music.feature.library.navigation.libraryGraph
import com.flix.music.feature.playlist.navigation.navigateToPlaylist
import com.flix.music.feature.playlist.navigation.navigateToPlaylistEdit
import com.flix.music.feature.playlist.navigation.playlistScreens
import com.flix.music.feature.search.navigation.searchGraph
import com.flix.music.feature.song.navigation.songScreen

@Composable
fun FlixNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val browseMusic = { navController.navigateToTopLevel(TopLevelDestination.Search) }
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        exitTransition = { FlixTransitions.tabExit() },
        enterTransition = { FlixTransitions.tabEnter() },
        popExitTransition = { FlixTransitions.tabExit() },
        popEnterTransition = { FlixTransitions.tabEnter() },
    ) {
        homeScreen(
            onOpenHistory = { navController.openInLibrary { navigateToHistory() } },
            onOpenSearch = browseMusic,
            onOpenGenre = { genre ->
                navController.navigateToTopLevel(TopLevelDestination.Search)
                navController.navigateToGenre(genre.id)
            },
            onOpenLibrary = { navController.navigateToTopLevel(TopLevelDestination.Library) },
            onOpenFavorites = { navController.openInLibrary { navigateToFavorites() } },
            onOpenPlaylist = navController::openPlaylistInLibrary,
        )
        searchGraph(
            onGenreClick = { genre -> navController.navigateToGenre(genre.id) },
        ) {
            genreScreen(onBack = navController::popBackStack)
        }
        libraryGraph(
            onOpenFavorites = navController::navigateToFavorites,
            onOpenHistory = navController::navigateToHistory,
            onOpenPlaylist = navController::navigateToPlaylist,
            onBrowseMusic = browseMusic,
        ) {
            favoritesScreen(onBack = navController::popBackStack, onBrowseMusic = browseMusic)
            historyScreen(onBack = navController::popBackStack, onBrowseMusic = browseMusic)
            playlistScreens(
                onBack = navController::popBackStack,
                onEdit = navController::navigateToPlaylistEdit,
                onBrowseMusic = browseMusic,
            )
        }
        songScreen(onBack = navController::popBackStack)
        artistScreen(onBack = navController::popBackStack)
        albumScreen(onBack = navController::popBackStack)
    }
}

/** Opens a playlist inside the Library tab, e.g. from "Ver" on the "Añadida a …" snackbar. */
fun NavController.openPlaylistInLibrary(playlistId: Long) = openInLibrary { navigateToPlaylist(playlistId) }

/** Switches to the Library tab and opens one of its screens there, so the tab stays right. */
fun NavController.openInLibrary(navigate: NavController.() -> Unit) {
    navigateToTopLevel(TopLevelDestination.Library)
    navigate()
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
