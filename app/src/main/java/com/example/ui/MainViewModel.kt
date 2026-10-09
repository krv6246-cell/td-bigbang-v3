package com.example.ui

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BigBangAudioEngine
import com.example.audio.OperatorSound
import com.example.audio.ResonanceSynthesizer
import com.example.data.AppDatabase
import com.example.data.SingularityRecord
import com.example.data.SingularityRepository
import com.example.game.GameEngine
import com.example.game.GameState
import com.example.game.HapticFeedbackEvent
import com.example.game.QuantumType
import com.example.haptics.HapticManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SingularityRepository
    val hapticManager = HapticManager(application.applicationContext)
    val synth = ResonanceSynthesizer()
    val audioEngine = BigBangAudioEngine(application.applicationContext)
    val engine = GameEngine()

    val gameState: StateFlow<GameState> = engine.state

    val archiveRecords: StateFlow<List<SingularityRecord>>
    val totalArchivedDcoins: StateFlow<Int?>

    private val _isAudioMuted = MutableStateFlow(audioEngine.isMuted())
    val isAudioMuted: StateFlow<Boolean> = _isAudioMuted.asStateFlow()

    private val _showArchiveDialog = MutableStateFlow(false)
    val showArchiveDialog: StateFlow<Boolean> = _showArchiveDialog.asStateFlow()

    private val _showHelpDialog = MutableStateFlow(false)
    val showHelpDialog: StateFlow<Boolean> = _showHelpDialog.asStateFlow()

    private val _showTransitionSummary = MutableStateFlow(false)
    val showTransitionSummary: StateFlow<Boolean> = _showTransitionSummary.asStateFlow()

    private val level3Prefs = application.getSharedPreferences("level3_point_zero_prefs", android.content.Context.MODE_PRIVATE)
    private val _showLevel3Tutorial = MutableStateFlow(!level3Prefs.getBoolean("level3_tutorial_dismissed", false))
    val showLevel3Tutorial: StateFlow<Boolean> = _showLevel3Tutorial.asStateFlow()

    private var wasResonating = false

    private val _lastExportRecord = MutableStateFlow<SingularityRecord?>(null)
    val lastExportRecord: StateFlow<SingularityRecord?> = _lastExportRecord.asStateFlow()

    private var gameLoopJob: Job? = null
    private var hasHandledTransition = false

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SingularityRepository(db.singularityRecordDao())
        archiveRecords = repository.allRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        totalArchivedDcoins = repository.totalDcoins.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        // Start real-time audio synthesizer for resonance pitch feedback
        synth.start()
        synth.setMuted(audioEngine.isMuted())

        // Connect engine haptic callback to HapticManager and BigBangAudioEngine
        engine.hapticListener = { event ->
            when (event) {
                is HapticFeedbackEvent.QuantumHarvest -> {
                    hapticManager.onQuantumHarvest(event.type)
                    if (event.type == QuantumType.PHANTOM_SINGULARITY) {
                        audioEngine.playEmptyTapOrPhantom(isPhantom = true)
                    } else {
                        audioEngine.playDcoinHarvest(engine.state.value.currentCombo)
                    }
                }
                is HapticFeedbackEvent.ParticleRepel -> hapticManager.onParticleRepel()
                is HapticFeedbackEvent.DilationAnchorPulse -> hapticManager.onDilationAnchorPulse()
                is HapticFeedbackEvent.SwipeSweep -> {
                    hapticManager.onSwipeSweep(event.count)
                    audioEngine.playSweepStreamSound(engine.state.value.level, dissolvedPhantoms = false)
                }
                is HapticFeedbackEvent.PhantomDissolve -> {
                    hapticManager.onSwipeSweep(2)
                    audioEngine.playSweepStreamSound(engine.state.value.level, dissolvedPhantoms = true)
                }
                is HapticFeedbackEvent.FrequencyResonanceMatch -> {
                    hapticManager.onFrequencyResonanceMatch(event.precision)
                    if (event.precision >= 0.85f && !wasResonating) {
                        audioEngine.playResonanceBellChord()
                        wasResonating = true
                    } else if (event.precision < 0.60f) {
                        wasResonating = false
                    }
                }
                is HapticFeedbackEvent.LevelTransition -> {
                    hapticManager.onLevelTransition(event.level)
                    audioEngine.playSingularityGatewayDive()
                }
                is HapticFeedbackEvent.EmptyTap -> {
                    audioEngine.playEmptyTapOrPhantom(isPhantom = false)
                }
                is HapticFeedbackEvent.PhantomTap -> {
                    audioEngine.playEmptyTapOrPhantom(isPhantom = true)
                }
            }
        }

        startGameLoop()
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val deltaSec = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                engine.update(deltaSec)

                val state = engine.state.value

                // Синхронизация состояния вселенной с BigBangAudioEngine
                audioEngine.currentLevel = state.level
                audioEngine.currentDensity = state.density
                audioEngine.isTransitioning = state.isTransitioning
                audioEngine.transitionProgress = state.transitionProgress
                audioEngine.gamePhaseId = state.phase.id

                // Modulate real-time synthesizer pitch & harmonics
                synth.updateParameters(
                    level = state.level,
                    targetFreq = state.targetFrequency,
                    playerFreq = state.playerFrequency,
                    isResonating = state.isResonating,
                    precision = state.resonancePrecision,
                    holdDuration = state.resonanceHoldDuration
                )

                if (state.isTransitioning && state.transitionProgress >= 1f && !hasHandledTransition) {
                    hasHandledTransition = true
                    handleSingularityExport(state)
                }

                delay(16) // ~60 FPS
            }
        }
    }

    private fun handleSingularityExport(state: GameState) {
        viewModelScope.launch {
            val levelTitle = when (state.level) {
                1 -> "Level 1: Big Bang"
                2 -> "Level 2: Quasar Dynamics"
                else -> "Level 3: Point Zero"
            }
            val outcomeDesc = if (state.transitionReason.contains("Evolution")) "Evolutionary Singularity" else "Gravitational Collapse"

            val record = SingularityRecord(
                universeCycle = state.universeCycle,
                levelName = levelTitle,
                outcome = outcomeDesc,
                dcoinsExported = state.dcoins,
                survivalTimeSeconds = state.gameTime,
                peakDensity = state.density,
                maxCombo = state.maxCombo,
                entropyResisted = state.entropyResistedTotal
            )
            repository.recordTransition(record)
            _lastExportRecord.value = record
            _showTransitionSummary.value = true

            if (state.transitionReason.contains("Evolution", ignoreCase = true) || state.level == 3) {
                audioEngine.playFinalTranscendentChord()
            }
        }
    }

    fun onAppBackgrounded() {
        engine.setPaused(true)
        audioEngine.pauseAudio()
        synth.pause()
    }

    fun onAppForegrounded() {
        engine.setPaused(false)
        audioEngine.resumeAudio()
        synth.resume()
    }

    fun exitGame() {
        engine.setPaused(true)
        audioEngine.pauseAudio()
        synth.pause()
        engine.stopEngine()
        gameLoopJob?.cancel()
        gameLoopJob = null
        engine.resetToStartScreen()
    }

    fun restartGame(level: Int = 1) {
        hasHandledTransition = false
        _showTransitionSummary.value = false
        if (level == 1) {
            engine.resetToStartScreen()
        } else {
            val current = engine.state.value
            val nextCycle = current.universeCycle + 1
            val carryCoins = if (level > 1) current.dcoins else 0
            engine.startNewGame(cycle = nextCycle, level = level, carryDcoins = carryCoins)
        }
    }

    fun startGameFromScreen() {
        audioEngine.playSweepStreamSound(1, false)
        engine.startNewGame(cycle = 1, level = 1, carryDcoins = 0)
    }

    fun proceedToLevel2() {
        hasHandledTransition = false
        _showTransitionSummary.value = false
        val current = engine.state.value
        engine.startNewGame(cycle = current.universeCycle, level = 2, carryDcoins = current.dcoins)
    }

    fun proceedToLevel3() {
        hasHandledTransition = false
        _showTransitionSummary.value = false
        val current = engine.state.value
        engine.startNewGame(cycle = current.universeCycle, level = 3, carryDcoins = current.dcoins)
    }

    fun onSingleTap(pos: Offset) {
        engine.handleTap(pos)
    }

    fun onLongPressStart(pos: Offset) {
        engine.setDilationAnchor(true, pos)
    }

    fun onLongPressEnd() {
        engine.setDilationAnchor(false, null)
    }

    fun onSwipe(start: Offset, end: Offset) {
        engine.handleSwipe(start, end)
    }

    fun onFrequencyDrag(ratio: Float, isTouching: Boolean) {
        if (_showLevel3Tutorial.value && isTouching) {
            dismissLevel3Tutorial()
        }
        engine.updatePlayerFrequencyInput(ratio, isTouching)
    }

    fun dismissLevel3Tutorial() {
        if (_showLevel3Tutorial.value) {
            _showLevel3Tutorial.value = false
            level3Prefs.edit().putBoolean("level3_tutorial_dismissed", true).apply()
        }
    }

    fun toggleArchiveDialog(show: Boolean) {
        _showArchiveDialog.value = show
    }

    fun toggleHelpDialog(show: Boolean) {
        _showHelpDialog.value = show
    }

    fun clearArchiveLedger() {
        viewModelScope.launch {
            repository.clearArchive()
        }
    }

    fun toggleAudioMute() {
        val newMuted = audioEngine.toggleMute()
        _isAudioMuted.value = newMuted
        synth.setMuted(newMuted)
    }

    /**
     * Активация оператора (Burst, Freeze, Revive, Magnet, Shield) —
     * механика выполняется в GameEngine, звук и хаптика только при успешной активации.
     */
    fun activateOperator(operator: OperatorSound) {
        val id = when (operator) {
            OperatorSound.BURST -> "BURST"
            OperatorSound.FREEZE -> "FREEZE"
            OperatorSound.REVIVE -> "REVIVE"
            OperatorSound.MAGNET -> "MAGNET"
            OperatorSound.SHIELD -> "SHIELD"
        }
        if (engine.activateOperator(id)) {
            audioEngine.playOperatorSound(operator)
            hapticManager.onParticleRepel()
        }
    }

    fun isOperatorReady(id: String): Boolean = engine.isOperatorReady(id)

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        synth.release()
        audioEngine.release()
    }
}
