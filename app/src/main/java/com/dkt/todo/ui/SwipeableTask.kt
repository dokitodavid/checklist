package com.dkt.todo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dkt.todo.data.Task

/**
 * Arrastar para a direita conclui (ou reabre) a tarefa e ela volta ao lugar;
 * arrastar para a esquerda exclui (com Desfazer na barra de baixo).
 */
@Composable
fun SwipeableTask(
    task: Task,
    onToggle: (Offset?) -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    var checkCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val state = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.35f })
    LaunchedEffect(state.currentValue) {
        when (state.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                val origin = checkCoordinates?.takeIf { it.isAttached }?.let { coordinates ->
                    coordinates.localToWindow(Offset(coordinates.size.width / 2f, coordinates.size.height / 2f))
                }
                onToggle(origin)
                state.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.EndToStart -> onDelete()
            SwipeToDismissBoxValue.Settled -> Unit
        }
    }
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val direction = state.dismissDirection
            val colors = MaterialTheme.colorScheme
            val (bg, icon, align) = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Triple(
                    colors.surfaceContainerHighest,
                    if (task.done) Icons.Filled.Refresh else Icons.Filled.Check,
                    Alignment.CenterStart,
                )
                SwipeToDismissBoxValue.EndToStart -> Triple(colors.error.copy(alpha = 0.25f), Icons.Filled.Delete, Alignment.CenterEnd)
                SwipeToDismissBoxValue.Settled -> Triple(Color.Transparent, null, Alignment.Center)
            }
            Box(
                Modifier.fillMaxSize().background(bg, MaterialTheme.shapes.medium).padding(horizontal = 20.dp),
                contentAlignment = align,
            ) {
                if (icon != null) Icon(icon, contentDescription = null, tint = colors.onSurface,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        if (direction == SwipeToDismissBoxValue.StartToEnd) checkCoordinates = coordinates
                    })
            }
        },
    ) {
        content()
    }
}
