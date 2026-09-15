package com.l1khith.calender28.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.RoomTaskDatabase
import com.l1khith.calender28.data.ScheduledAlarmEntity
import com.l1khith.calender28.utils.FixedCalendarHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "ServiceAlarmScheduler"

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_ITEM_TYPE = "item_type"
        const val EXTRA_TITLE = "title"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_IS_RECURRING = "is_recurring"
        const val EXTRA_RECURRENCE_INDEX = "recurrence_index"

        const val TYPE_TASK = "task"
        const val TYPE_HABIT = "habit"
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SCHEDULE TASK REMINDER (handles both normal and generated tasks)
    // ═══════════════════════════════════════════════════════════════════════
    fun scheduleTaskReminder(task: AppTask): Boolean {
        if (task.isReminder != 1 || task.utcTimestamp == null || task.completed) {
            Log.d(TAG, "scheduleTaskReminder: Skipping task id=${task.id} (isReminder=${task.isReminder}, utcTimestamp=${task.utcTimestamp}, completed=${task.completed})")
            return false
        }

        val offsetMillis = (task.reminderOffsetMin ?: 0) * 60_000L
        var triggerTime = task.utcTimestamp - offsetMillis
        val now = System.currentTimeMillis()

        // For generated recurring tasks whose time passed today, roll forward 24h
        if (triggerTime <= now) {
            if (task.recurringParentId != null) {
                while (triggerTime <= now) {
                    triggerTime += 86400000L
                }
                Log.d(TAG, "scheduleTaskReminder: Generated recurring task ${task.id} time passed, rolled to triggerTime=$triggerTime")
            } else {
                Log.d(TAG, "scheduleTaskReminder: Task ${task.id} timestamp $triggerTime is in the past, skipping")
                return false
            }
        }

        val intent = createAlarmIntent(
            itemId = task.id,
            itemType = TYPE_TASK,
            title = task.title,
            description = task.description,
            isRecurring = task.recurringParentId != null
        )

        val alarmId = task.id.hashCode() and 0x7FFFFFFF
        saveAlarmRecord(alarmId, task.id, TYPE_TASK, triggerTime, task.recurringParentId != null)
        val result = scheduleExactAlarm(alarmId, triggerTime, intent)
        Log.d(TAG, "scheduleTaskReminder: Scheduled task=${task.id} title='${task.title}' at triggerTime=$triggerTime result=$result")
        return result
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SCHEDULE HABIT DAILY REMINDER
    // ═══════════════════════════════════════════════════════════════════════
    fun scheduleHabitReminder(habit: Habit): Boolean {
        if (habit.isPaused || habit.reminderTime.isNullOrEmpty()) return false

        val nowFixed = FixedCalendarHelper.currentFixedDate()
        var triggerMs = FixedCalendarHelper.toTimestamp(nowFixed, habit.reminderTime)

        val nowMs = System.currentTimeMillis()
        while (triggerMs <= nowMs) {
            triggerMs += 86400000L
        }

        val intent = createAlarmIntent(
            itemId = habit.id,
            itemType = TYPE_HABIT,
            title = habit.name,
            description = "Time for your ${habit.name} habit!",
            isRecurring = true
        )

        val alarmId = "${habit.id}_daily".hashCode() and 0x7FFFFFFF
        saveAlarmRecord(alarmId, habit.id, TYPE_HABIT, triggerMs, true, 0)
        val result = scheduleExactAlarm(alarmId, triggerMs, intent)
        Log.d(TAG, "scheduleHabitReminder: Scheduled habit=${habit.id} name='${habit.name}' at triggerMs=$triggerMs result=$result")
        return result
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CANCEL ALARM
    // ═══════════════════════════════════════════════════════════════════════
    fun cancelAlarm(alarmId: Int) {
        Log.d(TAG, "cancelAlarm: Cancelling alarmId=$alarmId")
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        deleteAlarmRecord(alarmId)
    }

    fun cancelTaskAlarm(task: AppTask) {
        val alarmId = task.id.hashCode() and 0x7FFFFFFF
        cancelAlarm(alarmId)
    }

    fun cancelAllForItem(itemId: String) {
        Log.d(TAG, "cancelAllForItem: Cancelling all alarms for itemId=$itemId")
        scope.launch(Dispatchers.IO) {
            try {
                val db = RoomTaskDatabase.getInstance(context)
                val alarms = db.scheduledAlarmDao().getAlarmsForItem(itemId)
                // Cancel PendingIntents directly without calling cancelAlarm()
                alarms.forEach { alarm ->
                    val intent = Intent(context, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context, alarm.alarm_id, intent,
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    pendingIntent?.let {
                        alarmManager.cancel(it)
                        it.cancel()
                    }
                }
                // Bulk delete all alarm records for this item
                db.scheduledAlarmDao().deleteAlarmsForItem(itemId)
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling alarms for $itemId", e)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PRIVATE: Exact Alarm Scheduling with Permission Check
    // ═══════════════════════════════════════════════════════════════════════
    private fun scheduleExactAlarm(alarmId: Int, triggerTime: Long, intent: Intent): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!NotificationPermissionHelper.canScheduleExactAlarms(context)) {
                Log.w(TAG, "Cannot schedule exact alarms — permission denied, falling back")
                val pendingIntent = PendingIntent.getBroadcast(
                    context, alarmId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                return true
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "Scheduled exact alarm $alarmId at $triggerTime via setAlarmClock")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException — falling back to inexact", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            true
        }
    }

    private fun createAlarmIntent(
        itemId: String,
        itemType: String,
        title: String,
        description: String?,
        isRecurring: Boolean,
        recurrenceIndex: Int = 0
    ): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ITEM_ID, itemId)
            putExtra(EXTRA_ITEM_TYPE, itemType)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description ?: "")
            putExtra(EXTRA_IS_RECURRING, isRecurring)
            putExtra(EXTRA_RECURRENCE_INDEX, recurrenceIndex)
        }
    }

    private fun saveAlarmRecord(
        alarmId: Int,
        itemId: String,
        itemType: String,
        triggerTimeUtc: Long,
        isRecurring: Boolean,
        recurrenceIndex: Int = 0
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val db = RoomTaskDatabase.getInstance(context)
                db.scheduledAlarmDao().insertAlarm(
                    ScheduledAlarmEntity(
                        alarm_id = alarmId,
                        item_id = itemId,
                        item_type = itemType,
                        scheduled_time_utc = triggerTimeUtc,
                        is_recurring = isRecurring,
                        recurrence_index = recurrenceIndex
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error saving alarm record", e)
            }
        }
    }

    private fun deleteAlarmRecord(alarmId: Int) {
        scope.launch(Dispatchers.IO) {
            try {
                val db = RoomTaskDatabase.getInstance(context)
                db.scheduledAlarmDao().deleteAlarm(alarmId)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting alarm record", e)
            }
        }
    }
}
