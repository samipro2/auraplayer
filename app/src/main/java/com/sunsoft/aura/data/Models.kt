package com.sunsoft.aura.data

import android.net.Uri

/**
 * A single playable audio track pulled from the device's MediaStore.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val folderPath: String,
    val contentUri: Uri
)

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int
)

data class Artist(
    val name: String,
    val songCount: Int
)

data class Folder(
    val path: String,
    val name: String,
    val songCount: Int
)

/**
 * A single playable video file from the device's MediaStore.
 */
data class Video(
    val id: Long,
    val title: String,
    val durationMs: Long,
    val folderPath: String,
    val contentUri: Uri
)

enum class RepeatMode { OFF, ALL, ONE }

/** What the current queue was built from — used to label the "Now Playing" source. */
sealed class QueueSource {
    data object AllSongs : QueueSource()
    data class AlbumSource(val name: String) : QueueSource()
    data class ArtistSource(val name: String) : QueueSource()
    data class PlaylistSource(val name: String) : QueueSource()
    data object Favorites : QueueSource()
    data object SearchResults : QueueSource()
    data object Unknown : QueueSource()
}
