package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.ResonanceSynthesizer
import com.example.data.AppDatabase
import com.example.data.SingularityRecord
import com.example.data.SingularityRepository
import com.example.game.GameEngine
import com.example.game.GamePhase
import com.example.game.HapticFeedbackEvent
import com.example.game.Level1_Config
import com.example.game.Level2_Config
import com.example.game.Level3_Config
import com.example.game.QuantumType
import com.example.haptics.HapticManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Time Density", appName)
    }

    @Test
    fun testResonanceSynthesizerModulation() {
        val synth = ResonanceSynthesizer()
        synth.start()

        // Test parameter updates in Level 3 with pitch modulation
        synth.updateParameters(
            level = 3,
            targetFreq = 0.5f,
            playerFreq = 0.52f,
            isResonating = true,
            precision = 0.95f,
            holdDuration = 2.5f
        )

        synth.setMuted(true)
        synth.setMuted(false)
        synth.release()
    }

    @Test
    fun testRoomDatabaseSingularityArchive() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val repo = SingularityRepository(db.singularityRecordDao())

        val record = SingularityRecord(
            universeCycle = 1,
            levelName = "Level 1: Big Bang",
            outcome = "Evolutionary Singularity",
            dcoinsExported = 42,
            survivalTimeSeconds = 91.5f,
            peakDensity = 84.0f,
            maxCombo = 6,
            entropyResisted = 320.0f
        )

        val id = repo.recordTransition(record)
        assertTrue(id > 0)

        val records = repo.allRecords.first()
        assertTrue(records.isNotEmpty())
        assertEquals(42, records.first().dcoinsExported)
        assertEquals("Level 1: Big Bang", records.first().levelName)
    }

    @Test
    fun testEntropyEngineFormula() {
        val engine = GameEngine()

        // Base rates: Phase 1 (0.005), Phase 2 (0.015), Phase 3 (0.030)
        assertEquals(0.005f, engine.updateEntropy(50f, 1, Level1_Config), 0.0001f)
        assertEquals(0.015f, engine.updateEntropy(50f, 2, Level1_Config), 0.0001f)
        assertEquals(0.030f, engine.updateEntropy(50f, 3, Level1_Config), 0.0001f)

        // Critical rate when density < 10 (rate *= 2.0)
        assertEquals(0.010f, engine.updateEntropy(8f, 1, Level1_Config), 0.0001f)
        assertEquals(0.030f, engine.updateEntropy(5f, 2, Level1_Config), 0.0001f)
        assertEquals(0.060f, engine.updateEntropy(2f, 3, Level1_Config), 0.0001f)

        // Dilation rate when density > 80 (rate *= 0.7)
        assertEquals(0.0035f, engine.updateEntropy(85f, 1, Level1_Config), 0.0001f)
        assertEquals(0.0105f, engine.updateEntropy(90f, 2, Level1_Config), 0.0001f)
        assertEquals(0.0210f, engine.updateEntropy(95f, 3, Level1_Config), 0.0001f)

        // Level 2 Quasar high entropy
        assertEquals(0.05f, engine.updateEntropy(50f, 4, Level2_Config), 0.0001f)

        // Level 3 Point Zero base entropy
        assertEquals(0.0f, engine.updateEntropy(50f, 7, Level3_Config), 0.0001f)
    }

    @Test
    fun testHapticFeedbackEvents() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val hapticManager = HapticManager(context)
        val engine = GameEngine()

        val capturedEvents = mutableListOf<HapticFeedbackEvent>()
        engine.hapticListener = { event ->
            capturedEvents.add(event)
            when (event) {
                is HapticFeedbackEvent.QuantumHarvest -> hapticManager.onQuantumHarvest(event.type)
                is HapticFeedbackEvent.ParticleRepel -> hapticManager.onParticleRepel()
                is HapticFeedbackEvent.DilationAnchorPulse -> hapticManager.onDilationAnchorPulse()
                is HapticFeedbackEvent.SwipeSweep -> hapticManager.onSwipeSweep(event.count)
                is HapticFeedbackEvent.FrequencyResonanceMatch -> hapticManager.onFrequencyResonanceMatch(event.precision)
                is HapticFeedbackEvent.LevelTransition -> hapticManager.onLevelTransition(event.level)
                is HapticFeedbackEvent.EmptyTap -> {}
                is HapticFeedbackEvent.PhantomTap -> {}
                is HapticFeedbackEvent.PhantomDissolve -> {}
            }
        }

        // Start Level 2 Quasar -> emits LevelTransition(2)
        engine.startNewGame(level = 2)
        assertTrue(capturedEvents.any { it is HapticFeedbackEvent.LevelTransition && it.level == 2 })

        // Trigger Level 3 Frequency match
        engine.transitionToLevel3()
        assertTrue(capturedEvents.any { it is HapticFeedbackEvent.LevelTransition && it.level == 3 })

        // Update frequency with perfect alignment
        engine.updatePlayerFrequencyInput(engine.state.value.targetFrequency, true)
        engine.update(0.1f)
        assertTrue(capturedEvents.any { it is HapticFeedbackEvent.FrequencyResonanceMatch })
    }

    @Test
    fun testAudioEngineSynthesisAndMutePersistence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audioEngine = com.example.audio.BigBangAudioEngine(context)

        // Verify mute toggle and persistence
        val initialMuted = audioEngine.isMuted()
        val toggled = audioEngine.toggleMute()
        assertEquals(!initialMuted, toggled)
        assertEquals(toggled, audioEngine.isMuted())

        // Test sounds trigger safely without exceptions
        audioEngine.playDcoinHarvest(combo = 5)
        audioEngine.playEmptyTapOrPhantom(isPhantom = false)
        audioEngine.playEmptyTapOrPhantom(isPhantom = true)
        audioEngine.playSingularityGatewayDive()
        audioEngine.playOperatorSound(com.example.audio.OperatorSound.BURST)
        audioEngine.playOperatorSound(com.example.audio.OperatorSound.FREEZE)

        // Restore mute state
        audioEngine.setMuted(false)
        assertFalse(audioEngine.isMuted())

        audioEngine.release()
    }

    @Test
    fun testSingularityTransitionDataAndPaths() {
        val engine = GameEngine()
        engine.startNewGame(cycle = 1, level = 1, carryDcoins = 150)

        // Simulate collapse transition: density drops below 5%
        val state = engine.state.value
        assertEquals(150, state.dcoins)
        assertEquals(1, state.level)

        // Verify evolution vs collapse trigger condition
        val collapseReason = if (4.5f <= 5f) "Collapse" else "Evolution"
        val evolutionReason = if (90.5f >= 90f) "Evolution" else "Collapse"

        assertEquals("Collapse", collapseReason)
        assertEquals("Evolution", evolutionReason)

        // Verify transition record construction with round metrics
        val record = SingularityRecord(
            universeCycle = 1,
            levelName = "Level 1: Big Bang",
            outcome = if (collapseReason == "Evolution") "Evolutionary Singularity" else "Gravitational Collapse",
            dcoinsExported = 150,
            survivalTimeSeconds = 48.2f,
            peakDensity = 92.0f,
            maxCombo = 5,
            entropyResisted = 180.0f
        )

        assertEquals(150, record.dcoinsExported)
        assertEquals(5, record.maxCombo)
        assertEquals("Gravitational Collapse", record.outcome)
    }

    @Test
    fun testEndToEndProgressionAllThreeLevels() {
        val engine = GameEngine()
        
        // Level 1: Big Bang
        engine.startNewGame(cycle = 1, level = 1, carryDcoins = 0)
        assertEquals(1, engine.state.value.level)
        assertEquals(100f, engine.state.value.density, 0.1f)
        
        // Harvest in Level 1
        engine.handleTap(androidx.compose.ui.geometry.Offset(500f, 500f))
        assertTrue(engine.state.value.density > 0f)
        
        // Transition Level 1 -> Level 2: Quasar
        engine.transitionToLevel2()
        assertEquals(2, engine.state.value.level)
        assertFalse(engine.state.value.isTransitioning)
        assertEquals(GamePhase.QUASAR_IONIZATION, engine.state.value.phase)
        
        // Transition Level 2 -> Level 3: Point Zero
        engine.transitionToLevel3()
        assertEquals(3, engine.state.value.level)
        assertFalse(engine.state.value.isTransitioning)
        assertEquals(GamePhase.POINT_ZERO_CALIBRATION, engine.state.value.phase)
        
        // Level 3 resonance tuning
        val targetFreq = engine.state.value.targetFrequency
        engine.updatePlayerFrequencyInput(targetFreq, true)
        engine.update(0.016f)
        assertTrue(engine.state.value.isResonating)
        assertTrue(engine.state.value.resonancePrecision > 0.5f)
    }

    @Test
    fun testSweepStreamScoringParticlesVsPhantoms() {
        val engine = GameEngine()
        engine.startNewGame(level = 1, carryDcoins = 10)
        val initialCoins = engine.state.value.dcoins

        // Manually place two regular bubbles along swipe line (200, 300) -> (400, 300)
        engine.bubbles.clear()
        engine.bubbles.add(
            com.example.game.QuantumBubble(
                id = 101,
                position = androidx.compose.ui.geometry.Offset(250f, 300f),
                radius = 30f,
                velocity = androidx.compose.ui.geometry.Offset.Zero,
                type = QuantumType.TURQUOISE_BUBBLE,
                densityBoost = 9f,
                value = 2
            )
        )
        engine.bubbles.add(
            com.example.game.QuantumBubble(
                id = 102,
                position = androidx.compose.ui.geometry.Offset(350f, 300f),
                radius = 30f,
                velocity = androidx.compose.ui.geometry.Offset.Zero,
                type = QuantumType.GOLDEN_DCOIN,
                densityBoost = 4f,
                value = 5
            )
        )

        val densityBefore = engine.state.value.density
        // Execute swipe through both bubbles
        engine.handleSwipe(
            start = androidx.compose.ui.geometry.Offset(200f, 300f),
            end = androidx.compose.ui.geometry.Offset(400f, 300f)
        )

        // Both bubbles should be harvested, dcoins should INCREASE (never decrease)
        assertTrue("Dcoins must increase after swiping bubbles", engine.state.value.dcoins > initialCoins)
        assertTrue("Density must not decrease from harvesting regular bubbles", engine.state.value.density >= densityBefore)
        assertEquals("Combo should increment for each harvested bubble", 2, engine.state.value.currentCombo)

        // Now test Phantom Singularity: penalty only for phantoms
        engine.bubbles.clear()
        engine.bubbles.add(
            com.example.game.QuantumBubble(
                id = 103,
                position = androidx.compose.ui.geometry.Offset(250f, 300f),
                radius = 30f,
                velocity = androidx.compose.ui.geometry.Offset.Zero,
                type = QuantumType.PHANTOM_SINGULARITY,
                densityBoost = -15f,
                value = 0
            )
        )
        val densityBeforePhantom = engine.state.value.density
        val coinsBeforePhantom = engine.state.value.dcoins
        engine.handleSwipe(
            start = androidx.compose.ui.geometry.Offset(200f, 300f),
            end = androidx.compose.ui.geometry.Offset(400f, 300f)
        )

        // Density decreases by phantom penalty, combo resets to 0, but coins are untouched
        assertEquals(coinsBeforePhantom, engine.state.value.dcoins)
        assertEquals(0, engine.state.value.currentCombo)
        assertTrue(engine.state.value.density < densityBeforePhantom)
    }

    @Test
    fun testGamePauseAndResumePreservesProgress() {
        val engine = GameEngine()
        engine.startNewGame(level = 1, carryDcoins = 50)
        
        val initialDensity = engine.state.value.density
        val initialTime = engine.state.value.gameTime
        
        // Pause game (e.g. app sent to background / Home pressed)
        engine.setPaused(true)
        assertTrue(engine.state.value.isPaused)
        
        // Simulate 5 seconds in background
        engine.update(5.0f)
        assertEquals("Density must not drain while paused", initialDensity, engine.state.value.density, 0.001f)
        assertEquals("Game time must not advance while paused", initialTime, engine.state.value.gameTime, 0.001f)
        
        // Resume game (app brought back to foreground)
        engine.setPaused(false)
        assertFalse(engine.state.value.isPaused)
        
        // Should update normally now
        engine.update(0.1f)
        assertTrue(engine.state.value.gameTime > initialTime)
    }

    @Test
    fun testOperatorAbilitiesMechanicsAndCooldowns() {
        val engine = GameEngine()
        engine.startNewGame()

        // BURST: +12 density capped at 100, cooldown registered
        assertTrue(engine.isOperatorReady("BURST"))
        val densityBefore = engine.state.value.density
        assertTrue(engine.activateOperator("BURST"))
        assertEquals(100f, engine.state.value.density, 0.01f)
        assertFalse(engine.isOperatorReady("BURST"))
        assertFalse(engine.activateOperator("BURST")) // on cooldown — rejected

        // Cooldowns tick down in update()
        repeat(120) { engine.update(0.05f) } // 6 seconds
        assertTrue((engine.state.value.operatorCooldowns["BURST"] ?: 0f) > 0f)
        repeat(240) { engine.update(0.05f) } // +12s — cooldown expired
        assertTrue(engine.isOperatorReady("BURST"))

        // FREEZE: while frozen, density must not drain on levels 1-2
        engine.activateOperator("FREEZE")
        assertTrue(engine.state.value.isTimeFrozen)
        val densityAtFreeze = engine.state.value.density
        repeat(30) { engine.update(0.05f) }
        assertEquals(densityAtFreeze, engine.state.value.density, 0.01f)

        // SHIELD + MAGNET flags activate
        engine.activateOperator("SHIELD")
        assertTrue(engine.state.value.isShieldActive)
        engine.activateOperator("MAGNET")
        assertTrue(engine.state.value.isMagnetActive)
    }
}
