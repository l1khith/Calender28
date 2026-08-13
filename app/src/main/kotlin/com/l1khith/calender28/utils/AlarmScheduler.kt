package com.l1khith.calender28.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.receiver.AlarmReceiver
import com.l1khith.calender28.receiver.BootReceiver
import java.util.Calendar

private const val TAG = "AlarmScheduler"

class AlarmScheduler(private val context: Context) {

    fun scheduleTaskAlarm(task: AppTask) {
        if (task.isReminder != 1 || task.utcTimestamp == null || task.isCompleted == 1) {
            Log.d(TAG, "scheduleTaskAlarm: Skipping task ${task.id} (isReminder=${task.isReminder}, utcTimestamp=${task.utcTimestamp}, isCompleted=${task.isCompleted})")
            return
        }

        var triggerMs = task.utcTimestamp
        val now = System.currentTimeMillis()

        if (triggerMs < now) {
            if (task.recurringParentId != null) {
                while (triggerMs < now) {
                    triggerMs += 86400000L
                }
                Log.d(TAG, "scheduleTaskAlarm: Recurring task ${task.id} time passed today, rescheduled to future triggerMs=$triggerMs")
            } else {
                Log.d(TAG, "scheduleTaskAlarm: Task ${task.id} timestamp $triggerMs is in the past compared to now=$now")
                return
            }
        }

        Log.d(TAG, "scheduleTaskAlarm: Scheduling alarm for task ${task.id} ('${task.title}') at triggerMs=$triggerMs")

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("task_title", task.title)
                putExtra("task_desc", task.description ?: "")
            }

            val notificationId = task.id.hashCode() and 0x7FFFFFFF
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    alarmManager.canScheduleExactAlarms()
                } catch (_: Exception) {
                    false
                }
            } else {
                true
            }

            if (canScheduleExact) {
                try {
                    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMs, pendingIntent)
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } catch (_: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMs,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMs,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for task ${task.id}", e)
        }
    }

    fun cancelTaskAlarm(task: AppTask) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val notificationId = task.id.hashCode() and 0x7FFFFFFF
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm for task ${task.id}", e)
        }
    }

    fun scheduleMidnightRollover() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, BootReceiver::class.java).apply {
                action = "com.l1khith.calender28.ACTION_MIDNIGHT_ROLLOVER"
                setPackage(context.packageName)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val triggerTime = calendar.timeInMillis
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    alarmManager.canScheduleExactAlarms()
                } catch (_: Exception) {
                    false
                }
            } else {
                true
            }

            if (canScheduleExact) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } catch (_: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule midnight rollover", e)
        }
    }
}

// Global functions for direct access if needed
fun scheduleTaskAlarm(context: Context, task: AppTask) {
    AlarmScheduler(context).scheduleTaskAlarm(task)
}

fun cancelTaskAlarm(context: Context, task: AppTask) {
    AlarmScheduler(context).cancelTaskAlarm(task)
}

fun scheduleMidnightRollover(context: Context) {
    AlarmScheduler(context).scheduleMidnightRollover()
}

fun scheduleTaskAlarm(task: AppTask) {}
fun cancelTaskAlarm(task: AppTask) {}
fun scheduleMidnightRollover() {}

