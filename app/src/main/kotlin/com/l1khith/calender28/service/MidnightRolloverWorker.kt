package com.l1khith.calender28.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.widget.WidgetUpdater
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val TAG = "MidnightRolloverWorker"

class MidnightRolloverWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: Running midnight rollover background work")
        return try {
            val db = TaskDatabase(applicationContext)
            val scheduler = AlarmScheduler(applicationContext)
            val today = FixedCalendarHelper.currentFixedDate()

            // 1. Run catch-up rollover — generates instances and schedules alarms via service/AlarmScheduler
            db.catchUpRollover(today.toString())

            // 2. Reschedule alarms for any generated tasks that might have been created
            val allTasks = db.getAllTasks()
            val todayTasks = allTasks.filter {
                it.reminder && !it.completed && it.utcTimestamp != null && it.associatedDate == today.toString()
            }
            todayTasks.forEach { task ->
                scheduler.scheduleTaskReminder(task)
            }

            // 3. Reschedule all habit reminders for today
            val activeHabits = db.getAllHabits().filter { !it.isPaused && !it.reminderTime.isNullOrEmpty() }
            activeHabits.forEach { habit ->
                scheduler.scheduleHabitReminder(habit)
            }

            // 4. Update Widget
            WidgetUpdater.updateWidget(applicationContext)

            // 5. Schedule next midnight rollover
            scheduleNextMidnightRollover(applicationContext)

            Log.d(TAG, "doWork: Completed. ${todayTasks.size} task alarms, ${activeHabits.size} habit alarms rescheduled")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in MidnightRolloverWorker", e)
            Result.failure()
        }
    }

    companion object {
        fun scheduleNextMidnightRollover(context: Context) {
            try {
                val workManager = WorkManager.getInstance(context)

                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val delayMs = cal.timeInMillis - System.currentTimeMillis()
                Log.d(TAG, "scheduleNextMidnightRollover: Scheduling next rollover in ${delayMs / 1000}s")

                val request = OneTimeWorkRequestBuilder<MidnightRolloverWorker>()
                    .setInitialDelay(delayMs.coerceAtLeast(1000L), TimeUnit.MILLISECONDS)
                    .addTag("midnight_rollover")
                    .build()

                workManager.enqueueUniqueWork(
                    "midnight_rollover",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule midnight rollover via WorkManager", e)
            }
        }
    }
}
