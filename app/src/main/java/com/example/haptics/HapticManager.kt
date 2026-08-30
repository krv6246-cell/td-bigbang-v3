package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.game.QuantumType

/**
 * Cyber-Zen Tactile Feedback Engine
 * Provides nuanced haptic responses for gesture mechanics, particle repulsion,
 * gravitational dilation anchoring, frequency matching, and level transitions.
 */
class HapticManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var lastResonanceHapticTime = 0L
    private var lastRepelHapticTime = 0L

    /**
     * Tactile response when a quantum bubble is stabilized or burst.
     */
    fun onQuantumHarvest(type: QuantumType) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        try {
            when (type) {
                QuantumType.TURQUOISE_BUBBLE -> {
                    // Light crisp tick
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(10)
                    }
                }
                QuantumType.JADE_NODE -> {
                    // Harmonic click
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(20)
                    }
                }
                QuantumType.GOLDEN_DCOIN -> {
                    // Crisp double-pulse confirmation
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 15, 30, 20), -1)
                    }
                }
                QuantumType.ANTIMATTER_ANOMALY -> {
                    // Heavy slice feedback
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(35)
                    }
                }
                QuantumType.PHANTOM_SINGULARITY -> {
                    // Sharp hazard warning tremor
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val timings = longArrayOf(0, 40, 30, 60)
                        val amplitudes = intArrayOf(0, 255, 0, 200)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 40, 30, 60), -1)
                    }
                }
            }
        } catch (_: Exception) {
            // Silently swallow if device does not support specific haptic vibration pattern
        }
    }

    /**
     * Tactile feedback for Level 2 particle repulsion deflection / boundary rebounds.
     */
    fun onParticleRepel() {
        val now = System.currentTimeMillis()
        if (now - lastRepelHapticTime < 100) return // Rate-limit subtle repel ticks
        lastRepelHapticTime = now

        if (vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(8, 70))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(8)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile feedback during Long-Press Gravitational Dilation Anchor.
     */
    fun onDilationAnchorPulse() {
        if (vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(18, 90))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(18)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile feedback for fast multi-bubble swipe sweeps.
     */
    fun onSwipeSweep(count: Int) {
        if (vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    if (count > 1) VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    else VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate((count * 12L).coerceIn(15L, 45L))
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile feedback during Level 3 Frequency Matching (Resonance Tuning).
     * Modulates vibration intensity and texture based on harmonic alignment precision [0.0..1.0].
     */
    fun onFrequencyResonanceMatch(precision: Float) {
        val now = System.currentTimeMillis()
        val interval = if (precision > 0.85f) 80L else 140L
        if (now - lastResonanceHapticTime < interval) return
        lastResonanceHapticTime = now

        if (vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitude = ((precision.coerceIn(0.1f, 1.0f)) * 255).toInt().coerceIn(1, 255)
                val duration = (10 + (precision * 15)).toLong()
                vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    /**
     * Rich tactile transition wave when activating Level 2 (Quasar) or Level 3 (Point Zero).
     */
    fun onLevelTransition(level: Int) {
        if (vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = if (level == 2) {
                    longArrayOf(0, 30, 40, 50, 60, 90)
                } else {
                    longArrayOf(0, 20, 30, 40, 50, 60, 70, 100)
                }
                val amplitudes = if (level == 2) {
                    intArrayOf(0, 100, 0, 160, 0, 240)
                } else {
                    intArrayOf(0, 80, 0, 120, 0, 180, 0, 255)
                }
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 30, 40, 50, 60, 90), -1)
            }
        } catch (_: Exception) {}
    }
}
