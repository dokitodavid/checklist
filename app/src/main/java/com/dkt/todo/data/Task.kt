package com.dkt.todo.data

import java.time.LocalDate
import java.time.ZoneId

/**
 * Uma tarefa. [position] define a ordem manual (menor = mais acima); tarefas antigas, sem
 * posição salva, usam -id para manter as mais novas no topo. [dueDate] é o dia (epochDay) e
 * [remindAt] o horário do lembrete em minutos desde a meia-noite.
 */
data class Task(
    val id: Long,
    val title: String,
    val done: Boolean = false,
    val important: Boolean = false,
    val position: Long = -id,
    val dueDate: Long? = null,
    val remindAt: Int? = null,
) {
    val due: LocalDate? get() = dueDate?.let { LocalDate.ofEpochDay(it) }

    /** Momento do lembrete, se a tarefa tiver data e horário. */
    fun reminderMillis(zone: ZoneId = ZoneId.systemDefault()): Long? {
        val day = due ?: return null
        val minutes = remindAt ?: return null
        return day.atTime(minutes / 60, minutes % 60).atZone(zone).toInstant().toEpochMilli()
    }
}
