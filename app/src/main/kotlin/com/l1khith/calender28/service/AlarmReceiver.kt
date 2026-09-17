package com.l1khith.calender28.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.l1khith.calender28.data.RoomTaskDatabase
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.HabitCycleEngine
import com.l1khith.calender28.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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

                val db = TaskDatabase(context) // Used for habit lookup (getAllHabits enriches with cycle data)
                val roomDb = RoomTaskDatabase.getInstance(context)
                val notificationHelper = NotificationHelper(context)
                val scheduler = AlarmScheduler(context)

                val offsetMin = if (intent.hasExtra(AlarmScheduler.EXTRA_OFFSET_MIN)) {
                    intent.getIntExtra(AlarmScheduler.EXTRA_OFFSET_MIN, 0)
                } else null

                when (itemType) {
                    AlarmScheduler.TYPE_TASK -> {
                        // Handles both normal tasks AND generated recurring task instances
                        val task = roomDb.taskDao().getTaskById(itemId)?.toAppTask()
                        if (task != null) {
                            if (!task.completed) {
                                Log.d(TAG, "Showing task reminder for id=$itemId title='${task.title}' offsetMin=$offsetMin")
                                notificationHelper.showTaskReminder(task, offsetMin)
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
                            notificationHelper.showTaskReminder(tempTask, offsetMin)
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

                    AlarmScheduler.TYPE_DAILY_REMINDER -> {
                        val app = context.applicationContext as? com.l1khith.calender28.MatrixApplication
                        val userPrefs = app?.container?.userPreferencesRepository
                        val taskRepo = app?.container?.taskRepository
                        val getBetStatus = app?.container?.getBetStatusUseCase

                        val todayStr = FixedCalendarHelper.currentFixedDate().toString()

                        if (userPrefs != null) {
                            val enabled = userPrefs.dailyReminderEnabled.first()
                            val lastFired = userPrefs.dailyReminderLastFiredDate.first()
                            if (!enabled) {
                                Log.d(TAG, "Daily reminder disabled in prefs, skipping")
                                return@launch
                            }
                            if (lastFired == todayStr) {
                                Log.d(TAG, "Daily reminder already fired today ($todayStr), skipping")
                                return@launch
                            }

                            val userName = userPrefs.optionalUserName.first()
                            val lossStreak = userPrefs.betLossStreak.first()
                            val lastPlayedDate = userPrefs.betLastPlayedDate.first()
                            val isRecoveryDay = (lossStreak >= 3 && lastPlayedDate != todayStr)

                            val tasks = taskRepo?.getTasksSpanningDate(todayStr) ?: emptyList()
                            val betStatus = getBetStatus?.invoke(todayStr)

                            val content = DailyReminderContentBuilder.build(
                                userName = userName,
                                taskCount = tasks.size,
                                firstTaskTitle = tasks.firstOrNull()?.title,
                                betStatus = betStatus,
                                isRecoveryDay = isRecoveryDay
                            )

                            notificationHelper.showDailyTaskReminder(
                                title = content.title,
                                body = content.message,
                                openBetSheet = content.openBetSheet
                            )

                            userPrefs.updateDailyReminderLastFiredDate(todayStr)

                            // Reschedule next day's alarm
                            val hour = userPrefs.dailyReminderHour.first()
                            val minute = userPrefs.dailyReminderMinute.first()
                            scheduler.scheduleDailyTaskReminder(hour, minute)
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
