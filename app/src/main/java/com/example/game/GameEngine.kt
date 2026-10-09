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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class GameState(
    val level: Int = 1, // 1 = Big Bang, 2 = Quasar, 3 = Point Zero
    val config: LevelConfig = Level1_Config,
    val universeCycle: Int = 1,
    val phase: GamePhase = GamePhase.START_SCREEN,
    val density: Float = 100.0f, // 0..100%
    val dcoins: Int = 0,
    val currentCombo: Int = 0,
    val maxCombo: Int = 0,
    val gameTime: Float = 0.0f, // seconds
    val entropyRate: Float = 0.005f,
    val entropyResistedTotal: Float = 0f,
    val isPaused: Boolean = false,
    val isTransitioning: Boolean = false,
    val transitionProgress: Float = 0f,
    val transitionReason: String = "", // "Evolution", "Collapse", "QuasarTransition", "ZeroPointTransition"
    val isBlueShiftActive: Boolean = false,
    val blueShiftIntensity: Float = 0f,
    val isDilationAnchorActive: Boolean = false,
    val anchorPosition: Offset? = null,
    val quasarBeamAngle: Float = 0f,
    // Level 3 Resonance Fields
    val targetFrequency: Float = 0.5f,
    val playerFrequency: Float = 0.0f,
    val isResonating: Boolean = false,
    val resonancePrecision: Float = 0.0f,
    val resonanceHoldDuration: Float = 0.0f,
    // Operator ability states (Burst / Freeze / Revive / Magnet / Shield)
    val isTimeFrozen: Boolean = false,
    val isShieldActive: Boolean = false,
    val isMagnetActive: Boolean = false,
    val operatorCooldowns: Map<String, Float> = emptyMap()
)

class GameEngine {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    var hapticListener: ((HapticFeedbackEvent) -> Unit)? = null

    // Physics entities
    val bubbles = mutableListOf<QuantumBubble>()
    val particles = mutableListOf<Particle>()
    val shockwaves = mutableListOf<Shockwave>()
    val floatingTexts = mutableListOf<FloatingText>()
    val gestureTrails = mutableListOf<GestureTrail>()

    private var nextEntityId = 1L
    private var spawnTimer = 0f
    private var ambientParticleTimer = 0f
    private var dilationHapticTimer = 0f
    private var screenWidth = 1080f
    private var screenHeight = 1920f
    private var targetOscillatorPhase = 0f

    // Operator ability timers
    private val operatorCooldownsInternal = mutableMapOf<String, Float>()
    private var freezeTimer = 0f
    private var shieldTimer = 0f
    private var magnetTimer = 0f

    companion object {
        const val OPERATOR_COOLDOWN = 12f
        const val FREEZE_DURATION = 5f
        const val SHIELD_DURATION = 8f
        const val MAGNET_DURATION = 2.5f
    }

    // Base entropy rates for Level 1
    private val baseEntropyL1 = floatArrayOf(0.005f, 0.015f, 0.03f)

    fun updateScreenDimensions(width: Float, height: Float) {
        if (width > 0 && height > 0) {
            screenWidth = width
            screenHeight = height
        }
    }

    /**
     * Entropy Engine calculation:
     * Level 1: [0.005, 0.015, 0.03]
     * Level 2: 0.05
     * Level 3: 0.0 (controlled by resonance noise variance)
     * Dynamic critical acceleration (density < 10 -> * 2.0) and dilation (density > 80 -> * 0.7).
     */
    fun updateEntropy(density: Float, phaseId: Int, config: LevelConfig): Float {
        var rate = when (config.level) {
            2 -> config.baseEntropy
            3 -> 0.0f
            else -> baseEntropyL1.getOrElse(phaseId - 1) { 0.015f }
        }

        if (config.level != 3) {
            if (density < 10f) {
                rate *= 2.0f
            } else if (density > 80f) {
                rate *= 0.7f
            }
        }
        return rate
    }

    fun resetToStartScreen() {
        bubbles.clear()
        particles.clear()
        shockwaves.clear()
        floatingTexts.clear()
        gestureTrails.clear()
        _state.value = GameState(
            phase = GamePhase.START_SCREEN,
            density = 100.0f
        )
    }

    fun startNewGame(cycle: Int = 1, level: Int = 1, carryDcoins: Int = 0) {
        bubbles.clear()
        particles.clear()
        shockwaves.clear()
        floatingTexts.clear()
        gestureTrails.clear()
        operatorCooldownsInternal.clear()
        freezeTimer = 0f
        shieldTimer = 0f
        magnetTimer = 0f

        val config = when (level) {
            2 -> Level2_Config
            3 -> Level3_Config
            else -> Level1_Config
        }

        val phase = when (level) {
            2 -> GamePhase.QUASAR_IONIZATION
            3 -> GamePhase.POINT_ZERO_CALIBRATION
            else -> GamePhase.EXPANSION
        }

        _state.value = GameState(
            level = level,
            config = config,
            universeCycle = cycle,
            phase = phase,
            density = 100.0f,
            dcoins = carryDcoins,
            gameTime = 0.0f,
            entropyRate = updateEntropy(100f, phase.id, config),
            isPaused = false,
            isTransitioning = false,
            transitionProgress = 0f,
            isBlueShiftActive = (level == 2),
            blueShiftIntensity = if (level == 2) 1.0f else 0f
        )

        hapticListener?.invoke(HapticFeedbackEvent.LevelTransition(level))
    }

