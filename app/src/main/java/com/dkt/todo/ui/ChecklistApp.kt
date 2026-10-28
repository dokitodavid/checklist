package com.dkt.todo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dkt.todo.data.TaskStore

enum class Screen { LIST, ACTIVITY }

/** Troca entre a lista e a atividade, mantendo o estado de cada tela (texto digitado, filtro). */
@Composable
fun ChecklistApp(store: TaskStore, focusTick: Int) {
    var screen by rememberSaveable { mutableStateOf(Screen.LIST) }
    val states = rememberSaveableStateHolder()

    // O "+" do widget sempre leva para a lista
    LaunchedEffect(focusTick) {
        if (focusTick > 0) screen = Screen.LIST
    }

    Box(Modifier.fillMaxSize()) {
        states.SaveableStateProvider(screen.name) {
            when (screen) {
                Screen.LIST -> TodoScreen(store, focusTick, onOpenActivity = { screen = Screen.ACTIVITY })
                Screen.ACTIVITY -> {
                    BackHandler { screen = Screen.LIST }
                    ActivityScreen(store, onBack = { screen = Screen.LIST })
                }
            }
        }
        AmbientEmbers(Modifier.matchParentSize())
    }
}
