package com.dkt.todo.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.dkt.todo.R

// Os controles são discretos; a arte detalhada é o foco da identidade.
enum class PixelSymbol { Check, Star, Trash, Plus }

@Composable
fun PixelIcon(symbol: PixelSymbol, description: String?, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary) {
    Icon(
        imageVector = when (symbol) {
            PixelSymbol.Check -> Icons.Filled.Check
            PixelSymbol.Star -> Icons.Filled.Star
            PixelSymbol.Trash -> Icons.Filled.Delete
            PixelSymbol.Plus -> Icons.Filled.Add
        },
        contentDescription = description, modifier = modifier.size(21.dp), tint = tint,
    )
}

/** A arte usa o relógio Compose: respeita a escala de animação e o ciclo de vida. */
@Composable
fun AshenCompanion(modifier: Modifier = Modifier, animated: Boolean = true, alpha: Float = 1f) {
    val transition = if (animated) rememberInfiniteTransition(label = "Mago assassino") else null
    val breath = transition?.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "Respiração sutil",
    )
    Box(modifier) {
        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.ashen_companion),
            contentDescription = "Mago assassino de capuz com adaga e uma brasa mágica",
            modifier = Modifier.fillMaxSize().graphicsLayer {
                translationY = (breath?.value ?: 0f) * 1.dp.toPx()
                this.alpha = alpha
            },
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
        )
    }
}