    /**
     * TRANSITION TO LEVEL 2: Quasar Dynamics
     */
    fun transitionToLevel2() {
        val current = _state.value
        bubbles.clear()
        gestureTrails.clear()

        _state.value = current.copy(
            level = 2,
            config = Level2_Config,
            phase = GamePhase.QUASAR_IONIZATION,
            gameTime = 0f,
            isBlueShiftActive = true,
            blueShiftIntensity = 1.0f,
            isPaused = false,
            isTransitioning = false,
            transitionProgress = 0f
        )

        initQuasarVisuals()
        hapticListener?.invoke(HapticFeedbackEvent.LevelTransition(2))
    }

    /**
     * TRANSITION TO LEVEL 3: Point Zero / Frequency Resonance
     */
    fun transitionToLevel3() {
        val current = _state.value
        bubbles.clear()
        particles.clear()
        gestureTrails.clear()

        _state.value = current.copy(
            level = 3,
            config = Level3_Config,
            phase = GamePhase.POINT_ZERO_CALIBRATION,
            gameTime = 0f,
            isBlueShiftActive = false,
            density = 100f,
            targetFrequency = 0.5f,
            playerFrequency = 0.0f,
            isResonating = false,
            resonancePrecision = 0.0f,
            resonanceHoldDuration = 0.0f,
            isPaused = false,
            isTransitioning = false,
            transitionProgress = 0f
        )

        initZeroPointVisuals()
        hapticListener?.invoke(HapticFeedbackEvent.LevelTransition(3))
    }

    fun setPaused(paused: Boolean) {
        _state.value = _state.value.copy(isPaused = paused)
    }

    private fun initQuasarVisuals() {
        val center = Offset(screenWidth / 2f, screenHeight / 2f)
        shockwaves.add(
            Shockwave(
                center = center,
                radius = 10f,
                maxRadius = screenWidth * 1.1f,
                alpha = 1f,
                color = QuasarBlueShift
            )
        )
        floatingTexts.add(
            FloatingText(
                id = nextEntityId++,
                text = "BLUE-SHIFT: QUASAR DYNAMICS ACTIVE",
                position = Offset(center.x, center.y - 120f),
                color = QuasarBlueShift
            )
        )
    }

    private fun initZeroPointVisuals() {
        val center = Offset(screenWidth / 2f, screenHeight / 2f)
        shockwaves.add(
            Shockwave(
                center = center,
                radius = 10f,
                maxRadius = screenWidth * 1.2f,
                alpha = 1f,
                color = SingularityGold
            )
        )
        floatingTexts.add(
            FloatingText(
                id = nextEntityId++,
                text = "POINT ZERO: HARMONIC RESONANCE FIELD",
                position = Offset(center.x, center.y - 140f),
                color = SingularityGold
            )
        )
    }

    fun update(deltaSeconds: Float) {
        val snapshot = _state.value
        if (!snapshot.isPaused) {
            updateOperatorTimers(deltaSeconds)
        }
        val current = _state.value
        if (current.isPaused) return

        if (current.isTransitioning) {
            val newProgress = (current.transitionProgress + deltaSeconds * 0.45f).coerceAtMost(1f)
            _state.value = current.copy(transitionProgress = newProgress)
            updateTransitionPhysics(deltaSeconds)
            return
        }

        if (current.phase == GamePhase.START_SCREEN) {
            updateSpawning(deltaSeconds, current.phase, current.config)
            updateParticles(deltaSeconds, current)
            return
        }

        // Automatic Milestone Transitions
        if (current.level == 1 && current.dcoins >= 500) {
            transitionToLevel2()
            return
        } else if (current.level == 2 && current.dcoins >= 1000) {
            transitionToLevel3()
            return
        }

        // Dilation Hold Periodic Haptic Pulse
        if (current.isDilationAnchorActive) {
            dilationHapticTimer += deltaSeconds
            if (dilationHapticTimer >= 0.4f) {
                dilationHapticTimer = 0f
                hapticListener?.invoke(HapticFeedbackEvent.DilationAnchorPulse)
            }
        } else {
            dilationHapticTimer = 0f
        }

        // --- LEVEL 3 LOOP: POINT ZERO (RESONANCE TUNING) ---
        if (current.level == 3) {
            updateLevel3ZeroPoint(deltaSeconds, current)
            return
        }

        // --- LEVEL 1 & LEVEL 2 LOOP ---
        val newGameTime = current.gameTime + deltaSeconds

        val newPhase = if (current.level == 1) {
            when {
                newGameTime < 30f -> GamePhase.EXPANSION
                newGameTime < 60f -> GamePhase.EQUILIBRIUM
                else -> GamePhase.COMPRESSION
            }
        } else {
            when {
                newGameTime < 30f -> GamePhase.QUASAR_IONIZATION
                newGameTime < 60f -> GamePhase.QUASAR_EQUILIBRIUM
                else -> GamePhase.QUASAR_RELATIVISTIC
            }
        }

        val rawEntropyRate = updateEntropy(current.density, newPhase.id, current.config)
        val dilationFactor = if (current.isDilationAnchorActive) 0.35f else 1.0f
        val freezeFactor = if (current.isTimeFrozen) 0f else 1f
        val shieldFactor = if (current.isShieldActive) 0.5f else 1f
        val entropyDrain = (rawEntropyRate * 100f * dilationFactor * freezeFactor * shieldFactor) * deltaSeconds

        val newDensity = (current.density - entropyDrain).coerceIn(0f, 100f)
        val totalEntropyResisted = current.entropyResistedTotal + entropyDrain

        if (newGameTime >= 90f || newDensity <= 5f) {
            val reason = if (newGameTime >= 90f) "Evolution" else "Collapse"
            initiateBlackHoleTransition(reason)
            return
        }

        val updatedQuasarAngle = (current.quasarBeamAngle + deltaSeconds * (if (current.level == 2) 75f else 45f)) % 360f

        _state.value = current.copy(
            gameTime = newGameTime,
            phase = newPhase,
            density = newDensity,
            entropyRate = rawEntropyRate,
            entropyResistedTotal = totalEntropyResisted,
            quasarBeamAngle = updatedQuasarAngle
        )

        updateSpawning(deltaSeconds, newPhase, current.config)
        updateEntities(deltaSeconds, current)
        updateParticles(deltaSeconds, current)
        updateShockwaves(deltaSeconds)
        updateFloatingTexts(deltaSeconds)
    }

