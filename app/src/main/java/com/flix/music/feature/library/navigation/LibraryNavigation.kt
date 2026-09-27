package com.flix.music.feature.library.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.flix.music.core.navigation.TopLevelRoute
import com.flix.music.feature.library.LibraryScreen
import kotlinx.serialization.Serializable

@Serializable
data object LibraryRoute : TopLevelRoute

fun NavGraphBuilder.libraryScreen() {
    composable<LibraryRoute> {
        LibraryScreen()
    }
}