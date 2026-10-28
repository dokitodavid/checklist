package com.dkt.todo.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dkt.todo.ui.theme.AshenColors
import kotlin.math.floor
import kotlin.math.min

// Pixels inteiros, sem imagens remotas ou dependências de fontes.
private val fire = listOf(
    "................", "........e.......", ".......ee.......", ".......ef.......",
    "......eff.......", "......eff.e.....", ".....effffe.....", ".....effffe.....",
    "....effygffe....", "....effygffe....", "...effyyggffe...", "...effygggffe...",
    "....effggffe....", ".....effffe.....", "...wwwwwwwwww...", "..wwddwwddwwww..",
    "...dddddddddd...", "..sss....sss....", ".sssss..sssss...", "................",
)
private val palette = mapOf(
    'e' to Color(0xFFAE5139), 'f' to Color(0xFFE58B45),
    'y' to AshenColors.Ember, 'g' to AshenColors.Gold,
    'w' to Color(0xFF8B7655), 'd' to Color(0xFF574B39), 's' to Color(0xFF62695B),
)

enum class PixelSymbol(val pixels: List<String>) {
    Check(listOf(".......", ".....##", "....##.", "#..##..", "####...", ".##....", ".......")),
    Star(listOf("...#...", "..###..", "#######", ".#####.", "..###..", ".##.##.", ".#...#.")),
    Trash(listOf("..###..", "#######", ".#...#.", ".#.#.#.", ".#.#.#.", ".#...#.", ".#####.")),
    Plus(listOf(".......", "...#...", "...#...", ".#####.", "...#...", "...#...", ".......")),
}

@Composable
fun PixelIcon(symbol: PixelSymbol, description: String?, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier.size(21.dp).then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)) {
        val unit = min(size.width, size.height) / 7f
        symbol.pixels.forEachIndexed { y, row -> row.forEachIndexed { x, c ->
            if (c == '#') drawRect(tint, Offset(x * unit, y * unit), Size(unit, unit))
        } }
    }
}

/** Quatro poses de chama; o relógio Compose respeita a escala de animação do Android. */
@Composable
fun PixelCampfire(modifier: Modifier = Modifier, animated: Boolean = true) {
    val phase = if (animated) {
        val transition = rememberInfiniteTransition(label = "Fogueira")
        transition.animateFloat(
            initialValue = 0f, targetValue = 4f,
            animationSpec = infiniteRepeatable(tween(960, easing = LinearEasing), RepeatMode.Restart),
            label = "Chama pixelada",
        )
    } else null
    Canvas(modifier.semantics { contentDescription = "Fogueira em pixel art" }) {
        val unit = floor(min(size.width / 16, size.height / 20)).coerceAtLeast(1f)
        val origin = Offset((size.width - unit * 16) / 2, (size.height - unit * 20) / 2)
        val frame = (phase?.value ?: 0f).toInt().coerceIn(0, 3)
        fire.forEachIndexed { y, row -> row.forEachIndexed pixel@{ x, c ->
            val color = palette[c] ?: return@pixel
            val sway = if (y < 8) listOf(0, 1, 0, -1)[frame] else 0
            drawRect(color, origin + Offset((x + sway) * unit, y * unit), Size(unit, unit))
        } }
        // Uma única brasa sobe em passos, sem brilho ou flashes na tela.
        if (animated) drawRect(AshenColors.Ember.copy(alpha = 0.7f), origin + Offset((3 + frame) * unit, (5 - frame) * unit), Size(unit, unit))
    }
}
