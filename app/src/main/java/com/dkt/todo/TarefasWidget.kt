package com.dkt.todo

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.dkt.todo.data.Task
import com.dkt.todo.data.TaskStore

/** Chaves dos parâmetros passados pelos botões do widget. */
val TaskIdKey = ActionParameters.Key<Long>("taskId")
val FocusInputKey = ActionParameters.Key<Boolean>(EXTRA_FOCUS_INPUT)

class TarefasWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = TaskStore(context)
        provideContent {
            // Recompõe sozinho sempre que as tarefas mudam (no app ou no próprio widget)
            val flow = remember { store.changes() }
            val tasks by flow.collectAsState(initial = store.load())
            GlanceTheme {
                WidgetContent(tasks)
            }
        }
    }

    companion object {
        suspend fun refresh(context: Context) {
            TarefasWidget().updateAll(context)
        }
    }
}

class TarefasWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TarefasWidget()
}

/** Marca a tarefa como feita quando o checkbox do widget é tocado. */
class CompleteTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[TaskIdKey] ?: return
        val store = TaskStore(context)
        store.save(store.load().map { if (it.id == id) it.copy(done = true) else it })
        TarefasWidget().update(context, glanceId)
    }
}

@Composable
private fun WidgetContent(tasks: List<Task>) {
    val pending = tasks
        .filter { !it.done }
        .sortedWith(compareByDescending<Task> { it.important }.thenByDescending { it.id })

    val openApp = actionStartActivity<MainActivity>()

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(20.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(start = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight().clickable(openApp)) {
                Text(
                    text = "Tarefas",
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = GlanceTheme.colors.onSurface,
                    ),
                )
                Text(
                    text = when (pending.size) {
                        0 -> "Tudo em dia"
                        1 -> "1 pendente"
                        else -> "${pending.size} pendentes"
                    },
                    style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
            Spacer(GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier
                    .size(36.dp)
                    .cornerRadius(18.dp)
                    .background(GlanceTheme.colors.surfaceVariant)
                    .clickable(
                        actionStartActivity<MainActivity>(actionParametersOf(FocusInputKey to true))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_add),
                    contentDescription = "Adicionar tarefa",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier.size(20.dp),
                )
            }
        }

        if (pending.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize().clickable(openApp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nada pendente",
                    style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(pending, itemId = { it.id }) { task ->
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CheckBox(
                            checked = false,
                            onCheckedChange = actionRunCallback<CompleteTaskAction>(
                                actionParametersOf(TaskIdKey to task.id)
                            ),
                        )
                        Text(
                            text = task.title,
                            maxLines = 2,
                            style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurface),
                            modifier = GlanceModifier.defaultWeight().clickable(openApp),
                        )
                        if (task.important) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_star),
                                contentDescription = "Importante",
                                modifier = GlanceModifier.size(16.dp).padding(start = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
