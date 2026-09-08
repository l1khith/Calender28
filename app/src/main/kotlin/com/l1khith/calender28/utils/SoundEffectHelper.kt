package com.l1khith.calender28.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Helper to safely play sound effects (e.g. fire crackle on streak tap, coin clink on coin tap).
 * If specific audio resources in res/raw exist (e.g. R.raw.fire_sound, R.raw.coin_sound), it plays them.
 * If raw files are absent, it safely plays Android system sound effects and haptic vibration feedback.
 */
object SoundEffectHelper {

    private const val TAG = "SoundEffectHelper"

    @Volatile private var soundPool: SoundPool? = null
    @Volatile private var fireSoundId: Int = 0
    @Volatile private var coinSoundId: Int = 0
    @Volatile private var isInitialized = false

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

            val fireResId = context.resources.getIdentifier("fire_sound", "raw", context.packageName)
            if (fireResId != 0) {
                fireSoundId = soundPool?.load(context, fireResId, 1) ?: 0
            }

            val coinResId = context.resources.getIdentifier("coin_sound", "raw", context.packageName)
            if (coinResId != 0) {
                coinSoundId = soundPool?.load(context, coinResId, 1) ?: 0
            }

            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundPool", e)
        }
    }

    fun playFireSound(context: Context) {
        if (!AppSettingsManager.enableSounds.value) return
        init(context)
        try {
            var played = false
            if (fireSoundId != 0) {
                val streamId = soundPool?.play(fireSoundId, 1f, 1f, 1, 0, 1f) ?: 0
                played = streamId != 0
            }

            // If soundPool hasn't finished loading or stream failed, play via MediaPlayer fallback
            if (!played) {
                val fireResId = context.resources.getIdentifier("fire_sound", "raw", context.packageName)
                if (fireResId != 0) {
                    val mp = android.media.MediaPlayer.create(context.applicationContext, fireResId)
                    mp?.apply {
                        setVolume(1.0f, 1.0f)
                        setOnCompletionListener { it.release() }
                        start()
                    }
                    played = true
                }
            }

            if (!played) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                audioManager?.playSoundEffect(android.media.AudioManager.FX_KEYPRESS_STANDARD, 1.0f)
            }
            vibrate(context, 40)
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing fire sound", e)
        }
    }

    fun playCoinSound(context: Context) {
        if (!AppSettingsManager.enableSounds.value) return
        init(context)
        try {
            var played = false
            if (coinSoundId != 0) {
                val streamId = soundPool?.play(coinSoundId, 1f, 1f, 1, 0, 1f) ?: 0
                played = streamId != 0
            }

            // If soundPool hasn't finished loading or stream failed, play via MediaPlayer fallback
            if (!played) {
                val coinResId = context.resources.getIdentifier("coin_sound", "raw", context.packageName)
                if (coinResId != 0) {
                    val mp = android.media.MediaPlayer.create(context.applicationContext, coinResId)
                    mp?.apply {
                        setVolume(1.0f, 1.0f)
                        setOnCompletionListener { it.release() }
                        start()
                    }
                    played = true
                }
            }

            if (!played) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                audioManager?.playSoundEffect(android.media.AudioManager.FX_KEY_CLICK, 1.0f)
            }
            vibrate(context, 25)
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing coin sound", e)
        }
    }

    private fun vibrate(context: Context, durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}
