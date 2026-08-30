package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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

@Composable
fun TimeDensityScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val showArchive by viewModel.showArchiveDialog.collectAsStateWithLifecycle()
    val showHelp by viewModel.showHelpDialog.collectAsStateWithLifecycle()
    val showSummary by viewModel.showTransitionSummary.collectAsStateWithLifecycle()
    val lastExport by viewModel.lastExportRecord.collectAsStateWithLifecycle()
    val archiveList by viewModel.archiveRecords.collectAsStateWithLifecycle()
    val totalDcoins by viewModel.totalArchivedDcoins.collectAsStateWithLifecycle()

    val touchTrails = remember { mutableStateListOf<List<Offset>>() }
    var currentTrail by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var containerHeight by remember { mutableStateOf(1920f) }

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
                    if (state.level != 3) {
                        detectTapGestures(
                            onTap = { offset ->
                                viewModel.onSingleTap(offset)
                            },
                            onPress = { offset ->
                                viewModel.onLongPressStart(offset)
                                tryAwaitRelease()
                                viewModel.onLongPressEnd()
                            }
                        )
                    }
                }
                .pointerInput(state.level) {
                    if (state.level == 3) {
                        // Level 3: Vertical Frequency Tuning
                        detectDragGestures(
                            onDragStart = { offset ->
                                val normY = (1.0f - (offset.y / containerHeight)).coerceIn(0f, 1f)
                                viewModel.onFrequencyDrag(normY, true)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val normY = (1.0f - (change.position.y / containerHeight)).coerceIn(0f, 1f)
                                viewModel.onFrequencyDrag(normY, true)
                            },
                            onDragEnd = {
                                viewModel.onFrequencyDrag(0f, false)
                            },
                            onDragCancel = {
                                viewModel.onFrequencyDrag(0f, false)
                            }
                        )
                    } else {
                        // Levels 1 & 2: Multi-Slice Swipe Stream
                        var dragStart = Offset.Zero
                        var activePoints = mutableListOf<Offset>()
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragStart = offset
                                activePoints = mutableListOf(offset)
                                currentTrail = activePoints.toList()
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                activePoints.add(change.position)
                                if (activePoints.size > 20) {
                                    activePoints.removeAt(0)
                                }
                                currentTrail = activePoints.toList()
                            },
                            onDragEnd = {
                                if (activePoints.size >= 2) {
                                    viewModel.onSwipe(dragStart, activePoints.last())
                                }
                                touchTrails.add(activePoints.toList())
                                if (touchTrails.size > 5) {
                                    touchTrails.removeAt(0)
                                }
                                currentTrail = emptyList()
                            },
                            onDragCancel = {
                                currentTrail = emptyList()
                            }
                        )
                    }
                }
        ) {
            val allTrails = remember(touchTrails, currentTrail) {
                if (currentTrail.isNotEmpty()) touchTrails + listOf(currentTrail) else touchTrails.toList()
            }
            GameCanvas(
                engine = viewModel.engine,
                state = state,
                touchTrails = allTrails
            )
        }

        // --- Minimalist Cyber-Zen HUD Overlay ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            TopHudBar(
                state = state,
                onOpenArchive = { viewModel.toggleArchiveDialog(true) },
                onOpenHelp = { viewModel.toggleHelpDialog(true) }
            )

            // Center Floating Alerts / Horizon Status
            CenterHudIndicator(state = state)

            // Bottom Navigation & Gesture Instruction Guide
            BottomControlBar(state = state)
        }

        // --- Singularity Transition Gateway Dialog ---
        if (showSummary && lastExport != null) {
            SingularityExportDialog(
                record = lastExport!!,
                currentLevel = state.level,
                onProceedToLevel2 = { viewModel.proceedToLevel2() },
                onProceedToLevel3 = { viewModel.proceedToLevel3() },
                onReigniteCycle = { viewModel.restartGame(1) }
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
    }
}

@Composable
private fun TopHudBar(
    state: GameState,
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

        // Action Buttons (Codex Archive & Help)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenArchive,
                modifier = Modifier
                    .size(40.dp)
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
                    .size(40.dp)
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

@Composable
private fun CenterHudIndicator(state: GameState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time Density Progress Bar & Value
        val densityPercent = state.density.coerceIn(0f, 100f)
        val densityColor = when {
            densityPercent < 15f -> CriticalHorizon
            densityPercent < 45f -> CoronaAmber
            else -> QuantumJade
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, densityColor.copy(alpha = 0.5f)),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.level == 3) "RESONANCE STABILITY" else "TIME DENSITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    )
                    Text(
                        text = "${densityPercent.toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = densityColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { densityPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = densityColor,
                    trackColor = DeepAbyss
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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
                                color = if (state.density < 10f) CriticalHorizon else TextMuted
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
private fun BottomControlBar(state: GameState) {
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gesture Instructions
        if (state.level == 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = SingularityGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                GestureHintItem(label = "SLIDE VERTICALLY", desc = "Match Carrier Wave Frequency", color = SingularityGold)
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
                GestureHintItem(label = "SWIPE", desc = "Sweep Stream", color = QuantumJade)
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

@Composable
private fun GestureHintItem(label: String, desc: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "[$label]",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 11.sp
            )
        )
    }
}

// --- DIALOGS ---

@Composable
fun SingularityExportDialog(
    record: SingularityRecord,
    currentLevel: Int,
    onProceedToLevel2: () -> Unit,
    onProceedToLevel3: () -> Unit,
    onReigniteCycle: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DeepAbyss,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SingularityGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AllInclusive,
                    contentDescription = "Singularity Gate",
                    tint = SingularityGold,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SINGULARITY GATEWAY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = SingularityGold,
                        letterSpacing = 2.sp
                    )
                )

                Text(
                    text = "Cosmic State Compressed & Exported",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SurfaceBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Stats Matrix
                StatRow(label = "TRANSITION EVENT", value = record.outcome, color = RadiantTurquoise)
                StatRow(label = "UNIVERSE CYCLE", value = "CYC-${record.universeCycle.toString().padStart(2, '0')}", color = TextPrimary)
                StatRow(label = "DCOINS EXPORTED", value = "+${record.dcoinsExported} ⚡", color = SingularityGold)
                StatRow(
                    label = "SURVIVAL DURATION",
                    value = String.format(Locale.US, "%.1fs", record.survivalTimeSeconds),
                    color = QuantumJade
                )
                StatRow(label = "MAX RESONANCE COMBO", value = "${record.maxCombo}x", color = CoronaAmber)

                Spacer(modifier = Modifier.height(24.dp))

                if (currentLevel == 1) {
                    // Proceed to Level 2 (Quasar Dynamics)
                    Button(
                        onClick = onProceedToLevel2,
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
                } else if (currentLevel == 2) {
                    // Proceed to Level 3 (Point Zero)
                    Button(
                        onClick = onProceedToLevel3,
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Re-ignite Big Bang Cycle
                Button(
                    onClick = onReigniteCycle,
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
                        text = "RE-IGNITE BIG BANG (NEW CYCLE)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadiantTurquoise
                        )
                    )
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
                    title = "4. LEVEL 2 QUASAR & LEVEL 3 POINT ZERO",
                    desc = "At 500 Dcoins, enter Quasar Repulsion dynamics. At 1000 Dcoins, unlock Point Zero and slide vertically to tune carrier wave frequencies to Absolute Zero Entropy.",
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