    private fun updateLevel3ZeroPoint(deltaSeconds: Float, current: GameState) {
        val newGameTime = current.gameTime + deltaSeconds
        targetOscillatorPhase += deltaSeconds * 0.75f
        val targetFreq = 0.5f + 0.38f * sin(targetOscillatorPhase)

        val diff = if (current.isResonating) abs(current.playerFrequency - targetFreq) else 1.0f
        val inResonance = current.isResonating && diff < 0.12f
        val precision = if (inResonance) (1.0f - (diff / 0.12f)).coerceIn(0f, 1f) else 0f

        var newDcoins = current.dcoins
        var newDensity = current.density
        var holdDuration = current.resonanceHoldDuration

        if (inResonance) {
            holdDuration += deltaSeconds
            val coinRate = (60 * precision * current.config.currencyMultiplier * deltaSeconds).toInt()
            newDcoins += coinRate
            newDensity = (newDensity + (22f * precision * deltaSeconds)).coerceAtMost(100f)

            // Tactile feedback for frequency resonance matching
            hapticListener?.invoke(HapticFeedbackEvent.FrequencyResonanceMatch(precision))

            if (holdDuration >= 5.0f) {
                // Point Zero transcendence reached after holding 5 seconds
                initiateBlackHoleTransition("PointZeroEvolution")
                return
            }
        } else {
            holdDuration = (holdDuration - deltaSeconds * 2.5f).coerceAtLeast(0f)
            val decayRate = if (current.isTimeFrozen) 0f else if (current.isShieldActive) 1.75f else 3.5f
            newDensity = (newDensity - (decayRate * deltaSeconds)).coerceAtLeast(0f)
        }

        if (newDensity <= 5f) {
            initiateBlackHoleTransition("Collapse")
            return
        }

        val phase = if (inResonance) GamePhase.POINT_ZERO_RESONANCE else GamePhase.POINT_ZERO_CALIBRATION

        _state.value = current.copy(
            gameTime = newGameTime,
            phase = phase,
            targetFrequency = targetFreq,
            density = newDensity,
            dcoins = newDcoins,
            resonancePrecision = precision,
            resonanceHoldDuration = holdDuration
        )

        // Allow ambient quanta and standing wave nodes in Level 3
        updateSpawning(deltaSeconds, phase, current.config)
        updateEntities(deltaSeconds, current)
        updateParticles(deltaSeconds, current)
        updateShockwaves(deltaSeconds)
        updateFloatingTexts(deltaSeconds)
    }

    private fun initiateBlackHoleTransition(reason: String) {
        val current = _state.value
        _state.value = current.copy(
            isTransitioning = true,
            transitionProgress = 0f,
            transitionReason = reason
        )
        val center = Offset(screenWidth / 2f, screenHeight / 2f)
        shockwaves.add(
            Shockwave(
                center = center,
                radius = 10f,
                maxRadius = screenWidth * 0.95f,
                alpha = 1f,
                color = if (reason.contains("Evolution")) SingularityGold else CriticalHorizon
            )
        )
        hapticListener?.invoke(HapticFeedbackEvent.LevelTransition(current.level))
    }

    private fun updateSpawning(delta: Float, phase: GamePhase, config: LevelConfig) {
        if (phase == GamePhase.START_SCREEN) {
            spawnTimer += delta
            if (spawnTimer >= 0.15f) {
                spawnTimer = 0f
                val centerX = screenWidth / 2f
                val centerY = screenHeight / 2f
                val angle = Random.nextFloat() * 2f * PI.toFloat()
                val speed = Random.nextFloat() * 40f + 20f
                val life = Random.nextFloat() * 2f + 1f
                particles.add(
                    Particle(
                        position = Offset(centerX, centerY),
                        velocity = Offset(cos(angle) * speed, sin(angle) * speed),
                        alpha = 1.0f,
                        size = Random.nextFloat() * 3f + 1f,
                        color = RadiantTurquoise.copy(alpha = 0.4f),
                        life = life,
                        maxLife = life
                    )
                )
            }
            return
        }

        if (config.spawnRate <= 0f) return
        spawnTimer += delta
        val baseInterval = when (phase) {
            GamePhase.EXPANSION -> 1.2f
            GamePhase.EQUILIBRIUM -> 0.9f
            GamePhase.COMPRESSION -> 0.65f
            GamePhase.QUASAR_IONIZATION -> 0.6f
            GamePhase.QUASAR_EQUILIBRIUM -> 0.45f
            GamePhase.QUASAR_RELATIVISTIC -> 0.35f
            else -> 1.0f
        }

        val actualInterval = baseInterval / config.spawnRate
        val maxBubbles = if (config.level == 2) 28 else 20

        if (spawnTimer >= actualInterval && bubbles.size < maxBubbles) {
            spawnTimer = 0f
            spawnQuantumBubble(phase, config)
        }

        ambientParticleTimer += delta
        if (ambientParticleTimer >= (if (config.level == 2) 0.04f else 0.08f)) {
            ambientParticleTimer = 0f
            spawnAmbientParticles(phase, config)
        }
    }

