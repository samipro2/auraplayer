package com.sunsoft.aura.ui.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
fun SearchScreen(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel
) {
    val query by libraryViewModel.searchQuery.collectAsStateWithLifecycle()
    val results by libraryViewModel.searchResults.collectAsStateWithLifecycle()
    val favoriteIds by libraryViewModel.favoriteIds.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    var playlistPickerSongId by remember { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { libraryViewModel.setSearchQuery(it) },
            singleLine = true,
            placeholder = { Text("Titles, artists, albums\u2026") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        when {
            query.isBlank() -> EmptyState(
                icon = Icons.Filled.Search,
                title = "Find something to play",
                message = "Start typing to search your whole library."
            )
            results.isEmpty() -> EmptyState(
                icon = Icons.Filled.Search,
                title = "No matches",
                message = "Nothing in your library matches \"$query\"."
            )
            else -> LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
                itemsIndexed(results, key = { _, song -> song.id }) { index, song ->
                    SongRow(
                        song = song,
                        isFavorite = favoriteIds.contains(song.id),
                        isActive = song.id == playerState.currentSong?.id,
                        onClick = { playerViewModel.playQueue(results, index, QueueSource.SearchResults) },
                        onToggleFavorite = { libraryViewModel.toggleFavorite(song.id) },
                        onPlayNext = { playerViewModel.addToQueueNext(song) },
                        onAddToQueue = { playerViewModel.addToQueueEnd(song) },
                        onAddToPlaylist = { playlistPickerSongId = song.id }
                    )
                }
            }
        }
    }

    playlistPickerSongId?.let { songId ->
        AddToPlaylistDialog(playlistViewModel, songId, onDismiss = { playlistPickerSongId = null })
    }
}
