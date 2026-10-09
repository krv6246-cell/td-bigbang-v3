package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin

/**
 * Real-time pure-sine & harmonic wave synthesizer for Point Zero (Level 3).
 * Modulates pitch and harmonic resonance based on the player's frequency tuning
 * and pitch-holding mechanic using low-latency PCM 16-bit AudioTrack streaming.
 */
class ResonanceSynthesizer {

    private companion object {
        const val TAG = "ResonanceSynthesizer"
        const val SAMPLE_RATE = 44100
        const val BUFFER_SIZE_SAMPLES = 1024
        const val MIN_BASE_FREQ = 220.0f // A3
        const val MAX_BASE_FREQ = 660.0f // E5
        const val TWO_PI = 2.0 * PI
    }

    private var audioTrack: AudioTrack? = null
    private var synthThread: Thread? = null
    private val isRunning = AtomicBoolean(false)
    private val isMuted = AtomicBoolean(false)

    // Modulated synth parameters (thread-safe volatile)
    @Volatile private var targetFrequencyNorm: Float = 0.5f
    @Volatile private var playerFrequencyNorm: Float = 0.5f
    @Volatile private var isPlayerResonating: Boolean = false
    @Volatile private var resonancePrecision: Float = 0.0f
    @Volatile private var resonanceHoldDuration: Float = 0.0f
    @Volatile private var isLevel3Active: Boolean = false

    // Smooth internal parameters for audio generation loop
    private var currentTargetFreq = 440.0
    private var currentPlayerFreq = 440.0
    private var currentCarrierAmp = 0.0
    private var currentPlayerAmp = 0.0
    private var currentHarmonicAmp = 0.0
    private var phaseTarget = 0.0
    private var phasePlayer = 0.0
    private var phaseSub = 0.0
    private var phaseHarmonic = 0.0
    private var phaseModulation = 0.0

    /**
     * Start the real-time audio synthesis thread.
     */
    @Synchronized
    fun start() {
        if (isRunning.get()) return

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, BUFFER_SIZE_SAMPLES * 2 * 2)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isRunning.set(true)

