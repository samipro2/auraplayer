package com.sunsoft.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sunsoft.aura.ui.navigation.AuraScaffold
import com.sunsoft.aura.ui.screens.permission.PermissionScreen
import com.sunsoft.aura.ui.theme.AuraTheme
import com.sunsoft.aura.viewmodel.AuraViewModelFactory
import com.sunsoft.aura.viewmodel.LibraryViewModel
import com.sunsoft.aura.viewmodel.PlayerViewModel
import com.sunsoft.aura.viewmodel.PlaylistViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AuraApp(application = application)
        }
    }
}

@Composable
private fun AuraApp(application: android.app.Application) {
    val factory = remember { AuraViewModelFactory(application) }

    val libraryViewModel: LibraryViewModel = viewModel(factory = factory)
    val playerViewModel: PlayerViewModel = viewModel(factory = factory)
    val playlistViewModel: PlaylistViewModel = viewModel(factory = factory)
    val prefs = remember { application.appContainer().prefs }

    val amoledBlack by prefs.amoledBlack.collectAsStateWithLifecycle(initialValue = false)

    AuraTheme(amoledBlack = amoledBlack) {
        var permissionGranted by remember { mutableStateOf(false) }

        if (!permissionGranted) {
            PermissionScreen(onGranted = { permissionGranted = true })
        } else {
            AuraScaffold(
                libraryViewModel = libraryViewModel,
                playerViewModel = playerViewModel,
                playlistViewModel = playlistViewModel,
                prefs = prefs
            )
        }
    }
}
