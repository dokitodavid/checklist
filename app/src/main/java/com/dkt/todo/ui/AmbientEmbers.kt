package com.dkt.todo.ui

import android.content.Context
import android.os.PowerManager
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dkt.todo.R
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/** Quadros de cintilação de cada arte (design/brasas/animacao.json). */
private const val FRAMES = 24
private const val FRAME_MS = 80
private const val CYCLE_MS = 72_000

/**
 * Uma brasa: qual arte usa, onde sobe (fração da largura), tamanho do pixel da arte em dp,
 * quantas voltas dá por ciclo (velocidade), defasagem e inclinação/giro em graus.
 */
private class Ember(
    val sprite: Int,
    val x: Float,
    val pixelDp: Float,
    val laps: Int,
    val offset: Float,
    val tilt: Float,
    val swing: Float,
)

private val embers = listOf(
    Ember(0, 0.08f, 0.45f, 3, 0.00f, -6f, 10f),
    Ember(2, 0.22f, 0.28f, 4, 0.55f, 12f, 14f),
    Ember(1, 0.36f, 0.26f, 5, 0.20f, 0f, 18f),
    Ember(0, 0.50f, 0.38f, 4, 0.80f, 8f, 8f),
    Ember(2, 0.63f, 0.24f, 3, 0.35f, -14f, 12f),
    Ember(1, 0.77f, 0.22f, 5, 0.65f, 6f, 16f),
    Ember(0, 0.91f, 0.40f, 4, 0.10f, -10f, 10f),
)

/** Camada decorativa sem interceptar toques ou criar elementos de acessibilidade. */
@Composable
fun AmbientEmbers(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if ((context.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode) return
    val sprites: List<ImageBitmap> = listOf(
        // Tiras com os 24 quadros lado a lado
        ImageBitmap.imageResource(R.drawable.ember_long_frames),
        ImageBitmap.imageResource(R.drawable.ember_double_frames),
        ImageBitmap.imageResource(R.drawable.ember_shard_frames),
    )
    // Relógio próprio, a cada quadro da tela. Não usa as animações do Compose porque elas
    // param quando a escala de duração do Animator do sistema está zerada (alguns modos de
    // bateria/desempenho fazem isso), e as brasas devem se mexer sempre.
    var clockMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) withFrameMillis { clockMs = it - start }
    }
    Canvas(modifier) {
        // Ciclo de 72 s; cada brasa dá 3, 4 ou 5 voltas nele (24, 18 ou 14,4 s para atravessar a tela)
        val phase = (clockMs % CYCLE_MS) / CYCLE_MS.toFloat()
        embers.forEach { e ->
            val p = (phase * e.laps + e.offset) % 1f
            val img = sprites[e.sprite]
            val frameW = img.width / FRAMES
            // Cintilação: troca de quadro a cada 80 ms; cada brasa começa num quadro diferente
            val frame = ((clockMs / FRAME_MS).toInt() + (e.offset * FRAMES).toInt()) % FRAMES
            val px = e.pixelDp.dp.toPx()
            val w = frameW * px
            val h = img.height * px
            val cx = size.width * e.x + sin(p * 4 * PI).toFloat() * 8.dp.toPx()
            val cy = size.height + h - p * (size.height + 2 * h)
            // Acende rápido ao entrar e vai se apagando aos poucos na segunda metade da subida
            val alpha = 0.6f * when {
                p < 0.1f -> p / 0.1f
                p < 0.4f -> 1f
                else -> (1f - (p - 0.4f) / 0.6f).let { it * it }
            }
            rotate(e.tilt + sin(p * 3 * PI).toFloat() * e.swing, pivot = Offset(cx, cy)) {
                drawImage(
                    image = img,
                    srcOffset = IntOffset(frame * frameW, 0),
                    srcSize = IntSize(frameW, img.height),
                    dstOffset = IntOffset((cx - w / 2).roundToInt(), (cy - h / 2).roundToInt()),
                    dstSize = IntSize(w.roundToInt(), h.roundToInt()),
                    alpha = alpha,
                    filterQuality = FilterQuality.None,
                )
            }
        }
    }
}
