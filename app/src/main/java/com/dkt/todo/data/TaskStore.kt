package com.dkt.todo.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONArray
import org.json.JSONObject

/** Guarda as tarefas em JSON no SharedPreferences ("tarefas" → "items"). */
class TaskStore(context: Context) {
    private val prefs = context.getSharedPreferences("tarefas", Context.MODE_PRIVATE)

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
}
