package com.flix.music.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.rememberNavController
import com.flix.music.R
import com.flix.music.core.designsystem.component.FlixSnackbar
import com.flix.music.core.designsystem.component.InfoBanner
import com.flix.music.core.designsystem.component.LocalSnackbarHostState
import com.flix.music.core.designsystem.theme.FlixMotion
import com.flix.music.core.designsystem.theme.FlixTheme
import com.flix.music.core.model.AlbumRef
import com.flix.music.core.model.ArtistRef
import com.flix.music.core.model.Track
import com.flix.music.core.player.PlaybackStatus
import com.flix.music.core.player.PlayerEvent
import com.flix.music.core.ui.DetailNavigator
import com.flix.music.core.ui.ErrorShortcuts
import com.flix.music.core.ui.LocalDetailNavigator
import com.flix.music.core.ui.LocalErrorShortcuts
import com.flix.music.core.ui.LocalIsOnline
import com.flix.music.core.ui.LocalTrackMenu
import com.flix.music.feature.album.navigation.navigateToAlbum
import com.flix.music.feature.artist.navigation.navigateToArtist
import com.flix.music.feature.onboarding.OnboardingScreen
import com.flix.music.feature.player.FullPlayer
import com.flix.music.feature.player.FullPlayerActions
import com.flix.music.feature.player.LyricsScreen
import com.flix.music.feature.player.MiniPlayer
import com.flix.music.feature.player.PlayerLayer
import com.flix.music.feature.player.PlayerViewModel
import com.flix.music.feature.player.QueueActions
import com.flix.music.feature.player.QueueScreen
import com.flix.music.feature.settings.navigation.navigateToSettings
import com.flix.music.feature.song.navigation.navigateToSong
import com.flix.music.feature.trackmenu.TrackMenuHost
import com.flix.music.navigation.FlixNavHost
import com.flix.music.navigation.TopLevelDestination
import com.flix.music.navigation.navigateToTopLevel
import com.flix.music.navigation.openPlaylistInLibrary
import kotlinx.coroutines.delay

