package com.sunsoft.aura.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sunsoft.aura.data.QueueSource
import com.sunsoft.aura.ui.screens.credits.CreditsScreen
import com.sunsoft.aura.ui.screens.favorites.FavoritesScreen
import com.sunsoft.aura.ui.screens.library.LibraryScreen
import com.sunsoft.aura.ui.screens.library.SongListDetailScreen
import com.sunsoft.aura.ui.screens.nowplaying.NowPlayingScreen
import com.sunsoft.aura.ui.screens.playlists.PlaylistDetailScreen
import com.sunsoft.aura.ui.screens.playlists.PlaylistsScreen
import com.sunsoft.aura.ui.screens.queue.QueueScreen
import com.sunsoft.aura.ui.screens.search.SearchScreen
import com.sunsoft.aura.ui.screens.settings.EqualizerScreen
import com.sunsoft.aura.ui.screens.settings.SettingsScreen
import com.sunsoft.aura.ui.screens.videos.VideoPlayerScreen
import com.sunsoft.aura.util.Prefs
import com.sunsoft.aura.viewmodel.LibraryViewModel
import com.sunsoft.aura.viewmodel.PlayerViewModel
import com.sunsoft.aura.viewmodel.PlaylistViewModel

@Composable
fun AuraNavGraph(
    navController: NavHostController,
    modifier: Modifier,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    prefs: Prefs
) {
    NavHost(navController = navController, startDestination = Routes.LIBRARY, modifier = modifier) {

        composable(Routes.LIBRARY) {
            LibraryScreen(
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                playlistViewModel = playlistViewModel,
                onOpenAlbum = { navController.navigate(Routes.album(it)) },
                onOpenArtist = { navController.navigate(Routes.artist(it)) },
                onOpenFolder = { navController.navigate(Routes.folder(it)) },
                onOpenVideo = { navController.navigate(Routes.videoPlayer(it)) }
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(libraryViewModel, playerViewModel, playlistViewModel)
        }

        composable(Routes.PLAYLISTS) {
            PlaylistsScreen(playlistViewModel, onOpenPlaylist = { navController.navigate(Routes.playlistDetail(it)) })
        }

        composable(Routes.SEARCH) {
            SearchScreen(libraryViewModel, playerViewModel, playlistViewModel)
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                prefs = prefs,
                libraryViewModel = libraryViewModel,
                onOpenEqualizer = { navController.navigate(Routes.EQUALIZER) },
                onOpenCredits = { navController.navigate(Routes.CREDITS) }
            )
        }

        composable(Routes.EQUALIZER) {
            EqualizerScreen(prefs = prefs, onBack = { navController.popBackStack() })
        }

        composable(Routes.CREDITS) {
            CreditsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.NOW_PLAYING) {
            NowPlayingScreen(
                playerViewModel = playerViewModel,
                onCollapse = { navController.popBackStack() },
                onOpenQueue = { navController.navigate(Routes.QUEUE) }
            )
        }

        composable(Routes.QUEUE) {
            QueueScreen(playerViewModel, onBack = { navController.popBackStack() })
        }

        composable(
            Routes.ALBUM_PATTERN,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
            val songs = libraryViewModel.songsForAlbum(albumId)
            val album = libraryViewModel.albums.value.firstOrNull { it.id == albumId }
            SongListDetailScreen(
                title = album?.title ?: "Album",
                subtitle = album?.artist.orEmpty(),
                songs = songs,
                source = QueueSource.AlbumSource(album?.title ?: ""),
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                playlistViewModel = playlistViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.ARTIST_PATTERN,
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("artistName") ?: return@composable
            val name = Uri.decode(encoded)
            val songs = libraryViewModel.songsForArtist(name)
            SongListDetailScreen(
                title = name,
                subtitle = "Artist",
                songs = songs,
                source = QueueSource.ArtistSource(name),
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                playlistViewModel = playlistViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.FOLDER_PATTERN,
            arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("folderPath") ?: return@composable
            val path = Uri.decode(encoded)
            val songs = libraryViewModel.songsForFolder(path)
            SongListDetailScreen(
                title = path.substringAfterLast('/'),
                subtitle = path,
                songs = songs,
                source = QueueSource.Unknown,
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                playlistViewModel = playlistViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.PLAYLIST_DETAIL_PATTERN,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: return@composable
            val playlist = playlistViewModel.playlists.value.firstOrNull { it.id == playlistId }
            PlaylistDetailScreen(
                playlistId = playlistId,
                playlistName = playlist?.name ?: "Playlist",
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                playlistViewModel = playlistViewModel,
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(
            Routes.VIDEO_PLAYER_PATTERN,
            arguments = listOf(navArgument("videoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong("videoId") ?: return@composable
            val video = libraryViewModel.videos.value.firstOrNull { it.id == videoId }
            if (video != null) {
                VideoPlayerScreen(video = video, onBack = { navController.popBackStack() })
            }
        }
    }
}
