package com.sunsoft.aura.ui.navigation

import android.net.Uri

object Routes {
    const val LIBRARY = "library"
    const val FAVORITES = "favorites"
    const val PLAYLISTS = "playlists"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val EQUALIZER = "equalizer"
    const val CREDITS = "credits"
    const val NOW_PLAYING = "now_playing"
    const val QUEUE = "queue"

    const val ALBUM_PATTERN = "album/{albumId}"
    const val ARTIST_PATTERN = "artist/{artistName}"
    const val FOLDER_PATTERN = "folder/{folderPath}"
    const val PLAYLIST_DETAIL_PATTERN = "playlist/{playlistId}"
    const val VIDEO_PLAYER_PATTERN = "video/{videoId}"

    val topLevel = setOf(LIBRARY, FAVORITES, PLAYLISTS, SEARCH, SETTINGS)

    fun album(id: Long) = "album/$id"
    fun artist(name: String) = "artist/${Uri.encode(name)}"
    fun folder(path: String) = "folder/${Uri.encode(path)}"
    fun playlistDetail(id: Long) = "playlist/$id"
    fun videoPlayer(id: Long) = "video/$id"
}