    private fun spawnQuantumBubble(phase: GamePhase, config: LevelConfig) {
        val roll = Random.nextFloat()
        val type = if (config.level == 3) {
            when {
                roll < 0.25f -> QuantumType.PHANTOM_SINGULARITY
                roll < 0.50f -> QuantumType.GOLDEN_DCOIN
                roll < 0.80f -> QuantumType.JADE_NODE
                else -> QuantumType.TURQUOISE_BUBBLE
            }
        } else if (config.level == 2) {
            when {
                roll < 0.25f -> QuantumType.PHANTOM_SINGULARITY
                roll < 0.55f -> QuantumType.TURQUOISE_BUBBLE
                roll < 0.80f -> QuantumType.GOLDEN_DCOIN
                roll < 0.92f -> QuantumType.JADE_NODE
                else -> QuantumType.ANTIMATTER_ANOMALY
            }
        } else {
            when (phase) {
                GamePhase.EXPANSION -> {
                    if (roll < 0.65f) QuantumType.TURQUOISE_BUBBLE
                    else if (roll < 0.85f) QuantumType.GOLDEN_DCOIN
                    else QuantumType.JADE_NODE
                }
                GamePhase.EQUILIBRIUM -> {
                    if (roll < 0.45f) QuantumType.TURQUOISE_BUBBLE
                    else if (roll < 0.70f) QuantumType.JADE_NODE
                    else if (roll < 0.90f) QuantumType.GOLDEN_DCOIN
                    else QuantumType.ANTIMATTER_ANOMALY
                }
                else -> {
                    if (roll < 0.35f) QuantumType.TURQUOISE_BUBBLE
                    else if (roll < 0.55f) QuantumType.ANTIMATTER_ANOMALY
                    else if (roll < 0.80f) QuantumType.GOLDEN_DCOIN
                    else QuantumType.JADE_NODE
                }
            }
        }

        val center = Offset(screenWidth / 2f, screenHeight / 2f)
        val startPos: Offset
        val vx: Float
        val vy: Float

        if (config.particleBehavior == "repel") {
            val centerJitterAngle = Random.nextFloat() * 2 * PI.toFloat()
            val centerJitterDist = Random.nextFloat() * 40f + 10f
            startPos = Offset(
                center.x + cos(centerJitterAngle) * centerJitterDist,
                center.y + sin(centerJitterAngle) * centerJitterDist
            )
            val outwardSpeed = Random.nextFloat() * 120f + 80f
            vx = cos(centerJitterAngle) * outwardSpeed
            vy = sin(centerJitterAngle) * outwardSpeed
        } else {
            val padding = 120f
            val startX = Random.nextFloat() * (screenWidth - padding * 2) + padding
            val startY = Random.nextFloat() * (screenHeight - padding * 2) + padding
            startPos = Offset(startX, startY)

            val speed = when (phase) {
                GamePhase.EXPANSION -> Random.nextFloat() * 40f + 25f
                GamePhase.EQUILIBRIUM -> Random.nextFloat() * 70f + 40f
                else -> Random.nextFloat() * 110f + 60f
            }
            val angle = Random.nextFloat() * (2 * PI.toFloat())
            vx = cos(angle) * speed
            vy = sin(angle) * speed
        }

        val radius = when (type) {
            QuantumType.TURQUOISE_BUBBLE -> Random.nextFloat() * 14f + 32f
            QuantumType.JADE_NODE -> Random.nextFloat() * 10f + 28f
            QuantumType.GOLDEN_DCOIN -> Random.nextFloat() * 8f + 24f
            QuantumType.ANTIMATTER_ANOMALY -> Random.nextFloat() * 12f + 34f
            QuantumType.PHANTOM_SINGULARITY -> Random.nextFloat() * 10f + 30f
        }

        val densityBoost = when (type) {
            QuantumType.TURQUOISE_BUBBLE -> 9f
            QuantumType.JADE_NODE -> 16f
            QuantumType.GOLDEN_DCOIN -> 4f
            QuantumType.ANTIMATTER_ANOMALY -> 12f
            QuantumType.PHANTOM_SINGULARITY -> -config.phantomPenalty
        }

        val coinVal = when (type) {
            QuantumType.TURQUOISE_BUBBLE -> 1
            QuantumType.JADE_NODE -> 2
            QuantumType.GOLDEN_DCOIN -> 5
            QuantumType.ANTIMATTER_ANOMALY -> 8
            QuantumType.PHANTOM_SINGULARITY -> 0
        }

        bubbles.add(
            QuantumBubble(
                id = nextEntityId++,
                position = startPos,
                velocity = Offset(vx, vy),
                radius = radius,
                type = type,
                lifetime = 0f,
                maxLifetime = Random.nextFloat() * 4f + 7f,
                value = coinVal,
                densityBoost = densityBoost
            )
        )
    }

