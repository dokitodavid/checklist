package com.dkt.todo.ui

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.dkt.todo.data.Task

/** Diálogo de edição do título. Texto vazio é ignorado (fecha sem salvar). */
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var text by remember(task.id) { mutableStateOf(task.title) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { AshenCompanion(Modifier.size(48.dp, 72.dp), animated = false) },
        title = { Text("Editar tarefa") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Tarefa") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val v = text.trim()
                if (v.isNotEmpty()) onSave(v) else onDismiss()
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
