package com.example.ui

import com.example.audio.OperatorSound
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SingularityRecord
import com.example.game.GamePhase
import com.example.game.GameState
import com.example.ui.theme.AntimatterViolet
import com.example.ui.theme.CoronaAmber
import com.example.ui.theme.CriticalHorizon
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DeepAbyss
import com.example.ui.theme.PhantomHazard
import com.example.ui.theme.QuantumJade
import com.example.ui.theme.QuasarBlueShift
import com.example.ui.theme.RadiantTurquoise
import com.example.ui.theme.SingularityGold
import com.example.ui.theme.SpaceVoid
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun StartScreenOverlay(
    onStartClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StartPulse"
    )
    
    // Заголовок появляется с fade-in
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(2000)),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AllInclusive,
                    contentDescription = "Cosmic Flare",
                    tint = SingularityGold,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "TIME DENSITY",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = SingularityGold,
                        letterSpacing = 6.sp
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "BIG BANG",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RadiantTurquoise,
                        letterSpacing = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = "Keep the universe alive.\nWitness its birth and collapse.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    )
                )
                
                Spacer(modifier = Modifier.height(80.dp))
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .clickable { onStartClick() }
                        .border(2.dp, RadiantTurquoise.copy(alpha = pulse), RoundedCornerShape(32.dp))
                        .padding(horizontal = 36.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "TAP TO IGNITE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = RadiantTurquoise.copy(alpha = pulse),
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TimeDensityScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val showArchive by viewModel.showArchiveDialog.collectAsStateWithLifecycle()
    val showHelp by viewModel.showHelpDialog.collectAsStateWithLifecycle()
    val showSummary by viewModel.showTransitionSummary.collectAsStateWithLifecycle()
    val isAudioMuted by viewModel.isAudioMuted.collectAsStateWithLifecycle()
    val lastExport by viewModel.lastExportRecord.collectAsStateWithLifecycle()
    val archiveList by viewModel.archiveRecords.collectAsStateWithLifecycle()
    val totalDcoins by viewModel.totalArchivedDcoins.collectAsStateWithLifecycle()
    val showLevel3Tutorial by viewModel.showLevel3Tutorial.collectAsStateWithLifecycle()

    var containerHeight by remember { mutableStateOf(1920f) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.exitGame()
        }
    }

    BackHandler(enabled = true) {
        if (showArchive) {
            viewModel.toggleArchiveDialog(false)
        } else if (showHelp) {
            viewModel.toggleHelpDialog(false)
        } else if (showSummary) {
            viewModel.restartGame(1)
        } else if (state.phase != GamePhase.START_SCREEN) {
            viewModel.restartGame(1)
        } else {
            viewModel.exitGame()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceVoid)
            .onGloballyPositioned { coords ->
                if (coords.size.height > 0) {
                    containerHeight = coords.size.height.toFloat()
                }
            }
    ) {
        // --- Custom Canvas Rendering Layer with Gesture Handling & Haptics ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_interactive_canvas")
                .pointerInput(state.level) {
                    // Tap gestures for quanta stabilization and dilation anchors
                    detectTapGestures(
                        onTap = { offset ->
                            if (state.phase != GamePhase.START_SCREEN) {
                                viewModel.onSingleTap(offset)
                            }
                        },
                        onPress = { offset ->
                            if (state.phase != GamePhase.START_SCREEN) {
                                viewModel.onLongPressStart(offset)
                                tryAwaitRelease()
                                viewModel.onLongPressEnd()
                            }
                        }
                    )
                }
        ) {
            GameCanvas(
                engine = viewModel.engine,
                state = state
            )
        }

        // --- Minimalist Cyber-Zen HUD Overlay ---
        if (state.phase == GamePhase.START_SCREEN) {
            StartScreenOverlay(
                onStartClick = { viewModel.startGameFromScreen() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Верхняя область экрана (~15%): статус-бар и HUD-панель TIME DENSITY
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TopHudBar(
                        state = state,
                        isAudioMuted = isAudioMuted,
                        onToggleAudio = { viewModel.toggleAudioMute() },
                        onOpenArchive = { viewModel.toggleArchiveDialog(true) },
                        onOpenHelp = { viewModel.toggleHelpDialog(true) }
                    )

                    // Единый компонент панели плотности времени для всех трех уровней
                    DensityStatusBar(state = state)

                    // 1. Видимая цель: На осциллографе (кривая сверху) покажи ДВЕ линии: золотую опорную волну (target frequency) и текущую несущую (cyan)
                    if (state.level == 3) {
                        PointZeroOscilloscope(state = state)
                    }
                }

                // Центральные всплывающие предупреждения / синтезатор
                CenterHudIndicator(state = state)

                // Bottom Navigation & Gesture Instruction Guide
                BottomControlBar(
                    state = state,
                    onTriggerOperator = { operator -> viewModel.activateOperator(operator) },
                    onFrequencyDrag = { ratio, isTouching -> viewModel.onFrequencyDrag(ratio, isTouching) }
                )
            }
        }

        // --- Singularity Transition Gateway Dialog ---
        if (showSummary && lastExport != null) {
            SingularityExportDialog(
                record = lastExport!!,
                currentLevel = state.level,
                transitionReason = state.transitionReason,
                onProceedToLevel2 = { viewModel.proceedToLevel2() },
                onProceedToLevel3 = { viewModel.proceedToLevel3() },
                onReigniteCycle = { viewModel.restartGame(1) },
                onGatewayDiveSound = { viewModel.audioEngine.playSingularityGatewayDive() }
            )
        }

        // --- Universe Archive Ledger Dialog ---
        if (showArchive) {
            UniverseArchiveDialog(
                records = archiveList,
                totalCoins = totalDcoins ?: 0,
                onClose = { viewModel.toggleArchiveDialog(false) },
                onClear = { viewModel.clearArchiveLedger() }
            )
        }

        // --- Quantum Guidelines Dialog ---
        if (showHelp) {
            QuantumGuidanceDialog(
                onClose = { viewModel.toggleHelpDialog(false) }
            )
        }

        // --- 4. Входной туториал (одноразовый) для Уровня 3 (Point Zero) ---
        if (state.level == 3 && showLevel3Tutorial) {
            PointZeroTutorialOverlay(
                onDismiss = { viewModel.dismissLevel3Tutorial() }
            )
        }
    }
}

