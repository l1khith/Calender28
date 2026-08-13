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

private const val TAG = "ServiceAlarmReceiver"

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

                Log.d(TAG, "onReceive: Triggered alarm for itemId=$itemId, itemType=$itemType, title=$title")

                val db = TaskDatabase(context)
                val notificationHelper = NotificationHelper(context)
                val scheduler = AlarmScheduler(context)

                when (itemType) {
                    AlarmScheduler.TYPE_TASK -> {
                        // Handles both normal tasks AND generated recurring task instances
                        val task = db.getAllTasks().find { it.id == itemId }
                        if (task != null) {
                            if (!task.completed) {
                                Log.d(TAG, "Showing task reminder for id=$itemId title='${task.title}'")
                                notificationHelper.showTaskReminder(task)
                            } else {
                                Log.d(TAG, "Task $itemId already completed, skipping notification")
                            }
                        } else {
                            // Task may have been deleted; show from intent extras as fallback
                            Log.d(TAG, "Task $itemId not found in DB, showing from intent extras")
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

                    AlarmScheduler.TYPE_HABIT -> {
                        val habit = db.getAllHabits().find { it.id == itemId }
                        if (habit != null && !habit.isPaused) {
                            val todayEpochDay = HabitCycleEngine.currentEpochDay()
                            val anchorEpochDay = HabitCycleEngine.getEpochDay(habit.createdAtMs)
                            val pos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)

                            val isAlreadyCompletedToday = habit.completedDays.contains(pos.safeDayInCycle)
                            if (!isAlreadyCompletedToday) {
                                Log.d(TAG, "Showing habit reminder for id=$itemId name='${habit.name}'")
                                notificationHelper.showHabitReminder(habit)
                            } else {
                                Log.d(TAG, "Habit $itemId already completed today, skipping notification")
                            }
                            // Schedule tomorrow's habit reminder
                            scheduler.scheduleHabitReminder(habit)
                        }
                    }

                    else -> {
                        Log.w(TAG, "Unknown itemType=$itemType for itemId=$itemId, ignoring")
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