    private fun spawnAmbientParticles(phase: GamePhase, config: LevelConfig) {
        val color = if (config.level == 2) {
            QuasarBlueShift
        } else {
            when (phase) {
                GamePhase.EXPANSION -> RadiantTurquoise
                GamePhase.EQUILIBRIUM -> QuantumJade
                else -> SingularityGold
            }
        }
        val center = Offset(screenWidth / 2f, screenHeight / 2f)
        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val dist = if (config.particleBehavior == "repel") {
            Random.nextFloat() * 50f
        } else {
            Random.nextFloat() * (screenWidth * 0.48f)
        }
        val pos = Offset(center.x + cos(angle) * dist, center.y + sin(angle) * dist)

        val vel = if (config.particleBehavior == "repel") {
            val repelSpeed = Random.nextFloat() * 80f + 40f
            Offset(cos(angle) * repelSpeed, sin(angle) * repelSpeed)
        } else {
            Offset(cos(angle + PI.toFloat() / 2) * 15f, sin(angle + PI.toFloat() / 2) * 15f)
        }

        particles.add(
            Particle(
                position = pos,
                velocity = vel,
                alpha = Random.nextFloat() * 0.6f + 0.2f,
                size = Random.nextFloat() * 3.5f + 1.5f,
                color = color,
                life = 0f,
                maxLife = Random.nextFloat() * 2f + 1.5f
            )
        )
    }

    private fun updateEntities(delta: Float, state: GameState) {
        val center = Offset(screenWidth / 2f, screenHeight / 2f)
        val iterator = bubbles.iterator()

        while (iterator.hasNext()) {
            val bubble = iterator.next()
            bubble.lifetime += delta
            bubble.pulsePhase += delta * (if (state.level == 2) 5f else 3f)

            val dx = bubble.position.x - center.x
            val dy = bubble.position.y - center.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(20f)

            if (state.config.particleBehavior == "repel") {
                val repelAcc = 240f * delta
                bubble.velocity = Offset(
                    bubble.velocity.x + (dx / dist) * repelAcc,
                    bubble.velocity.y + (dy / dist) * repelAcc
                )
            } else if (state.phase == GamePhase.COMPRESSION) {
                val gravityForce = 1800f / dist
                val gx = -(dx / dist) * gravityForce * delta
                val gy = -(dy / dist) * gravityForce * delta
                bubble.velocity = Offset(bubble.velocity.x + gx, bubble.velocity.y + gy)
            }

            if (state.isDilationAnchorActive && state.anchorPosition != null) {
                val ax = state.anchorPosition.x - bubble.position.x
                val ay = state.anchorPosition.y - bubble.position.y
                val aDist = sqrt(ax * ax + ay * ay).coerceAtLeast(30f)
                if (aDist < 450f) {
                    val pull = (400f - aDist) * 2.2f * delta
                    bubble.velocity = Offset(
                        bubble.velocity.x + (ax / aDist) * pull,
                        bubble.velocity.y + (ay / aDist) * pull
                    )
                }
            }

            if (state.isMagnetActive) {
                val magnetPull = 700f * delta
                bubble.velocity = Offset(
                    bubble.velocity.x - (dx / dist) * magnetPull,
                    bubble.velocity.y - (dy / dist) * magnetPull
                )
            }

            val newPos = Offset(
                bubble.position.x + bubble.velocity.x * delta,
                bubble.position.y + bubble.velocity.y * delta
            )

            var vx = bubble.velocity.x
            var vy = bubble.velocity.y
            var posX = newPos.x
            var posY = newPos.y
            var didBounce = false

            if (posX - bubble.radius < 20f) {
                posX = 20f + bubble.radius
                vx = -vx * 0.85f
                didBounce = true
            } else if (posX + bubble.radius > screenWidth - 20f) {
                posX = screenWidth - 20f - bubble.radius
                vx = -vx * 0.85f
                didBounce = true
            }

            if (posY - bubble.radius < 120f) {
                posY = 120f + bubble.radius
                vy = -vy * 0.85f
                didBounce = true
            } else if (posY + bubble.radius > screenHeight - 120f) {
                posY = screenHeight - 120f - bubble.radius
                vy = -vy * 0.85f
                didBounce = true
            }

            if (didBounce && state.level == 2) {
                hapticListener?.invoke(HapticFeedbackEvent.ParticleRepel)
            }

            bubble.position = Offset(posX, posY)
            bubble.velocity = Offset(vx, vy)

            if (bubble.lifetime >= bubble.maxLifetime) {
                spawnBurst(bubble.position, bubble.type, 6)
                iterator.remove()
            }
        }
    }

