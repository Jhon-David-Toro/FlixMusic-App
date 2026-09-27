package com.flix.music.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.flix.music.core.navigation.TopLevelRoute
import com.flix.music.feature.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : TopLevelRoute

fun NavGraphBuilder.homeScreen() {
    composable<HomeRoute> {
        HomeScreen()
    }
}