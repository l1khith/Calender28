# 8. Notification & Alarm System

## Notification Channels
The application creates two distinct system notification channels on Android 8.0+ (API 26+):

1. **`task_reminders`**: High importance channel with sound, vibration, and heads-up display for time-critical task deadlines.
2. **`habit_reminders`**: Default importance channel for daily habit reminders and streak maintenance notifications.

---

## NotificationHelper Implementation

```kotlin
package com.l1khith.calender28.presentation.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.l1khith.calender28.MainActivity
import com.l1khith.calender28.R
import com.l1khith.calender28.domain.model.Task

class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_TASK_REMINDERS = "task_reminders"
        const val CHANNEL_HABIT_REMINDERS = "habit_reminders"
    }
    
    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val taskChannel = NotificationChannel(
                CHANNEL_TASK_REMINDERS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Time-critical task reminders and reminders"
                enableVibration(true)
                enableLights(true)
            }
            
            val habitChannel = NotificationChannel(
                CHANNEL_HABIT_REMINDERS,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily habit check-in reminders"
            }
            
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannels(listOf(taskChannel, habitChannel))
        }
    }
    
    fun showTaskReminder(task: Task) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("task_id", task.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.title)
            .setContentText(task.description ?: "Task reminder deadline")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        NotificationManagerCompat.from(context)
            .notify(task.id.hashCode(), notification)
    }
}
```

---

## Alarm Management Architecture

- **`AlarmScheduler`**: Schedules exact alarms via `AlarmManager.setExactAndAllowWhileIdle()`.
- **`AlarmReceiver`**: `BroadcastReceiver` invoked when an alarm triggers. Passes task payload to `NotificationHelper`.
- **`BootReceiver`**: `BroadcastReceiver` listening for `ACTION_BOOT_COMPLETED` to reschedule active database reminders after device reboots.
