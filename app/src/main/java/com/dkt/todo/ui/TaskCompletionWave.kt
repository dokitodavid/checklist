package com.dkt.todo.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.random.Random

private data class WaveTile(val x: Float, val y: Float, val side: Float, val gray: Float, val alpha: Float, val distance: Float)

/**
 * Onda monocromática de quadradinhos que se espalha ao concluir uma tarefa (960 ms).
 * [trigger] começa em 0 e só aumenta quando uma tarefa pendente é concluída; cada aumento
 * reinicia a onda. Decorativa: não recebe toques nem aparece para leitores de tela. Usa as
 * animações do Compose, então respeita a escala de animação do sistema (desligada = some).
 */
@Composable
fun TaskCompletionWave(trigger: Int, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(1f) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    val cell = with(LocalDensity.current) { 10.dp.toPx() }
    val tiles = remember(viewport, cell) {
        if (viewport == IntSize.Zero) emptyList() else buildList {
            val random = Random(41)
            val cx = viewport.width * 0.5f
            val cy = viewport.height * 0.64f
            for (row in -1..(viewport.height / cell).toInt() + 1) {
                for (column in -1..(viewport.width / cell).toInt() + 1) {
                    if (random.nextFloat() < 0.32f) continue
                    val side = listOf(0.2f, 0.3f, 0.4f, 0.5f, 0.6f)[random.nextInt(5)] * cell
                    val x = column * cell + (cell - side) / 2
                    val y = row * cell + (cell - side) / 2
                    add(
                        WaveTile(
                            x, y, side, listOf(160, 192, 220, 244)[random.nextInt(4)] / 255f,
                            0.13f + random.nextFloat() * 0.27f, hypot(x - cx, y - cy),
                        )
                    )
                }
            }
        }
    }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 960, easing = LinearEasing))
        }
    }
    Canvas(modifier.onSizeChanged { viewport = it }) {
        val t = progress.value
        if (t <= 0f || t >= 1f) return@Canvas
        val radiusMax = hypot(size.width * 0.5f, size.height * 0.64f) + cell * 6.5f
        val radius = radiusMax * (1f - (1f - t).pow(1.5f))
        val strength = minOf(1f, t / 0.1f, (1f - t) / 0.18f).coerceAtLeast(0f)
        val band = cell * 5
        for (tile in tiles) {
            val p = (1f - abs(tile.distance - radius) / band).coerceAtLeast(0f)
            val alpha = tile.alpha * p * p * strength
            if (alpha > 0f) {
                drawRect(Color(tile.gray, tile.gray, tile.gray, alpha), Offset(tile.x, tile.y), Size(tile.side, tile.side))
            }
        }
    }
}
