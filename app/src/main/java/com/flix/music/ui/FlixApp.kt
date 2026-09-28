package com.flix.music.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.navigation.FlixNavHost
import com.flix.music.navigation.TopLevelDestination
import com.flix.music.navigation.navigateToTopLevel

@Composable
fun FlixApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        containerColor = FlixTheme.colors.background,
        bottomBar = {
            FlixBottomBar(
                currentDestination = currentDestination,
                onNavigate = { destination -> navController.navigateToTopLevel(destination) },
            )
        },
    ) { innerPadding ->
        FlixNavHost(navController, Modifier.padding(innerPadding))
    }
}

@Composable
private fun FlixBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    Column {
        HorizontalDivider(thickness = Dp.Hairline, color = FlixTheme.colors.divider)
        NavigationBar(containerColor = FlixTheme.colors.surface) {
            TopLevelDestination.entries.forEach { destination ->
                val selected = currentDestination?.hierarchy
                    ?.any { navDestination -> navDestination.hasRoute(destination.route::class) } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(destination) },
                    icon = {
                        Icon(
                            painter = painterResource(
                                if (selected) destination.selectedIcon else destination.icon
                            ),
                            contentDescription = null,
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(destination.label),
                            style = FlixTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlixTheme.colors.textPrimary,
                        selectedTextColor = FlixTheme.colors.textPrimary,
                        indicatorColor = FlixTheme.colors.surfaceVariant,
                        unselectedIconColor = FlixTheme.colors.textSecondary,
                        unselectedTextColor = FlixTheme.colors.textSecondary,
                    ),
                )
            }
        }
    }
}
