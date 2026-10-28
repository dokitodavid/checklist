package com.dkt.todo.ui

import android.content.Context
import android.os.PowerManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dkt.todo.ui.theme.AshenColors
import kotlin.math.floor
import kotlin.math.sin

/** Camada decorativa sem interceptar toques ou criar elementos de acessibilidade. */
@Composable
fun AmbientEmbers(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if ((context.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode) return
    val transition = rememberInfiniteTransition(label = "Brasas ambientes")
    val phase = transition.animateFloat(0f, 1f,
        infiniteRepeatable(tween(24000, easing = LinearEasing)), label = "Passagem lenta")
    Canvas(modifier) {
        val pixel = 1.dp.toPx()
        repeat(7) { i ->
            val p = (phase.value + i / 7f) % 1f
            val x = size.width * ((0.12f + i * 0.137f + sin(p * 6.28f) * 0.025f) % 1f)
            val y = size.height * (1.04f - p * 1.12f)
            val alpha = sin(p * Math.PI).toFloat().coerceAtLeast(0f) * 0.50f
            drawRect(AshenColors.Ember.copy(alpha = alpha),
                Offset(floor(x / pixel) * pixel, floor(y / pixel) * pixel), Size(pixel * (if (i % 3 == 0) 3 else 2), pixel * 3))
        }
    }
}
