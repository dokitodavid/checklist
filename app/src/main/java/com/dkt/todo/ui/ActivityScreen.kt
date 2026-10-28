package com.dkt.todo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dkt.todo.data.DayActivity
import com.dkt.todo.data.TaskStore
import com.dkt.todo.ui.theme.AshenColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

enum class Metric(val label: String) {
    USE("Uso"),
    CREATED("Criadas"),
    DONE("Feitas");

    fun of(a: DayActivity?): Int = when (this) {
        USE -> (a?.created ?: 0) + (a?.done ?: 0)
        CREATED -> a?.created ?: 0
        DONE -> a?.done ?: 0
    }
}

private val ptBR = Locale.forLanguageTag("pt-BR")
private val dayFormat = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ptBR)
private val monthFormat = DateTimeFormatter.ofPattern("MMM", ptBR)

private val CellSize = 14.dp
private val CellGap = 3.dp
private val LabelWidth = 28.dp

/** Medidor no estilo do GitHub: um quadrado por dia, mais forte quanto mais atividade. */
@Composable
fun ActivityScreen(store: TaskStore, onBack: () -> Unit) {
    val changes = remember { store.changes() }
    val tasks by changes.collectAsState(initial = store.load())
    val activity = remember(tasks) { store.activity() }
    var metric by rememberSaveable { mutableStateOf(Metric.USE) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    val today = LocalDate.now()
    val pending = tasks.count { !it.done }

    Scaffold(
        topBar = {
            ScreenHeader(
                title = "Atividade",
                subtitle = "Tarefas criadas e feitas",
                navigation = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Metric.entries.forEach { m ->
                    FilterChip(
                        selected = metric == m,
                        shape = CircleShape,
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        onClick = { metric = m },
                        label = { Text(m.label) },
                    )
                }
            }

            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
            ) {
                // Quantas semanas cabem na largura (no máximo um ano)
                val weeks = ((maxWidth - LabelWidth + CellGap) / (CellSize + CellGap)).toInt().coerceIn(4, 53)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Heatmap(activity, metric, today, weeks, selected) { selected = if (selected == it) null else it }
                    val detail = selected?.let { day ->
                        val a = activity[day]
                        val label = day.format(dayFormat).replaceFirstChar { it.titlecase(ptBR) }
                        "$label: ${plural(a?.created ?: 0, "criada", "criadas")} · ${plural(a?.done ?: 0, "feita", "feitas")}"
                    } ?: "Últimas $weeks semanas. Toque em um dia para ver os detalhes."
                    Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Legend()
                }
            }

            val start = today.minusWeeks(52)
            val inYear = activity.filterKeys { !it.isBefore(start) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat("Criadas", inYear.values.sumOf { it.created }.toString(), "no último ano", Modifier.weight(1f))
                Stat("Feitas", inYear.values.sumOf { it.done }.toString(), "no último ano", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val streak = streak(activity, today)
                Stat("Sequência", streak.toString(), if (streak == 1) "dia concluindo" else "dias concluindo", Modifier.weight(1f))
                Stat("Pendentes", pending.toString(), "agora", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Desenha o quadro: uma coluna por semana, de domingo a sábado; a última é a semana de hoje. */
@Composable
private fun Heatmap(
    activity: Map<LocalDate, DayActivity>,
    metric: Metric,
    today: LocalDate,
    weeks: Int,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
) {
    val step = CellSize + CellGap
    val lastSunday = today.minusDays((today.dayOfWeek.value % 7).toLong())
    val firstDay = lastSunday.minusWeeks((weeks - 1).toLong())
    val max = (0 until weeks * 7).maxOfOrNull { metric.of(activity[firstDay.plusDays(it.toLong())]) } ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.fillMaxWidth().height(16.dp)) {
            for (w in 0 until weeks) {
                val sunday = firstDay.plusWeeks(w.toLong())
                val newMonth = w == 0 || sunday.month != sunday.minusWeeks(1).month
                if (newMonth && w < weeks - 1) {
                    Text(
                        sunday.format(monthFormat).trimEnd('.'),
                        modifier = Modifier.offset(x = LabelWidth + step * w),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Row {
            Column(Modifier.width(LabelWidth), verticalArrangement = Arrangement.spacedBy(CellGap)) {
                listOf("", "seg", "", "qua", "", "sex", "").forEach { label ->
                    Box(Modifier.height(CellSize), contentAlignment = Alignment.CenterStart) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(CellGap)) {
                for (w in 0 until weeks) {
                    Column(verticalArrangement = Arrangement.spacedBy(CellGap)) {
                        for (d in 0 until 7) {
                            val day = firstDay.plusDays(w * 7L + d)
                            if (day.isAfter(today)) {
                                Spacer(Modifier.size(CellSize))
                            } else {
                                val value = metric.of(activity[day])
                                Cell(
                                    color = levelColor(level(value, max)),
                                    selected = day == selected,
                                    modifier = Modifier
                                        .clickable { onSelect(day) }
                                        .semantics { contentDescription = "${day.format(dayFormat)}: $value" },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(color: Color, selected: Boolean = false, modifier: Modifier = Modifier, size: Dp = CellSize) {
    val shape = RoundedCornerShape(3.dp)
    Box(
        modifier
            .size(size)
            .background(color, shape)
            .then(if (selected) Modifier.border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface), shape) else Modifier)
    )
}

@Composable
private fun Legend() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(CellGap)) {
        Text("Menos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        for (l in 0..4) Cell(levelColor(l), size = 12.dp)
        Spacer(Modifier.width(4.dp))
        Text("Mais", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Stat(title: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 0 = sem atividade; 1–4 = quartos do dia mais ativo do período. */
private fun level(value: Int, max: Int): Int =
    if (value <= 0 || max <= 0) 0 else ceil(value * 4.0 / max).toInt().coerceIn(1, 4)

@Composable
private fun levelColor(level: Int): Color = when (level) {
    0 -> MaterialTheme.colorScheme.surfaceContainerHighest
    1 -> AshenColors.Ember.copy(alpha = 0.3f)
    2 -> AshenColors.Ember.copy(alpha = 0.5f)
    3 -> AshenColors.Ember.copy(alpha = 0.75f)
    else -> AshenColors.Ember
}

/** Dias seguidos com pelo menos uma tarefa feita, até hoje (ou até ontem, se hoje ainda não teve). */
private fun streak(activity: Map<LocalDate, DayActivity>, today: LocalDate): Int {
    var day = if ((activity[today]?.done ?: 0) > 0) today else today.minusDays(1)
    var count = 0
    while ((activity[day]?.done ?: 0) > 0) {
        count++
        day = day.minusDays(1)
    }
    return count
}

private fun plural(n: Int, one: String, many: String) = "$n ${if (n == 1) one else many}"
