package com.sunsoft.aura.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.sunsoft.aura.appContainer

class AuraViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val container = application.appContainer()
        return when {
            modelClass.isAssignableFrom(LibraryViewModel::class.java) ->
                LibraryViewModel(container.mediaStoreRepository, container.favoritesRepository) as T

            modelClass.isAssignableFrom(PlayerViewModel::class.java) ->
                PlayerViewModel(container.playerController, container.favoritesRepository, container.prefs) as T

            modelClass.isAssignableFrom(PlaylistViewModel::class.java) ->
                PlaylistViewModel(container.playlistRepository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
