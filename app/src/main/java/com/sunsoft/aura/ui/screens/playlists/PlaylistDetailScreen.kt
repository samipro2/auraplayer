package com.sunsoft.aura.ui.screens.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunsoft.aura.data.QueueSource
import com.sunsoft.aura.data.Song
import com.sunsoft.aura.ui.components.EmptyState
import com.sunsoft.aura.ui.components.SongRow
import com.sunsoft.aura.ui.theme.AuraTextSecondary
import com.sunsoft.aura.viewmodel.LibraryViewModel
import com.sunsoft.aura.viewmodel.PlayerViewModel
import com.sunsoft.aura.viewmodel.PlaylistViewModel

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playlistName: String,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val allSongs by libraryViewModel.songs.collectAsStateWithLifecycle()
    val songIds by playlistViewModel.songIdsFlow(playlistId).collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteIds by libraryViewModel.favoriteIds.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()

    val bySongId = remember(allSongs) { allSongs.associateBy { it.id } }
    val songs: List<Song> = remember(songIds, bySongId) { songIds.mapNotNull { bySongId[it] } }

    var menuOpen by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var renameText by remember(playlistName) { mutableStateOf(playlistName) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(playlistName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            androidx.compose.foundation.layout.Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Playlist options")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { menuOpen = false; showRename = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete playlist") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { menuOpen = false; showDeleteConfirm = true }
                    )
                }
            }
        }

        if (songs.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.PlayArrow,
                title = "This playlist is empty",
                message = "Add songs from the Songs tab using the \u22ee menu on any track."
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { playerViewModel.playQueue(songs, 0, QueueSource.PlaylistSource(playlistName)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Play")
                }
                OutlinedButton(
                    onClick = {
                        playerViewModel.playQueue(songs.shuffled(), 0, QueueSource.PlaylistSource(playlistName))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Shuffle")
                }
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SongRow(
                            song = song,
                            isFavorite = favoriteIds.contains(song.id),
                            isActive = song.id == playerState.currentSong?.id,
                            onClick = {
                                playerViewModel.playQueue(songs, index, QueueSource.PlaylistSource(playlistName))
                            },
                            onToggleFavorite = { libraryViewModel.toggleFavorite(song.id) },
                            onPlayNext = { playerViewModel.addToQueueNext(song) },
                            onAddToQueue = { playerViewModel.addToQueueEnd(song) },
                            onAddToPlaylist = { },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { playlistViewModel.removeSong(playlistId, song.id) }) {
                            Icon(
                                Icons.Filled.RemoveCircleOutline,
                                contentDescription = "Remove from playlist",
                                tint = AuraTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text("Rename playlist") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    playlistViewModel.renamePlaylist(playlistId, renameText)
                    showRename = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showRename = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete \"$playlistName\"?") },
            text = { Text("This only removes the playlist - your songs stay right where they are.") },
            confirmButton = {
                TextButton(onClick = {
                    playlistViewModel.deletePlaylist(playlistId)
                    showDeleteConfirm = false
                    onDeleted()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
