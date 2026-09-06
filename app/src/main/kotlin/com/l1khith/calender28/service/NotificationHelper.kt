package com.l1khith.calender28.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.l1khith.calender28.MainActivity
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.TaskDatabase

private const val TAG = "NotificationHelper"

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_TASK_REMINDERS = "task_reminders"
        const val CHANNEL_RECURRING_REMINDERS = "recurring_reminders"
        const val CHANNEL_HABIT_REMINDERS = "habit_reminders"
        const val CHANNEL_SYSTEM = "system"

        const val ACTION_MARK_COMPLETE = "com.l1khith.calender28.ACTION_MARK_COMPLETE"
        const val ACTION_SNOOZE = "com.l1khith.calender28.ACTION_SNOOZE"
        const val ACTION_LOG_HABIT = "com.l1khith.calender28.ACTION_LOG_HABIT"
        const val ACTION_DISMISS = "com.l1khith.calender28.ACTION_DISMISS"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TASK_REMINDERS,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Time-critical reminders for your tasks"
                    enableVibration(true)
                    enableLights(true)
                    lightColor = Color.BLUE
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_RECURRING_REMINDERS,
                    "Recurring Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for recurring tasks and routines"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_HABIT_REMINDERS,
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily reminders to build your habits"
                    enableVibration(true)
                    setShowBadge(true)
                },
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "System",
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = "Background system notifications"
                    setShowBadge(false)
                }
            )

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(channels)
            Log.d(TAG, "Notification channels created successfully")
        }
    }

    fun showTaskReminder(task: AppTask) {
        val notificationId = task.id.hashCode() and 0x7FFFFFFF
        Log.d(TAG, "showTaskReminder: Displaying notification for task id=${task.id}, title=${task.title}")

        val markCompleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_COMPLETE
            putExtra("task_id", task.id)
        }
        val markCompletePending = PendingIntent.getBroadcast(
            context, notificationId, markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("task_id", task.id)
            putExtra("snooze_minutes", 15)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context, notificationId + 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "task_detail")
            putExtra("task_id", task.id)
        } ?: Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "task_detail")
            putExtra("task_id", task.id)
        }

        val contentPending = PendingIntent.getActivity(
            context, notificationId, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(task.title)
            .setContentText(task.description ?: "Task reminder")
            .setSubText(task.reminderTime ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentPending)
            .addAction(android.R.drawable.checkbox_on_background, "Done", markCompletePending)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze 15m", snoozePending)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setLights(Color.BLUE, 1000, 1000)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing", e)
        }
    }


    fun showHabitReminder(habit: Habit) {
        val notificationId = "${habit.id}_habit".hashCode() and 0x7FFFFFFF
        Log.d(TAG, "showHabitReminder: Displaying notification for habit id=${habit.id}, name=${habit.name}")

        val logIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_LOG_HABIT
            putExtra("habit_id", habit.id)
        }
        val logPending = PendingIntent.getBroadcast(
            context, notificationId, logIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra("notification_id", notificationId)
        }
        val dismissPending = PendingIntent.getBroadcast(
            context, notificationId + 1, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "habit_detail")
            putExtra("habit_id", habit.id)
        } ?: Intent(context, MainActivity::class.java)

        val builder = NotificationCompat.Builder(context, CHANNEL_HABIT_REMINDERS)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setContentTitle(habit.name)
            .setContentText("Time to build your streak! (${habit.completedCount}/28 Days)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context, notificationId, contentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(android.R.drawable.checkbox_on_background, "Log it", logPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPending)
            .setProgress(28, habit.completedCount, false)

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing", e)
        }
    }

    fun snoozeNotification(taskId: String, minutes: Int) {
        val notificationId = taskId.hashCode() and 0x7FFFFFFF
        cancelNotification(notificationId)

        Log.d(TAG, "snoozeNotification: Snoozing task id=$taskId for $minutes minutes")
        val db = TaskDatabase(context)
        val task = db.getAllTasks().find { it.id == taskId } ?: return

        val snoozeTime = System.currentTimeMillis() + (minutes * 60_000L)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ITEM_ID, taskId)
            putExtra(AlarmScheduler.EXTRA_ITEM_TYPE, AlarmScheduler.TYPE_TASK)
            putExtra(AlarmScheduler.EXTRA_TITLE, task.title)
            putExtra(AlarmScheduler.EXTRA_DESCRIPTION, "Snoozed: ${task.description ?: ""}")
        }

        val alarmId = "${taskId}_snooze".hashCode() and 0x7FFFFFFF
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
        }
    }

    fun cancelNotification(notificationId: Int) {
        Log.d(TAG, "cancelNotification: Cancelling notification id=$notificationId")
        notificationManager.cancel(notificationId)
    }
}
