package com.dkt.todo.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dkt.todo.data.Task

@Composable
fun TaskRow(task: Task, onToggle: () -> Unit, onStar: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        if (task.done) colors.secondaryContainer.copy(alpha = 0.35f) else colors.surface,
        tween(240), label = "Tarefa concluída",
    )
    val markScale by animateFloatAsState(if (task.done) 1f else 0.75f, tween(180), label = "Selo de conclusão")
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(background, CutCornerShape(4.dp))
            .border(1.dp, if (task.important && !task.done) colors.primary.copy(alpha = 0.6f) else colors.outlineVariant, CutCornerShape(4.dp))
            .clickable(onClickLabel = "Editar tarefa", onClick = onEdit)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp)
                .toggleable(value = task.done, role = Role.Checkbox, onValueChange = { onToggle() })
                .semantics { contentDescription = if (task.done) "Reabrir: ${task.title}" else "Concluir: ${task.title}" },
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(24.dp).border(2.dp, if (task.done) colors.secondary else colors.outlineVariant), contentAlignment = Alignment.Center) {
                if (task.done) PixelIcon(PixelSymbol.Check, null, Modifier.size(18.dp).scale(markScale), colors.secondary)
            }
        }
        Text(
            task.title, modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            textDecoration = if (task.done) TextDecoration.LineThrough else null,
            color = if (task.done) colors.onSurfaceVariant else colors.onSurface,
            style = MaterialTheme.typography.bodyLarge, maxLines = 3, overflow = TextOverflow.Ellipsis,
        )
        IconButton(onClick = onStar) {
            PixelIcon(PixelSymbol.Star, if (task.important) "Desmarcar importante" else "Marcar importante",
                tint = if (task.important) colors.primary else colors.outline)
        }
        IconButton(onClick = onDelete) {
            PixelIcon(PixelSymbol.Trash, "Excluir tarefa", tint = colors.onSurfaceVariant)
        }
    }
}
