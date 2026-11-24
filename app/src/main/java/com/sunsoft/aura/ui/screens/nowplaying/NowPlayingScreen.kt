package com.sunsoft.aura.ui.screens.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sunsoft.aura.data.ArtRequest
import com.sunsoft.aura.data.QueueSource
import com.sunsoft.aura.data.RepeatMode
import com.sunsoft.aura.ui.components.GlassSurface
import com.sunsoft.aura.ui.theme.AuraAccent
import com.sunsoft.aura.ui.theme.AuraBackground
import com.sunsoft.aura.ui.theme.AuraTextSecondary
import com.sunsoft.aura.util.TimeUtils
import com.sunsoft.aura.viewmodel.PlayerViewModel

private fun sourceLabel(source: QueueSource): String = when (source) {
    is QueueSource.AllSongs -> "Playing from your library"
    is QueueSource.AlbumSource -> "Playing from album \u00b7 ${source.name}"
    is QueueSource.ArtistSource -> "Playing from artist \u00b7 ${source.name}"
    is QueueSource.PlaylistSource -> "Playing from playlist \u00b7 ${source.name}"
    is QueueSource.Favorites -> "Playing from favorites"
    is QueueSource.SearchResults -> "Playing from search"
    is QueueSource.Unknown -> ""
}

@Composable
fun NowPlayingScreen(
    playerViewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit
) {
    val state by playerViewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by playerViewModel.isCurrentFavorite.collectAsStateWithLifecycle()
    val sleepRemaining by playerViewModel.sleepTimerRemainingMs.collectAsStateWithLifecycle()
    val sleepAtEnd by playerViewModel.sleepAtEndOfTrack.collectAsStateWithLifecycle()
    val song = state.currentSong

    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableStateOf(0f) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (song != null) {
            AsyncImage(
                model = ArtRequest(song.contentUri),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(70.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.30f), AuraBackground.copy(alpha = 0.92f))
                    )
                )
        )

        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Collapse")
                }
                Text(
                    text = sourceLabel(state.source),
                    style = MaterialTheme.typography.labelMedium,
                    color = AuraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                IconButton(onClick = onOpenQueue) {
                    Icon(Icons.Filled.QueueMusic, contentDescription = "Queue")
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (song != null) {
                        AsyncImage(
                            model = ArtRequest(song.contentUri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = AuraTextSecondary,
                            modifier = Modifier.align(Alignment.Center).size(64.dp)
                        )
                    }
                }
            }

            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    Text(
                        text = song?.title ?: "Nothing playing",
                        style = MaterialTheme.typography.displaySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOfNotNull(song?.artist, song?.album).joinToString("  \u2014  "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    val durationFloat = state.durationMs.coerceAtLeast(1L).toFloat()
                    val sliderValue = if (isDragging) dragValue else state.positionMs.toFloat()
                    Slider(
                        value = sliderValue.coerceIn(0f, durationFloat),
                        onValueChange = { isDragging = true; dragValue = it },
                        onValueChangeFinished = {
                            playerViewModel.seekTo(dragValue.toLong())
                            isDragging = false
                        },
                        valueRange = 0f..durationFloat,
                        colors = SliderDefaults.colors(
                            thumbColor = AuraAccent,
                            activeTrackColor = AuraAccent,
                            inactiveTrackColor = AuraAccent.copy(alpha = 0.18f)
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            TimeUtils.formatDuration(if (isDragging) dragValue.toLong() else state.positionMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraTextSecondary
                        )
                        Text(
                            TimeUtils.formatDuration(state.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraTextSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { playerViewModel.toggleShuffle() }) {
                            Icon(
                                Icons.Filled.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (state.shuffleEnabled) AuraAccent else AuraTextSecondary
                            )
                        }
                        IconButton(onClick = { playerViewModel.skipToPrevious() }) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp))
                        }
                        IconButton(
                            onClick = { playerViewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(50))
                                .background(AuraAccent)
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = AuraBackground,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        IconButton(onClick = { playerViewModel.skipToNext() }) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
                        }
                        IconButton(onClick = { playerViewModel.cycleRepeatMode() }) {
                            Icon(
                                imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                                contentDescription = "Repeat",
                                tint = if (state.repeatMode != RepeatMode.OFF) AuraAccent else AuraTextSecondary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { playerViewModel.toggleFavoriteCurrent() }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) AuraAccent else AuraTextSecondary
                            )
                        }
                        TextButton(onClick = { showSpeedDialog = true }) {
                            Icon(Icons.Filled.Speed, contentDescription = null, tint = AuraTextSecondary)
                            Text(
                                " ${state.playbackSpeed}x",
                                color = AuraTextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        TextButton(onClick = { showSleepDialog = true }) {
                            Icon(
                                Icons.Filled.Timer,
                                contentDescription = "Sleep timer",
                                tint = if (sleepRemaining != null || sleepAtEnd) AuraAccent else AuraTextSecondary
                            )
                            val label = when {
                                sleepRemaining != null -> " ${(sleepRemaining!! / 60000) + 1}m"
                                sleepAtEnd -> " End"
                                else -> ""
                            }
                            if (label.isNotEmpty()) {
                                Text(label, color = AuraAccent, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSpeedDialog) {
        SpeedDialog(
            current = state.playbackSpeed,
            onSelect = { playerViewModel.setPlaybackSpeed(it); showSpeedDialog = false },
            onDismiss = { showSpeedDialog = false }
        )
    }

    if (showSleepDialog) {
        SleepTimerDialog(
            onSelectMinutes = { playerViewModel.setSleepTimerMinutes(it); showSleepDialog = false },
            onSelectEndOfTrack = { playerViewModel.setSleepAtEndOfTrack(true); showSleepDialog = false },
            onCancel = { playerViewModel.cancelSleepTimer(); showSleepDialog = false },
            onDismiss = { showSleepDialog = false }
        )
    }
}

@Composable
private fun SpeedDialog(current: Float, onSelect: (Float) -> Unit, onDismiss: () -> Unit) {
    val speeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playback speed") },
        text = {
            Column {
                speeds.forEach { speed ->
                    TextButton(onClick = { onSelect(speed) }) {
                        Text(
                            "${speed}x" + if (speed == current) "  \u2713" else "",
                            color = if (speed == current) AuraAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun SleepTimerDialog(
    onSelectMinutes: (Int) -> Unit,
    onSelectEndOfTrack: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(10, 15, 30, 45, 60)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep timer") },
        text = {
            Column {
                options.forEach { minutes ->
                    TextButton(onClick = { onSelectMinutes(minutes) }) { Text("$minutes minutes") }
                }
                TextButton(onClick = onSelectEndOfTrack) { Text("End of current track") }
                TextButton(onClick = onCancel) { Text("Turn off", color = AuraTextSecondary) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
