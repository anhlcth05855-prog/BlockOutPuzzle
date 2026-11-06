package com.blockout.puzzle.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var soundEnabled: Boolean = true
    var hapticEnabled: Boolean = true

    private var soundPool: SoundPool? = null
    private var soundClickId: Int = 0
    private var soundSlideId: Int = 0
    private var soundBumpId: Int = 0
    private var soundEscapeId: Int = 0
    private var soundWinId: Int = 0
    private var soundBoosterId: Int = 0
    private var soundHammerId: Int = 0

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool = pool

            // Generate clean lightweight PCM WAV files in cache directory and load into SoundPool
            val cacheDir = context.cacheDir

            val clickFile = File(cacheDir, "sfx_click.wav")
            writeWav(clickFile, generateTonePcm(frequency = 520.0, durationMs = 30, decay = true))
            soundClickId = pool.load(clickFile.absolutePath, 1)

            val slideFile = File(cacheDir, "sfx_slide.wav")
            writeWav(slideFile, generateTonePcm(frequency = 420.0, durationMs = 25, decay = true))
            soundSlideId = pool.load(slideFile.absolutePath, 1)

            val bumpFile = File(cacheDir, "sfx_bump.wav")
            writeWav(bumpFile, generateTonePcm(frequency = 190.0, durationMs = 35, decay = true))
            soundBumpId = pool.load(bumpFile.absolutePath, 1)

            val escapeFile = File(cacheDir, "sfx_escape.wav")
            writeWav(escapeFile, generateChirpPcm(startFreq = 440.0, endFreq = 980.0, durationMs = 110))
            soundEscapeId = pool.load(escapeFile.absolutePath, 1)

            val winFile = File(cacheDir, "sfx_win.wav")
            writeWav(winFile, generateArpeggioPcm(listOf(523.25, 659.25, 783.99, 1046.50), noteDurationMs = 70))
            soundWinId = pool.load(winFile.absolutePath, 1)

            val boosterFile = File(cacheDir, "sfx_booster.wav")
            writeWav(boosterFile, generateChirpPcm(startFreq = 260.0, endFreq = 720.0, durationMs = 90))
            soundBoosterId = pool.load(boosterFile.absolutePath, 1)

            val hammerFile = File(cacheDir, "sfx_hammer.wav")
            writeWav(hammerFile, generateNoisePcm(durationMs = 80))
            soundHammerId = pool.load(hammerFile.absolutePath, 1)
        } catch (_: Exception) {}
    }

    fun vibrate(type: HapticType = HapticType.LIGHT) {
        if (!hapticEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createOneShot(14, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.MEDIUM -> VibrationEffect.createOneShot(28, 160)
                    HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 25, 30, 40), intArrayOf(0, 140, 0, 220), -1)
                    HapticType.EXPLODE -> VibrationEffect.createOneShot(50, 255)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val duration = when (type) {
                    HapticType.LIGHT -> 14L
                    HapticType.MEDIUM -> 28L
                    HapticType.SUCCESS -> 60L
                    HapticType.EXPLODE -> 50L
                }
                vibrator.vibrate(duration)
            }
        } catch (_: Exception) {}
    }

    fun playClick() {
        playSound(soundClickId, volume = 0.8f)
    }

    fun playSlide() {
        playSound(soundSlideId, volume = 0.65f)
    }

    fun playBump() {
        playSound(soundBumpId, volume = 0.7f)
    }

    fun playEscape() {
        playSound(soundEscapeId, volume = 1.0f)
    }

    fun playWin() {
        playSound(soundWinId, volume = 1.0f)
    }

    fun playBooster() {
        playSound(soundBoosterId, volume = 0.9f)
    }

    fun playHammer() {
        playSound(soundHammerId, volume = 0.95f)
    }

    private fun playSound(soundId: Int, volume: Float) {
        if (!soundEnabled || soundId == 0) return
        try {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (_: Exception) {}
    }

    // Audio PCM Generation Helpers
    private fun generateTonePcm(frequency: Double, durationMs: Int, decay: Boolean): ShortArray {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = if (decay) 1.0 - (i.toDouble() / numSamples) else 1.0
            buffer[i] = (sin(2.0 * PI * frequency * t) * env * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return buffer
    }

    private fun generateChirpPcm(startFreq: Double, endFreq: Double, durationMs: Int): ShortArray {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val p = i.toDouble() / numSamples
            val curFreq = startFreq + (endFreq - startFreq) * p
            val t = i.toDouble() / sampleRate
            val env = 1.0 - p * 0.35
            buffer[i] = (sin(2.0 * PI * curFreq * t) * env * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return buffer
    }

    private fun generateArpeggioPcm(frequencies: List<Double>, noteDurationMs: Int): ShortArray {
        val sampleRate = 22050
        val noteSamples = (noteDurationMs * sampleRate) / 1000
        val totalSamples = noteSamples * frequencies.size
        val buffer = ShortArray(totalSamples)

        for (n in frequencies.indices) {
            val freq = frequencies[n]
            for (i in 0 until noteSamples) {
                val t = i.toDouble() / sampleRate
                val env = 1.0 - (i.toDouble() / noteSamples)
                val idx = n * noteSamples + i
                buffer[idx] = (sin(2.0 * PI * freq * t) * env * Short.MAX_VALUE * 0.7).toInt().toShort()
            }
        }
        return buffer
    }

    private fun generateNoisePcm(durationMs: Int): ShortArray {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val env = 1.0 - (i.toDouble() / numSamples)
            buffer[i] = ((Math.random() * 2.0 - 1.0) * env * Short.MAX_VALUE * 0.8).toInt().toShort()
        }
        return buffer
    }

    private fun writeWav(file: File, pcm: ShortArray) {
        val sampleRate = 22050
        val channels = 1
        val byteRate = sampleRate * channels * 2
        val dataSize = pcm.size * 2
        val totalSize = 36 + dataSize

        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray())
            header.putInt(totalSize)
            header.put("WAVE".toByteArray())
            header.put("fmt ".toByteArray())
            header.putInt(16) // Subchunk1Size
            header.putShort(1) // PCM format
            header.putShort(channels.toShort())
            header.putInt(sampleRate)
            header.putInt(byteRate)
            header.putShort((channels * 2).toShort()) // block align
            header.putShort(16) // bits per sample
            header.put("data".toByteArray())
            header.putInt(dataSize)

            out.write(header.array())

            val dataBytes = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcm) {
                dataBytes.putShort(sample)
            }
            out.write(dataBytes.array())
        }
    }

    enum class HapticType {
        LIGHT,
        MEDIUM,
        SUCCESS,
        EXPLODE
    }
}
