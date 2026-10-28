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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dkt.todo.ui.theme.AshenColors
import kotlin.math.PI
import kotlin.math.sin

private const val CYCLE_MS = 24_000

/** Sete pixels âmbar que sobem a tela devagar, acendendo e apagando. */
@Composable
fun AmbientEmbers(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    if ((context.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode) return
    // Relógio próprio, a cada quadro da tela. Não usa as animações do Compose porque elas
    // param quando a escala de duração do Animator do sistema está zerada (alguns modos de
    // bateria/desempenho fazem isso), e as brasas devem se mexer sempre.
    var clockMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) withFrameMillis { clockMs = it - start }
    }
    Canvas(modifier) {
        val phase = (clockMs % CYCLE_MS) / CYCLE_MS.toFloat()
        val pixel = 1.dp.toPx()
        repeat(7) { i ->
            val p = (phase + i / 7f) % 1f
            val x = size.width * ((0.12f + i * 0.137f + sin(p * 2 * PI).toFloat() * 0.025f) % 1f)
            val y = size.height * (1.04f - p * 1.12f)
            val alpha = sin(p * PI).toFloat().coerceAtLeast(0f) * 0.5f
            // Posição contínua (sem prender à grade de pixels) para o movimento ficar fluido
            drawRect(
                AshenColors.Ember.copy(alpha = alpha),
                Offset(x, y),
                Size(pixel * (if (i % 3 == 0) 3 else 2), pixel * 3),
            )
        }
    }
}
