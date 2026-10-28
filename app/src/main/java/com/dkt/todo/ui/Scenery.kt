package com.dkt.todo.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import com.dkt.todo.R

/** Arte decorativa sem suavização (pixel art nítido) e sem descrição para leitores de tela. */
@Composable
fun SceneryImage(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    alpha: Float = 1f,
) {
    val bitmap = ImageBitmap.imageResource(id)
    val painter = remember(bitmap) { BitmapPainter(bitmap, filterQuality = FilterQuality.None) }
    Image(painter, contentDescription = null, modifier = modifier, alignment = alignment, contentScale = contentScale, alpha = alpha)
}

/**
 * Fundo das abas secundárias: cenário vertical de ruínas preso à tela (o conteúdo rola na
 * frente), discreto para não atrapalhar a leitura.
 */
@Composable
fun RuinsBackground(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        SceneryImage(R.drawable.ruins_tabs, Modifier.fillMaxSize(), ContentScale.Crop, alpha = 0.30f)
    }
}
