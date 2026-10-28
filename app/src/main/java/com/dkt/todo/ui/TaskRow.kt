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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dkt.todo.data.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDate = DateTimeFormatter.ofPattern("EEE, d 'de' MMM", Locale.forLanguageTag("pt-BR"))

/** "Hoje", "Amanhã", "Ontem" ou a data curta ("qua., 29 de out."). */
fun dueLabel(day: LocalDate, today: LocalDate = LocalDate.now()): String = when (day) {
    today -> "Hoje"
    today.plusDays(1) -> "Amanhã"
    today.minusDays(1) -> "Ontem"
    else -> day.format(shortDate)
}

@Composable
fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onStar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    dragging: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    var menu by remember(task.id) { mutableStateOf(false) }
    Row(
        modifier.fillMaxWidth()
            .background(if (dragging) colors.surfaceContainerHighest else Color.Transparent, MaterialTheme.shapes.medium)
            .clickable(onClickLabel = "Editar tarefa", onClick = onEdit).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).toggleable(task.done, role = Role.Checkbox, onValueChange = { onToggle() })
            .semantics { contentDescription = if (task.done) "Reabrir: ${task.title}" else "Concluir: ${task.title}" }, contentAlignment = Alignment.Center) {
            Box(Modifier.size(23.dp).background(if (task.done) colors.onSurfaceVariant else colors.background, CircleShape)
                .border(1.5.dp, colors.onSurfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                if (task.done) PixelIcon(PixelSymbol.Check, null, Modifier.size(16.dp), colors.background)
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
            Text(task.title,
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
                color = if (task.done) colors.onSurfaceVariant else colors.onSurface,
                style = MaterialTheme.typography.bodyLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
            task.due?.let { day ->
                val late = !task.done && day.isBefore(LocalDate.now())
                val time = task.remindAt?.let { " · ${timeLabel(it)}" } ?: ""
                Text(
                    dueLabel(day) + time,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (late) colors.error else colors.onSurfaceVariant,
                )
            }
        }
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
