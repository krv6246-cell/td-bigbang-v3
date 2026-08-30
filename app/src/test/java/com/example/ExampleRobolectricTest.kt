package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.SingularityRecord
import com.example.data.SingularityRepository
import com.example.game.GameEngine
import com.example.game.HapticFeedbackEvent
import com.example.game.Level1_Config
import com.example.game.Level2_Config
import com.example.game.Level3_Config
import com.example.game.QuantumType
import com.example.haptics.HapticManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
}
