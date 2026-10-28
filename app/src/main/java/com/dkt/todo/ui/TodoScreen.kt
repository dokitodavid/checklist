package com.dkt.todo.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import com.dkt.todo.R
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.dkt.todo.Reminders
import com.dkt.todo.TarefasWidget
import com.dkt.todo.data.Task
import com.dkt.todo.data.TaskStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

enum class Filter(val label: String) {
    ALL("Todas"),
    PENDING("Pendentes"),
    DONE("Feitas"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    store: TaskStore,
    filter: Filter,
    onFilterChange: (Filter) -> Unit,
    focusTick: Int = 0,
    onBack: () -> Unit = {},
    onTaskCompleted: (Offset) -> Unit = {},
) {
    var tasks by remember { mutableStateOf(store.load()) }
    var input by rememberSaveable { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Task?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    // Ler a posição só no toque: LayoutCoordinates inclui rolagem e translação do gesto.
    // Não é estado de UI: registrar o layout não deve recompor a lista.
    val checkCoordinates = remember { mutableMapOf<Long, LayoutCoordinates>() }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    // Mantém a tela em dia quando algo é marcado pelo widget
    LaunchedEffect(Unit) {
        store.changes().collect { tasks = it }
    }

    // Aberto pelo "+" do widget: foca o campo e abre o teclado (uma vez por toque no "+",
    // mesmo voltando da tela de atividade)
    var handledTick by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(focusTick) {
        if (focusTick > handledTick) {
            handledTick = focusTick
            delay(250)
            try {
                focusRequester.requestFocus()
                keyboard?.show()
            } catch (e: Exception) {
            }
        }
    }

    fun update(newList: List<Task>) {
        tasks = newList
        store.save(newList)
        scope.launch { TarefasWidget.refresh(context.applicationContext) }
    }

    fun add() {
        val text = input.trim()
        if (text.isEmpty()) return
        val id = maxOf(System.currentTimeMillis(), (tasks.maxOfOrNull { it.id } ?: 0L) + 1)
        // Tarefa nova entra no topo da lista
        val position = (tasks.minOfOrNull { it.position } ?: 0L) - 1
        store.logCreated()
        update(tasks + Task(id = id, title = text, position = position))
        input = ""
    }

    fun undoable(message: String, removed: List<Task>) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = message,
                actionLabel = "Desfazer",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                update(tasks + removed)
            }
        }
    }

    fun delete(task: Task) {
        update(tasks.filterNot { it.id == task.id })
        undoable("Tarefa excluída", listOf(task))
    }

    fun toggle(task: Task, gestureOrigin: Offset? = null) {
        // Capturar antes de salvar/reordenar/remover a linha concluída do filtro.
        val checkOrigin = gestureOrigin ?: checkCoordinates[task.id]?.takeIf { it.isAttached }?.let { coordinates ->
            coordinates.localToWindow(Offset(coordinates.size.width / 2f, coordinates.size.height / 2f))
        }
        if (!task.done && checkOrigin != null) onTaskCompleted(checkOrigin)
        store.logDone(if (task.done) -1 else 1)
        update(tasks.map { if (it.id == task.id) it.copy(done = !it.done) else it })
    }

    /** Reordena as pendentes pela data (e horário); as sem data ficam depois, na ordem atual. */
    fun organizeByDate() {
        val before = tasks
        val ordered = tasks.filter { !it.done }.sortedWith(
            compareBy<Task> { it.dueDate == null }
                .thenBy { it.dueDate }
                .thenBy { it.remindAt ?: Int.MAX_VALUE }
                .thenBy { it.position }
        )
        val newPos = ordered.withIndex().associate { (i, t) -> t.id to i.toLong() }
        update(tasks.map { t -> newPos[t.id]?.let { t.copy(position = it) } ?: t })
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar("Organizado por data", "Desfazer", duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) update(before)
        }
    }

    fun clearDone() {
        val done = tasks.filter { it.done }
        if (done.isEmpty()) return
        update(tasks.filterNot { it.done })
        undoable("${done.size} concluída(s) removida(s)", done)
    }

    // Pendentes antes das feitas; dentro de cada grupo, a ordem manual (arrastar para reordenar)
    val visible = tasks
        .filter {
            when (filter) {
                Filter.ALL -> true
                Filter.PENDING -> !it.done
                Filter.DONE -> it.done
            }
        }
        .sortedWith(compareBy<Task> { it.done }.thenBy { it.position })

    // Arrastar para reordenar: troca a posição na memória enquanto arrasta e grava ao soltar
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val order = tasks.filter { !it.done }.sortedBy { it.position }.map { it.id }.toMutableList()
        val fromIndex = order.indexOf(from.key as Long)
        val toIndex = order.indexOf(to.key as Long)
        if (fromIndex < 0 || toIndex < 0) return@rememberReorderableLazyListState
        order.add(toIndex, order.removeAt(fromIndex))
        val newPos = order.withIndex().associate { (i, id) -> id to i.toLong() }
        tasks = tasks.map { t -> newPos[t.id]?.let { t.copy(position = it) } ?: t }
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val pending = tasks.count { !it.done }
    val subtitle = when (pending) {
        0 -> if (tasks.isEmpty()) "Nenhuma tarefa" else "Tudo em dia"
        1 -> "1 pendente"
        else -> "$pending pendentes"
    }

    // Papel de parede preso à tela inteira, atrás de tudo: não se move nem muda de tamanho
    // quando a tela abre ou o teclado sobe (o conteúdo é que se ajusta por cima dele)
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    RuinsBackground()
    Scaffold(
        containerColor = Color.Transparent,
        // Com o fundo transparente, a cor do conteúdo precisa ser dada (senão título e ícones ficam pretos)
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            ScreenHeader(
                title = "Checklist",
                subtitle = subtitle,
                showCompanion = false,
                navigation = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar para a atividade")
                    }
                },
                actions = {
                    if (tasks.any { !it.done && it.dueDate != null }) {
                        IconButton(onClick = { organizeByDate() }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Organizar por data")
                        }
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Mais opções")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Limpar concluídas") },
                                enabled = tasks.any { it.done },
                                onClick = {
                                    menuOpen = false
                                    clearDone()
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            // Sem faixa de fundo: o papel de parede aparece atrás; só a cápsula do campo é sólida
            Surface(color = Color.Transparent) {
                Surface(
                    modifier = Modifier.navigationBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = input, onValueChange = { input = it },
                            modifier = Modifier.weight(1f).padding(vertical = 10.dp).focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface), singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { add() }),
                            decorationBox = { field ->
                                Box {
                                    if (input.isEmpty()) Text("Adicionar uma tarefa", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    field()
                                }
                            },
                        )
                        FilledIconButton(onClick = { add() }, enabled = input.isNotBlank(), modifier = Modifier.size(48.dp), shape = CircleShape) {
                            Icon(painterResource(R.drawable.ic_send_up), "Adicionar tarefa")
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Filter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        shape = CircleShape,
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        onClick = { onFilterChange(f) },
                        label = { Text(f.label) },
                    )
                }
            }

            if (visible.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        when (filter) {
                            Filter.ALL -> "Sua lista começa aqui"
                            Filter.PENDING -> "Tudo em dia"
                            Filter.DONE -> "Nenhuma tarefa concluída"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when (filter) {
                            Filter.ALL -> "Adicione uma tarefa para começar."
                            Filter.PENDING -> "Você concluiu todas as tarefas."
                            Filter.DONE -> "Suas tarefas concluídas aparecerão aqui."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(visible, key = { it.id }) { task ->
                        // Só as pendentes se reordenam (e não no filtro Feitas)
                        ReorderableItem(reorderState, key = task.id, enabled = !task.done && filter != Filter.DONE) { dragging ->
                            val dragHandle = Modifier.longPressDraggableHandle(
                                onDragStarted = { haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                                onDragStopped = { update(tasks) },
                            )
                            Column {
                                SwipeableTask(
                                    task = task,
                                    onToggle = { origin -> toggle(task, gestureOrigin = origin) },
                                    onDelete = { delete(task) },
                                ) {
                                    TaskRow(
                                        task = task,
                                        onToggle = { toggle(task) },
                                        onStar = {
                                            update(tasks.map { if (it.id == task.id) it.copy(important = !it.important) else it })
                                        },
                                        onEdit = { editing = task },
                                        onDelete = { delete(task) },
                                        dragging = dragging,
                                        onCheckPositioned = { coordinates ->
                                            if (coordinates != null) checkCoordinates[task.id] = coordinates
                                            else checkCoordinates.remove(task.id)
                                        },
                                        modifier = dragHandle,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
    }

    editing?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { editing = null },
            onSave = { title, dueDate, remindAt ->
                update(tasks.map {
                    if (it.id == task.id) it.copy(title = title, dueDate = dueDate, remindAt = remindAt) else it
                })
                editing = null
                // Lembrete precisa da permissão de notificação (Android 13+)
                if (remindAt != null && !Reminders.canNotify(context) && Build.VERSION.SDK_INT >= 33) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        )
    }
}
