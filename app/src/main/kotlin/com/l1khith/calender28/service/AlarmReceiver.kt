package com.l1khith.calender28.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.HabitCycleEngine
import com.l1khith.calender28.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "AlarmReceiver"

class AlarmReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        scope.launch {
            try {
                val itemId = intent.getStringExtra(AlarmScheduler.EXTRA_ITEM_ID)
                    ?: intent.getStringExtra("task_id")
                    ?: return@launch

                val itemType = intent.getStringExtra(AlarmScheduler.EXTRA_ITEM_TYPE)
                    ?: AlarmScheduler.TYPE_TASK

                val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE)
                    ?: intent.getStringExtra("task_title")
                    ?: "Reminder"

                val description = intent.getStringExtra(AlarmScheduler.EXTRA_DESCRIPTION)
                    ?: intent.getStringExtra("task_desc")

                val isRecurring = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_RECURRING, false)
                val recurrenceIndex = intent.getIntExtra(AlarmScheduler.EXTRA_RECURRENCE_INDEX, 0)

                Log.d(TAG, "onReceive: Triggered alarm for itemId=$itemId, itemType=$itemType, title=$title")

                val db = TaskDatabase(context)
                val notificationHelper = NotificationHelper(context)
                val scheduler = AlarmScheduler(context)

                when (itemType) {
                    AlarmScheduler.TYPE_TASK -> {
                        val task = db.getAllTasks().find { it.id == itemId }
                        if (task != null) {
                            if (!task.completed) {
                                notificationHelper.showTaskReminder(task)
                            }
                        } else {
                            // Temporary or generated task fallback
                            val tempTask = com.l1khith.calender28.data.AppTask(
                                id = itemId,
                                title = title,
                                description = description,
                                associatedDate = FixedCalendarHelper.currentFixedDate().toString(),
                                isReminder = 1,
                                reminderTime = null,
                                utcTimestamp = System.currentTimeMillis(),
                                isCompleted = 0
                            )
                            notificationHelper.showTaskReminder(tempTask)
                        }
                    }

                    AlarmScheduler.TYPE_RECURRING -> {
                        val recurring = db.getAllRecurringTasks().find { it.id == itemId }
                        if (recurring != null && recurring.isActive) {
                            notificationHelper.showRecurringTaskReminder(recurring, recurrenceIndex)
                            scheduler.scheduleRecurringTask(recurring)
                        }
                    }

                    AlarmScheduler.TYPE_HABIT -> {
                        val habit = db.getAllHabits().find { it.id == itemId }
                        if (habit != null && !habit.isPaused) {
                            val today = FixedCalendarHelper.currentFixedDate()
                            val todayEpochDay = today.toEpochDay()
                            val anchorEpochDay = (habit.createdAtMs / 86400000L).coerceAtLeast(0L)
                            val pos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)

                            val isAlreadyCompletedToday = habit.completedDays.contains(pos.safeDayInCycle)
                            if (!isAlreadyCompletedToday) {
                                notificationHelper.showHabitReminder(habit)
                            }
                            scheduler.scheduleHabitReminder(habit)
                        }
                    }
                }

                WidgetUpdater.updateWidget(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle alarm", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
