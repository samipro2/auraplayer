package com.sunsoft.aura.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunsoft.aura.data.Album
import com.sunsoft.aura.data.Artist
import com.sunsoft.aura.data.FavoritesRepository
import com.sunsoft.aura.data.Folder
import com.sunsoft.aura.data.MediaStoreRepository
import com.sunsoft.aura.data.Song
import com.sunsoft.aura.data.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repo: MediaStoreRepository,
    private val favoritesRepo: FavoritesRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasLoadedOnce = MutableStateFlow(false)
    val hasLoadedOnce: StateFlow<Boolean> = _hasLoadedOnce

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val favoriteIds: StateFlow<Set<Long>> = favoritesRepo.observeFavoriteIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val albums: StateFlow<List<Album>> = songs
        .map { repo.deriveAlbums(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = songs
        .map { repo.deriveArtists(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<Folder>> = songs
        .map { repo.deriveFolders(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = combine(songs, favoriteIds) { all, favIds ->
        all.filter { favIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<Song>> = combine(songs, searchQuery) { all, query ->
        if (query.isBlank()) emptyList()
        else all.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadLibrary() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = repo.loadSongs()
            _videos.value = repo.loadVideos()
            _isLoading.value = false
            _hasLoadedOnce.value = true
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            favoritesRepo.toggle(songId, favoriteIds.value.contains(songId))
        }
    }

    fun songsForAlbum(albumId: Long): List<Song> = songs.value.filter { it.albumId == albumId }
    fun songsForArtist(name: String): List<Song> = songs.value.filter { it.artist == name }
    fun songsForFolder(path: String): List<Song> = songs.value.filter { it.folderPath == path }
}
