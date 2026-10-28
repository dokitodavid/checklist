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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.random.Random

/** Centro do check em coordenadas da janela, capturado no instante da conclusão. */
data class TaskCompletionEvent(val id: Int, val originInWindow: Offset)

private data class WaveTile(val x: Float, val y: Float, val side: Float, val gray: Float, val alpha: Float)

/**
 * Onda monocromática de quadradinhos que se espalha ao concluir uma tarefa (960 ms).
 * [event] é efêmero: cada novo id dispara a onda no check que originou a conclusão.
 * O ponto da janela é convertido para a Canvas após considerar seus insets. Decorativa: não recebe toques nem aparece para leitores de tela. Usa as
 * animações do Compose, então respeita a escala de animação do sistema (desligada = some).
 */
@Composable
fun TaskCompletionWave(event: TaskCompletionEvent?, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(1f) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var canvasOriginInWindow by remember { mutableStateOf(Offset.Zero) }
    val cell = with(LocalDensity.current) { 10.dp.toPx() }
    val tiles = remember(viewport, cell) {
        if (viewport == IntSize.Zero) emptyList() else buildList {
            val random = Random(41)
            for (row in -1..(viewport.height / cell).toInt() + 1) {
                for (column in -1..(viewport.width / cell).toInt() + 1) {
                    if (random.nextFloat() < 0.32f) continue
                    val side = listOf(0.2f, 0.3f, 0.4f, 0.5f, 0.6f)[random.nextInt(5)] * cell
                    val x = column * cell + (cell - side) / 2
                    val y = row * cell + (cell - side) / 2
                    add(
                        WaveTile(
                            x, y, side, listOf(160, 192, 220, 244)[random.nextInt(4)] / 255f,
                            0.13f + random.nextFloat() * 0.27f,
                        )
                    )
                }
            }
        }
    }
    LaunchedEffect(event?.id) {
        if (event != null) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 960, easing = LinearEasing))
        }
    }
    Canvas(modifier.onGloballyPositioned { coordinates ->
        viewport = coordinates.size
        canvasOriginInWindow = coordinates.localToWindow(Offset.Zero)
    }) {
        val t = progress.value
        if (t <= 0f || t >= 1f) return@Canvas
        val origin = event?.originInWindow?.minus(canvasOriginInWindow) ?: return@Canvas
        // A distância ao canto mais longe cobre a tela mesmo com checks nas bordas.
        val radiusMax = maxOf(
            hypot(origin.x, origin.y),
            hypot(size.width - origin.x, origin.y),
            hypot(origin.x, size.height - origin.y),
            hypot(size.width - origin.x, size.height - origin.y),
        ) + cell * 6.5f
        val radius = radiusMax * (1f - (1f - t).pow(1.5f))
        val strength = minOf(1f, t / 0.1f, (1f - t) / 0.18f).coerceAtLeast(0f)
        val band = cell * 5
        // Primeiro quadradinho no ponto exato do check, antes de a frente se expandir.
        if (t < 0.16f) {
            val side = cell * 0.35f
            drawRect(Color.White.copy(alpha = 0.28f * (1f - t / 0.16f)),
                origin - Offset(side / 2, side / 2), Size(side, side))
        }
        for (tile in tiles) {
            val distance = hypot(tile.x + tile.side / 2 - origin.x, tile.y + tile.side / 2 - origin.y)
            val p = (1f - abs(distance - radius) / band).coerceAtLeast(0f)
            val alpha = tile.alpha * p * p * strength
            if (alpha > 0f) {
                drawRect(Color(tile.gray, tile.gray, tile.gray, alpha), Offset(tile.x, tile.y), Size(tile.side, tile.side))
            }
        }
    }
}