            synthThread = Thread({ runSynthesisLoop() }, "ResonanceSynthThread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
            Log.d(TAG, "Resonance Synthesizer started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack", e)
        }
    }

    /**
     * Update dynamic synthesis modulation parameters from the game loop.
     */
    fun updateParameters(
        level: Int,
        targetFreq: Float,
        playerFreq: Float,
        isResonating: Boolean,
        precision: Float,
        holdDuration: Float
    ) {
        isLevel3Active = (level == 3)
        targetFrequencyNorm = targetFreq
        playerFrequencyNorm = playerFreq
        isPlayerResonating = isResonating
        resonancePrecision = precision
        resonanceHoldDuration = holdDuration
    }

    fun setMuted(muted: Boolean) {
        isMuted.set(muted)
    }

    fun isMuted(): Boolean = isMuted.get()

    fun pause() {
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    pause()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing synth: ${e.message}", e)
        }
    }

    fun resume() {
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PAUSED) {
                    play()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming synth: ${e.message}", e)
        }
    }

    /**
     * Main audio synthesis PCM generation loop running on dedicated high-priority audio thread.
     */
    private fun runSynthesisLoop() {
        val audioBuffer = ShortArray(BUFFER_SIZE_SAMPLES)

        while (isRunning.get()) {
            if (!isLevel3Active || isMuted.get()) {
                // Decay volume smoothly to zero, output silence
                if (currentCarrierAmp > 0.001 || currentPlayerAmp > 0.001) {
                    currentCarrierAmp *= 0.85
                    currentPlayerAmp *= 0.85
                    currentHarmonicAmp *= 0.85
                } else {
                    currentCarrierAmp = 0.0
                    currentPlayerAmp = 0.0
                    currentHarmonicAmp = 0.0
                    audioBuffer.fill(0)
                    audioTrack?.write(audioBuffer, 0, BUFFER_SIZE_SAMPLES)
                    continue
                }
            }

            // Map normalized frequencies (0..1) to audible Hertz (220 Hz - 660 Hz)
            val desiredTargetHz = (MIN_BASE_FREQ + targetFrequencyNorm * (MAX_BASE_FREQ - MIN_BASE_FREQ)).toDouble()
            val desiredPlayerHz = (MIN_BASE_FREQ + playerFrequencyNorm * (MAX_BASE_FREQ - MIN_BASE_FREQ)).toDouble()

            // Proximity to target frequency (0.0 far -> 1.0 exact match)
            val freqDiff = kotlin.math.abs(playerFrequencyNorm - targetFrequencyNorm).toDouble()
            val proximity = (1.0 - (freqDiff / 0.40)).coerceIn(0.0, 1.0)
            // Rising harmonic overtone pitch as player gets closer to target frequency
            val desiredHarmonicHz = desiredTargetHz * (1.0 + proximity * 0.75)

            // Desired amplitude targets
            val targetCarrierVol = if (isLevel3Active && !isMuted.get()) 0.18 else 0.0
            val targetPlayerVol = if (isLevel3Active && isPlayerResonating && !isMuted.get()) 0.32 else 0.0
            val targetHarmonicVol = if (isLevel3Active && isPlayerResonating && !isMuted.get()) (proximity * 0.35) else 0.0

            for (i in 0 until BUFFER_SIZE_SAMPLES) {
                // Smooth frequency glide (portamento) per sample
                currentTargetFreq += (desiredTargetHz - currentTargetFreq) * 0.002
                currentPlayerFreq += (desiredPlayerHz - currentPlayerFreq) * 0.005

                // Smooth amplitude envelope (attack / release) per sample
                currentCarrierAmp += (targetCarrierVol - currentCarrierAmp) * 0.0015
                currentPlayerAmp += (targetPlayerVol - currentPlayerAmp) * 0.003
                currentHarmonicAmp += (targetHarmonicVol - currentHarmonicAmp) * 0.002

                // Phase accumulators for seamless, click-free pitch modulation
                val dtTarget = TWO_PI * currentTargetFreq / SAMPLE_RATE
                val dtPlayer = TWO_PI * currentPlayerFreq / SAMPLE_RATE
                val dtSub = TWO_PI * (currentTargetFreq * 0.5) / SAMPLE_RATE
                val dtHarmonic = TWO_PI * desiredHarmonicHz / SAMPLE_RATE
                val dtMod = TWO_PI * (3.0 + proximity * 8.0) / SAMPLE_RATE // Vibrato/tremolo shimmer

                phaseTarget = (phaseTarget + dtTarget) % TWO_PI
                phasePlayer = (phasePlayer + dtPlayer) % TWO_PI
                phaseSub = (phaseSub + dtSub) % TWO_PI
                phaseHarmonic = (phaseHarmonic + dtHarmonic) % TWO_PI
                phaseModulation = (phaseModulation + dtMod) % TWO_PI

                // 1. Target Carrier Wave (warm ambient sine with subtle sub-octave)
                val targetWave = sin(phaseTarget) * 0.75 + sin(phaseSub) * 0.25
                val carrierSignal = targetWave * currentCarrierAmp

                // 2. Player Pitch Wave (pure sine with responsive gliding)
                val playerWave = sin(phasePlayer)
                val playerSignal = playerWave * currentPlayerAmp

                // 3. Resonance Harmonic & Shimmer (triggered when holding pitch alignment)
                val holdBonus = (resonanceHoldDuration / 5.0f).coerceIn(0f, 1f).toDouble()
                val shimmerLfo = 1.0 + 0.15 * sin(phaseModulation) * holdBonus
                val harmonicWave = (sin(phaseHarmonic) * 0.6 + sin(phaseTarget * 1.5) * 0.4) * shimmerLfo
                val harmonicSignal = harmonicWave * currentHarmonicAmp * (1.0 + holdBonus * 0.5)

                // Sum all components and soft clip
                val combined = (carrierSignal + playerSignal + harmonicSignal)
                val softClipped = combined.coerceIn(-0.95, 0.95)

                audioBuffer[i] = (softClipped * 32767.0).toInt().toShort()
            }

            audioTrack?.write(audioBuffer, 0, BUFFER_SIZE_SAMPLES)
        }
    }

    /**
     * Stop and release AudioTrack resources.
     */
    @Synchronized
    fun release() {
        isRunning.set(false)
        try {
            synthThread?.interrupt()
            synthThread = null
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    stop()
                }
                release()
            }
            audioTrack = null
            Log.d(TAG, "Resonance Synthesizer released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio track", e)
        }
    }
}
