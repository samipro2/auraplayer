package com.sunsoft.aura.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer

data class EqBand(val index: Short, val frequencyHz: Int, val minMb: Short, val maxMb: Short, val levelMb: Short)

/**
 * The service and the Settings/Equalizer screen run in the same process, so a plain
 * singleton is enough to share one Equalizer/BassBoost instance between them - no
 * cross-process plumbing needed.
 */
object EqualizerManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var attachedSessionId: Int = 0

    val isAvailable: Boolean get() = equalizer != null
    val isEnabled: Boolean get() = equalizer?.enabled ?: false
    fun currentBassBoostStrength(): Short = try { bassBoost?.roundedStrength ?: 0 } catch (e: Exception) { 0 }

    fun attach(audioSessionId: Int) {
        if (audioSessionId == attachedSessionId && equalizer != null) return
        release()
        attachedSessionId = audioSessionId
        if (audioSessionId == 0) return
        try {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = false }
            bassBoost = BassBoost(0, audioSessionId).apply { enabled = false }
        } catch (e: Exception) {
            equalizer = null
            bassBoost = null
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        equalizer = null
        bassBoost = null
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
    }

    fun bands(): List<EqBand> {
        val eq = equalizer ?: return emptyList()
        val range = eq.bandLevelRange
        return (0 until eq.numberOfBands.toInt()).map { i ->
            val band = i.toShort()
            EqBand(
                index = band,
                frequencyHz = eq.getCenterFreq(band) / 1000,
                minMb = range[0],
                maxMb = range[1],
                levelMb = eq.getBandLevel(band)
            )
        }
    }

    fun setBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }

    fun setBassBoostStrength(strength: Short) {
        bassBoost?.setStrength(strength)
    }

    /** A few hand-picked starting points; the user can nudge sliders from there. */
    fun applyPreset(preset: Preset) {
        val eq = equalizer ?: return
        val bandCount = eq.numberOfBands.toInt()
        val curve = when (preset) {
            Preset.FLAT -> IntArray(bandCount) { 0 }
            Preset.BASS -> intArrayOf(600, 500, 300, 0, -100, -100, 0, 0, 0, 0).take(bandCount).toIntArray()
            Preset.TREBLE -> intArrayOf(0, 0, 0, 0, 100, 200, 400, 500, 600, 600).take(bandCount).toIntArray()
            Preset.VOCAL -> intArrayOf(-200, -100, 0, 200, 400, 400, 200, 0, -100, -200).take(bandCount).toIntArray()
        }
        curve.forEachIndexed { index, mb ->
            val range = eq.bandLevelRange
            val clamped = mb.coerceIn(range[0].toInt(), range[1].toInt()).toShort()
            eq.setBandLevel(index.toShort(), clamped)
        }
    }

    enum class Preset { FLAT, BASS, TREBLE, VOCAL }
}
