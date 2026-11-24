package com.sunsoft.aura.data

import com.sunsoft.aura.data.db.FavoriteDao
import com.sunsoft.aura.data.db.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val dao: FavoriteDao) {

    fun observeFavoriteIds(): Flow<List<Long>> = dao.observeFavoriteIds()

    suspend fun toggle(songId: Long, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            dao.removeFavorite(songId)
        } else {
            dao.addFavorite(FavoriteEntity(songId, System.currentTimeMillis()))
        }
    }
}
