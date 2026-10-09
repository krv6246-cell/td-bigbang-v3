package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AntimatterViolet
import com.example.ui.theme.CoronaAmber
import com.example.ui.theme.CriticalHorizon
import com.example.ui.theme.PhantomHazard
import com.example.ui.theme.QuantumJade
import com.example.ui.theme.QuasarBlueShift
import com.example.ui.theme.RadiantTurquoise
import com.example.ui.theme.SingularityGold

enum class GamePhase(val id: Int, val title: String, val subtitle: String, val color: Color) {
    START_SCREEN(0, "Start", "Initialization", RadiantTurquoise),
    EXPANSION(1, "Phase I: Expansion", "Cosmic Inflation • Turquoise Quanta", RadiantTurquoise),
    EQUILIBRIUM(2, "Phase II: Equilibrium", "Harmonic Balance • Jade & Gold Resonance", QuantumJade),
    COMPRESSION(3, "Phase III: Compression", "Gravitational Pull • Critical Horizon", SingularityGold),
    QUASAR_IONIZATION(4, "Level 2: Quasar Ionization", "Relativistic Jet Outflow • Blue-Shift", QuasarBlueShift),
    QUASAR_EQUILIBRIUM(5, "Level 2: High Energy Core", "Accretion Dynamics • High Velocity", RadiantTurquoise),
    QUASAR_RELATIVISTIC(6, "Level 2: Relativistic Horizon", "Extreme Repulsion • Max Dcoins", SingularityGold),
    POINT_ZERO_CALIBRATION(7, "Level 3: Calibration", "Carrier Wave Tuning • Standing Wave", RadiantTurquoise),
    POINT_ZERO_RESONANCE(8, "Level 3: Point Zero", "Harmonic Lock • Zero Entropy", SingularityGold)
}

enum class QuantumType {
    TURQUOISE_BUBBLE,
    JADE_NODE,
    GOLDEN_DCOIN,
    ANTIMATTER_ANOMALY,
    PHANTOM_SINGULARITY
}

data class LevelConfig(
    val level: Int = 1,
    val baseEntropy: Float = 0.005f,
    val spawnRate: Float = 1.0f,
    val particleBehavior: String = "attract", // "attract" (Level 1) or "repel" (Level 2) or "resonance" (Level 3)
    val phantomPenalty: Float = 0f,
    val currencyMultiplier: Float = 1.0f
)

val Level1_Config = LevelConfig(
    level = 1,
    baseEntropy = 0.005f,
    spawnRate = 1.0f,
    particleBehavior = "attract",
    phantomPenalty = 0f,
    currencyMultiplier = 1.0f
)

val Level2_Config = LevelConfig(
    level = 2,
    baseEntropy = 0.05f,
    spawnRate = 2.0f,
    particleBehavior = "repel",
    phantomPenalty = 15f,
    currencyMultiplier = 1.5f
)

val Level3_Config = LevelConfig(
    level = 3,
    baseEntropy = 0.0f,
    spawnRate = 0.8f,
    particleBehavior = "resonance",
    phantomPenalty = 10f,
    currencyMultiplier = 2.0f
)

sealed interface HapticFeedbackEvent {
    data class QuantumHarvest(val type: QuantumType) : HapticFeedbackEvent
    data object ParticleRepel : HapticFeedbackEvent
    data object DilationAnchorPulse : HapticFeedbackEvent
    data class SwipeSweep(val count: Int) : HapticFeedbackEvent
    data class FrequencyResonanceMatch(val precision: Float) : HapticFeedbackEvent
    data class LevelTransition(val level: Int) : HapticFeedbackEvent
    data object EmptyTap : HapticFeedbackEvent
    data object PhantomTap : HapticFeedbackEvent
    data object PhantomDissolve : HapticFeedbackEvent
}

data class QuantumBubble(
    val id: Long,
    var position: Offset,
    var velocity: Offset,
    var radius: Float,
    val type: QuantumType,
    var lifetime: Float = 1.0f,
    var maxLifetime: Float = 12.0f,
    var pulsePhase: Float = 0f,
    val value: Int = 1,
    val densityBoost: Float = 8f
)

data class Particle(
    var position: Offset,
    var velocity: Offset,
    var alpha: Float,
    var size: Float,
    var color: Color,
    var life: Float,
    var maxLife: Float
)

data class Shockwave(
    val center: Offset,
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float,
    val color: Color
)

data class GestureTrail(
    val id: Long = 0L,
    val points: List<Offset>,
    var alpha: Float = 1.0f,
    val color: Color = RadiantTurquoise,
    val level: Int = 1
)

data class FloatingText(
    val id: Long,
    val text: String,
    var position: Offset,
    var alpha: Float = 1f,
    val color: Color
)
