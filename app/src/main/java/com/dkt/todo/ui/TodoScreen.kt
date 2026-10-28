package com.dkt.todo.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.dkt.todo.TarefasWidget
import com.dkt.todo.data.Task
import com.dkt.todo.data.TaskStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Filter(val label: String) {
    ALL("Todas"),
    PENDING("Pendentes"),
    DONE("Feitas"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(store: TaskStore, focusTick: Int = 0) {
    var tasks by remember { mutableStateOf(store.load()) }
    var filter by rememberSaveable { mutableStateOf(Filter.ALL) }
    var input by rememberSaveable { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Task?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // Mantém a tela em dia quando algo é marcado pelo widget
    LaunchedEffect(Unit) {
        store.changes().collect { tasks = it }
    }

    // Aberto pelo "+" do widget: foca o campo e abre o teclado
    LaunchedEffect(focusTick) {
        if (focusTick > 0) {
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
        update(tasks + Task(id = id, title = text))
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

    fun clearDone() {
        val done = tasks.filter { it.done }
        if (done.isEmpty()) return
        update(tasks.filterNot { it.done })
        undoable("${done.size} concluída(s) removida(s)", done)
    }

    // Pendentes antes das feitas; dentro de cada grupo, importantes no topo e as mais novas primeiro
    val visible = tasks
        .filter {
            when (filter) {
                Filter.ALL -> true
                Filter.PENDING -> !it.done
                Filter.DONE -> it.done
            }
        }
        .sortedWith(
            compareBy<Task> { it.done }
                .thenByDescending { it.important }
                .thenByDescending { it.id }
        )

    val pending = tasks.count { !it.done }
    val subtitle = when (pending) {
        0 -> "Tudo em dia"
        1 -> "1 pendente"
        else -> "$pending pendentes"
    }

    val completed = tasks.count { it.done }
    val progress by animateFloatAsState(
        if (tasks.isEmpty()) 0f else completed.toFloat() / tasks.size,
        animationSpec = tween(400), label = "Progresso das tarefas",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PixelCampfire(Modifier.size(48.dp, 60.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Checklist", style = MaterialTheme.typography.titleLarge)
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
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
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Sua próxima missão…") },
                        label = { Text("Nova tarefa") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { add() }),
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { add() },
                        enabled = input.isNotBlank(),
                        modifier = Modifier.size(48.dp),
                        shape = CutCornerShape(4.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        PixelIcon(PixelSymbol.Plus, "Adicionar tarefa", tint = LocalContentColor.current)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CutCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface, CutCornerShape(6.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("UM PASSO A CADA VEZ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (tasks.isEmpty()) "Toda jornada começa com uma tarefa."
                    else if (pending == 0) "A jornada de hoje está completa."
                    else "A chama segue acesa. Continue.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                LinearProgressIndicator(
                    progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    gapSize = 0.dp, drawStopIndicator = {},
                )
                Text("$completed de ${tasks.size} concluídas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Filter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
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
                    PixelCampfire(Modifier.size(96.dp, 120.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        when (filter) {
                            Filter.ALL -> "Prepare sua jornada"
                            Filter.PENDING -> "Descanse junto à fogueira"
                            Filter.DONE -> "Cada conquista conta"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when (filter) {
                            Filter.ALL -> "Adicione sua primeira tarefa abaixo."
                            Filter.PENDING -> "Tudo em dia. Sua próxima missão pode esperar."
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(visible, key = { it.id }) { task ->
                        Column(Modifier.animateItem()) {
                            TaskRow(
                                task = task,
                                onToggle = {
                                    update(tasks.map { if (it.id == task.id) it.copy(done = !it.done) else it })
                                },
                                onStar = {
                                    update(tasks.map { if (it.id == task.id) it.copy(important = !it.important) else it })
                                },
                                onEdit = { editing = task },
                                onDelete = { delete(task) },
                            )
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
            onSave = { title ->
                update(tasks.map { if (it.id == task.id) it.copy(title = title) else it })
                editing = null
            },
        )
    }
}