    private fun updateParticles(delta: Float, state: GameState) {
        val center = Offset(screenWidth / 2f, screenHeight / 2f)
        val iterator = particles.iterator()

        while (iterator.hasNext()) {
            val p = iterator.next()
            p.life += delta
            if (p.life >= p.maxLife) {
                iterator.remove()
                continue
            }

            if (state.config.particleBehavior == "repel") {
                p.position = Offset(p.position.x + p.velocity.x * delta, p.position.y + p.velocity.y * delta)
            } else {
                val dx = center.x - p.position.x
                val dy = center.y - p.position.y
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(10f)
                val gForce = 120f / dist
                val gx = (dx / dist) * gForce * delta * 40f
                val gy = (dy / dist) * gForce * delta * 40f
                p.position = Offset(p.position.x + p.velocity.x * delta + gx, p.position.y + p.velocity.y * delta + gy)
            }

            p.alpha = ((1f - (p.life / p.maxLife)) * 0.9f).coerceIn(0f, 1f)
        }

        if (particles.size > 160) {
            particles.subList(0, particles.size - 160).clear()
        }
    }

    private fun updateTransitionPhysics(delta: Float) {
        val center = Offset(screenWidth / 2f, screenHeight / 2f)
        val bubbleIterator = bubbles.iterator()
        while (bubbleIterator.hasNext()) {
            val b = bubbleIterator.next()
            val dx = center.x - b.position.x
            val dy = center.y - b.position.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(5f)
            val implosionSpeed = 1200f * delta
            b.position = Offset(b.position.x + (dx / dist) * implosionSpeed, b.position.y + (dy / dist) * implosionSpeed)
            b.radius = (b.radius - delta * 30f).coerceAtLeast(0f)
            if (dist < 30f || b.radius <= 0f) {
                spawnBurst(center, b.type, 4)
                bubbleIterator.remove()
            }
        }

        val pIterator = particles.iterator()
        while (pIterator.hasNext()) {
            val p = pIterator.next()
            val dx = center.x - p.position.x
            val dy = center.y - p.position.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(5f)
            p.position = Offset(p.position.x + (dx / dist) * 1600f * delta, p.position.y + (dy / dist) * 1600f * delta)
            if (dist < 20f) {
                pIterator.remove()
            }
        }
    }

    private fun updateShockwaves(delta: Float) {
        val iterator = shockwaves.iterator()
        while (iterator.hasNext()) {
            val sw = iterator.next()
            sw.radius += delta * 600f
            sw.alpha = (1f - (sw.radius / sw.maxRadius)).coerceIn(0f, 1f)
            if (sw.radius >= sw.maxRadius || sw.alpha <= 0.01f) {
                iterator.remove()
            }
        }
    }

    private fun updateFloatingTexts(delta: Float) {
        val iterator = floatingTexts.iterator()
        while (iterator.hasNext()) {
            val ft = iterator.next()
            ft.position = Offset(ft.position.x, ft.position.y - delta * 60f)
            ft.alpha = (ft.alpha - delta * 1.2f).coerceAtLeast(0f)
            if (ft.alpha <= 0f) {
                iterator.remove()
            }
        }
    }

    // --- OPERATOR ABILITIES (Burst / Freeze / Revive / Magnet / Shield) ---

    fun isOperatorReady(id: String): Boolean = (operatorCooldownsInternal[id] ?: 0f) <= 0f

    fun activateOperator(id: String): Boolean {
        val current = _state.value
        if (current.isPaused || current.isTransitioning) return false
        if (!isOperatorReady(id)) return false

        operatorCooldownsInternal[id] = OPERATOR_COOLDOWN
        val center = Offset(screenWidth / 2f, screenHeight / 2f)

        when (id) {
            "BURST" -> {
                val phantoms = bubbles.filter { it.type == QuantumType.PHANTOM_SINGULARITY }
                bubbles.removeAll(phantoms)
                phantoms.forEach { spawnBurst(it.position, it.type, 10) }
                shockwaves.add(
                    Shockwave(center = center, radius = 10f, maxRadius = screenWidth * 0.9f, alpha = 0.9f, color = RadiantTurquoise)
                )
                floatingTexts.add(
                    FloatingText(id = nextEntityId++, text = "BURST: PHANTOMS PURGED", position = Offset(center.x, center.y - 160f), color = RadiantTurquoise)
                )
                _state.value = _state.value.copy(density = (_state.value.density + 12f).coerceAtMost(100f))
            }
            "FREEZE" -> {
                freezeTimer = FREEZE_DURATION
                shockwaves.add(
                    Shockwave(center = center, radius = 10f, maxRadius = screenWidth * 0.8f, alpha = 0.8f, color = QuasarBlueShift)
                )
                floatingTexts.add(
                    FloatingText(id = nextEntityId++, text = "TIME FREEZE ACTIVE", position = Offset(center.x, center.y - 160f), color = QuasarBlueShift)
                )
            }
            "REVIVE" -> {
                _state.value = _state.value.copy(density = maxOf(_state.value.density, 45f))
                shockwaves.add(
                    Shockwave(center = center, radius = 10f, maxRadius = screenWidth * 0.7f, alpha = 0.85f, color = QuantumJade)
                )
                floatingTexts.add(
                    FloatingText(id = nextEntityId++, text = "REVIVE: DENSITY RESTORED", position = Offset(center.x, center.y - 160f), color = QuantumJade)
                )
            }
            "MAGNET" -> {
                magnetTimer = MAGNET_DURATION
                shockwaves.add(
                    Shockwave(center = center, radius = 10f, maxRadius = screenWidth * 0.6f, alpha = 0.8f, color = SingularityGold)
                )
                floatingTexts.add(
                    FloatingText(id = nextEntityId++, text = "MAGNET: QUANTA ATTRACTED", position = Offset(center.x, center.y - 160f), color = SingularityGold)
                )
            }
            "SHIELD" -> {
                shieldTimer = SHIELD_DURATION
                shockwaves.add(
                    Shockwave(center = center, radius = 10f, maxRadius = screenWidth * 0.7f, alpha = 0.8f, color = CoronaAmber)
                )
                floatingTexts.add(
                    FloatingText(id = nextEntityId++, text = "SHIELD: ENTROPY DAMPED", position = Offset(center.x, center.y - 160f), color = CoronaAmber)
                )
            }
        }

        syncOperatorFlags()
        return true
    }

