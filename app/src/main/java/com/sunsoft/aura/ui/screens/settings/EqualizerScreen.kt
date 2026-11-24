package com.sunsoft.aura.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sunsoft.aura.equalizer.EqualizerManager
import com.sunsoft.aura.ui.components.EmptyState
import com.sunsoft.aura.ui.theme.AuraAccent
import com.sunsoft.aura.ui.theme.AuraTextSecondary
import com.sunsoft.aura.util.Prefs
import kotlinx.coroutines.launch

@Composable
fun EqualizerScreen(prefs: Prefs, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var enabled by remember { mutableStateOf(EqualizerManager.isEnabled) }
    var bands by remember { mutableStateOf(EqualizerManager.bands()) }
    var bassBoost by remember { mutableStateOf(EqualizerManager.currentBassBoostStrength().toFloat()) }

    fun persist() {
        scope.launch {
            prefs.setEqualizerEnabled(enabled)
            prefs.setEqualizerBandsCsv(bands.joinToString(",") { it.levelMb.toString() })
            prefs.setBassBoost(bassBoost)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
            Text("Equalizer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        if (!EqualizerManager.isAvailable || bands.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Equalizer,
                title = "Equalizer not ready yet",
                message = "Start playing a song first, then come back here - the equalizer attaches to the active playback session."
            )
            return@Column
        }

        ListItem(
            headlineContent = { Text("Enable equalizer") },
            trailingContent = {
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        EqualizerManager.setEnabled(it)
                        persist()
                    }
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Flat" to EqualizerManager.Preset.FLAT,
                "Bass" to EqualizerManager.Preset.BASS,
                "Treble" to EqualizerManager.Preset.TREBLE,
                "Vocal" to EqualizerManager.Preset.VOCAL
            ).forEach { (label, preset) ->
                AssistChip(
                    onClick = {
                        EqualizerManager.applyPreset(preset)
                        bands = EqualizerManager.bands()
                        persist()
                    },
                    label = { Text(label) }
                )
            }
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            bands.forEachIndexed { i, band ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (band.frequencyHz >= 1000) "${band.frequencyHz / 1000}k" else "${band.frequencyHz}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraTextSecondary,
                        modifier = Modifier.width(36.dp)
                    )
                    Slider(
                        value = band.levelMb.toFloat(),
                        onValueChange = { newValue ->
                            val level = newValue.toInt().toShort()
                            EqualizerManager.setBandLevel(band.index, level)
                            bands = bands.toMutableList().also { it[i] = band.copy(levelMb = level) }
                        },
                        onValueChangeFinished = { persist() },
                        valueRange = band.minMb.toFloat()..band.maxMb.toFloat(),
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = AuraAccent, activeTrackColor = AuraAccent)
                    )
                }
            }

            Text(
                "Bass boost",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 12.dp)
            )
            Slider(
                value = bassBoost,
                onValueChange = {
                    bassBoost = it
                    EqualizerManager.setBassBoostStrength(it.toInt().toShort())
                },
                onValueChangeFinished = { persist() },
                valueRange = 0f..1000f,
                colors = SliderDefaults.colors(thumbColor = AuraAccent, activeTrackColor = AuraAccent)
            )
        }
    }
}
