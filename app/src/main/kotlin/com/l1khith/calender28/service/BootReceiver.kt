package com.l1khith.calender28.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "BootReceiver"

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive: Triggered with action=$action")

        if (action !in listOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
                "com.l1khith.calender28.ACTION_MIDNIGHT_ROLLOVER"
            )
        ) {
            return
        }

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val db = TaskDatabase(context)
                val scheduler = AlarmScheduler(context)

                // Reschedule all active task reminders
                val allTasks = db.getAllTasks()
                val activeTasksWithReminders = allTasks.filter {
                    it.reminder && !it.completed && it.utcTimestamp != null && it.utcTimestamp > System.currentTimeMillis()
                }
                activeTasksWithReminders.forEach { task ->
                    scheduler.scheduleTaskReminder(task)
                }

                // Reschedule all active recurring tasks
                val activeRecurring = db.getAllRecurringTasks().filter { it.isActive }
                activeRecurring.forEach { recurring ->
                    scheduler.scheduleRecurringTask(recurring)
                }

                // Reschedule all habit reminders
                val activeHabits = db.getAllHabits().filter { !it.isPaused && !it.reminderTime.isNullOrEmpty() }
                activeHabits.forEach { habit ->
                    scheduler.scheduleHabitReminder(habit)
                }

                Log.d(
                    TAG,
                    "Rescheduled ${activeTasksWithReminders.size} task alarms, ${activeRecurring.size} recurring alarms, ${activeHabits.size} habit alarms"
                )

                WidgetUpdater.updateWidget(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule alarms on boot/time change", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
