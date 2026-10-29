package com.dkt.todo.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

enum class AppAppearance(val key: String, val label: String, val description: String) {
    MAGE("mage", "Mago", "Ruínas, tons escuros e um companheiro misterioso."),
    KITTEN("kitten", "Gatinho", "Jardim, cores pastel e uma companhia fofinha."),
}

/** Preferência separada das tarefas: atualizar o app mantém o tema original. */
class AppearanceStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("appearance", Context.MODE_PRIVATE)

    fun load(): AppAppearance = AppAppearance.entries.firstOrNull {
        it.key == prefs.getString("theme", null)
    } ?: AppAppearance.MAGE

    fun save(appearance: AppAppearance) {
        prefs.edit().putString("theme", appearance.key).apply()
    }

    fun changes(): Flow<AppAppearance> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "theme") trySend(load())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(load())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()
}
