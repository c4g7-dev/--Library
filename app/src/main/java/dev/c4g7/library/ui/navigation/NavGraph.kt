package dev.c4g7.library.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.c4g7.library.ui.components.MiniPlayer
import dev.c4g7.library.ui.screens.LibraryScreen
import dev.c4g7.library.ui.screens.PlayerScreen
import dev.c4g7.library.ui.screens.SettingsScreen
import dev.c4g7.library.viewmodel.LibraryViewModel
import dev.c4g7.library.viewmodel.PlayerViewModel

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val playerState by playerViewModel.state.collectAsState()

    val navItems = listOf(
        Triple(Screen.Library, Icons.Filled.GridView, "Library"),
        Triple(Screen.Player, Icons.Filled.PlayCircle, "Player"),
        Triple(Screen.Settings, Icons.Filled.Settings, "Settings")
    )

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFF000000),
        bottomBar = {
            Column {
                // Mini player — visible on Library and Settings, hidden on Player
                AnimatedVisibility(
                    visible = playerState.currentTrack != null && currentRoute != Screen.Player.route,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Library.route,
            modifier = Modifier.padding(innerPadding)
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
                    onLoad = {
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Library.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