@Composable
private fun TopHudBar(
    state: GameState,
    isAudioMuted: Boolean,
    onToggleAudio: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenHelp: () -> Unit
) {
    val levelTitle = when (state.level) {
        1 -> "BIG BANG"
        2 -> "QUASAR"
        else -> "POINT ZERO"
    }
    val levelColor = when (state.level) {
        1 -> RadiantTurquoise
        2 -> QuasarBlueShift
        else -> SingularityGold
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CyberSurface.copy(alpha = 0.85f), DeepAbyss.copy(alpha = 0.65f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Level & Cycle Pill
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(levelColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = levelTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• CYC-${state.universeCycle.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                )
            }
            Text(
                text = state.phase.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = state.phase.color
                )
            )
        }

        // Dcoins Bank Counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(DeepAbyss, RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Dcoins",
                tint = SingularityGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${state.dcoins}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = SingularityGold
                )
            )
        }

        // Action Buttons (Audio Synthesizer Mute, Codex Archive & Help)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleAudio,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("toggle_audio_button")
            ) {
                Icon(
                    imageVector = if (isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = if (isAudioMuted) "Unmute Synthesizer" else "Mute Synthesizer",
                    tint = if (isAudioMuted) TextMuted else SingularityGold
                )
            }

            IconButton(
                onClick = onOpenArchive,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("open_archive_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Universe Archive",
                    tint = RadiantTurquoise
                )
            }

            IconButton(
                onClick = onOpenHelp,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("open_help_button")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Cosmic Guide",
                    tint = TextSecondary
                )
            }
        }
    }
}

