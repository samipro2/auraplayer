package com.sunsoft.aura.ui.screens.library

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sunsoft.aura.data.QueueSource
import com.sunsoft.aura.data.Song
import com.sunsoft.aura.ui.components.AddToPlaylistDialog
import com.sunsoft.aura.ui.components.EmptyState
import com.sunsoft.aura.ui.components.SongRow
import com.sunsoft.aura.ui.theme.AuraTextSecondary
import com.sunsoft.aura.util.TimeUtils
import com.sunsoft.aura.viewmodel.LibraryViewModel
import com.sunsoft.aura.viewmodel.PlayerViewModel
import com.sunsoft.aura.viewmodel.PlaylistViewModel
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * One shared layout for "songs that belong to X": an album, an artist, or a folder.
 * The three screens below each just supply a title + the matching song list + source.
 */
@Composable
fun SongListDetailScreen(
    title: String,
    subtitle: String,
    songs: List<Song>,
    source: QueueSource,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    onBack: () -> Unit
) {
    val favoriteIds by libraryViewModel.favoriteIds.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    var playlistPickerSongId by remember { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 12.dp, end = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(Modifier.padding(start = 4.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary)
            }
        }

        if (songs.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.MusicOff,
                title = "Nothing here",
                message = "No songs matched."
            )
            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { playerViewModel.playQueue(songs, 0, source) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Play all")
            }
            OutlinedButton(
                onClick = { playerViewModel.playQueue(songs.shuffled(), 0, source) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Shuffle")
            }
        }

        val totalDuration = songs.sumOf { it.durationMs }
        Text(
            text = "${songs.size} songs • ${TimeUtils.formatDurationLong(totalDuration)}",
            style = MaterialTheme.typography.bodySmall,
            color = AuraTextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 140.dp)) {
            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                SongRow(
                    song = song,
                    isFavorite = favoriteIds.contains(song.id),
                    isActive = song.id == playerState.currentSong?.id,
                    onClick = { playerViewModel.playQueue(songs, index, source) },
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
