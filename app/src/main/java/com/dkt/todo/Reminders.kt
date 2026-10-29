package com.dkt.todo

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.dkt.todo.data.Task
import com.dkt.todo.data.TaskStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Agenda um alarme para cada tarefa pendente com lembrete no futuro. Chamado a cada gravação
 * das tarefas ([TaskStore.save]) e depois de reiniciar o celular ou atualizar o app.
 */
object Reminders {
    const val CHANNEL = "lembretes"
    const val EXTRA_TASK_ID = "taskId"
    const val ACTION_REMIND = "com.dkt.todo.LEMBRETE"
    const val ACTION_COMPLETE = "com.dkt.todo.CONCLUIR"
    private const val SCHEDULED_KEY = "scheduledReminders"

    fun sync(context: Context, tasks: List<Task>) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val prefs = context.getSharedPreferences("tarefas", Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val wanted = tasks
            .filter { !it.done }
            .mapNotNull { t -> t.reminderMillis()?.takeIf { it > now }?.let { t.id to it } }
            .toMap()
        val before = prefs.getStringSet(SCHEDULED_KEY, emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }
        (before - wanted.keys).forEach { id -> alarms.cancel(alarmIntent(context, id)) }
        wanted.forEach { (id, at) ->
            val intent = alarmIntent(context, id)
            // Exato quando o sistema permite; senão, o Android pode atrasar alguns minutos
            if (Build.VERSION.SDK_INT < 31 || alarms.canScheduleExactAlarms()) {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
            } else {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
            }
        }
        prefs.edit().putStringSet(SCHEDULED_KEY, wanted.keys.map { it.toString() }.toSet()).apply()
    }

    private fun requestCode(id: Long) = (id xor (id ushr 32)).toInt()

    private fun alarmIntent(context: Context, id: Long): PendingIntent = PendingIntent.getBroadcast(
        context, requestCode(id),
        Intent(context, ReminderReceiver::class.java).setAction(ACTION_REMIND).putExtra(EXTRA_TASK_ID, id),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun canNotify(context: Context) = Build.VERSION.SDK_INT < 33 ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun notify(context: Context, task: Task) {
        if (!canNotify(context)) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Lembretes", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Lembretes das tarefas com horário" }
        )
        val open = PendingIntent.getActivity(
            context, requestCode(task.id),
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val complete = PendingIntent.getBroadcast(
            context, requestCode(task.id),
            Intent(context, ReminderReceiver::class.java).setAction(ACTION_COMPLETE).putExtra(EXTRA_TASK_ID, task.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(task.title)
            .setContentText("Lembrete do Checklist")
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            // Mostra o título na tela de bloqueio; o botão Concluir funciona sem desbloquear
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(Notification.Action.Builder(null, "Concluir", complete).build())
            .build()
        manager.notify(requestCode(task.id), notification)
    }
}

/** Mostra a notificação do lembrete, ou conclui a tarefa pelo botão "Concluir". */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(Reminders.EXTRA_TASK_ID, 0L)
        val store = TaskStore(context)
        val tasks = store.load()
        val task = tasks.find { it.id == id && !it.done } ?: return
        when (intent.action) {
            Reminders.ACTION_REMIND -> Reminders.notify(context, task)
            Reminders.ACTION_COMPLETE -> {
                store.logDone(1)
                store.save(tasks.map { if (it.id == id) it.copy(done = true) else it })
                context.getSystemService(NotificationManager::class.java)
                    ?.cancel((id xor (id ushr 32)).toInt())
                val pending = goAsync()
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        TarefasWidget.refresh(context.applicationContext)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}

/** Alarmes somem ao reiniciar o celular (e podem sumir ao atualizar o app): reagenda tudo. */
class RemindersBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val store = TaskStore(context)
        Reminders.sync(context, store.load())
    }
}
