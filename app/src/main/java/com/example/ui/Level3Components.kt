package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameState
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DeepAbyss
import com.example.ui.theme.RadiantTurquoise
import com.example.ui.theme.SingularityGold
import com.example.ui.theme.SpaceVoid
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * 1. ОСЦИЛЛОГРАФ (КРИВАЯ СВЕРХУ)
 * Отображает две отчетливо различимые волны:
 * - Золотую опорную волну (target frequency)
 * - Циановую несущую волну игрока (player carrier wave)
 * Когда игрок двигает слайдер — циановая волна меняет частоту и синхронизируется с золотой.
 */
@Composable
fun PointZeroOscilloscope(
    state: GameState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OscilloscopeTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OscilloscopePhase"
    )

    val freqDiff = abs(state.playerFrequency - state.targetFrequency)
    val matchRatio = (1.0f - (freqDiff / 0.38f)).coerceIn(0f, 1f)
    val matchPercent = (matchRatio * 100f).toInt()

    val targetHz = (220.0f + state.targetFrequency * 440.0f).toInt()
    val playerHz = (220.0f + state.playerFrequency * 440.0f).toInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepAbyss.copy(alpha = 0.95f), SpaceVoid.copy(alpha = 0.98f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                1.5.dp,
                if (matchRatio > 0.8f) SingularityGold else SurfaceBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("level3_oscilloscope")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Верхняя строка осциллографа: Частоты и Процент совпадения
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Метка золотой волны (Цель)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(SingularityGold, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "TARGET: $targetHz Hz",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SingularityGold,
                            fontSize = 9.sp
                        )
                    )
                }

                // Индикатор процента совпадения (HARMONIC MATCH)
                Box(
                    modifier = Modifier
                        .background(
                            if (matchRatio > 0.75f) SingularityGold.copy(alpha = 0.25f) else CyberSurface.copy(alpha = 0.7f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (matchRatio > 0.75f) SingularityGold else SurfaceBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "MATCH: $matchPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (matchRatio > 0.75f) SingularityGold else RadiantTurquoise,
                            fontSize = 9.sp
                        )
                    )
                }

                // Метка циановой волны (Несущая игрока)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CARRIER: $playerHz Hz",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadiantTurquoise,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(RadiantTurquoise, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Холст отрисовки двух осциллографических кривых
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
            ) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f

                // 1. Координатная сетка осциллографа
                val gridCols = 8
                for (c in 1 until gridCols) {
                    val x = (c.toFloat() / gridCols) * width
                    drawLine(
                        color = SurfaceBorder.copy(alpha = 0.35f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1f
                    )
                }
                drawLine(
                    color = SurfaceBorder.copy(alpha = 0.5f),
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 1.2f
                )

                // 2. Золотая опорная волна (Target Frequency)
                val targetCycles = 2.0f + state.targetFrequency * 4.0f
                val targetAmp = height * 0.35f
                val targetPath = Path()
                val steps = 120

                for (i in 0..steps) {
                    val x = (i.toFloat() / steps) * width
                    val angle = 2f * PI.toFloat() * targetCycles * (i.toFloat() / steps) + wavePhase
                    val y = centerY + sin(angle) * targetAmp
                    if (i == 0) targetPath.moveTo(x, y) else targetPath.lineTo(x, y)
                }

                // Свечение золотой волны
                val glowAlpha = (0.35f + matchRatio * 0.45f).coerceIn(0.35f, 0.85f)
                drawPath(
                    path = targetPath,
                    color = SingularityGold.copy(alpha = glowAlpha),
                    style = Stroke(width = 6f + matchRatio * 4f, cap = StrokeCap.Round)
                )
                // Сердечник золотой волны
                drawPath(
                    path = targetPath,
                    color = SingularityGold,
                    style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                )

                // 3. Циановая волна игрока (Player Carrier Wave)
                val playerCycles = 2.0f + state.playerFrequency * 4.0f
                val playerAmp = height * 0.35f
                val playerPath = Path()

                for (i in 0..steps) {
                    val x = (i.toFloat() / steps) * width
                    val angle = 2f * PI.toFloat() * playerCycles * (i.toFloat() / steps) + wavePhase
                    val y = centerY + sin(angle) * playerAmp
                    if (i == 0) playerPath.moveTo(x, y) else playerPath.lineTo(x, y)
                }

                // Свечение циановой волны игрока
                drawPath(
                    path = playerPath,
                    color = RadiantTurquoise.copy(alpha = glowAlpha),
                    style = Stroke(width = 6f + matchRatio * 4f, cap = StrokeCap.Round)
                )
                // Сердечник циановой волны
                drawPath(
                    path = playerPath,
                    color = RadiantTurquoise,
                    style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                )

                // 4. При точном совпадении — белая световая интерференция в узлах
                if (matchRatio > 0.85f) {
                    drawPath(
                        path = targetPath,
                        color = Color.White.copy(alpha = (matchRatio - 0.85f) * 6f),
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

/**
 * 2. КРУПНЫЙ ГОРИЗОНТАЛЬНЫЙ СЛАЙДЕР НА ВСЮ ШИРИНУ
 * - Высота: не меньше 60px (задано 68dp)
 * - Яркая ручка с подписью «TUNE»
 * - Плавное перемещение и удержание для блокировки
 */
@Composable
fun PointZeroTuneSlider(
    playerFrequency: Float,
    isResonating: Boolean,
    matchPercent: Int,
    onFrequencyChange: (Float, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidthPx by remember { mutableFloatStateOf(1000f) }
    val density = LocalDensity.current
    val thumbWidthDp = 86.dp
    val thumbWidthPx = with(density) { thumbWidthDp.toPx() }

    val infiniteTransition = rememberInfiniteTransition(label = "TuneThumbPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp) // Высота не менее 60px
            .onGloballyPositioned { coords ->
                if (coords.size.width > 0) {
                    trackWidthPx = coords.size.width.toFloat()
                }
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        DeepAbyss.copy(alpha = 0.95f),
                        CyberSurface.copy(alpha = 0.98f),
                        DeepAbyss.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                2.dp,
                if (matchPercent > 80) SingularityGold else RadiantTurquoise.copy(alpha = 0.7f),
                RoundedCornerShape(20.dp)
            )
            .pointerInput(trackWidthPx) {
                detectTapGestures(
                    onPress = { offset ->
                        val effectiveWidth = (trackWidthPx - thumbWidthPx).coerceAtLeast(1f)
                        val touchX = (offset.x - thumbWidthPx / 2f).coerceIn(0f, effectiveWidth)
                        val ratio = touchX / effectiveWidth
                        onFrequencyChange(ratio, true)
                        tryAwaitRelease()
                        onFrequencyChange(ratio, false)
                    }
                )
            }
            .pointerInput(trackWidthPx) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val effectiveWidth = (trackWidthPx - thumbWidthPx).coerceAtLeast(1f)
                        val touchX = (offset.x - thumbWidthPx / 2f).coerceIn(0f, effectiveWidth)
                        val ratio = touchX / effectiveWidth
                        onFrequencyChange(ratio, true)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val effectiveWidth = (trackWidthPx - thumbWidthPx).coerceAtLeast(1f)
                        val touchX = (change.position.x - thumbWidthPx / 2f).coerceIn(0f, effectiveWidth)
                        val ratio = touchX / effectiveWidth
                        onFrequencyChange(ratio, true)
                    },
                    onDragEnd = {
                        onFrequencyChange(playerFrequency, false)
                    },
                    onDragCancel = {
                        onFrequencyChange(playerFrequency, false)
                    }
                )
            }
            .testTag("point_zero_tune_slider"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Фоновая разметка частот и спектра
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOW 220Hz",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            )
            Text(
                text = "◄ SLIDE TO TUNE FREQUENCY ►",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (matchPercent > 80) SingularityGold else RadiantTurquoise.copy(alpha = 0.5f),
                    fontSize = 9.sp
                )
            )
            Text(
                text = "HIGH 660Hz",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            )
        }

        // Позиционируемая ручка слайдера с надписью «TUNE»
        val maxOffsetPx = (trackWidthPx - thumbWidthPx).coerceAtLeast(0f)
        val currentOffsetPx = (maxOffsetPx * playerFrequency).coerceIn(0f, maxOffsetPx)

        Box(
            modifier = Modifier
                .offset { IntOffset(currentOffsetPx.toInt(), 0) }
                .width(thumbWidthDp)
                .height(34.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (matchPercent > 80) {
                            listOf(SingularityGold, Color(0xFFFFD54F))
                        } else {
                            listOf(RadiantTurquoise, SingularityGold)
                        }
                    )
                )
                .border(
                    2.dp,
                    Color.White.copy(alpha = if (isResonating) pulseAlpha else 0.8f),
                    RoundedCornerShape(16.dp)
                )
                .testTag("tune_slider_handle"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Tune Handle",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "TUNE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

/**
 * 3. ЖИВАЯ ОБРАТНАЯ СВЯЗЬ:
 * - Надпись «RESONANCE»
 * - Кольцо «LOCK 5.0s» с обратным отсчётом при точном совпадении
 * - Если сойти с резонанса — кольцо сбрасывается
 */
@Composable
fun PointZeroResonanceFeedback(
    state: GameState,
    modifier: Modifier = Modifier
) {
    val inResonance = state.isResonating && state.resonancePrecision > 0.70f
    val remainingLockSec = (5.0f - state.resonanceHoldDuration).coerceAtLeast(0f)
    val progress = (state.resonanceHoldDuration / 5.0f).coerceIn(0f, 1f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = inResonance,
            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
            exit = fadeOut(tween(200)) + scaleOut(tween(200))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Баннер «RESONANCE»
                Box(
                    modifier = Modifier
                        .background(SingularityGold.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                        .border(1.5.dp, SingularityGold, RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✦ RESONANCE MATCHED ✦",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = SingularityGold,
                            fontSize = 14.sp
                        )
                    )
                }

                // Кольцо «LOCK 5.0s» с обратным отсчетом
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Фоновое кольцо
                    CircularProgressIndicator(
                        progress = { 1.0f },
                        modifier = Modifier.fillMaxSize(),
                        color = SurfaceBorder.copy(alpha = 0.35f),
                        strokeWidth = 8.dp
                    )
                    // Активное кольцо прогресса блокировки
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = SingularityGold,
                        strokeWidth = 8.dp
                    )
                    // Текст внутри кольца
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LOCK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = SingularityGold,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = String.format(Locale.US, "%.1fs", remainingLockSec),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = SingularityGold,
                                fontSize = 22.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. ВХОДНОЙ ТУТОРИАЛ (ОДНОРАЗОВЫЙ)
 * При старте уровня — полупрозрачная подсказка по центру:
 * «SLIDE TO TUNE — без постукивания. Совпадение с золотой волной. Задерживайте 5 секунд до блокировки.»
 * Исчезает по тапу или при первом движении слайдера.
 */
@Composable
fun PointZeroTutorialOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable { onDismiss() }
            .testTag("point_zero_tutorial_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DeepAbyss.copy(alpha = 0.96f), CyberSurface.copy(alpha = 0.98f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(2.dp, SingularityGold, RoundedCornerShape(20.dp))
                .padding(24.dp)
                .clickable(enabled = false) {}, // предотвращает клик сквозь карточку
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Иконка калибровки волн
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(SingularityGold.copy(alpha = 0.18f), CircleShape)
                        .border(1.5.dp, SingularityGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Tutorial Icon",
                        tint = SingularityGold,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "POINT ZERO CALIBRATION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = SingularityGold,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center
                )

                // Обязательный текст туториала
                Text(
                    text = "SLIDE TO TUNE — no tapping.\nMatch the golden carrier wave.\nHold the resonance for 5 seconds to lock.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Кнопка подтверждения и начала
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SingularityGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .testTag("dismiss_level3_tutorial_button")
                ) {
                    Text(
                        text = "ENGAGE RESONATOR",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}
