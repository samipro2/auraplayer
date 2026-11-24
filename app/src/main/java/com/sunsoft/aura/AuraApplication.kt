package com.sunsoft.aura

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.sunsoft.aura.data.AlbumArtFetcher
import com.sunsoft.aura.data.FavoritesRepository
import com.sunsoft.aura.data.MediaStoreRepository
import com.sunsoft.aura.data.PlaylistRepository
import com.sunsoft.aura.data.db.AppDatabase
import com.sunsoft.aura.playback.PlayerController
import com.sunsoft.aura.util.Prefs

/** Holds every shared dependency for the app's lifetime - no DI framework needed at this size. */
class AppContainer(app: Application) {
    val mediaStoreRepository = MediaStoreRepository(app)
    private val database = AppDatabase.getInstance(app)
    val playlistRepository = PlaylistRepository(database.playlistDao())
    val favoritesRepository = FavoritesRepository(database.favoriteDao())
    val prefs = Prefs(app)
    val playerController = PlayerController(app)
}

class AuraApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(AlbumArtFetcher.Factory(this@AuraApplication)) }
            .build()
}

fun Context.appContainer(): AppContainer = (applicationContext as AuraApplication).container
