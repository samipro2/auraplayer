package com.sunsoft.aura.playback

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sunsoft.aura.equalizer.EqualizerManager
import com.sunsoft.aura.util.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Keeps playback alive while the app is backgrounded and surfaces the
 * system media notification (play/pause/skip on the lock screen, etc).
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSession.Builder(this, player).build()

        // Session id is assigned as soon as the player exists, so the equalizer can
        // attach straight away - same process as the UI, so it's a direct call.
        EqualizerManager.attach(player.audioSessionId)
        restorePersistedEqualizerSettings()
    }

    private fun restorePersistedEqualizerSettings() {
        val prefs = Prefs(applicationContext)
        serviceScope.launch {
            val enabled = prefs.equalizerEnabled.first()
            val csv = prefs.equalizerBandsCsv.first()
            val bass = prefs.bassBoost.first()
            EqualizerManager.setEnabled(enabled)
            if (csv.isNotBlank()) {
                csv.split(",").forEachIndexed { index, value ->
                    value.toShortOrNull()?.let { EqualizerManager.setBandLevel(index.toShort(), it) }
                }
            }
            EqualizerManager.setBassBoostStrength(bass.toInt().toShort())
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val session = mediaSession ?: return
        val currentPlayer = session.player
        if (!currentPlayer.playWhenReady || currentPlayer.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        EqualizerManager.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        serviceScope.coroutineContext.cancelChildren()
        super.onDestroy()
    }
}
