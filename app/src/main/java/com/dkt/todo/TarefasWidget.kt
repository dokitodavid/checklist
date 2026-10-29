package com.dkt.todo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dkt.todo.data.DayActivity
import com.dkt.todo.data.Task
import com.dkt.todo.data.TaskStore
import com.dkt.todo.ui.heatColors
import com.dkt.todo.ui.Metric
import com.dkt.todo.ui.heatLevel
import com.dkt.todo.ui.streak
import com.dkt.todo.ui.theme.paletteFor
import com.dkt.todo.data.AppAppearance
import com.dkt.todo.data.AppearanceStore
import java.time.LocalDate

/** Parâmetro do botão "+": abre o app já com o teclado no campo de nova tarefa. */
val FocusInputKey = ActionParameters.Key<Boolean>(EXTRA_FOCUS_INPUT)

/** Quantas semanas o quadro do widget mostra. */
private const val WIDGET_WEEKS = 16

class TarefasWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = TaskStore(context)
        val appearances = AppearanceStore(context)
        provideContent {
            // Recompõe sozinho sempre que as tarefas mudam no app
            val flow = remember { store.changes() }
            val tasks by flow.collectAsState(initial = store.load())
            val appearanceFlow = remember { appearances.changes() }
            val appearance by appearanceFlow.collectAsState(initial = appearances.load())
            val activity = remember(tasks) { store.activity() }
            GlanceTheme {
                WidgetContent(tasks, activity, appearance)
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

/** Quadro de atividade com o cenário e as cores da aparência salva, sem personagem. */
@Composable
private fun WidgetContent(tasks: List<Task>, activity: Map<LocalDate, DayActivity>, appearance: AppAppearance) {
    val palette = paletteFor(appearance)
    val today = LocalDate.now()
    val pending = tasks.count { !it.done }
    val doneToday = activity[today]?.done ?: 0
    val streak = streak(activity, today)
    val heatmap = remember(activity, today, appearance) { heatmapBitmap(activity, today, WIDGET_WEEKS, appearance) }
    val openApp = actionStartActivity<MainActivity>()

    Box(GlanceModifier.fillMaxSize().appWidgetBackground().background(ColorProvider(palette.Night)).cornerRadius(20.dp)) {
        // O cenário já é esmaecido; textos e botões mantêm opacidade total.
        Image(
            provider = ImageProvider(if (appearance == AppAppearance.KITTEN) R.drawable.kitten_widget_bg else R.drawable.ruins_widget_bg),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(start = 4.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = GlanceModifier.defaultWeight().clickable(openApp)) {
                    Text(
                        text = if (appearance == AppAppearance.KITTEN) "Checklist Gatinho" else "Checklist",
                        style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 17.sp, color = ColorProvider(palette.Parchment)),
                    )
                    Text(
                        text = when (pending) {
                            0 -> "Tudo em dia"
                            1 -> "1 pendente"
                            else -> "$pending pendentes"
                        },
                        style = TextStyle(fontSize = 12.sp, color = ColorProvider(palette.Muted)),
                    )
                }
                Spacer(GlanceModifier.width(8.dp))
                Box(
                    modifier = GlanceModifier
                        .size(36.dp)
                        .cornerRadius(18.dp)
                        .background(ColorProvider(palette.Raised))
                        .clickable(actionStartActivity<MainActivity>(actionParametersOf(FocusInputKey to true))),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_add),
                        contentDescription = "Adicionar tarefa",
                        colorFilter = ColorFilter.tint(ColorProvider(palette.Muted)),
                        modifier = GlanceModifier.size(20.dp),
                    )
                }
            }
            Image(
                provider = ImageProvider(heatmap),
                contentDescription = "Atividade das últimas $WIDGET_WEEKS semanas",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight().clickable(openApp),
                contentScale = ContentScale.Fit,
            )
            Spacer(GlanceModifier.height(6.dp))
            Text(
                text = "$doneToday feita(s) hoje · sequência de $streak dia(s)",
                modifier = GlanceModifier.padding(start = 4.dp),
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(palette.Muted)),
            )
        }
    }
}

/**
 * Desenha o quadro como imagem: widgets não aceitam muitos elementos, e um bitmap mostra
 * quantas semanas quiser. Colunas de domingo a sábado; a última é a semana de hoje.
 */
private fun heatmapBitmap(activity: Map<LocalDate, DayActivity>, today: LocalDate, weeks: Int, appearance: AppAppearance): Bitmap {
    val palette = paletteFor(appearance)
    val colors = heatColors(palette)
    val cell = 30f
    val gap = 8f
    val step = cell + gap
    val bitmap = Bitmap.createBitmap((weeks * step - gap).toInt(), (7 * step - gap).toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = palette.Parchment.copy(alpha = 0.7f).toArgb()
    }
    val lastSunday = today.minusDays((today.dayOfWeek.value % 7).toLong())
    val firstDay = lastSunday.minusWeeks((weeks - 1).toLong())
    val max = (0 until weeks * 7).maxOf { Metric.USE.of(activity[firstDay.plusDays(it.toLong())]) }
    for (w in 0 until weeks) {
        for (d in 0 until 7) {
            val day = firstDay.plusDays(w * 7L + d)
            if (day.isAfter(today)) continue
            val x = w * step
            val y = d * step
            fill.color = colors[heatLevel(Metric.USE.of(activity[day]), max)].toArgb()
            canvas.drawRoundRect(x, y, x + cell, y + cell, 7f, 7f, fill)
            if (day == today) canvas.drawRoundRect(x + 1.5f, y + 1.5f, x + cell - 1.5f, y + cell - 1.5f, 7f, 7f, outline)
        }
    }
    return bitmap
}