// --- Единый компонент HUD панели TIME DENSITY для всех трех уровней (Big Bang, Quasar, Point Zero) ---
@Composable
private fun DensityStatusBar(state: GameState) {
    val densityPercent = state.density.coerceIn(0f, 100f)

    // Базовый цвет уровня: Big Bang -> Бирюзовый/Нефритовый, Quasar -> Фиолетовый/Розовый, Point Zero -> Золотой
    val levelBaseColor = when (state.level) {
        1 -> RadiantTurquoise
        2 -> AntimatterViolet
        else -> SingularityGold
    }

    // Целевой динамический цвет в зависимости от состояния плотности:
    // < 15%: критическое состояние (CriticalHorizon / Red)
    // > 80%: теплое золотое свечение (SingularityGold)
    // нормальное: акцентная палитра уровня
    val targetDynamicColor = when {
        densityPercent < 15f -> CriticalHorizon
        densityPercent > 80f -> SingularityGold
        else -> levelBaseColor
    }

    // Плавная анимация цвета панели
    val animatedColor by animateColorAsState(
        targetValue = targetDynamicColor,
        animationSpec = tween(durationMillis = 350),
        label = "hudColorTransition"
    )

    // Juice: анимация вспышки свечения (glow) при сборе Dcoin (state.dcoins)
    val tapPulseGlow = remember { Animatable(0f) }
    LaunchedEffect(state.dcoins) {
        if (state.dcoins > 0) {
            tapPulseGlow.snapTo(1f)
            tapPulseGlow.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
    }

    // Juice: тревожное мерцание при критически низкой плотности (< 15%)
    val criticalInfiniteTransition = rememberInfiniteTransition(label = "criticalBlink")
    val criticalBlinkAlpha by criticalInfiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "criticalBlinkAlpha"
    )

    val currentAlpha = if (densityPercent < 15f) criticalBlinkAlpha else 1f
    val glowIntensity = tapPulseGlow.value

    Card(
        colors = CardDefaults.cardColors(
            containerColor = CyberSurface.copy(alpha = (0.85f + glowIntensity * 0.12f).coerceAtMost(0.98f))
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (glowIntensity > 0.05f || densityPercent > 80f || densityPercent < 15f) 2.dp else 1.dp,
            color = animatedColor.copy(alpha = ((0.5f + glowIntensity * 0.5f) * currentAlpha).coerceIn(0.15f, 1f))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("density_status_bar")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.level == 3) "RESONANCE STABILITY" else "TIME DENSITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                    if (densityPercent > 80f) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "★ OPTIMAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = SingularityGold,
                                fontSize = 10.sp
                            )
                        )
                    } else if (densityPercent < 15f) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚠ WARNING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CriticalHorizon,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Text(
                    text = "${densityPercent.toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = animatedColor.copy(alpha = currentAlpha)
                    )
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Полоска прогресса плотности с эффектом подсветки
            LinearProgressIndicator(
                progress = { densityPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = animatedColor.copy(alpha = currentAlpha),
                trackColor = DeepAbyss
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.level == 3) {
                    val resPercent = (state.resonancePrecision * 100f).toInt()
                    Text(
                        text = "HARMONIC MATCH: $resPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = if (resPercent > 70) SingularityGold else TextMuted
                        )
                    )
                    val holdStr = String.format(Locale.US, "%.1fs / 5.0s", state.resonanceHoldDuration)
                    Text(
                        text = "LOCK: $holdStr",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = SingularityGold
                        )
                    )
                } else {
                    val entropyFormatted = String.format(Locale.US, "%.3f", state.entropyRate)
                    Text(
                        text = "ENTROPY: -$entropyFormatted/s",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = if (state.density < 15f) CriticalHorizon else TextMuted
                        )
                    )

                    val remainingTime = (90f - state.gameTime).coerceAtLeast(0f)
                    val timeFormatted = String.format(Locale.US, "%04.1fs", remainingTime)
                    Text(
                        text = "SINGULARITY: $timeFormatted",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = SingularityGold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterHudIndicator(state: GameState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Level 3 Живая обратная связь: «RESONANCE» и кольцо «LOCK 5.0s» с обратным отсчетом
        if (state.level == 3) {
            PointZeroResonanceFeedback(state = state)
        }

        // Active Dilation Alert Badge
        AnimatedVisibility(
            visible = state.isDilationAnchorActive,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .background(SingularityGold.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, SingularityGold, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⏳ GRAVITATIONAL ANCHOR: DILATION ACTIVE (-65% ENTROPY)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SingularityGold
                    )
                )
            }
        }

        // Critical Collapse Warning
        if (state.density < 10f) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(CriticalHorizon.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .border(1.dp, CriticalHorizon, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⚠️ CRITICAL INSTABILITY: COLLAPSE IMMINENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CriticalHorizon
                    )
                )
            }
        }
    }
}