    private fun syncOperatorFlags() {
        _state.value = _state.value.copy(
            isTimeFrozen = freezeTimer > 0f,
            isShieldActive = shieldTimer > 0f,
            isMagnetActive = magnetTimer > 0f,
            operatorCooldowns = operatorCooldownsInternal.toMap()
        )
    }

    private fun updateOperatorTimers(delta: Float) {
        if (operatorCooldownsInternal.isNotEmpty()) {
            operatorCooldownsInternal.keys.forEach { k ->
                operatorCooldownsInternal[k] = ((operatorCooldownsInternal[k] ?: 0f) - delta).coerceAtLeast(0f)
            }
        }
        if (freezeTimer > 0f) freezeTimer = (freezeTimer - delta).coerceAtLeast(0f)
        if (shieldTimer > 0f) shieldTimer = (shieldTimer - delta).coerceAtLeast(0f)
        if (magnetTimer > 0f) magnetTimer = (magnetTimer - delta).coerceAtLeast(0f)
        syncOperatorFlags()
    }

    // --- GESTURE INTERACTIONS ---

    fun handleTap(tapPos: Offset): Boolean {
        val current = _state.value
        if (current.isPaused || current.isTransitioning) return false

        if (current.level == 3) {
            // Level 3 requirement: Any tap gives visual ripples without affecting gameplay
            shockwaves.add(
                Shockwave(
                    center = tapPos,
                    radius = 8f,
                    maxRadius = 200f,
                    alpha = 0.85f,
                    color = SingularityGold
                )
            )
            shockwaves.add(
                Shockwave(
                    center = tapPos,
                    radius = 20f,
                    maxRadius = 140f,
                    alpha = 0.5f,
                    color = RadiantTurquoise
                )
            )
            hapticListener?.invoke(HapticFeedbackEvent.EmptyTap)
            return false
        }

        var hit = false
        val iterator = bubbles.iterator()

        shockwaves.add(
            Shockwave(
                center = tapPos,
                radius = 8f,
                maxRadius = 160f,
                alpha = 0.85f,
                color = if (current.level == 2) QuasarBlueShift else RadiantTurquoise
            )
        )

        while (iterator.hasNext()) {
            val bubble = iterator.next()
            val dx = tapPos.x - bubble.position.x
            val dy = tapPos.y - bubble.position.y
            val dist = sqrt(dx * dx + dy * dy)

            if (dist <= bubble.radius + 32f) {
                hit = true
                iterator.remove()
                processBubbleHarvest(bubble, tapPos)
                break
            }
        }

        if (!hit) {
            val newDensity = (current.density + 0.5f).coerceAtMost(100f)
            _state.value = current.copy(density = newDensity)
            if (current.level == 2) {
                hapticListener?.invoke(HapticFeedbackEvent.ParticleRepel)
            }
            hapticListener?.invoke(HapticFeedbackEvent.EmptyTap)
        }

        return hit
    }

    fun setDilationAnchor(active: Boolean, position: Offset?) {
        val current = _state.value
        if (current.isPaused || current.isTransitioning) return

        if (active && position != null) {
            _state.value = current.copy(
                isDilationAnchorActive = true,
                anchorPosition = position
            )
            shockwaves.add(
                Shockwave(
                    center = position,
                    radius = 20f,
                    maxRadius = 240f,
                    alpha = 0.9f,
                    color = SingularityGold
                )
            )
            hapticListener?.invoke(HapticFeedbackEvent.DilationAnchorPulse)
        } else {
            _state.value = current.copy(
                isDilationAnchorActive = false,
                anchorPosition = null
            )
        }
    }

    fun handleSwipe(start: Offset, end: Offset) {
        val current = _state.value
        if (current.isPaused || current.isTransitioning) return

        val swipeDx = end.x - start.x
        val swipeDy = end.y - start.y
        val swipeLen = sqrt(swipeDx * swipeDx + swipeDy * swipeDy)
        if (swipeLen < 40f) return

        var harvested = 0
        var dissolvedPhantoms = 0
        val iterator = bubbles.iterator()

        while (iterator.hasNext()) {
            val bubble = iterator.next()
            val distToSegment = distanceToSegment(bubble.position, start, end)

            if (distToSegment <= bubble.radius + 38f) {
                iterator.remove()
                if (bubble.type == QuantumType.PHANTOM_SINGULARITY) {
                    // Штраф только за фантомы (X)!
                    dissolvedPhantoms++
                    processBubbleHarvest(bubble, bubble.position)
                } else {
                    // Все обычные квантовые частицы (включая Dcoins, Jade, Turquoise, Antimatter) добавляют очки и плотность
                    processBubbleHarvest(bubble, bubble.position)
                    harvested++
                }
            }
        }

        val mid = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
        val shockwaveColor = when (current.level) {
            3 -> SingularityGold
            2 -> QuasarBlueShift
            else -> RadiantTurquoise
        }

        shockwaves.add(
            Shockwave(
                center = mid,
                radius = 12f,
                maxRadius = swipeLen * 0.7f,
                alpha = 0.75f,
                color = shockwaveColor
            )
        )

        if (dissolvedPhantoms > 0) {
            hapticListener?.invoke(HapticFeedbackEvent.PhantomDissolve)
        } else if (harvested > 0) {
            hapticListener?.invoke(HapticFeedbackEvent.SwipeSweep(harvested))
        }

        if (harvested > 1) {
            floatingTexts.add(
                FloatingText(
                    id = nextEntityId++,
                    text = "SWEEP x$harvested",
                    position = mid,
                    color = SingularityGold
                )
            )
        }
    }

