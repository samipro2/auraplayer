package com.sunsoft.aura.ui.screens.settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sunsoft.aura.ui.theme.AuraAccent
import com.sunsoft.aura.ui.theme.AuraTextSecondary
import com.sunsoft.aura.util.Prefs
import com.sunsoft.aura.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    prefs: Prefs,
    libraryViewModel: LibraryViewModel,
    onOpenEqualizer: () -> Unit,
    onOpenCredits: () -> Unit
) {
    val amoled by prefs.amoledBlack.collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        )

        ListItem(
            headlineContent = { Text("Equalizer") },
            supportingContent = { Text("5-band EQ, bass boost & presets", color = AuraTextSecondary) },
            leadingContent = { Icon(Icons.Filled.Equalizer, contentDescription = null, tint = AuraAccent) },
            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
            modifier = Modifier.clickableRipple(onOpenEqualizer)
        )

        ListItem(
            headlineContent = { Text("Pure black theme") },
            supportingContent = { Text("Deeper blacks, easier on AMOLED screens", color = AuraTextSecondary) },
            trailingContent = {
                Switch(
                    checked = amoled,
                    onCheckedChange = { checked -> scope.launch { prefs.setAmoledBlack(checked) } }
                )
            }
        )

        ListItem(
            headlineContent = { Text("Rescan library") },
            supportingContent = { Text("Pick up new files added to the device", color = AuraTextSecondary) },
            leadingContent = { Icon(Icons.Filled.Refresh, contentDescription = null) },
            modifier = Modifier.clickableRipple { libraryViewModel.loadLibrary() }
        )

        ListItem(
            headlineContent = { Text("About Aura") },
            supportingContent = { Text("Credits & version", color = AuraTextSecondary) },
            leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
            modifier = Modifier.clickableRipple(onOpenCredits)
        )
    }
}

private fun Modifier.clickableRipple(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
