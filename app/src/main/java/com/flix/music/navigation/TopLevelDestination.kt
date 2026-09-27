package com.flix.music.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.flix.music.R
import com.flix.music.core.navigation.TopLevelRoute
import com.flix.music.feature.home.navigation.HomeRoute
import com.flix.music.feature.library.navigation.LibraryRoute
import com.flix.music.feature.search.navigation.SearchRoute

enum class TopLevelDestination (
    val route: TopLevelRoute,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
) {
    Home(HomeRoute, R.string.tab_home, R.drawable.ic_home, R.drawable.ic_home_filled),
    Search(SearchRoute, R.string.tab_search, R.drawable.ic_search, R.drawable.ic_search),
    Library(LibraryRoute, R.string.tab_library, R.drawable.ic_library_music, R.drawable.ic_library_music_filled)
}