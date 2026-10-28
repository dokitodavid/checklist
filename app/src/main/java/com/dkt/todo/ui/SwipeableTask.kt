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
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.35f })
    LaunchedEffect(state.currentValue) {
        when (state.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                onToggle()
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
                if (icon != null) Icon(icon, contentDescription = null, tint = colors.onSurface)
            }
        },
    ) {
        content()
    }
}
