package com.dkt.todo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var menu by remember(task.id) { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().clickable(onClickLabel = "Editar tarefa", onClick = onEdit).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).toggleable(task.done, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics { contentDescription = if (task.done) "Reabrir: ${task.title}" else "Concluir: ${task.title}" }, contentAlignment = Alignment.Center) {
            Box(Modifier.size(23.dp).background(if (task.done) colors.onSurfaceVariant else colors.background, CircleShape)
                .border(1.5.dp, colors.onSurfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                if (task.done) PixelIcon(PixelSymbol.Check, null, Modifier.size(16.dp), colors.background)
            }
        }
        Text(task.title, Modifier.weight(1f).padding(horizontal = 4.dp),
            textDecoration = if (task.done) TextDecoration.LineThrough else null,
            color = if (task.done) colors.onSurfaceVariant else colors.onSurface,
            style = MaterialTheme.typography.bodyLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
        if (task.important) PixelIcon(PixelSymbol.Star, "Importante", Modifier.size(14.dp), colors.onSurfaceVariant)
        Box {
            IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Opções: ${task.title}", tint = colors.onSurfaceVariant) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Editar") }, onClick = { menu = false; onEdit() })
                DropdownMenuItem(text = { Text(if (task.important) "Remover importância" else "Marcar importante") }, onClick = { menu = false; onStar() })
                DropdownMenuItem(text = { Text("Excluir") }, onClick = { menu = false; onDelete() })
            }
        }
    }
}
