package com.dkt.todo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.dkt.todo.data.TaskStore
import com.dkt.todo.ui.ChecklistApp
import com.dkt.todo.ui.theme.AppTheme

const val EXTRA_FOCUS_INPUT = "focusInput"

class MainActivity : ComponentActivity() {
    // Incrementa quando o app é aberto pelo "+" do widget, pra focar o campo de texto
    private var focusTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && intent?.getBooleanExtra(EXTRA_FOCUS_INPUT, false) == true) {
            focusTick++
        }
        val store = TaskStore(applicationContext)
        setContent {
            AppTheme {
                ChecklistApp(store, focusTick)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_FOCUS_INPUT, false)) focusTick++
    }
}
