package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Идентификаторы операторов (Burst, Freeze, Revive, Magnet, Shield)
 * Каждый оператор обладает собственным узнаваемым тембром звучания (длительность ~0.3 сек).
 */
enum class OperatorSound {
    BURST,   // Мощный взрывной импульс (низкий тон + шум + быстрый спад)
    FREEZE,  // Кристаллический высокий тон с ледяной модуляцией
    REVIVE,  // Восходящий мажорный арпеджио-глиссандо
    MAGNET,  // Пульсирующий электромагнитный свист с LFO
    SHIELD   // Металлический резонансный колокол с суб-басом
}

/**
 * Внутренняя структура синтезируемого звукового события с генератором огибающей (Envelope)
 */
private class ActiveSoundEvent(
    val id: Long,
    val durationSamples: Int,
    val sampleGenerator: (sampleIndex: Int, progress: Double) -> Double
) {
    var currentSample = 0
}

/**
 * Комплексный аудио-модуль синтеза в реальном времени (WebAudio / Pure PCM Synth Architecture).
 *
 * Синтезирует без внешних mp3/wav файлов:
 * 1. Тап по Dcoin — мягкий «дзынь» с повышением тона по цепочке комбо (каждое 5-е комбо выше).
 * 2. Тап по пустому месту / phantom — глухой низкий «тук» без вибрации.
 * 3. Критическая плотность (density < 10%) — нарастающий низкий гул + тревожный синхронный пульс.
 * 4. Singularity Gateway — глубокий нарастающий «свист-провал» (dive + rumble) падения в чёрную дыру (1.5–2 сек).
 * 5. Активация операторов (Burst/Freeze/Revive/Magnet/Shield) — уникальный тембр 0.3 сек.
 * 6. Эмбиент-музыка: 8–16 тактов процедурной музыки (Expansion: колокольные тона, Equilibrium: баланс,
 *    Compression: мягкий пульсирующий бас, Point Zero: тёплые мажорные обертона). Громкость музыки 30% от эффектов.
 * 7. Ленивая инициализация по первому тапу (user gesture).
 */
class BigBangAudioEngine(context: Context) {

