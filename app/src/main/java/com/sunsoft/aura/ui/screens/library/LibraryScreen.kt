package com.sunsoft.aura.ui.screens.library

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sunsoft.aura.data.Album
import com.sunsoft.aura.data.ArtRequest
import com.sunsoft.aura.data.Artist
import com.sunsoft.aura.data.Folder
import com.sunsoft.aura.data.QueueSource
import com.sunsoft.aura.data.Song
import com.sunsoft.aura.data.Video
import com.sunsoft.aura.ui.components.AddToPlaylistDialog
import com.sunsoft.aura.ui.components.ArtTile
import com.sunsoft.aura.ui.components.EmptyState
import com.sunsoft.aura.ui.components.SongRow
import com.sunsoft.aura.ui.theme.AuraAccent
import com.sunsoft.aura.ui.theme.AuraTextSecondary
import com.sunsoft.aura.util.TimeUtils
import com.sunsoft.aura.viewmodel.LibraryViewModel
import com.sunsoft.aura.viewmodel.PlayerViewModel
import com.sunsoft.aura.viewmodel.PlaylistViewModel

private val tabTitles = listOf("Songs", "Albums", "Artists", "Folders", "Videos")

@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    onOpenAlbum: (Long) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenVideo: (Long) -> Unit
) {
    val songs by libraryViewModel.songs.collectAsStateWithLifecycle()
    val albums by libraryViewModel.albums.collectAsStateWithLifecycle()
    val artists by libraryViewModel.artists.collectAsStateWithLifecycle()
    val folders by libraryViewModel.folders.collectAsStateWithLifecycle()
    val videos by libraryViewModel.videos.collectAsStateWithLifecycle()
    val favoriteIds by libraryViewModel.favoriteIds.collectAsStateWithLifecycle()
    val isLoading by libraryViewModel.isLoading.collectAsStateWithLifecycle()
    val hasLoadedOnce by libraryViewModel.hasLoadedOnce.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { if (!hasLoadedOnce) libraryViewModel.loadLibrary() }

    var selectedTab by remember { mutableStateOf(0) }
    var playlistPickerSongId by remember { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Your library",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
        )

        if (isLoading && !hasLoadedOnce) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AuraAccent)
            }
        } else {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = AuraAccent
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> SongsTab(
                    songs = songs,
                    favoriteIds = favoriteIds,
                    activeId = playerState.currentSong?.id,
                    onSongClick = { index -> playerViewModel.playQueue(songs, index, QueueSource.AllSongs) },
                    onToggleFavorite = { libraryViewModel.toggleFavorite(it) },
                    onPlayNext = { playerViewModel.addToQueueNext(it) },
                    onAddToQueue = { playerViewModel.addToQueueEnd(it) },
                    onAddToPlaylist = { playlistPickerSongId = it }
                )
                1 -> AlbumsTab(albums, onOpenAlbum)
                2 -> ArtistsTab(artists, onOpenArtist)
                3 -> FoldersTab(folders, onOpenFolder)
                4 -> VideosTab(videos, onOpenVideo)
            }
        }
    }

    playlistPickerSongId?.let { songId ->
        AddToPlaylistDialog(playlistViewModel, songId, onDismiss = { playlistPickerSongId = null })
    }
}

@Composable
private fun SongsTab(
    songs: List<Song>,
    favoriteIds: Set<Long>,
    activeId: Long?,
    onSongClick: (Int) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToPlaylist: (Long) -> Unit
) {
    if (songs.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.LibraryMusic,
            title = "No music found",
            message = "Aura looks for audio already on this device. Add some tracks to your phone's storage and they'll show up here."
        )
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            SongRow(
                song = song,
                isFavorite = favoriteIds.contains(song.id),
                isActive = song.id == activeId,
                onClick = { onSongClick(index) },
                onToggleFavorite = { onToggleFavorite(song.id) },
                onPlayNext = { onPlayNext(song) },
                onAddToQueue = { onAddToQueue(song) },
                onAddToPlaylist = { onAddToPlaylist(song.id) }
            )
        }
    }
}

@Composable
private fun AlbumsTab(albums: List<Album>, onOpenAlbum: (Long) -> Unit) {
    if (albums.isEmpty()) {
        EmptyState(Icons.Filled.LibraryMusic, "No albums yet", "Albums will appear once your songs are scanned.")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(albums, key = { it.id }) { album ->
            ArtTile(
                title = album.title,
                subtitle = "${album.artist} • ${album.songCount} songs",
                artRequest = null,
                onClick = { onOpenAlbum(album.id) }
            )
        }
    }
}

@Composable
private fun ArtistsTab(artists: List<Artist>, onOpenArtist: (String) -> Unit) {
    if (artists.isEmpty()) {
        EmptyState(Icons.Filled.Person, "No artists yet", "Artists will appear once your songs are scanned.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
        items(artists, key = { it.name }) { artist ->
            ListItem(
                headlineContent = { Text(artist.name) },
                supportingContent = { Text("${artist.songCount} songs", color = AuraTextSecondary) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, AuraTextSecondary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = AuraTextSecondary)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenArtist(artist.name) }
            )
        }
    }
}

@Composable
private fun FoldersTab(folders: List<Folder>, onOpenFolder: (String) -> Unit) {
    if (folders.isEmpty()) {
        EmptyState(Icons.Filled.Folder, "No folders yet", "Folders will appear once your songs are scanned.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
        items(folders, key = { it.path }) { folder ->
            ListItem(
                headlineContent = { Text(folder.name) },
                supportingContent = { Text("${folder.songCount} songs", color = AuraTextSecondary) },
                leadingContent = { Icon(Icons.Filled.Folder, contentDescription = null, tint = AuraAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFolder(folder.path) }
            )
        }
    }
}

@Composable
private fun VideosTab(videos: List<Video>, onOpenVideo: (Long) -> Unit) {
    if (videos.isEmpty()) {
        EmptyState(Icons.Filled.VideoLibrary, "No videos yet", "Videos on this device will show up here.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
        items(videos, key = { it.id }) { video ->
            ListItem(
                headlineContent = { Text(video.title) },
                supportingContent = { Text(TimeUtils.formatDuration(video.durationMs), color = AuraTextSecondary) },
                leadingContent = {
                    AsyncImage(
                        model = ArtRequest(video.contentUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenVideo(video.id) }
            )
        }
    }
}

