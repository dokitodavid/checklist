package com.dkt.todo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

// ---------- Modelo e armazenamento ----------

const val EXTRA_FOCUS_INPUT = "focusInput"

data class Task(
    val id: Long,
    val title: String,
    val done: Boolean = false,
    val important: Boolean = false,
)

class TaskStore(context: Context) {
    private val prefs = context.getSharedPreferences("tarefas", Context.MODE_PRIVATE)

    fun load(): List<Task> {
        val raw = prefs.getString("items", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Task(
                    id = o.getLong("id"),
                    title = o.getString("title"),
                    done = o.optBoolean("done", false),
                    important = o.optBoolean("important", false),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(tasks: List<Task>) {
        val arr = JSONArray()
        tasks.forEach { t ->
            arr.put(
                JSONObject()
                    .put("id", t.id)
                    .put("title", t.title)
                    .put("done", t.done)
                    .put("important", t.important)
            )
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    /** Emite a lista sempre que ela muda (usado pelo app e pelo widget). */
    fun changes(): Flow<List<Task>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "items") trySend(load())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(load())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}

// ---------- Activity ----------

class MainActivity : ComponentActivity() {
    // Incrementa quando o app é aberto pelo "+" do widget, pra focar o campo de texto
    private var focusTick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && intent?.getBooleanExtra(EXTRA_FOCUS_INPUT, false) == true) {
            focusTick++
        }
        val store = TaskStore(applicationContext)
        setContent {
            AppTheme {
                TodoScreen(store, focusTick)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_FOCUS_INPUT, false)) focusTick++
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

// ---------- Tela ----------

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
                        Text("Tarefas")
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
        var text by remember(task.id) { mutableStateOf(task.title) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Editar tarefa") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val v = text.trim()
                    if (v.isNotEmpty()) {
                        update(tasks.map { if (it.id == task.id) it.copy(title = v) else it })
                    }
                    editing = null
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onStar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onEdit() },
        leadingContent = {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
        },
        headlineContent = {
            Text(
                text = task.title,
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
                color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onStar) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = if (task.important) "Desmarcar importante" else "Marcar importante",
                        tint = if (task.important) Color(0xFFFFB300)
                        else MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
        },
    )
}
