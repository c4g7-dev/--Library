package dev.c4g7.library.ui.navigation

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.c4g7.library.ui.components.MiniPlayer
import dev.c4g7.library.ui.i18n.EnglishStrings
import dev.c4g7.library.ui.i18n.GermanStrings
import dev.c4g7.library.ui.i18n.LocalStrings
import dev.c4g7.library.ui.screens.LibraryScreen
import dev.c4g7.library.ui.screens.PlayerScreen
import dev.c4g7.library.ui.screens.SettingsScreen
import dev.c4g7.library.viewmodel.LibraryViewModel
import dev.c4g7.library.viewmodel.PlayerViewModel

private val routeOrder = mapOf("library" to 0, "player" to 1, "settings" to 2)

private sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Player : Screen("player")
    object Settings : Screen("settings")
}

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val libraryViewModel: LibraryViewModel = viewModel()
    val playerViewModel: PlayerViewModel = viewModel()
    val context = LocalContext.current

    // Language state — persisted in SharedPreferences
    val langPrefs = remember {
        context.getSharedPreferences("lang_prefs", Context.MODE_PRIVATE)
    }
    var language by remember { mutableStateOf(langPrefs.getString("lang", "en") ?: "en") }
    val strings = if (language == "de") GermanStrings else EnglishStrings

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = navBackStackEntry?.destination

    val playerState by playerViewModel.state.collectAsState()

    val navItems = listOf(
        Triple(Screen.Library, Icons.Filled.GridView, strings.library),
        Triple(Screen.Player, Icons.Filled.PlayCircle, strings.player),
        Triple(Screen.Settings, Icons.Filled.Settings, strings.settings)
    )

    CompositionLocalProvider(LocalStrings provides strings) {
        Scaffold(
            modifier = modifier,
            containerColor = Color(0xFF000000),
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF000000),
                    tonalElevation = 0.dp
                ) {
                    navItems.forEach { (screen, icon, label) ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) Color.White else Color(0xFF444444)
                                )
                            },
                            label = {
                                Text(
                                    label,
                                    color = if (selected) Color.White else Color(0xFF444444),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF1A1A1A)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            // Overlay approach: padding is always just the nav bar height — no layout shift
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Library.route,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        val from = routeOrder[initialState.destination.route] ?: 0
                        val to = routeOrder[targetState.destination.route] ?: 0
                        slideInHorizontally(tween(300)) { if (to >= from) it / 4 else -(it / 4) } +
                            fadeIn(tween(300))
                    },
                    exitTransition = {
                        val from = routeOrder[initialState.destination.route] ?: 0
                        val to = routeOrder[targetState.destination.route] ?: 0
                        slideOutHorizontally(tween(300)) { if (to >= from) -(it / 4) else it / 4 } +
                            fadeOut(tween(200))
                    },
                    popEnterTransition = {
                        val from = routeOrder[initialState.destination.route] ?: 0
                        val to = routeOrder[targetState.destination.route] ?: 0
                        slideInHorizontally(tween(300)) { if (to >= from) it / 4 else -(it / 4) } +
                            fadeIn(tween(300))
                    },
                    popExitTransition = {
                        val from = routeOrder[initialState.destination.route] ?: 0
                        val to = routeOrder[targetState.destination.route] ?: 0
                        slideOutHorizontally(tween(300)) { if (to >= from) -(it / 4) else it / 4 } +
                            fadeOut(tween(200))
                    }
                ) {
                    composable(Screen.Library.route) {
                        LibraryScreen(
                            libraryViewModel = libraryViewModel,
                            playerViewModel = playerViewModel,
                            onTrackClick = {
                                navController.navigate(Screen.Player.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(Screen.Player.route) {
                        PlayerScreen(playerViewModel = playerViewModel)
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            libraryViewModel = libraryViewModel,
                            language = language,
                            onLanguageChange = { lang ->
                                language = lang
                                langPrefs.edit().putString("lang", lang).apply()
                            },
                            onLoad = {
                                navController.navigate(Screen.Library.route) {
                                    popUpTo(Screen.Library.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }

                // Mini player overlaid above nav bar — no effect on innerPadding, no layout shift
                AnimatedVisibility(
                    visible = playerState.currentTrack != null && currentRoute != Screen.Player.route,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    playerState.currentTrack?.let { track ->
                        MiniPlayer(
                            track = track,
                            isPlaying = playerState.isPlaying,
                            progress = playerState.progressFraction,
                            onTogglePlayPause = { playerViewModel.togglePlayPause() },
                            onSkipNext = { playerViewModel.skipNext() },
                            onClick = {
                                navController.navigate(Screen.Player.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
