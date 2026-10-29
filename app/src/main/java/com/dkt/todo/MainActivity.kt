package com.dkt.todo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.dkt.todo.data.TaskStore
import com.dkt.todo.data.AppAppearance
import com.dkt.todo.data.AppearanceStore
import kotlinx.coroutines.launch
import com.dkt.todo.ui.ChecklistApp
import com.dkt.todo.ui.theme.AppTheme

const val EXTRA_FOCUS_INPUT = "focusInput"

class MainActivity : ComponentActivity() {
    // Incrementa quando o app é aberto pelo "+" do widget, pra focar o campo de texto
    private var focusTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && intent?.getBooleanExtra(EXTRA_FOCUS_INPUT, false) == true) {
            focusTick++
        }
        val store = TaskStore(applicationContext)
        val appearances = AppearanceStore(applicationContext)
        setContent {
            val appearanceFlow = remember { appearances.changes() }
            val appearance by appearanceFlow.collectAsState(initial = appearances.load())
            val scope = rememberCoroutineScope()
            SideEffect {
                val bars = if (appearance == AppAppearance.KITTEN) {
                    SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            AppTheme(appearance) {
                ChecklistApp(store, focusTick, onAppearanceChange = {
                    appearances.save(it)
                    scope.launch { TarefasWidget.refresh(applicationContext) }
                })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_FOCUS_INPUT, false)) focusTick++
    }
}
