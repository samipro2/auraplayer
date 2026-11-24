package com.sunsoft.aura.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "aura_prefs")

class Prefs(private val context: Context) {

    private object Keys {
        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val SHUFFLE = booleanPreferencesKey("shuffle_enabled")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val EQ_BANDS = stringPreferencesKey("eq_bands_csv")
        val BASS_BOOST = floatPreferencesKey("bass_boost")
        val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
    }

    val repeatMode: Flow<String> = context.dataStore.data.map { it[Keys.REPEAT_MODE] ?: "OFF" }
    val shuffleEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHUFFLE] ?: false }
    val playbackSpeed: Flow<Float> = context.dataStore.data.map { it[Keys.PLAYBACK_SPEED] ?: 1f }
    val equalizerEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.EQ_ENABLED] ?: false }
    val equalizerBandsCsv: Flow<String> = context.dataStore.data.map { it[Keys.EQ_BANDS] ?: "" }
    val bassBoost: Flow<Float> = context.dataStore.data.map { it[Keys.BASS_BOOST] ?: 0f }
    val amoledBlack: Flow<Boolean> = context.dataStore.data.map { it[Keys.AMOLED_BLACK] ?: false }

    suspend fun setRepeatMode(value: String) = context.dataStore.edit { it[Keys.REPEAT_MODE] = value }
    suspend fun setShuffleEnabled(value: Boolean) = context.dataStore.edit { it[Keys.SHUFFLE] = value }
    suspend fun setPlaybackSpeed(value: Float) = context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = value }
    suspend fun setEqualizerEnabled(value: Boolean) = context.dataStore.edit { it[Keys.EQ_ENABLED] = value }
    suspend fun setEqualizerBandsCsv(value: String) = context.dataStore.edit { it[Keys.EQ_BANDS] = value }
    suspend fun setBassBoost(value: Float) = context.dataStore.edit { it[Keys.BASS_BOOST] = value }
    suspend fun setAmoledBlack(value: Boolean) = context.dataStore.edit { it[Keys.AMOLED_BLACK] = value }
}
