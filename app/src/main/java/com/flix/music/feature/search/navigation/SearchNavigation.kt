package com.flix.music.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.flix.music.core.navigation.TopLevelRoute
import com.flix.music.feature.search.SearchScreen
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute : TopLevelRoute

fun NavGraphBuilder.searchScreen() {
    composable<SearchRoute> {
        SearchScreen()
    }
}