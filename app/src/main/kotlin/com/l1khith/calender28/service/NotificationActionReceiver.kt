package com.l1khith.calender28.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.l1khith.calender28.data.HabitEntryEntity
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.HabitCycleEngine
import com.l1khith.calender28.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "NotificationActionRecv"

class NotificationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val action = intent.action
        Log.d(TAG, "onReceive: Notification action triggered: $action")

        scope.launch {
            try {
                val db = TaskDatabase(context)
                val notificationHelper = NotificationHelper(context)

                when (action) {
                    NotificationHelper.ACTION_MARK_COMPLETE -> {
                        val taskId = intent.getStringExtra("task_id")
                        val recurringId = intent.getStringExtra("recurring_id")

                        taskId?.let { id ->
                            val task = db.getTaskById(id)
                            if (task != null) {
                                db.updateTask(task.copy(isCompleted = 1))
                                notificationHelper.cancelNotification(id.hashCode() and 0x7FFFFFFF)
                                Log.d(TAG, "Marked task $id complete via notification action")
                            }
                        }

                        recurringId?.let { id ->
                            val activeRecurring = db.getAllRecurringTasks().find { it.id == id }
                            if (activeRecurring != null) {
                                notificationHelper.cancelNotification("${id}_0".hashCode() and 0x7FFFFFFF)
                                Log.d(TAG, "Marked recurring task $id complete via notification action")
                            }
                        }

                        WidgetUpdater.updateWidget(context)
                    }

                    NotificationHelper.ACTION_SNOOZE -> {
                        val taskId = intent.getStringExtra("task_id") ?: return@launch
                        val minutes = intent.getIntExtra("snooze_minutes", 15)
                        Log.d(TAG, "Snoozing task $taskId for $minutes minutes")
                        notificationHelper.snoozeNotification(taskId, minutes)
                    }

                    NotificationHelper.ACTION_LOG_HABIT -> {
                        val habitId = intent.getStringExtra("habit_id") ?: return@launch
                        val habit = db.getAllHabits().find { it.id == habitId } ?: return@launch

                        val todayEpochDay = HabitCycleEngine.currentEpochDay()
                        val anchorEpochDay = HabitCycleEngine.getEpochDay(habit.createdAtMs)
                        val pos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)

                        db.upsertHabitEntry(habitId, pos.cycleIndex, pos.safeDayInCycle, true)
                        Log.d(TAG, "Logged habit $habitId completion for today via notification action")

                        notificationHelper.cancelNotification("${habitId}_habit".hashCode() and 0x7FFFFFFF)
                        WidgetUpdater.updateWidget(context)
                    }

                    NotificationHelper.ACTION_DISMISS -> {
                        val notificationId = intent.getIntExtra("notification_id", 0)
                        Log.d(TAG, "Dismissing notification id=$notificationId")
                        notificationHelper.cancelNotification(notificationId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling notification action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
