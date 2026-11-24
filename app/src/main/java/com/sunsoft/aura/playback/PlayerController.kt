package com.sunsoft.aura.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.sunsoft.aura.data.QueueSource
import com.sunsoft.aura.data.RepeatMode
import com.sunsoft.aura.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1f,
    val source: QueueSource = QueueSource.Unknown
) {
    val hasNext: Boolean get() = currentIndex in 0 until queue.lastIndex || repeatMode != RepeatMode.OFF
    val hasPrevious: Boolean get() = currentIndex > 0 || repeatMode != RepeatMode.OFF
}

private fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(contentUri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .build()
        )
        .build()

/**
 * Thin wrapper around a [MediaController] bound to [PlaybackService]. ViewModels
 * read [uiState] and call the command functions; nothing here touches the UI directly.
 */
class PlayerController(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controller: MediaController? = null
    private var tickerJob: Job? = null
    private var queueSongs: List<Song> = emptyList()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            manageTicker(isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _uiState.update { it.copy(isBuffering = playbackState == Player.STATE_BUFFERING) }
            refreshPositionAndDuration()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncCurrentIndex()
            refreshPositionAndDuration()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _uiState.update { it.copy(shuffleEnabled = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _uiState.update { it.copy(repeatMode = repeatMode.toAppRepeatMode()) }
        }

        override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
            _uiState.update { it.copy(playbackSpeed = playbackParameters.speed) }
        }
    }

    private suspend fun ensureConnected() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val built = MediaController.Builder(context, token).buildAsync().await()
        built.addListener(listener)
        controller = built
    }

    private fun syncCurrentIndex() {
        val c = controller ?: return
        val index = c.currentMediaItemIndex
        val song = queueSongs.getOrNull(index)
        _uiState.update { it.copy(currentIndex = index, currentSong = song) }
    }

    private fun refreshPositionAndDuration() {
        val c = controller ?: return
        val duration = c.duration.let { if (it == androidx.media3.common.C.TIME_UNSET) 0L else it }
        _uiState.update { it.copy(positionMs = c.currentPosition.coerceAtLeast(0), durationMs = duration) }
    }

    private fun manageTicker(isPlaying: Boolean) {
        tickerJob?.cancel()
        if (!isPlaying) return
        tickerJob = scope.launch {
            while (true) {
                refreshPositionAndDuration()
                delay(500)
            }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int, source: QueueSource) {
        scope.launch {
            ensureConnected()
            val c = controller ?: return@launch
            queueSongs = songs
            c.setMediaItems(songs.map { it.toMediaItem() }, startIndex.coerceIn(0, songs.lastIndex.coerceAtLeast(0)), 0L)
            c.prepare()
            c.play()
            _uiState.update { it.copy(queue = songs, source = source) }
            syncCurrentIndex()
        }
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun pause() {
        controller?.pause()
    }

    fun skipToNext() {
        val c = controller ?: return
        if (c.hasNextMediaItem() || c.repeatMode != Player.REPEAT_MODE_OFF) c.seekToNext()
    }

    fun skipToPrevious() {
        val c = controller ?: return
        // Restart the current track if we're more than 3s in, like most players do.
        if (c.currentPosition > 3000) {
            c.seekTo(0)
        } else if (c.hasPreviousMediaItem() || c.repeatMode != Player.REPEAT_MODE_OFF) {
            c.seekToPrevious()
        } else {
            c.seekTo(0)
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        refreshPositionAndDuration()
    }

    fun playQueueItem(index: Int) {
        controller?.seekTo(index, 0L)
        controller?.play()
    }

    fun removeFromQueue(index: Int) {
        controller?.removeMediaItem(index)
        queueSongs = queueSongs.toMutableList().also { if (index in it.indices) it.removeAt(index) }
        _uiState.update { it.copy(queue = queueSongs) }
        syncCurrentIndex()
    }

    fun moveInQueue(from: Int, to: Int) {
        controller?.moveMediaItem(from, to)
        queueSongs = queueSongs.toMutableList().also {
            if (from in it.indices) {
                val item = it.removeAt(from)
                it.add(to.coerceIn(0, it.size), item)
            }
        }
        _uiState.update { it.copy(queue = queueSongs) }
        syncCurrentIndex()
    }

    fun addToQueueEnd(song: Song) {
        controller?.addMediaItem(song.toMediaItem())
        queueSongs = queueSongs + song
        _uiState.update { it.copy(queue = queueSongs) }
    }

    fun addToQueueNext(song: Song) {
        val c = controller ?: return
        val insertAt = (c.currentMediaItemIndex + 1).coerceAtMost(c.mediaItemCount)
        c.addMediaItem(insertAt, song.toMediaItem())
        queueSongs = queueSongs.toMutableList().also { it.add(insertAt.coerceAtMost(it.size), song) }
        _uiState.update { it.copy(queue = queueSongs) }
    }

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun cycleRepeatMode() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
    }

    fun setShuffleEnabled(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
    }

    fun setRepeatMode(mode: RepeatMode) {
        controller?.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

}

private fun Int.toAppRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    else -> RepeatMode.OFF
}
