package uk.chinnidiwakar.sliptrack

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun pulse(duration: Long, amplitude: Int) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(duration)
            }
        }
    }

    fun waveform(pattern: LongArray, amplitudes: IntArray? = null) {
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (amplitudes != null) {
                    VibrationEffect.createWaveform(pattern, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(pattern, -1)
                }
                it.vibrate(effect)
            }
        }
    }

    fun cancel() {
        vibrator?.cancel()
    }

    fun victory() {
        waveform(
            longArrayOf(0, 40, 60, 60),
            intArrayOf(0, 255, 0, 200)
        )
    }

    fun slip() {
        waveform(
            longArrayOf(0, 50),
            intArrayOf(0, 120)
        )
    }

    fun emergencyGround() {
        waveform(
            longArrayOf(0, 80),
            intArrayOf(0, 220)
        )
    }

    fun breatheInhale() {
        pulse(70, 180)
    }

    fun breatheHold() {
        pulse(25, 80)
    }

    fun breatheExhale() {
        pulse(60, 150)
    }

}
