package com.sunsoft.aura.ui.screens.favorites

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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunsoft.aura.data.QueueSource
import com.sunsoft.aura.ui.components.AddToPlaylistDialog
import com.sunsoft.aura.ui.components.EmptyState
import com.sunsoft.aura.ui.components.SongRow
import com.sunsoft.aura.viewmodel.LibraryViewModel
import com.sunsoft.aura.viewmodel.PlayerViewModel
import com.sunsoft.aura.viewmodel.PlaylistViewModel

@Composable
fun FavoritesScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel
) {
    val favorites by libraryViewModel.favoriteSongs.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    var playlistPickerSongId by remember { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )

        if (favorites.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Favorite,
                title = "No favorites yet",
                message = "Tap the heart on any song to pin it here."
            )
            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { playerViewModel.playQueue(favorites, 0, QueueSource.Favorites) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Play all")
            }
            OutlinedButton(
                onClick = { playerViewModel.playQueue(favorites.shuffled(), 0, QueueSource.Favorites) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Shuffle")
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
            itemsIndexed(favorites, key = { _, song -> song.id }) { index, song ->
                SongRow(
                    song = song,
                    isFavorite = true,
                    isActive = song.id == playerState.currentSong?.id,
                    onClick = { playerViewModel.playQueue(favorites, index, QueueSource.Favorites) },
                    onToggleFavorite = { libraryViewModel.toggleFavorite(song.id) },
                    onPlayNext = { playerViewModel.addToQueueNext(song) },
                    onAddToQueue = { playerViewModel.addToQueueEnd(song) },
                    onAddToPlaylist = { playlistPickerSongId = song.id }
                )
            }
        }
    }

    playlistPickerSongId?.let { songId ->
        AddToPlaylistDialog(playlistViewModel, songId, onDismiss = { playlistPickerSongId = null })
    }
}