    // --- LEVEL 3 FREQUENCY MODULATION INPUT ---
    fun updatePlayerFrequencyInput(yNormalized: Float, isTouching: Boolean) {
        val current = _state.value
        if (current.level != 3) return
        _state.value = current.copy(
            playerFrequency = yNormalized.coerceIn(0f, 1f),
            isResonating = isTouching
        )
    }

    private fun processBubbleHarvest(bubble: QuantumBubble, interactionPos: Offset) {
        val current = _state.value
        val isPhantom = bubble.type == QuantumType.PHANTOM_SINGULARITY

        val combo = if (isPhantom) 0 else current.currentCombo + 1
        val maxC = maxOf(current.maxCombo, combo)
        val comboMultiplier = (1 + (combo / 4)).coerceAtMost(5)

        val baseGained = if (isPhantom) 0 else (bubble.value * comboMultiplier * current.config.currencyMultiplier).toInt().coerceAtLeast(1)
        val gainedCoins = baseGained

        val newDcoins = current.dcoins + gainedCoins
        val densityDelta = if (isPhantom && current.isShieldActive) 0f else bubble.densityBoost
        val newDensity = (current.density + densityDelta).coerceIn(0f, 100f)

        _state.value = current.copy(
            dcoins = newDcoins,
            density = newDensity,
            currentCombo = combo,
            maxCombo = maxC
        )

        spawnBurst(interactionPos, bubble.type, 16)
        hapticListener?.invoke(HapticFeedbackEvent.QuantumHarvest(bubble.type))

        val textLabel = when (bubble.type) {
            QuantumType.TURQUOISE_BUBBLE -> "+${bubble.densityBoost.toInt()}% TIME • +$gainedCoins"
            QuantumType.JADE_NODE -> "HARMONIC +${bubble.densityBoost.toInt()}% • +$gainedCoins"
            QuantumType.GOLDEN_DCOIN -> "+$gainedCoins DCOINS! ⚡"
            QuantumType.ANTIMATTER_ANOMALY -> "NEUTRALIZED! +$gainedCoins"
            QuantumType.PHANTOM_SINGULARITY -> "⚠️ PHANTOM HAZARD! -${current.config.phantomPenalty.toInt()}%"
        }

        val textColor = when (bubble.type) {
            QuantumType.TURQUOISE_BUBBLE -> RadiantTurquoise
            QuantumType.JADE_NODE -> QuantumJade
            QuantumType.GOLDEN_DCOIN -> SingularityGold
            QuantumType.ANTIMATTER_ANOMALY -> CoronaAmber
            QuantumType.PHANTOM_SINGULARITY -> PhantomHazard
        }

        floatingTexts.add(
            FloatingText(
                id = nextEntityId++,
                text = textLabel,
                position = interactionPos,
                color = textColor
            )
        )
    }

    private fun spawnBurst(pos: Offset, type: QuantumType, count: Int) {
        val color = when (type) {
            QuantumType.TURQUOISE_BUBBLE -> RadiantTurquoise
            QuantumType.JADE_NODE -> QuantumJade
            QuantumType.GOLDEN_DCOIN -> SingularityGold
            QuantumType.ANTIMATTER_ANOMALY -> AntimatterViolet
            QuantumType.PHANTOM_SINGULARITY -> PhantomHazard
        }

        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val speed = Random.nextFloat() * 220f + 60f
            particles.add(
                Particle(
                    position = pos,
                    velocity = Offset(cos(angle) * speed, sin(angle) * speed),
                    alpha = 1f,
                    size = Random.nextFloat() * 5f + 2.5f,
                    color = color,
                    life = 0f,
                    maxLife = Random.nextFloat() * 0.6f + 0.4f
                )
            )
        }
    }

    private fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
        val abx = b.x - a.x
        val aby = b.y - a.y
        val apx = p.x - a.x
        val apy = p.y - a.y
        val abLenSq = abx * abx + aby * aby
        if (abLenSq == 0f) {
            val dx = p.x - a.x
            val dy = p.y - a.y
            return sqrt(dx * dx + dy * dy)
        }
        val t = ((apx * abx + apy * aby) / abLenSq).coerceIn(0f, 1f)
        val projX = a.x + t * abx
        val projY = a.y + t * aby
        val dx = p.x - projX
        val dy = p.y - projY
        return sqrt(dx * dx + dy * dy)
    }

    fun stopEngine() {
        hapticListener = null
        bubbles.clear()
        particles.clear()
        shockwaves.clear()
        floatingTexts.clear()
        gestureTrails.clear()
    }
}