    companion object {
        private const val TAG = "BigBangAudioEngine"
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 1024
        private const val TWO_PI = 2.0 * PI
        private const val PREFS_NAME = "big_bang_audio_prefs"
        private const val PREF_KEY_MUTED = "is_audio_muted"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Флаги состояния аудио
    private val isRunning = AtomicBoolean(false)
    private val isInitialized = AtomicBoolean(false)
    private val isMuted = AtomicBoolean(prefs.getBoolean(PREF_KEY_MUTED, false))

    private var audioTrack: AudioTrack? = null
    private var synthThread: Thread? = null

    // Очередь воспроизведения звуковых эффектов
    private val activeSounds = mutableListOf<ActiveSoundEvent>()
    private val pendingSounds = ConcurrentLinkedQueue<ActiveSoundEvent>()
    private var nextEventId = 1L

    // Параметры вселенной для эмбиент-музыки и тревоги
    @Volatile var currentLevel: Int = 1
    @Volatile var currentDensity: Float = 100f
    @Volatile var isTransitioning: Boolean = false
    @Volatile var transitionProgress: Float = 0f
    @Volatile var gamePhaseId: Int = 1 // 1: Expansion, 2: Equilibrium, 3: Compression, etc.

    // Внутренние фазы для музыкального генератора
    private var musicStepTimer = 0
    private var musicMeasure = 0
    private var musicStep = 0
    private var bassPulsePhase = 0.0
    private var alarmPhase = 0.0

    // Активные ноты музыкального эмбиента
    private val musicNoteVoices = Array(4) { MusicVoice() }

    private class MusicVoice {
        var active = false
        var freq = 0.0
        var phase = 0.0
        var sampleCount = 0
        var totalSamples = 0
        var maxAmp = 0.0
        var decayRate = 0.0
    }

    /**
     * Возвращает текущее состояние Mute.
     */
    fun isMuted(): Boolean = isMuted.get()

    /**
     * Переключение Mute с сохранением в SharedPreferences между сессиями.
     */
    fun toggleMute(): Boolean {
        val newMuted = !isMuted.get()
        isMuted.set(newMuted)
        prefs.edit().putBoolean(PREF_KEY_MUTED, newMuted).apply()
        return newMuted
    }

    fun setMuted(muted: Boolean) {
        isMuted.set(muted)
        prefs.edit().putBoolean(PREF_KEY_MUTED, muted).apply()
    }

    /**
     * Ленивая инициализация аудио-контекста по первому тапу/действию пользователя (Mobile User Gesture Requirement).
     */
    fun ensureAudioInitialized() {
        if (isInitialized.get()) return
        synchronized(this) {
            if (isInitialized.get()) return
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, BUFFER_SIZE * 2)

                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                val track = AudioTrack(
                    attributes,
                    format,
                    bufferSize,
                    AudioTrack.MODE_STREAM,
                    android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
                )

                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.play()
                    audioTrack = track
                    isRunning.set(true)
                    isInitialized.set(true)

                    synthThread = Thread(::audioSynthesisLoop, "BigBangAudioThread").apply {
                        priority = Thread.MAX_PRIORITY
                        start()
                    }
                    Log.d(TAG, "BigBang Audio Engine lazy-initialized on user gesture successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize AudioTrack: ${e.message}", e)
            }
        }
    }

    // ==========================================
    // ЗВУКОВЫЕ ЭФФЕКТЫ (FX)
    // ==========================================

    /**
     * ЗВУК 1: Тап по Dcoin — короткий мягкий «дзынь» с нарастанием частоты по комбо.
     * Каждое 5-е комбо совершает скачок в октаву.
     */
    fun playDcoinHarvest(combo: Int) {
        ensureAudioInitialized()
        if (isMuted.get()) return

        // Базовая пентатоническая шкала Cyber-Zen (A4, B4, C#5, E5, F#5)
        val pentatonicScale = doubleArrayOf(440.0, 493.88, 554.37, 659.25, 739.99, 880.0, 987.77, 1108.73, 1318.5)
        val scaleIndex = (combo % pentatonicScale.size)
        val octaveShift = (combo / 5) * 0.25 // Плавный подъем регистра на каждые 5 комбо
        val baseFreq = pentatonicScale[scaleIndex] * (1.0 + octaveShift)

        val duration = 0.22 // секунды
        val totalSamples = (duration * SAMPLE_RATE).toInt()

        pendingSounds.add(
            ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                // Мягкий колокольный «дзынь»: синус + мажорный обертон 2.75x + затухание exp
                val t = sIdx.toDouble() / SAMPLE_RATE
                val envelope = exp(-progress * 9.0) * (1.0 - progress) // Экспоненциальный спад
                val fundamental = sin(TWO_PI * baseFreq * t)
                val overtone = sin(TWO_PI * (baseFreq * 2.76) * t) * 0.35
                val harmonic = sin(TWO_PI * (baseFreq * 4.0) * t) * 0.15
                (fundamental + overtone + harmonic) * envelope * 0.7
            }
        )
    }

    /**
     * ЗВУК 2: Тап по пустому месту или phantom-частице — глухой низкий «тук» без вибрации.
     */
    fun playEmptyTapOrPhantom(isPhantom: Boolean = false) {
        ensureAudioInitialized()
        if (isMuted.get()) return

        val duration = if (isPhantom) 0.28 else 0.14
        val totalSamples = (duration * SAMPLE_RATE).toInt()
        val startFreq = if (isPhantom) 160.0 else 120.0
        val endFreq = if (isPhantom) 45.0 else 55.0

        pendingSounds.add(
            ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                val t = sIdx.toDouble() / SAMPLE_RATE
                // Быстрый питч-дроп вниз создаёт глухой перкуссионный стук («тук»)
                val currentFreq = startFreq + (endFreq - startFreq) * (progress * progress)
                val envelope = (1.0 - progress) * exp(-progress * 14.0)
                val wave = sin(TWO_PI * currentFreq * t)
                val sub = sin(TWO_PI * (currentFreq * 0.5) * t) * 0.4
                // Лёгкий суб-щелчок для атаки
                val click = if (sIdx < 120) (Random.nextDouble() - 0.5) * 0.25 else 0.0
                (wave + sub + click) * envelope * 0.55
            }
        )
    }

    /**
     * ЗВУК SWEEP STREAM: Уровень 1 (свист-кристалл), Уровень 2 (энергичный ионный вжик), Уровень 3 (мягкий колокольный перезвон-эхо).
     */
    fun playSweepStreamSound(level: Int, dissolvedPhantoms: Boolean = false) {
        ensureAudioInitialized()
        if (isMuted.get()) return

        when (level) {
            3 -> {
                // Level 3 (Point Zero): Тонкий мягкий колокольный звук с лёгким эхо-затуханием
                val duration = if (dissolvedPhantoms) 0.65 else 0.48
                val totalSamples = (duration * SAMPLE_RATE).toInt()
                val baseFreq = if (dissolvedPhantoms) 880.0 else 784.0 // A5 / G5

                pendingSounds.add(
                    ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                        val t = sIdx.toDouble() / SAMPLE_RATE
                        val envelope = exp(-progress * 5.0) * (1.0 - progress)
                        val bell1 = sin(TWO_PI * baseFreq * t)
                        val bell2 = sin(TWO_PI * (baseFreq * 2.76) * t) * 0.25
                        val echo = sin(TWO_PI * (baseFreq * 1.5) * (t - 0.08)) * exp(-progress * 4.0).coerceAtLeast(0.0) * 0.2
                        (bell1 + bell2 + echo) * envelope * 0.5
                    }
                )
            }
            2 -> {
                // Level 2 (Quasar): Плотный, энергичный ионный разряд («вжух» + затухание релятивистского потока)
                val duration = if (dissolvedPhantoms) 0.42 else 0.32
                val totalSamples = (duration * SAMPLE_RATE).toInt()

                pendingSounds.add(
                    ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                        val t = sIdx.toDouble() / SAMPLE_RATE
                        val freq = 520.0 * (1.0 - progress * 0.6) + sin(progress * 18.0) * 45.0
                        val envelope = (1.0 - progress) * exp(-progress * 7.5)
                        val wave = sin(TWO_PI * freq * t)
                        val sub = sin(TWO_PI * (freq * 0.5) * t) * 0.35
                        val noise = (Random.nextDouble() - 0.5) * (1.0 - progress) * 0.2
                        (wave + sub + noise) * envelope * 0.6
                    }
                )
            }
            else -> {
                // Level 1 (Big Bang): Тонкий свист квантового рассечения (воздушный кристалличный чирк)
                val duration = if (dissolvedPhantoms) 0.38 else 0.28
                val totalSamples = (duration * SAMPLE_RATE).toInt()

                pendingSounds.add(
                    ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                        val t = sIdx.toDouble() / SAMPLE_RATE
                        val freq = 680.0 + (1100.0 - 680.0) * (1.0 - progress)
                        val envelope = exp(-progress * 8.5) * (1.0 - progress)
                        val wave = sin(TWO_PI * freq * t)
                        val harmonic = sin(TWO_PI * (freq * 2.0) * t) * 0.22
                        (wave + harmonic) * envelope * 0.48
                    }
                )
            }
        }
    }

    /**
     * ЗВУК 4: Переход Singularity Gateway — глубокий нарастающий «свист-провал» (dive + rumble) падения в туннель (1.8 сек).
     */
    fun playSingularityGatewayDive() {
        ensureAudioInitialized()
        if (isMuted.get()) return

        val duration = 1.85 // секунды
        val totalSamples = (duration * SAMPLE_RATE).toInt()

        pendingSounds.add(
            ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                val t = sIdx.toDouble() / SAMPLE_RATE
                // 1. Свист-провал: частота экспоненциально падает от 850 Гц до суб-баса 35 Гц
                val pitch = 850.0 * exp(-progress * 3.2) + 35.0
                val diveWave = sin(TWO_PI * pitch * t)

                // 2. Нарастающий космический рокот (Rumble) + шум туннеля
                val rumbleFreq = 42.0 + sin(TWO_PI * 3.5 * t) * 12.0
                val rumbleWave = sin(TWO_PI * rumbleFreq * t) * (progress * 0.8)
                val noise = (Random.nextDouble() - 0.5) * (progress * 0.35)

                // Огибающая: атака -> плато -> мощное затухание в сингулярность
                val envelope = when {
                    progress < 0.15 -> progress / 0.15
                    progress < 0.85 -> 1.0
                    else -> (1.0 - progress) / 0.15
                }

                (diveWave * 0.5 + rumbleWave * 0.6 + noise) * envelope * 0.85
            }
        )
    }

    /**
     * ЗВУК РЕЗОНАНСА: Кристаллический колокольный аккорд при точном совпадении частоты (1.2 сек).
     */
    fun playResonanceBellChord() {
        ensureAudioInitialized()
        if (isMuted.get()) return

        val duration = 1.2
        val totalSamples = (duration * SAMPLE_RATE).toInt()
        val freqs = doubleArrayOf(528.0, 660.0, 792.0, 1056.0) // Ethereal Harmonic Solfeggio chord

        pendingSounds.add(
            ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                val t = sIdx.toDouble() / SAMPLE_RATE
                val envelope = exp(-progress * 4.2) * (1.0 - progress)
                var mix = 0.0
                for (f in freqs) {
                    mix += sin(TWO_PI * f * t) * 0.24
                    mix += sin(TWO_PI * (f * 2.01) * t) * 0.08
                }
                mix * envelope * 0.75
            }
        )
    }

    /**
     * ЗВУК ФИНАЛЬНОГО ТРИУМФА: Торжественный космический аккорд при завершении уровня / трансцендентности (1.8 сек).
     */
    fun playFinalTranscendentChord() {
        ensureAudioInitialized()
        if (isMuted.get()) return

        val duration = 1.85
        val totalSamples = (duration * SAMPLE_RATE).toInt()
        val freqs = doubleArrayOf(432.0, 540.0, 648.0, 864.0, 1296.0) // Pythagorean celestial harmonic series

        pendingSounds.add(
            ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                val t = sIdx.toDouble() / SAMPLE_RATE
                val envelope = when {
                    progress < 0.1 -> progress / 0.1
                    else -> exp(-progress * 2.8) * (1.0 - progress)
                }
                var mix = 0.0
                for ((idx, f) in freqs.withIndex()) {
                    val weight = 0.25 / (1.0 + idx * 0.3)
                    mix += sin(TWO_PI * f * t) * weight
                    mix += sin(TWO_PI * (f * 1.5) * t) * (weight * 0.35)
                }
                mix * envelope * 0.85
            }
        )
    }

    /**
     * ЗВУК 5: Активация оператора (Burst, Freeze, Revive, Magnet, Shield).
     * У каждого оператора уникальный тембр звучания (0.3 сек).
     */
    fun playOperatorSound(operator: OperatorSound) {
        ensureAudioInitialized()
        if (isMuted.get()) return

        val duration = 0.32
        val totalSamples = (duration * SAMPLE_RATE).toInt()

        pendingSounds.add(
            ActiveSoundEvent(nextEventId++, totalSamples) { sIdx, progress ->
                val t = sIdx.toDouble() / SAMPLE_RATE
                val envelope = (1.0 - progress) * (1.0 - progress)

                when (operator) {
                    OperatorSound.BURST -> {
                        // Низкий взрывной импульс с белым шумом и суб-басом
                        val freq = 180.0 * (1.0 - progress * 0.8) + 40.0
                        val sine = sin(TWO_PI * freq * t)
                        val noise = (Random.nextDouble() - 0.5) * 0.7 * (1.0 - progress)
                        (sine * 0.6 + noise) * envelope * 0.8
                    }
                    OperatorSound.FREEZE -> {
                        // Высокий кристаллический тон со звонкой ЧМ-модуляцией
                        val carrier = 1400.0
                        val mod = sin(TWO_PI * 480.0 * t) * 600.0
                        val wave = sin(TWO_PI * (carrier + mod) * t)
                        wave * envelope * 0.5
                    }
                    OperatorSound.REVIVE -> {
                        // Восходящий мажорный арпеджио-глиссандо (330 Гц -> 990 Гц)
                        val freq = 330.0 + (990.0 - 330.0) * (progress * progress)
                        val wave = sin(TWO_PI * freq * t) + sin(TWO_PI * freq * 1.5 * t) * 0.4
                        wave * envelope * 0.65
                    }
                    OperatorSound.MAGNET -> {
                        // Пульсирующий электромагнитный свист с быстрым LFO (30 Гц)
                        val lfo = 1.0 + 0.3 * sin(TWO_PI * 28.0 * t)
                        val freq = (480.0 + sin(TWO_PI * 14.0 * t) * 120.0) * lfo
                        val wave = sin(TWO_PI * freq * t)
                        wave * envelope * 0.6
                    }
                    OperatorSound.SHIELD -> {
                        // Резонансный колокольный суб-бас с богатым металлическим обертоном
                        val base = 196.0
                        val wave = sin(TWO_PI * base * t) * 0.7 +
                                sin(TWO_PI * base * 2.74 * t) * 0.4 +
                                sin(TWO_PI * base * 5.4 * t) * 0.2
                        wave * envelope * 0.75
                    }
                }
            }
        )
    }

    // ==========================================
    // ПРОЦЕДУРНЫЙ АУДИО-ПОТОК (LOOP)
    // ==========================================

    private fun audioSynthesisLoop() {
        val audioBuffer = ShortArray(BUFFER_SIZE)
        val stepSamples = (SAMPLE_RATE * 0.30).toInt() // Длина тактового шага (~100 BPM)

        while (isRunning.get()) {
            if (isMuted.get()) {
                audioBuffer.fill(0)
                audioTrack?.write(audioBuffer, 0, BUFFER_SIZE)
                continue
            }

            // Перенос ожидающих звуковых событий
            while (!pendingSounds.isEmpty()) {
                pendingSounds.poll()?.let { activeSounds.add(it) }
            }

            for (i in 0 until BUFFER_SIZE) {
                var fxSample = 0.0

                // 1. Микширование активных звуковых эффектов
                val sIterator = activeSounds.iterator()
                while (sIterator.hasNext()) {
                    val sound = sIterator.next()
                    val progress = sound.currentSample.toDouble() / sound.durationSamples
                    fxSample += sound.sampleGenerator(sound.currentSample, progress)
                    sound.currentSample++
                    if (sound.currentSample >= sound.durationSamples) {
                        sIterator.remove()
                    }
                }

                // 2. ЗВУК 3: Критическая плотность (density < 10%) — тревожный низкий гул + пульс
                var alarmSample = 0.0
                if (currentDensity < 10f && !isTransitioning) {
                    val severity = ((10f - currentDensity) / 10f).coerceIn(0f, 1f).toDouble()
                    alarmPhase = (alarmPhase + TWO_PI * 55.0 / SAMPLE_RATE) % TWO_PI
                    // Пульсирующий LFO гул (~2.5 Гц), синхронизированный с мерцанием рамки
                    val lfoPulse = (sin(alarmPhase * (2.5 / 55.0)) * 0.5 + 0.5).coerceIn(0.0, 1.0)
                    val rumble = sin(alarmPhase) * (0.35 + 0.35 * lfoPulse)
                    val warningBeep = if (lfoPulse > 0.8) sin(alarmPhase * 4.0) * 0.25 else 0.0
                    alarmSample = (rumble + warningBeep) * severity * 0.75
                }

                // 3. МУЗЫКА: Процедурный Cyber-Zen эмбиент (громкость 30% от эффектов)
                val musicSample = generateAmbientMusicSample(stepSamples)

                // Итоговое суммирование: Звуки (100%) + Тревога (100%) + Музыка (30%)
                val finalMix = fxSample + alarmSample + (musicSample * 0.30)
                val softClipped = finalMix.coerceIn(-0.95, 0.95)

                audioBuffer[i] = (softClipped * 32767.0).toInt().toShort()
            }

            audioTrack?.write(audioBuffer, 0, BUFFER_SIZE)
        }
    }

    /**
     * Генератор процедурной эмбиент-петли (8–16 тактов) в зависимости от фазы и уровня:
     * - Expansion: редкие низкие колокольные тона, медитативная тишина, 50–60 BPM.
     * - Compression: мягкий пульсирующий бас, нагнетание динамики.
     * - Point Zero (Золотая вселенная): тёплый тембр, мажорные обертона.
     */
    private fun generateAmbientMusicSample(stepSamples: Int): Double {
        musicStepTimer++
        if (musicStepTimer >= stepSamples) {
            musicStepTimer = 0
            musicStep = (musicStep + 1) % 16
            if (musicStep == 0) {
                musicMeasure = (musicMeasure + 1) % 8
            }
            triggerMusicStep(musicStep, musicMeasure)
        }

        var musicMix = 0.0

        // Синтез голосов колокольных тонов
        for (voice in musicNoteVoices) {
            if (voice.active) {
                voice.phase = (voice.phase + TWO_PI * voice.freq / SAMPLE_RATE) % TWO_PI
                val tProgress = voice.sampleCount.toDouble() / voice.totalSamples
                val env = exp(-tProgress * voice.decayRate) * (1.0 - tProgress)
                val wave = sin(voice.phase) * 0.75 + sin(voice.phase * 2.0) * 0.25
                musicMix += wave * env * voice.maxAmp

                voice.sampleCount++
                if (voice.sampleCount >= voice.totalSamples) {
                    voice.active = false
                }
            }
        }

        // В фазе Compression добавляется мягкий пульсирующий суб-бас
        if (gamePhaseId == 3 || currentLevel == 2) {
            val bassFreq = if (musicMeasure % 2 == 0) 55.0 else 65.41 // A1 / C2
            bassPulsePhase = (bassPulsePhase + TWO_PI * bassFreq / SAMPLE_RATE) % TWO_PI
            val pulseLfo = sin(TWO_PI * 1.5 * (musicStepTimer.toDouble() / stepSamples)) * 0.5 + 0.5
            val bassWave = sin(bassPulsePhase) * 0.35 * pulseLfo
            musicMix += bassWave
        }

        return musicMix
    }

    /**
     * Запуск нот процедурной партитуры на тактовых долях.
     */
    private fun triggerMusicStep(step: Int, measure: Int) {
        val isExpansion = gamePhaseId == 1 && currentLevel == 1
        val isPointZero = currentLevel == 3

        // Базовые частоты Cyber-Zen (A, D, E, G)
        val zenFrequencies = if (isPointZero) {
            // Мажорные гармонические обертона для финального уровня (E4, G#4, B4, E5)
            doubleArrayOf(329.63, 415.30, 493.88, 659.25)
        } else {
            // Медитативные пентатонические тона (A3, C4, D4, E4)
            doubleArrayOf(220.0, 261.63, 293.66, 329.63)
        }

        // В Expansion ноты звучат редко (много тишины, только на долях 0 и 8)
        val shouldPlay = if (isExpansion) {
            step == 0 || (step == 8 && measure % 2 == 1)
        } else {
            step % 4 == 0
        }

        if (shouldPlay) {
            val freq = zenFrequencies[(step / 4 + measure) % zenFrequencies.size]
            spawnMusicVoice(freq, durationSec = if (isExpansion) 2.4 else 1.2, isWarm = isPointZero)
        }
    }

    private fun spawnMusicVoice(frequency: Double, durationSec: Double, isWarm: Boolean) {
        val voice = musicNoteVoices.firstOrNull { !it.active } ?: musicNoteVoices[0]
        voice.active = true
        voice.freq = frequency
        voice.phase = 0.0
        voice.sampleCount = 0
        voice.totalSamples = (durationSec * SAMPLE_RATE).toInt()
        voice.maxAmp = if (isWarm) 0.35 else 0.28
        voice.decayRate = if (isWarm) 3.5 else 4.5
    }

    /**
     * Приостановка воспроизведения аудио при сворачивании приложения.
     */
    fun pauseAudio() {
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                    pause()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio: ${e.message}", e)
        }
    }

    /**
     * Возобновление воспроизведения аудио при возврате в приложение.
     */
    fun resumeAudio() {
        try {
            audioTrack?.apply {
                if (playState == AudioTrack.PLAYSTATE_PAUSED) {
                    play()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio: ${e.message}", e)
        }
    }

    /**
     * Освобождение системных ресурсов AudioTrack при уничтожении ViewModel.
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
            Log.d(TAG, "BigBang Audio Engine released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing BigBang Audio Engine: ${e.message}", e)
        }
    }
}
