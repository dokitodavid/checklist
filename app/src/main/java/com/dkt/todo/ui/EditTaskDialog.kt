package com.dkt.todo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.dkt.todo.data.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Edição do título, da data e do lembrete. Texto vazio é ignorado (fecha sem salvar).
 * [onSave] recebe o título, o dia (epochDay) e o horário do lembrete em minutos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (title: String, dueDate: Long?, remindAt: Int?) -> Unit,
) {
    var text by remember(task.id) { mutableStateOf(task.title) }
    var dueDate by remember(task.id) { mutableStateOf(task.dueDate) }
    var remindAt by remember(task.id) { mutableStateOf(task.remindAt) }
    var pickingDate by remember { mutableStateOf(false) }
    var pickingTime by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { AshenCompanion(Modifier.size(48.dp, 72.dp), animated = false) },
        title = { Text("Editar tarefa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Tarefa") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { pickingDate = true },
                        label = { Text(dueDate?.let { dueLabel(LocalDate.ofEpochDay(it)) } ?: "Sem data") },
                        leadingIcon = { Icon(Icons.Filled.DateRange, null, Modifier.size(18.dp)) },
                    )
                    if (dueDate != null) {
                        IconButton(onClick = { dueDate = null; remindAt = null }) {
                            Icon(Icons.Filled.Close, "Tirar data")
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = { pickingTime = true },
                        label = { Text(remindAt?.let { timeLabel(it) } ?: "Sem lembrete") },
                        leadingIcon = { Icon(Icons.Filled.Notifications, null, Modifier.size(18.dp)) },
                    )
                    if (remindAt != null) {
                        IconButton(onClick = { remindAt = null }) {
                            Icon(Icons.Filled.Close, "Tirar lembrete")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = text.trim()
                if (v.isNotEmpty()) onSave(v, dueDate, remindAt) else onDismiss()
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )

    if (pickingDate) {
        // O DatePicker trabalha com meia-noite em UTC
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (dueDate ?: LocalDate.now().toEpochDay()) * 86_400_000L,
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        dueDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    pickingDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingDate = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state)
        }
    }

    if (pickingTime) {
        val initial = remindAt ?: (9 * 60)
        val state = rememberTimePickerState(initialHour = initial / 60, initialMinute = initial % 60, is24Hour = true)
        AlertDialog(
            onDismissRequest = { pickingTime = false },
            title = { Text("Horário do lembrete") },
            text = { TimeInput(state) },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = state.hour * 60 + state.minute
                    remindAt = minutes
                    // Lembrete sem data: hoje, ou amanhã se o horário já passou
                    if (dueDate == null) {
                        val now = LocalTime.now()
                        val today = LocalDate.now()
                        dueDate = (if (minutes > now.hour * 60 + now.minute) today else today.plusDays(1)).toEpochDay()
                    }
                    pickingTime = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingTime = false }) { Text("Cancelar") } },
        )
    }
}

fun timeLabel(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