/**
 * App shell: navigation, the persistent mini player above the bottom bar, and the
 * Full Player as a layer above everything. The artwork is a shared element, so it
 * grows from the mini player into the Full Player (design Motion → "Mini → Full").
 * [TrackMenuHost] makes the song menu available to every screen.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FlixApp(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val showOnboarding by appViewModel.showOnboarding.collectAsStateWithLifecycle()
    when (showOnboarding) {
        null -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(FlixTheme.colors.background),
            )
            return
        }
        true -> {
            OnboardingScreen(onFinished = appViewModel::onOnboardingFinished)
            return
        }
        false -> Unit
    }
    val isOnline by appViewModel.isOnline.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStack by navController.currentBackStack.collectAsStateWithLifecycle()
    val playerState by playerViewModel.state.collectAsStateWithLifecycle()
    val isCurrentFavorite by playerViewModel.isCurrentFavorite.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val hasTrack = playerState.status != PlaybackStatus.Idle && playerState.current != null
    val isExpanded = playerViewModel.isExpanded && hasTrack

    val fullPlayerActions = remember(playerViewModel) {
        FullPlayerActions(
            onCollapse = playerViewModel::collapse,
            onPlayPause = playerViewModel::onPlayPause,
            onSeek = playerViewModel::onSeek,
            onNext = playerViewModel::onNext,
            onPrevious = playerViewModel::onPrevious,
            onToggleShuffle = playerViewModel::onToggleShuffle,
            onCycleRepeat = playerViewModel::onCycleRepeat,
            onRetry = playerViewModel::onRetry,
            onToggleFavorite = playerViewModel::onToggleFavorite,
            onOpenQueue = playerViewModel::openQueue,
            onOpenLyrics = playerViewModel::openLyrics,
        )
    }
    val queueActions = remember(playerViewModel, navController) {
        QueueActions(
            onClose = playerViewModel::closeLayer,
            onItemClick = playerViewModel::onQueueItemClick,
            onRemove = playerViewModel::onRemoveQueueItem,
            onRestore = playerViewModel::onRestoreQueueItem,
            onMove = playerViewModel::onMoveQueueItem,
            onClear = playerViewModel::onClearQueue,
            onBrowseMusic = {
                playerViewModel.collapse()
                navController.navigateToTopLevel(TopLevelDestination.Search)
            },
        )
    }

    LaunchedEffect(playerViewModel) {
        playerViewModel.events.collect { event ->
            when (event) {
                is PlayerEvent.TrackSkipped ->
                    snackbarHostState.showSnackbar(context.getString(R.string.player_track_skipped, event.title))
            }
        }
    }

    BackHandler(enabled = isExpanded) {
        if (playerViewModel.layer == PlayerLayer.NowPlaying) playerViewModel.collapse() else playerViewModel.closeLayer()
    }

    val detailNavigator = remember(navController, playerViewModel) {
        object : DetailNavigator {
            override fun openSong(trackId: String) {
                playerViewModel.collapse()
                navController.navigateToSong(trackId)
            }

            override fun openArtist(artist: ArtistRef) {
                playerViewModel.collapse()
                navController.navigateToArtist(artist.id, artist.name)
            }

            override fun openAlbum(album: AlbumRef, artist: ArtistRef?) {
                playerViewModel.collapse()
                navController.navigateToAlbum(album.id, album.name, artist?.name.orEmpty(), artist?.id)
            }
        }
    }

    val openSongFromPlayer: (Track) -> Unit = { track -> detailNavigator.openSong(track.id) }
    val openArtistFromPlayer: (Track) -> Unit = { track -> track.artists.firstOrNull()?.let(detailNavigator::openArtist) }

    val errorShortcuts = remember(navController) {
        ErrorShortcuts(
            openLibrary = { navController.navigateToTopLevel(TopLevelDestination.Library) },
            openServerSettings = navController::navigateToSettings,
        )
    }

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState,
        LocalDetailNavigator provides detailNavigator,
        LocalIsOnline provides isOnline,
        LocalErrorShortcuts provides errorShortcuts,
    ) {
        TrackMenuHost(onOpenPlaylist = navController::openPlaylistInLibrary) {
            SharedTransitionLayout {
                Scaffold(
                    containerColor = FlixTheme.colors.background,
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { snackbarData -> FlixSnackbar(snackbarData) }
                    },
                    bottomBar = {
                        Column {
                            AnimatedVisibility(
                                visible = hasTrack && !isExpanded,
                                enter = fadeIn(tween(FlixMotion.SLOW)),
                                exit = fadeOut(tween(FlixMotion.FAST)),
                            ) {
                                MiniPlayer(
                                    state = playerState,
                                    onOpen = playerViewModel::expand,
                                    onPlayPause = playerViewModel::onPlayPause,
                                    onRetry = playerViewModel::onRetry,
                                    onNext = playerViewModel::onNext,
                                    onPrevious = playerViewModel::onPrevious,
                                    isFavorite = isCurrentFavorite,
                                    onToggleFavorite = playerViewModel::onToggleFavorite,
                                    artworkModifier = Modifier.sharedElement(
                                        rememberSharedContentState(PLAYER_ARTWORK_KEY),
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                    ),
                                )
                            }
                            FlixBottomBar(
                                selectedTab = selectedTab(backStack),
                                onNavigate = { destination -> navController.navigateToTopLevel(destination) },
                            )
                        }
                    },
                ) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        ConnectivityBanner(isOnline = isOnline)
                        FlixNavHost(navController, Modifier.weight(1f))
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(tween(FlixMotion.SLOW, easing = FlixMotion.Standard)) +
                        slideInVertically(tween(FlixMotion.SLOW, easing = FlixMotion.Standard)) { fullHeight ->
                            fullHeight / PLAYER_SLIDE_DIVISOR
                        },
                    exit = fadeOut(tween(FlixMotion.NORMAL, easing = FlixMotion.Accelerate)) +
                        slideOutVertically(tween(FlixMotion.NORMAL, easing = FlixMotion.Accelerate)) { fullHeight ->
                            fullHeight / PLAYER_SLIDE_DIVISOR
                        },
                ) {
                    val trackMenu = LocalTrackMenu.current
                    Box {
                        FullPlayer(
                            state = playerState,
                            actions = fullPlayerActions.copy(
                                onMore = { playerState.current?.let { track -> trackMenu.open(track) } },
                                onOpenSong = openSongFromPlayer,
                                onOpenArtist = openArtistFromPlayer,
                            ),
                            isFavorite = isCurrentFavorite,
                            artworkModifier = Modifier.sharedElement(
                                rememberSharedContentState(PLAYER_ARTWORK_KEY),
                                animatedVisibilityScope = this@AnimatedVisibility,
                            ),
                        )
                        PlayerLayerVisibility(visible = playerViewModel.layer == PlayerLayer.Queue) {
                            QueueScreen(state = playerState, actions = queueActions)
                        }
                        PlayerLayerVisibility(visible = playerViewModel.layer == PlayerLayer.Lyrics) {
                            LyricsScreen(
                                state = playerState,
                                actions = fullPlayerActions,
                                onClose = playerViewModel::closeLayer,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact connectivity banner above the screens (design "Errores"): while offline the
 * local library keeps working, and when the network returns a green "Conectado de
 * nuevo" shows for 3 s while failed screens retry by themselves.
 */
