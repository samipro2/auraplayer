package com.sunsoft.aura.ui.navigation
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sunsoft.aura.ui.components.MiniPlayer
import com.sunsoft.aura.util.Prefs
import com.sunsoft.aura.viewmodel.LibraryViewModel
import com.sunsoft.aura.viewmodel.PlayerViewModel
import com.sunsoft.aura.viewmodel.PlaylistViewModel

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.LIBRARY, "Library", Icons.Filled.LibraryMusic),
    BottomTab(Routes.FAVORITES, "Favorites", Icons.Filled.Favorite),
    BottomTab(Routes.PLAYLISTS, "Playlists", Icons.Filled.QueueMusic),
    BottomTab(Routes.SEARCH, "Search", Icons.Filled.Search),
    BottomTab(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
)

@Composable
fun AuraScaffold(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    prefs: Prefs
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()

    val showBottomBar = currentRoute == null || Routes.topLevel.contains(currentRoute)
    val showMiniPlayer = playerState.currentSong != null &&
        currentRoute != Routes.NOW_PLAYING &&
        currentRoute?.startsWith("video/") != true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                if (showMiniPlayer) {
                    MiniPlayer(
                        state = playerState,
                        onClick = { navController.navigate(Routes.NOW_PLAYING) },
                        onTogglePlayPause = { playerViewModel.togglePlayPause() },
                        onSkipNext = { playerViewModel.skipToNext() }
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (showBottomBar) {
                    NavigationBar {
                        bottomTabs.forEach { tab ->
                            val selected = backStackEntry?.destination?.hierarchy
                                ?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        AuraNavGraph(
            navController = navController,
            modifier = Modifier.padding(padding),
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            playlistViewModel = playlistViewModel,
            prefs = prefs
        )
    }
}
