package com.dkt.todo.ui

import android.content.Context
import android.os.PowerManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
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
    Ember(2, 0.22f, 0.40f, 4, 0.55f, 12f, 14f),
    Ember(1, 0.36f, 0.50f, 5, 0.20f, 0f, 18f),
    Ember(0, 0.50f, 0.38f, 4, 0.80f, 8f, 8f),
    Ember(2, 0.63f, 0.34f, 3, 0.35f, -14f, 12f),
    Ember(1, 0.77f, 0.42f, 5, 0.65f, 6f, 16f),
    Ember(0, 0.91f, 0.40f, 4, 0.10f, -10f, 10f),
)

/** Camada decorativa sem interceptar toques ou criar elementos de acessibilidade. */
@Composable
fun AmbientEmbers(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if ((context.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode) return
    val sprites: List<ImageBitmap> = listOf(
        ImageBitmap.imageResource(R.drawable.ember_long),
        ImageBitmap.imageResource(R.drawable.ember_double),
        ImageBitmap.imageResource(R.drawable.ember_shard),
    )
    val transition = rememberInfiniteTransition(label = "Brasas ambientes")
    // Ciclo de 72 s; cada brasa dá 3, 4 ou 5 voltas nele (24, 18 ou 14,4 s para atravessar a tela)
    val phase = transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(72_000, easing = LinearEasing)), label = "Passagem lenta",
    )
    Canvas(modifier) {
        embers.forEach { e ->
            val p = (phase.value * e.laps + e.offset) % 1f
            val img = sprites[e.sprite]
            val px = e.pixelDp.dp.toPx()
            val w = img.width * px
            val h = img.height * px
            val cx = size.width * e.x + sin(p * 4 * PI).toFloat() * 8.dp.toPx()
            val cy = size.height + h - p * (size.height + 2 * h)
            // Acende ao entrar, fica estável no meio e apaga ao sair (máximo de 60%)
            val alpha = (sin(p * PI).toFloat() * 1.4f).coerceIn(0f, 1f) * 0.6f
            rotate(e.tilt + sin(p * 3 * PI).toFloat() * e.swing, pivot = Offset(cx, cy)) {
                drawImage(
                    image = img,
                    dstOffset = IntOffset((cx - w / 2).roundToInt(), (cy - h / 2).roundToInt()),
                    dstSize = IntSize(w.roundToInt(), h.roundToInt()),
                    alpha = alpha,
                    filterQuality = FilterQuality.None,
                )
            }
        }
    }
}
