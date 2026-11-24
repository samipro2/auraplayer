package com.sunsoft.aura.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunsoft.aura.data.FavoritesRepository
import com.sunsoft.aura.data.QueueSource
import com.sunsoft.aura.data.RepeatMode
import com.sunsoft.aura.data.Song
import com.sunsoft.aura.playback.PlayerController
import com.sunsoft.aura.playback.PlayerUiState
import com.sunsoft.aura.util.Prefs
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val controller: PlayerController,
    private val favoritesRepo: FavoritesRepository,
    private val prefs: Prefs
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = controller.uiState

    private val favoriteIds: StateFlow<Set<Long>> = favoritesRepo.observeFavoriteIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val isCurrentFavorite: StateFlow<Boolean> = combine(uiState, favoriteIds) { state, favs ->
        state.currentSong?.let { favs.contains(it.id) } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _sleepTimerRemainingMs = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMs: StateFlow<Long?> = _sleepTimerRemainingMs

    private val _sleepAtEndOfTrack = MutableStateFlow(false)
    val sleepAtEndOfTrack: StateFlow<Boolean> = _sleepAtEndOfTrack

    private var sleepTimerJob: Job? = null

    init {
        // Watches for a track change while "stop at end of track" is armed.
        var previousIndex: Int? = null
        viewModelScope.launch {
            uiState.collect { state ->
                val prev = previousIndex
                if (_sleepAtEndOfTrack.value && prev != null && state.currentIndex != prev) {
                    controller.pause()
                    _sleepAtEndOfTrack.value = false
                }
                previousIndex = state.currentIndex
            }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int, source: QueueSource) =
        controller.playQueue(songs, startIndex, source)

    fun togglePlayPause() = controller.togglePlayPause()
    fun skipToNext() = controller.skipToNext()
    fun skipToPrevious() = controller.skipToPrevious()
    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)
    fun playQueueItem(index: Int) = controller.playQueueItem(index)
    fun removeFromQueue(index: Int) = controller.removeFromQueue(index)
    fun moveInQueue(from: Int, to: Int) = controller.moveInQueue(from, to)
    fun addToQueueEnd(song: Song) = controller.addToQueueEnd(song)
    fun addToQueueNext(song: Song) = controller.addToQueueNext(song)
    fun toggleShuffle() = controller.toggleShuffle()
    fun cycleRepeatMode() = controller.cycleRepeatMode()

    fun setPlaybackSpeed(speed: Float) {
        controller.setPlaybackSpeed(speed)
        viewModelScope.launch { prefs.setPlaybackSpeed(speed) }
    }

    fun toggleFavoriteCurrent() {
        val id = uiState.value.currentSong?.id ?: return
        viewModelScope.launch { favoritesRepo.toggle(id, favoriteIds.value.contains(id)) }
    }

    fun setSleepTimerMinutes(minutes: Int?) {
        sleepTimerJob?.cancel()
        _sleepAtEndOfTrack.value = false
        if (minutes == null) {
            _sleepTimerRemainingMs.value = null
            return
        }
        val totalMs = minutes * 60_000L
        _sleepTimerRemainingMs.value = totalMs
        sleepTimerJob = viewModelScope.launch {
            var remaining = totalMs
            while (remaining > 0) {
                kotlinx.coroutines.delay(1000)
                remaining -= 1000
                _sleepTimerRemainingMs.value = remaining.coerceAtLeast(0)
            }
            controller.pause()
            _sleepTimerRemainingMs.value = null
        }
    }

    fun setSleepAtEndOfTrack(enabled: Boolean) {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingMs.value = null
        _sleepAtEndOfTrack.value = enabled
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingMs.value = null
        _sleepAtEndOfTrack.value = false
    }
}
