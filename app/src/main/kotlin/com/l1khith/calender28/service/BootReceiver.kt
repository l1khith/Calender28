package com.l1khith.calender28.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "ServiceBootReceiver"

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
                val today = FixedCalendarHelper.currentFixedDate()
                val todayStr = today.toString()

                // 1. Reschedule all active normal task reminders (including generated recurring instances)
                val allTasks = db.getAllTasks()
                val activeTasksWithReminders = allTasks.filter {
                    it.reminder && !it.completed && it.utcTimestamp != null
                }
                var taskCount = 0
                activeTasksWithReminders.forEach { task ->
                    if (scheduler.scheduleTaskReminder(task)) {
                        taskCount++
                    }
                }

                // 2. For active recurring tasks, generate today's instance if missing
                val activeRecurring = db.getAllRecurringTasks().filter { it.isActive }
                var recurringGenCount = 0
                activeRecurring.forEach { recurring ->
                    if (FixedCalendarHelper.shouldGenerateInstance(recurring, today)) {
                        val hasGen = allTasks.any {
                            it.recurringParentId == recurring.id && it.associatedDate == todayStr
                        }
                        if (!hasGen) {
                            val isRem = if (recurring.reminderTime != null) 1 else 0
                            val utcTs = if (recurring.reminderTime != null) {
                                FixedCalendarHelper.toTimestamp(today, recurring.reminderTime)
                            } else null

                            val genTask = AppTask(
                                id = "gen_${recurring.id}_$todayStr",
                                title = recurring.title,
                                description = recurring.description,
                                associatedDate = todayStr,
                                isReminder = isRem,
                                reminderTime = recurring.reminderTime,
                                utcTimestamp = utcTs,
                                isCompleted = 0,
                                priority = recurring.priority,
                                recurringParentId = recurring.id,
                                isGenerated = 1
                            )
                            db.insertTask(genTask)
                            if (isRem == 1) {
                                scheduler.scheduleTaskReminder(genTask)
                            }
                            recurringGenCount++
                        }
                    }
                }

                // 3. Reschedule all habit reminders
                val activeHabits = db.getAllHabits().filter { !it.isPaused && !it.reminderTime.isNullOrEmpty() }
                var habitCount = 0
                activeHabits.forEach { habit ->
                    if (scheduler.scheduleHabitReminder(habit)) {
                        habitCount++
                    }
                }

                Log.d(
                    TAG,
                    "Rescheduled $taskCount task alarms, generated $recurringGenCount recurring instances, $habitCount habit alarms"
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