@Composable
private fun BottomControlBar(
    state: GameState,
    onTriggerOperator: (OperatorSound) -> Unit = {},
    onFrequencyDrag: (Float, Boolean) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepAbyss.copy(alpha = 0.7f), CyberSurface.copy(alpha = 0.95f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 2. Слайдер видим и крупный: Внизу экрана — горизонтальный ползунок на всю ширину, высотой не меньше 60px, с яркой ручкой и подписью «TUNE»
        if (state.level == 3) {
            val freqDiff = abs(state.playerFrequency - state.targetFrequency)
            val matchPercent = ((1.0f - (freqDiff / 0.38f)).coerceIn(0f, 1f) * 100f).toInt()

            PointZeroTuneSlider(
                playerFrequency = state.playerFrequency,
                isResonating = state.isResonating,
                matchPercent = matchPercent,
                onFrequencyChange = onFrequencyDrag,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Quick Operators Action Pills (Burst, Freeze, Revive, Magnet, Shield)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OperatorActionButton(
                label = operatorLabel("BURST", state.operatorCooldowns),
                color = RadiantTurquoise,
                enabled = operatorReady("BURST", state.operatorCooldowns),
                onClick = { onTriggerOperator(OperatorSound.BURST) },
                testTag = "operator_burst"
            )
            OperatorActionButton(
                label = operatorLabel("FREEZE", state.operatorCooldowns),
                color = QuasarBlueShift,
                enabled = operatorReady("FREEZE", state.operatorCooldowns),
                onClick = { onTriggerOperator(OperatorSound.FREEZE) },
                testTag = "operator_freeze"
            )
            OperatorActionButton(
                label = operatorLabel("REVIVE", state.operatorCooldowns),
                color = QuantumJade,
                enabled = operatorReady("REVIVE", state.operatorCooldowns),
                onClick = { onTriggerOperator(OperatorSound.REVIVE) },
                testTag = "operator_revive"
            )
            OperatorActionButton(
                label = operatorLabel("MAGNET", state.operatorCooldowns),
                color = SingularityGold,
                enabled = operatorReady("MAGNET", state.operatorCooldowns),
                onClick = { onTriggerOperator(OperatorSound.MAGNET) },
                testTag = "operator_magnet"
            )
            OperatorActionButton(
                label = operatorLabel("SHIELD", state.operatorCooldowns),
                color = CoronaAmber,
                enabled = operatorReady("SHIELD", state.operatorCooldowns),
                onClick = { onTriggerOperator(OperatorSound.SHIELD) },
                testTag = "operator_shield"
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Gesture Instructions
        if (state.level == 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GestureHintItem(label = "TUNE", desc = "Hold 5s to Lock", color = SingularityGold)
                Text(text = "•", color = TextMuted)
                GestureHintItem(label = "SWIPE", desc = "Sweep Stream", color = SingularityGold)
                Text(text = "•", color = TextMuted)
                GestureHintItem(label = "TAP", desc = "Field Ripple", color = RadiantTurquoise)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GestureHintItem(label = "TAP", desc = "Stabilize Quanta", color = RadiantTurquoise)
                Text(text = "•", color = TextMuted)
                GestureHintItem(label = "HOLD", desc = "Dilate Time", color = SingularityGold)
                Text(text = "•", color = TextMuted)
                GestureHintItem(
                    label = "SWIPE",
                    desc = "Sweep Stream",
                    color = if (state.level == 2) QuasarBlueShift else RadiantTurquoise
                )
            }
        }

        if (state.currentCombo > 1) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "QUANTUM RESONANCE: ${state.currentCombo}x COMBO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = SingularityGold
                )
            )
        }
    }
}

