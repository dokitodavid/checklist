package com.dkt.todo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.dkt.todo.data.Task

@Composable
fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onStar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onEdit() },
        leadingContent = {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
        },
        headlineContent = {
            Text(
                text = task.title,
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
                color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onStar) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = if (task.important) "Desmarcar importante" else "Marcar importante",
                        tint = if (task.important) Color(0xFFFFB300)
                        else MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
        },
    )
}
