package com.dkt.todo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dkt.todo.data.TaskStore

enum class Screen { ACTIVITY, LIST }

/**
 * O app abre na Atividade; os botões Todas/Pendentes/Feitas de lá abrem a lista
 * já filtrada, e o voltar retorna para a Atividade.
 */
@Composable
fun ChecklistApp(store: TaskStore, focusTick: Int) {
    var screen by rememberSaveable { mutableStateOf(Screen.ACTIVITY) }
    var filter by rememberSaveable { mutableStateOf(Filter.ALL) }
    // Cada pedido de "nova tarefa" (widget ou Atividade) abre a lista com o teclado
    var focusRequests by rememberSaveable { mutableIntStateOf(0) }
    val states = rememberSaveableStateHolder()

    fun openList(f: Filter, focus: Boolean = false) {
        filter = f
        screen = Screen.LIST
        if (focus) focusRequests++
    }

    LaunchedEffect(focusTick) {
        if (focusTick > 0) openList(Filter.ALL, focus = true)
    }

    Box(Modifier.fillMaxSize()) {
        states.SaveableStateProvider(screen.name) {
            when (screen) {
                Screen.ACTIVITY -> ActivityScreen(
                    store,
                    onOpenList = { openList(it) },
                    onAddTask = { openList(Filter.ALL, focus = true) },
                )
                Screen.LIST -> {
                    BackHandler { screen = Screen.ACTIVITY }
                    TodoScreen(
                        store,
                        filter = filter,
                        onFilterChange = { filter = it },
                        focusTick = focusRequests,
                        onBack = { screen = Screen.ACTIVITY },
                    )
                }
            }
        }
        AmbientEmbers(Modifier.matchParentSize())
    }
}
