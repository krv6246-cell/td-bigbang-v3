package com.example.ui

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SingularityRecord
import com.example.data.SingularityRepository
import com.example.game.GameEngine
import com.example.game.GameState
import com.example.game.HapticFeedbackEvent
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
    val engine = GameEngine()

    val gameState: StateFlow<GameState> = engine.state

    val archiveRecords: StateFlow<List<SingularityRecord>>
    val totalArchivedDcoins: StateFlow<Int?>

    private val _showArchiveDialog = MutableStateFlow(false)
    val showArchiveDialog: StateFlow<Boolean> = _showArchiveDialog.asStateFlow()

    private val _showHelpDialog = MutableStateFlow(false)
    val showHelpDialog: StateFlow<Boolean> = _showHelpDialog.asStateFlow()

    private val _showTransitionSummary = MutableStateFlow(false)
    val showTransitionSummary: StateFlow<Boolean> = _showTransitionSummary.asStateFlow()

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

        // Connect engine haptic callback to HapticManager
        engine.hapticListener = { event ->
            when (event) {
                is HapticFeedbackEvent.QuantumHarvest -> hapticManager.onQuantumHarvest(event.type)
                is HapticFeedbackEvent.ParticleRepel -> hapticManager.onParticleRepel()
                is HapticFeedbackEvent.DilationAnchorPulse -> hapticManager.onDilationAnchorPulse()
                is HapticFeedbackEvent.SwipeSweep -> hapticManager.onSwipeSweep(event.count)
                is HapticFeedbackEvent.FrequencyResonanceMatch -> hapticManager.onFrequencyResonanceMatch(event.precision)
                is HapticFeedbackEvent.LevelTransition -> hapticManager.onLevelTransition(event.level)
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
        }
    }

    fun restartGame(level: Int = 1) {
        hasHandledTransition = false
        _showTransitionSummary.value = false
        val current = engine.state.value
        val nextCycle = current.universeCycle + 1
        val carryCoins = if (level > 1) current.dcoins else 0
        engine.startNewGame(cycle = nextCycle, level = level, carryDcoins = carryCoins)
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

    fun onFrequencyDrag(yRatio: Float, isTouching: Boolean) {
        engine.updatePlayerFrequencyInput(yRatio, isTouching)
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

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }
}
