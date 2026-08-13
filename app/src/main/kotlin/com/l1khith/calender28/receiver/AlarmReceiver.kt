package com.l1khith.calender28.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.utils.AlarmScheduler
import com.l1khith.calender28.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "calender28_task_reminders"
        const val CHANNEL_NAME = "Task Reminders"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for time-critical task reminders"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        val taskId = intent.getStringExtra("task_id") ?: run {
            Log.d("AlarmReceiver", "onReceive: Triggered but task_id is null")
            pendingResult.finish()
            return
        }
        val taskTitle = intent.getStringExtra("task_title") ?: "Reminder"
        val taskDesc = intent.getStringExtra("task_desc") ?: ""

        Log.d("AlarmReceiver", "onReceive: Processing alarm for taskId=$taskId, title=$taskTitle")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val clickIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("selected_task_id", taskId)
        }

        val notificationId = taskId.hashCode() and 0x7FFFFFFF

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(taskTitle)
            .setContentText(taskDesc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)

        if (clickIntent != null) {
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }

        notificationManager.notify(notificationId, builder.build())

        scope.launch {
            try {
                val db = TaskDatabase(context)
                val task = db.getAllTasks().find { it.id == taskId }
                if (task != null && task.recurringParentId != null) {
                    val upcoming = db.getAllTasks().find {
                        it.recurringParentId == task.recurringParentId &&
                        !it.completed &&
                        it.reminder &&
                        it.utcTimestamp != null &&
                        it.utcTimestamp > System.currentTimeMillis()
                    }
                    if (upcoming != null) {
                        AlarmScheduler(context).scheduleTaskAlarm(upcoming)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error rescheduling recurring task", e)
            } finally {
                pendingResult.finish()
            }
        }

        WidgetUpdater.updateWidget(context)
    }
}

