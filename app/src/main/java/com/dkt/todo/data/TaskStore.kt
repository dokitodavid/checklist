package com.dkt.todo.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Guarda as tarefas em JSON no SharedPreferences ("tarefas" → "items") e o histórico
 * diário de criadas/feitas ("activity"), que continua mesmo se as tarefas forem excluídas.
 */
class TaskStore(context: Context) {
    private val prefs = context.getSharedPreferences("tarefas", Context.MODE_PRIVATE)

    init {
        // Primeira vez com histórico: recupera o dia de criação das tarefas existentes
        // (o id é o horário de criação). Quando foram concluídas não dá pra saber.
        if (!prefs.contains("activity")) {
            val seed = load()
                .filter { it.id >= 946_684_800_000L } // ids a partir de 2000 são horários
                .groupingBy { dayOf(it.id) }
                .eachCount()
                .mapValues { DayActivity(created = it.value) }
            saveActivity(seed)
        }
    }

    fun load(): List<Task> {
        val raw = prefs.getString("items", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Task(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    done = o.optBoolean("done", false),
                    important = o.optBoolean("important", false),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(tasks: List<Task>) {
        val arr = JSONArray()
        tasks.forEach { t ->
            arr.put(
                JSONObject()
                    .put("id", t.id)
                    .put("title", t.title)
                    .put("done", t.done)
                    .put("important", t.important)
            )
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    /** Emite a lista sempre que ela muda (usado pelo app e pelo widget). */
    fun changes(): Flow<List<Task>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "items") trySend(load())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(load())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun activity(): Map<LocalDate, DayActivity> {
        val raw = prefs.getString("activity", null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            obj.keys().asSequence().associate { key ->
                val arr = obj.getJSONArray(key)
                LocalDate.parse(key) to DayActivity(created = arr.optInt(0), done = arr.optInt(1))
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /** Registra uma tarefa criada hoje. */
    fun logCreated() = bump(created = 1)

    /** Registra uma tarefa concluída (+1) ou reaberta (-1) hoje. */
    fun logDone(delta: Int) = bump(done = delta)

    private fun bump(created: Int = 0, done: Int = 0) {
        val today = LocalDate.now()
        val days = activity().toMutableMap()
        val d = days[today] ?: DayActivity()
        days[today] = DayActivity(
            created = (d.created + created).coerceAtLeast(0),
            done = (d.done + done).coerceAtLeast(0),
        )
        saveActivity(days)
    }

    private fun saveActivity(days: Map<LocalDate, DayActivity>) {
        val obj = JSONObject()
        days.forEach { (day, a) -> obj.put(day.toString(), JSONArray().put(a.created).put(a.done)) }
        prefs.edit().putString("activity", obj.toString()).apply()
    }

    private fun dayOf(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
}
