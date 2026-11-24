package com.sunsoft.aura.data

import com.sunsoft.aura.data.db.PlaylistDao
import com.sunsoft.aura.data.db.PlaylistEntity
import com.sunsoft.aura.data.db.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val dao: PlaylistDao) {

    fun observePlaylists(): Flow<List<PlaylistEntity>> = dao.observePlaylists()

    fun observeSongIds(playlistId: Long): Flow<List<Long>> = dao.observeSongIdsForPlaylist(playlistId)

    suspend fun createPlaylist(name: String): Long =
        dao.insertPlaylist(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))

    suspend fun renamePlaylist(id: Long, name: String) = dao.renamePlaylist(id, name)

    suspend fun deletePlaylist(id: Long) {
        dao.clearPlaylistSongs(id)
        dao.deletePlaylist(id)
    }

    suspend fun addSong(playlistId: Long, songId: Long) {
        val position = dao.nextPosition(playlistId)
        dao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId, position))
    }

    suspend fun removeSong(playlistId: Long, songId: Long) = dao.removeSongFromPlaylist(playlistId, songId)
}
