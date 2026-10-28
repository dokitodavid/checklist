package com.dkt.todo.ui

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Checklist")
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
            Surface(tonalElevation = 3.dp) {
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
                        placeholder = { Text("Nova tarefa…") },
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
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Adicionar")
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
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = when (filter) {
                            Filter.ALL -> "Nenhuma tarefa ainda.\nDigite lá embaixo pra adicionar."
                            Filter.PENDING -> "Nada pendente 🎉"
                            Filter.DONE -> "Nenhuma tarefa concluída."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp),
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
