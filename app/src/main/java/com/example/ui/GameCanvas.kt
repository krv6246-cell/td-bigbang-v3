package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.game.GameEngine
import com.example.game.GamePhase
import com.example.game.GameState
import com.example.game.QuantumBubble
import com.example.game.QuantumType
import com.example.ui.theme.AntimatterViolet
import com.example.ui.theme.CoronaAmber
import com.example.ui.theme.CriticalHorizon
import com.example.ui.theme.PhantomHazard
import com.example.ui.theme.QuantumJade
import com.example.ui.theme.QuasarBlueShift
import com.example.ui.theme.QuasarDeepElectric
import com.example.ui.theme.RadiantTurquoise
import com.example.ui.theme.SingularityGold
import com.example.ui.theme.SpaceVoid
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: GameEngine,
    state: GameState,
    touchTrails: List<List<Offset>>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "singularity_pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        engine.updateScreenDimensions(width, height)

        // 1. Draw Deep Cyber-Zen Space Void Background
        drawRect(color = SpaceVoid)

        // Level 2 "Blue-Shift" Visual Filter (Quasar state)
        if (state.isBlueShiftActive || state.level == 2) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        QuasarBlueShift.copy(alpha = 0.18f),
                        QuasarDeepElectric.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = width * 0.95f
                )
            )
        }

        // Level 3 "Point Zero" Harmonic Background Glow
        if (state.level == 3) {
            val resAlpha = (state.resonancePrecision * 0.25f + 0.06f).coerceIn(0.06f, 0.35f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SingularityGold.copy(alpha = resAlpha),
                        QuantumJade.copy(alpha = resAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = width * 0.9f
                )
            )
        }

        // Radial ambient core glow
        val bgGlowColor = when (state.level) {
            2 -> QuasarBlueShift.copy(alpha = 0.12f)
            3 -> SingularityGold.copy(alpha = 0.15f)
            else -> when (state.phase) {
                GamePhase.EXPANSION -> RadiantTurquoise.copy(alpha = 0.07f)
                GamePhase.EQUILIBRIUM -> QuantumJade.copy(alpha = 0.08f)
                else -> SingularityGold.copy(alpha = 0.10f)
            }
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(bgGlowColor, Color.Transparent),
                center = center,
                radius = width * 0.75f
            ),
            radius = width * 0.75f,
            center = center
        )

        // 2. Gravitational Grid / Warp Lines (Lensing distortion effect)
        if (state.level != 3) {
            drawGravitationalGrid(center, width, height, state, pulseAnim)
            drawSingularityCore(center, state, pulseAnim)
        } else {
            // Level 3: Chladni Standing Wave Resonance Field
            drawPointZeroResonanceField(center, width, height, state, pulseAnim)
        }

        // 3. Level 2: Quasar Relativistic Jets (if Level 2)
        if (state.level == 2) {
            drawQuasarJets(center, state.quasarBeamAngle, width, height)
        }

        // 4. Draw Active Dilation Anchor field (Long-press)
        if (state.isDilationAnchorActive && state.anchorPosition != null) {
            drawDilationAnchor(state.anchorPosition, pulseAnim)
        }

        // 5. Draw Ambient Particles
        for (p in engine.particles) {
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = p.position
            )
        }

        // 6. Draw Quantum Bubbles
        for (bubble in engine.bubbles) {
            drawQuantumBubble(bubble, pulseAnim)
        }

        // 7. Draw Touch Gestures Trails
        for (trail in touchTrails) {
            if (trail.size > 1) {
                val path = Path()
                path.moveTo(trail.first().x, trail.first().y)
                for (i in 1 until trail.size) {
                    path.lineTo(trail[i].x, trail[i].y)
                }
                drawPath(
                    path = path,
                    color = if (state.level == 2) QuasarBlueShift.copy(alpha = 0.75f) else RadiantTurquoise.copy(alpha = 0.65f),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
        }

        // 8. Draw Shockwaves
        for (sw in engine.shockwaves) {
            drawCircle(
                color = sw.color.copy(alpha = sw.alpha),
                radius = sw.radius,
                center = sw.center,
                style = Stroke(width = (4f * sw.alpha).coerceAtLeast(1f))
            )
        }

        // 9. Draw Floating Feedback Texts
        for (ft in engine.floatingTexts) {
            val textLayoutResult = textMeasurer.measure(
                text = ft.text,
                style = TextStyle(
                    color = ft.color.copy(alpha = ft.alpha),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(ft.position.x - textLayoutResult.size.width / 2f, ft.position.y)
            )
        }

        // 10. Gravitational Lens Distortion Transition Overlay
        if (state.isTransitioning) {
            drawSingularityTransitionDistortion(center, width, height, state.transitionProgress, state.transitionReason)
        }
    }
}

private fun DrawScope.drawGravitationalGrid(
    center: Offset,
    width: Float,
    height: Float,
    state: GameState,
    pulse: Float
) {
    val warpFactor = when (state.phase) {
        GamePhase.EXPANSION -> 0.05f
        GamePhase.EQUILIBRIUM -> 0.15f
        GamePhase.COMPRESSION -> 0.35f
        GamePhase.QUASAR_IONIZATION -> 0.40f
        GamePhase.QUASAR_EQUILIBRIUM -> 0.50f
        GamePhase.QUASAR_RELATIVISTIC -> 0.65f
        else -> 0.2f
    } + if (state.isTransitioning) state.transitionProgress * 0.6f else 0f

    val ringColor = if (state.level == 2) {
        QuasarBlueShift.copy(alpha = 0.14f)
    } else {
        when (state.phase) {
            GamePhase.EXPANSION -> RadiantTurquoise.copy(alpha = 0.08f)
            GamePhase.EQUILIBRIUM -> QuantumJade.copy(alpha = 0.10f)
            else -> SingularityGold.copy(alpha = 0.14f)
        }
    }

    val baseRadii = floatArrayOf(80f, 160f, 260f, 380f, 520f, 680f)
    for (i in baseRadii.indices) {
        val r = baseRadii[i] * (1f - warpFactor * 0.25f) + (pulse * 20f)
        drawCircle(
            color = ringColor,
            radius = r,
            center = center,
            style = Stroke(width = 1.2f)
        )
    }

    val spokes = 12
    for (i in 0 until spokes) {
        val angle = (i * (2 * PI / spokes)).toFloat()
        val startDist = 60f
        val endDist = width.coerceAtLeast(height) * 0.7f
        val p1 = Offset(center.x + cos(angle) * startDist, center.y + sin(angle) * startDist)
        val p2 = Offset(center.x + cos(angle + warpFactor * 0.5f) * endDist, center.y + sin(angle + warpFactor * 0.5f) * endDist)
        drawLine(
            color = ringColor.copy(alpha = 0.05f),
            start = p1,
            end = p2,
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawSingularityCore(
    center: Offset,
    state: GameState,
    pulse: Float
) {
    val baseCoreRadius = if (state.level == 2) 48f else 32f
    val compressionScale = when (state.phase) {
        GamePhase.EXPANSION -> 1.0f
        GamePhase.EQUILIBRIUM -> 1.25f
        GamePhase.COMPRESSION -> 1.6f
        GamePhase.QUASAR_IONIZATION -> 1.35f
        GamePhase.QUASAR_EQUILIBRIUM -> 1.55f
        GamePhase.QUASAR_RELATIVISTIC -> 1.85f
        else -> 1.0f
    }
    val currentRadius = baseCoreRadius * compressionScale

    val haloColor = if (state.level == 2) QuasarBlueShift else SingularityGold
    val outerHaloRadius = currentRadius * 2.8f + (pulse * 8f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                haloColor.copy(alpha = 0.50f),
                RadiantTurquoise.copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = center,
            radius = outerHaloRadius
        ),
        radius = outerHaloRadius,
        center = center
    )

    drawCircle(
        color = haloColor.copy(alpha = 0.85f),
        radius = currentRadius * 1.35f,
        center = center,
        style = Stroke(width = 2.5f)
    )

    drawCircle(
        color = Color.Black,
        radius = currentRadius,
        center = center
    )

    val innerColor = if (state.level == 2) QuasarBlueShift else if (state.phase == GamePhase.COMPRESSION) CriticalHorizon else RadiantTurquoise
    drawCircle(
        color = innerColor.copy(alpha = 0.9f),
        radius = 4f + (pulse * 2f),
        center = center
    )
}

private fun DrawScope.drawQuasarJets(
    center: Offset,
    angleDeg: Float,
    width: Float,
    height: Float
) {
    val rad = Math.toRadians(angleDeg.toDouble()).toFloat()
    val jetLength = width * 0.95f
    val cosA = cos(rad)
    val sinA = sin(rad)

    val pNorth = Offset(center.x + cosA * jetLength, center.y + sinA * jetLength)
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(QuasarBlueShift, RadiantTurquoise, Color.Transparent),
            start = center,
            end = pNorth
        ),
        start = center,
        end = pNorth,
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    val pSouth = Offset(center.x - cosA * jetLength, center.y - sinA * jetLength)
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(QuasarBlueShift, RadiantTurquoise, Color.Transparent),
            start = center,
            end = pSouth
        ),
        start = center,
        end = pSouth,
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawPointZeroResonanceField(
    center: Offset,
    width: Float,
    height: Float,
    state: GameState,
    pulse: Float
) {
    val targetY = height * (1.0f - state.targetFrequency)
    val playerY = height * (1.0f - state.playerFrequency)
    val precision = state.resonancePrecision

    // 1. Target Carrier Wave Nodal Band
    val bandColor = if (precision > 0.7f) SingularityGold else RadiantTurquoise
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(bandColor.copy(alpha = 0.45f), Color.Transparent),
            center = Offset(center.x, targetY),
            radius = width * 0.7f
        ),
        radius = width * 0.7f,
        center = Offset(center.x, targetY)
    )

    drawLine(
        color = bandColor.copy(alpha = 0.7f),
        start = Offset(40f, targetY),
        end = Offset(width - 40f, targetY),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // 2. Harmonic Lissajous / Chladni Wave Interference
    val wavePath = Path()
    val steps = 80
    for (i in 0..steps) {
        val x = (i.toFloat() / steps) * width
        val wave1 = sin((x / width) * 4 * PI.toFloat() + pulse * 2 * PI.toFloat()) * (40f * (1f - precision * 0.7f))
        val wave2 = cos((x / width) * 6 * PI.toFloat() - pulse * 2 * PI.toFloat()) * (25f * (1f - precision * 0.7f))
        val y = targetY + wave1 + wave2
        if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
    }

    drawPath(
        path = wavePath,
        color = if (precision > 0.8f) SingularityGold else RadiantTurquoise.copy(alpha = 0.6f),
        style = Stroke(width = if (precision > 0.8f) 3.5f else 1.8f)
    )

    // 3. Player Frequency Indicator Cursor
    if (state.isResonating) {
        drawLine(
            color = if (precision > 0.8f) SingularityGold else Color.White.copy(alpha = 0.8f),
            start = Offset(60f, playerY),
            end = Offset(width - 60f, playerY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = if (precision > 0.8f) SingularityGold else Color.White,
            radius = 12f + pulse * 6f,
            center = Offset(center.x, playerY)
        )
    }
}

private fun DrawScope.drawDilationAnchor(anchor: Offset, pulse: Float) {
    val r1 = 50f + pulse * 40f
    val r2 = 90f + pulse * 40f
    val r3 = 140f + pulse * 40f

    drawCircle(
        color = SingularityGold.copy(alpha = 0.4f * (1f - pulse)),
        radius = r1,
        center = anchor,
        style = Stroke(width = 2.5f)
    )
    drawCircle(
        color = QuantumJade.copy(alpha = 0.3f * (1f - pulse)),
        radius = r2,
        center = anchor,
        style = Stroke(width = 2f)
    )
    drawCircle(
        color = RadiantTurquoise.copy(alpha = 0.2f * (1f - pulse)),
        radius = r3,
        center = anchor,
        style = Stroke(width = 1.5f)
    )

    drawCircle(
        color = SingularityGold,
        radius = 10f,
        center = anchor
    )
}

private fun DrawScope.drawQuantumBubble(bubble: QuantumBubble, pulse: Float) {
    val pos = bubble.position
    val baseRadius = bubble.radius
    val pulseOffset = sin(bubble.pulsePhase) * 2.5f
    val rad = (baseRadius + pulseOffset).coerceAtLeast(6f)

    when (bubble.type) {
        QuantumType.TURQUOISE_BUBBLE -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RadiantTurquoise.copy(alpha = 0.75f),
                        RadiantTurquoise.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = pos,
                    radius = rad * 1.5f
                ),
                radius = rad * 1.5f,
                center = pos
            )
            drawCircle(
                color = RadiantTurquoise,
                radius = rad,
                center = pos,
                style = Stroke(width = 2.2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = rad * 0.35f,
                center = pos
            )
        }

        QuantumType.JADE_NODE -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        QuantumJade.copy(alpha = 0.85f),
                        QuantumJade.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = pos,
                    radius = rad * 1.4f
                ),
                radius = rad * 1.4f,
                center = pos
            )
            drawCircle(
                color = QuantumJade,
                radius = rad,
                center = pos,
                style = Stroke(width = 2.4f)
            )
            drawCircle(
                color = QuantumJade.copy(alpha = 0.6f),
                radius = rad * 0.6f,
                center = pos,
                style = Stroke(width = 1.5f)
            )
        }

        QuantumType.GOLDEN_DCOIN -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SingularityGold.copy(alpha = 0.9f),
                        CoronaAmber.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = pos,
                    radius = rad * 1.6f
                ),
                radius = rad * 1.6f,
                center = pos
            )
            drawCircle(
                color = SingularityGold,
                radius = rad,
                center = pos,
                style = Stroke(width = 2.8f)
            )
            val sLen = rad * 0.55f
            drawLine(
                color = Color.White,
                start = Offset(pos.x - sLen, pos.y),
                end = Offset(pos.x + sLen, pos.y),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.White,
                start = Offset(pos.x, pos.y - sLen),
                end = Offset(pos.x, pos.y + sLen),
                strokeWidth = 2f
            )
        }

        QuantumType.ANTIMATTER_ANOMALY -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AntimatterViolet.copy(alpha = 0.9f),
                        CriticalHorizon.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = pos,
                    radius = rad * 1.5f
                ),
                radius = rad * 1.5f,
                center = pos
            )
            drawCircle(
                color = AntimatterViolet,
                radius = rad,
                center = pos,
                style = Stroke(width = 2.5f)
            )
            val spikes = 6
            for (i in 0 until spikes) {
                val a = (i * (2 * PI / spikes) + bubble.pulsePhase * 2f).toFloat()
                val spStart = Offset(pos.x + cos(a) * (rad * 0.8f), pos.y + sin(a) * (rad * 0.8f))
                val spEnd = Offset(pos.x + cos(a) * (rad * 1.35f), pos.y + sin(a) * (rad * 1.35f))
                drawLine(
                    color = CriticalHorizon,
                    start = spStart,
                    end = spEnd,
                    strokeWidth = 2f
                )
            }
        }

        QuantumType.PHANTOM_SINGULARITY -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PhantomHazard.copy(alpha = 0.95f),
                        CriticalHorizon.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = pos,
                    radius = rad * 1.6f
                ),
                radius = rad * 1.6f,
                center = pos
            )
            drawCircle(
                color = PhantomHazard,
                radius = rad,
                center = pos,
                style = Stroke(width = 3.0f)
            )
            val crossSize = rad * 0.6f
            drawLine(
                color = Color.White,
                start = Offset(pos.x - crossSize, pos.y - crossSize),
                end = Offset(pos.x + crossSize, pos.y + crossSize),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color.White,
                start = Offset(pos.x + crossSize, pos.y - crossSize),
                end = Offset(pos.x - crossSize, pos.y + crossSize),
                strokeWidth = 2.5f
            )
        }
    }
}

private fun DrawScope.drawSingularityTransitionDistortion(
    center: Offset,
    width: Float,
    height: Float,
    progress: Float,
    reason: String
) {
    val warpRadius = (width * 1.2f) * (1f - progress)
    val haloColor = if (reason.contains("Evolution")) SingularityGold else CriticalHorizon

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                haloColor.copy(alpha = 0.95f),
                Color.Black.copy(alpha = (progress * 0.95f).coerceIn(0f, 1f)),
                Color.Black
            ),
            center = center,
            radius = (width * 0.8f * (1f + progress)).coerceAtLeast(10f)
        ),
        radius = width * 1.5f,
        center = center
    )

    val numRings = 5
    for (i in 1..numRings) {
        val r = (warpRadius * (i.toFloat() / numRings)) * (1f - progress * 0.5f)
        drawCircle(
            color = haloColor.copy(alpha = (1f - progress) * 0.8f),
            radius = r.coerceAtLeast(2f),
            center = center,
            style = Stroke(width = 3.5f)
        )
    }
}