private fun operatorLabel(base: String, cooldowns: Map<String, Float>): String {
    val cd = cooldowns[base] ?: 0f
    return if (cd > 0f) "$base ${cd.toInt()}s" else base
}

private fun operatorReady(base: String, cooldowns: Map<String, Float>): Boolean =
    (cooldowns[base] ?: 0f) <= 0f

@Composable
private fun OperatorActionButton(
    label: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .background(DeepAbyss, RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (enabled) color.copy(alpha = 0.6f) else color.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                color = if (enabled) color else color.copy(alpha = 0.35f)
            )
        )
    }
}

@Composable
private fun GestureHintItem(label: String, desc: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "[$label]",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                color = color
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 9.sp
            )
        )
    }
}

// --- DIALOGS ---

@Composable
fun SingularityExportDialog(
    record: SingularityRecord,
    currentLevel: Int,
    transitionReason: String = "",
    onProceedToLevel2: () -> Unit,
    onProceedToLevel3: () -> Unit,
    onReigniteCycle: () -> Unit,
    onGatewayDiveSound: () -> Unit = {}
) {
    // Определение причины перехода: Эволюция (выжил 90 секунд) или Гравитационный коллапс (density < 5%)
    val isEvolution = transitionReason.contains("Evolution", ignoreCase = true) ||
            record.outcome.contains("Evolution", ignoreCase = true)

    // Тематические цвета и акценты в зависимости от исхода цикла
    val themeAccentColor = if (isEvolution) SingularityGold else CriticalHorizon
    val outcomeTitle = if (isEvolution) "COSMIC EVOLUTION" else "GRAVITATIONAL COLLAPSE"
    val outcomeSubtitle = if (isEvolution) {
        "Threshold Surpassed • Quantum Horizons Unlocked"
    } else {
        "Density Critical (<5%) • Singularity Compression"
    }

    // Состояние анимации схлопывания в сингулярность при выборе действия выхода / перехода
    var isCollapsing by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val collapseAnim = remember { Animatable(0f) }

    LaunchedEffect(isCollapsing) {
        if (isCollapsing) {
            onGatewayDiveSound()
            collapseAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
            )
            pendingAction?.invoke()
        }
    }

    val triggerExitCollapse: (() -> Unit) -> Unit = { action ->
        if (!isCollapsing) {
            pendingAction = action
            isCollapsing = true
        }
    }

    val progress = collapseAnim.value
    val dialogScale = (1f - progress * 0.96f).coerceAtLeast(0.01f)
    val dialogRotation = progress * 720f // Закручивание вихря горизонта событий
    val dialogAlpha = (1f - progress * progress).coerceIn(0f, 1f)

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Анимированная карточка диалога: схлопывание, вращение и уменьшение в точку сингулярности
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DeepAbyss,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, themeAccentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = dialogScale
                        scaleY = dialogScale
                        rotationZ = dialogRotation
                        alpha = dialogAlpha
                    }
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Иконка исхода перехода (Вселенская петля для эволюции, Предупреждение для коллапса)
                    Icon(
                        imageVector = if (isEvolution) Icons.Default.AllInclusive else Icons.Default.Warning,
                        contentDescription = if (isEvolution) "Cosmic Evolution" else "Gravitational Collapse",
                        tint = themeAccentColor,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Заголовок экрана перехода
                    Text(
                        text = "SINGULARITY GATEWAY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = themeAccentColor,
                            letterSpacing = 2.sp
                        )
                    )

                    // Дифференцированный подзаголовок исхода
                    Text(
                        text = outcomeSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isEvolution) TextSecondary else CriticalHorizon.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Данные раунда (Dcoins, Combo, Survival, Event)
                    StatRow(
                        label = "TRANSITION EVENT",
                        value = outcomeTitle,
                        color = if (isEvolution) QuantumJade else CriticalHorizon
                    )
                    StatRow(
                        label = "UNIVERSE CYCLE",
                        value = "CYC-${record.universeCycle.toString().padStart(2, '0')}",
                        color = TextPrimary
                    )
                    StatRow(
                        label = "DCOINS EXPORTED",
                        value = "+${record.dcoinsExported} ⚡",
                        color = SingularityGold
                    )
                    StatRow(
                        label = "SURVIVAL DURATION",
                        value = String.format(Locale.US, "%.1fs", record.survivalTimeSeconds),
                        color = if (isEvolution) QuantumJade else CoronaAmber
                    )
                    StatRow(
                        label = "MAX RESONANCE COMBO",
                        value = "${record.maxCombo}x",
                        color = CoronaAmber
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопки выхода/перехода со схлопыванием в сингулярность
                    if (currentLevel == 1) {
                        // Переход на Уровень 2: Quasar Dynamics
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { triggerExitCollapse(onProceedToLevel2) },
                                colors = ButtonDefaults.buttonColors(containerColor = SingularityGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("proceed_level_2_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    tint = SpaceVoid,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EXPORT TO LEVEL 2: QUASAR",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = SpaceVoid
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Preserves +${record.dcoinsExported} Dcoins • Unlocks Relativistic Quasar Jet dynamics & Higher Entropy",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    } else if (currentLevel == 2) {
                        // Переход на Уровень 3: Point Zero
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { triggerExitCollapse(onProceedToLevel3) },
                                colors = ButtonDefaults.buttonColors(containerColor = SingularityGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("proceed_level_3_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SpaceVoid,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ASCEND TO LEVEL 3: POINT ZERO",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = SpaceVoid
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Preserves +${record.dcoinsExported} Dcoins • Calibrates Auditory Resonance Synthesizer",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    } else if (currentLevel == 3 && isEvolution) {
                        // Финальная победа: все 3 уровня пройдены (Point Zero трансцендентность)
                        Surface(
                            color = SingularityGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SingularityGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "✦ TRANSCENDENCE ACHIEVED ✦",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = SingularityGold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Big Bang • Quasar • Point Zero Harmonic Converged",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Кнопка перезапуска цикла Big Bang со схлопыванием
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { triggerExitCollapse(onReigniteCycle) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .testTag("reignite_cycle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = RadiantTurquoise,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLevel == 3 && isEvolution) {
                                    "COMMENCE PRESTIGE COSMIC CYCLE"
                                } else {
                                    "RE-IGNITE BIG BANG (NEW CYCLE)"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = RadiantTurquoise
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Banks +${record.dcoinsExported} Dcoins to Ledger Archive • Resets Universe density to 100% (Cycle CYC-${(record.universeCycle + 1).toString().padStart(2, '0')})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Оверлей схлопывания Сингулярности (Singularity Gateway Vortex Collapse)
            if (isCollapsing) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = 1f }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.width.coerceAtLeast(size.height) * 0.8f

                    // Аккреционный диск, сжимающийся к центру с ускорением
                    val currentRadius = (maxRadius * (1f - progress)).coerceAtLeast(4f)
                    val flashAlpha = if (progress > 0.8f) ((progress - 0.8f) / 0.2f) else 0f

                    // Внешнее свечение гравитационного линзирования
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                themeAccentColor.copy(alpha = (1f - progress) * 0.9f),
                                themeAccentColor.copy(alpha = (1f - progress) * 0.35f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = (currentRadius * 1.5f).coerceAtLeast(10f)
                        ),
                        radius = (currentRadius * 1.5f).coerceAtLeast(10f),
                        center = center
                    )

                    // Концентрические кольца вихря горизонта событий
                    val ringCount = 5
                    for (i in 1..ringCount) {
                        val ringR = (currentRadius * (i.toFloat() / ringCount))
                        drawCircle(
                            color = themeAccentColor.copy(alpha = ((1f - progress) * 0.75f)),
                            radius = ringR.coerceAtLeast(2f),
                            center = center,
                            style = Stroke(width = (4f * (1f - progress * 0.5f)).coerceAtLeast(1f))
                        )
                    }

                    // Центральная чёрная дыра (Сингулярность), поглощающая свет
                    val eventHorizonRadius = (currentRadius * 0.45f).coerceAtLeast(2f)
                    drawCircle(
                        color = Color.Black,
                        radius = eventHorizonRadius,
                        center = center
                    )
                    drawCircle(
                        color = themeAccentColor,
                        radius = eventHorizonRadius,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )

                    // Финальная яркая квантовая вспышка коллапса сингулярности в момент перехода
                    if (flashAlpha > 0f) {
                        drawRect(
                            color = themeAccentColor.copy(alpha = flashAlpha * 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun UniverseArchiveDialog(
    records: List<SingularityRecord>,
    totalCoins: Int,
    onClose: () -> Unit,
    onClear: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DeepAbyss,
            border = androidx.compose.foundation.BorderStroke(1.dp, RadiantTurquoise.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = RadiantTurquoise,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UNIVERSE ARCHIVE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_archive_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Archived Bank Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL DCOINS BANKED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                            )
                            Text(
                                text = "$totalCoins DCOINS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = SingularityGold
                                )
                            )
                        }
                        Text(
                            text = "${records.size} CYCLES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = RadiantTurquoise
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // History List
                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO SINGULARITY RECORDS YET\nSurvive or Collapse to Export",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(records) { item ->
                            ArchiveRecordCard(item, dateFormat)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clear Ledger Option
                if (records.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClear() }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Ledger",
                            tint = CriticalHorizon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PURGE ARCHIVE DATA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = CriticalHorizon
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchiveRecordCard(record: SingularityRecord, dateFormat: SimpleDateFormat) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CYCLE #${record.universeCycle} • ${record.levelName}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = RadiantTurquoise
                    )
                )
                Text(
                    text = dateFormat.format(Date(record.timestamp)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.outcome,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (record.outcome.contains("Evolution")) SingularityGold else CriticalHorizon
                    )
                )
                Text(
                    text = "+${record.dcoinsExported} ⚡",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = SingularityGold
                    )
                )
            }
        }
    }
}

@Composable
fun QuantumGuidanceDialog(onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DeepAbyss,
            border = androidx.compose.foundation.BorderStroke(1.dp, QuantumJade.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CYBER-ZEN QUANTUM CODEX",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = QuantumJade
                        )
                    )

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_guide_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                GuidanceItem(
                    title = "1. TAP: QUANTUM STABILIZATION",
                    desc = "Tap glowing bubbles to absorb quantum time density and harvest Dcoins with crisp tactile confirmation.",
                    color = RadiantTurquoise
                )

                GuidanceItem(
                    title = "2. HOLD: GRAVITATIONAL DILATION ANCHOR",
                    desc = "Press and hold anywhere on the cosmos to trigger a gravitational dilation anchor with low-frequency tactile hum, slowing entropy by 65%.",
                    color = SingularityGold
                )

                GuidanceItem(
                    title = "3. SWIPE: QUANTUM STREAM SWEEP",
                    desc = "Slice through chains of bubbles to harvest multi-combos with rapid haptic bursts.",
                    color = QuantumJade
                )

                GuidanceItem(
                    title = "4. LEVEL 2 QUASAR & LEVEL 3 POINT ZERO SYNTHESIZER",
                    desc = "At 500 Dcoins, enter Quasar Repulsion dynamics. At 1000 Dcoins, unlock Point Zero: slide vertically to modulate pitch with the real-time audio synthesizer, matching the carrier frequency and holding pitch to lock harmonic resonance and achieve Absolute Zero Entropy.",
                    color = QuasarBlueShift
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = QuantumJade),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "RETURN TO COSMOS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SpaceVoid
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidanceItem(title: String, desc: String, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        )
    }
}
