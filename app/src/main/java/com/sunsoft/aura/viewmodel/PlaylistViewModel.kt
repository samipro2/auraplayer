package com.sunsoft.aura.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunsoft.aura.data.PlaylistRepository
import com.sunsoft.aura.data.db.PlaylistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistViewModel(private val repo: PlaylistRepository) : ViewModel() {

    val playlists: StateFlow<List<PlaylistEntity>> = repo.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun songIdsFlow(playlistId: Long): Flow<List<Long>> = repo.observeSongIds(playlistId)

    fun createPlaylist(name: String, onCreated: (Long) -> Unit = {}) {
        if (name.isBlank()) return
        viewModelScope.launch { onCreated(repo.createPlaylist(name.trim())) }
    }

    fun renamePlaylist(id: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.renamePlaylist(id, name.trim()) }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch { repo.deletePlaylist(id) }
    }

    fun addSong(playlistId: Long, songId: Long) {
        viewModelScope.launch { repo.addSong(playlistId, songId) }
    }

    fun removeSong(playlistId: Long, songId: Long) {
        viewModelScope.launch { repo.removeSong(playlistId, songId) }
    }
}