@Composable
private fun ConnectivityBanner(isOnline: Boolean) {
    var wasOffline by remember { mutableStateOf(false) }
    var showReconnected by remember { mutableStateOf(false) }
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            wasOffline = true
            showReconnected = false
        } else if (wasOffline) {
            wasOffline = false
            showReconnected = true
            delay(RECONNECTED_BANNER_MILLIS)
            showReconnected = false
        }
    }
    AnimatedVisibility(visible = !isOnline || showReconnected, enter = expandVertically(), exit = shrinkVertically()) {
        InfoBanner(
            text = stringResource(if (isOnline) R.string.connectivity_back else R.string.connectivity_offline),
            icon = if (isOnline) R.drawable.ic_wifi_filled else R.drawable.ic_cloud_off,
            iconTint = if (isOnline) FlixTheme.colors.success else FlixTheme.colors.warning,
            modifier = Modifier
                .padding(horizontal = FlixTheme.spacing.gutter)
                .padding(top = FlixTheme.spacing.sm, bottom = FlixTheme.spacing.xs)
                .semantics { liveRegion = LiveRegionMode.Polite },
            containerColor = if (isOnline) {
                lerp(FlixTheme.colors.surfaceVariant, FlixTheme.colors.success, RECONNECTED_TINT)
            } else {
                FlixTheme.colors.surfaceVariant
            },
        )
    }
}

/** Cola and Letra slide up from the bottom over the Full Player (design Navigation). */
@Composable
private fun PlayerLayerVisibility(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(FlixMotion.NORMAL, easing = FlixMotion.Decelerate)) +
            slideInVertically(tween(FlixMotion.NORMAL, easing = FlixMotion.Decelerate)) { fullHeight -> fullHeight },
        exit = fadeOut(tween(FlixMotion.NORMAL, easing = FlixMotion.Accelerate)) +
            slideOutVertically(tween(FlixMotion.NORMAL, easing = FlixMotion.Accelerate)) { fullHeight -> fullHeight },
    ) {
        content()
    }
}

@Composable
private fun FlixBottomBar(
    selectedTab: TopLevelDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    Column {
        HorizontalDivider(thickness = Dp.Hairline, color = FlixTheme.colors.divider)
        NavigationBar(containerColor = FlixTheme.colors.surface) {
            TopLevelDestination.entries.forEach { destination ->
                val selected = destination == selectedTab

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

/**
 * The tab that owns the top of the back stack. Song, artist and album screens are
 * shared by every tab, so the tab is the last graph on the stack, not the current
 * destination's parent.
 */
private fun selectedTab(backStack: List<NavBackStackEntry>): TopLevelDestination? =
    backStack.asReversed().firstNotNullOfOrNull { entry ->
        TopLevelDestination.entries.firstOrNull { destination ->
            entry.destination.hasRoute(destination.route::class)
        }
    }

private const val RECONNECTED_BANNER_MILLIS = 3_000L
private const val RECONNECTED_TINT = 0.16f
private const val PLAYER_ARTWORK_KEY = "player-artwork"
private const val PLAYER_SLIDE_DIVISOR = 8
