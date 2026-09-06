package com.l1khith.calender28.utils

import android.content.Context
import android.util.Log
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.Habit

/**
 * Manages the calculation and persistence of the user's Overall Daily Streak.
 *
 * Rules:
 * - A day is considered active/completed if:
 *     1. At least one habit entry was completed on that day, OR
 *     2. At least one recurring task was completed on that day.
 * - The overall streak increments consecutively for each active day up to today.
 * - If today is not completed yet, the streak stays intact based on yesterday's completion.
 * - If yesterday was also missed, streak resets to 0 (or 1 if today is completed).
 */
object OverallStreakManager {

    private const val TAG = "OverallStreakManager"
    private const val PREFS_NAME = "overall_streak_prefs"
    private const val KEY_CACHED_STREAK = "cached_overall_streak"

    fun getCachedStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CACHED_STREAK, 0)
    }

    fun saveCachedStreak(context: Context, streak: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_CACHED_STREAK, streak)
            .apply()
    }

    /**
     * Converts FixedDate to epoch day using toTimestamp.
     */
    fun toEpochDay(fixedDate: FixedDate): Long {
        val timestamp = FixedCalendarHelper.toTimestamp(fixedDate)
        return HabitCycleEngine.getEpochDay(timestamp)
    }

    /**
     * Computes the overall streak given the current list of all tasks and habits.
     */
    fun computeOverallStreak(
        allTasks: List<AppTask>,
        habits: List<Habit>,
        todayEpochDay: Long = HabitCycleEngine.currentEpochDay()
    ): Int {
        val activeEpochDays = mutableSetOf<Long>()

        // 1. Gather active days from completed recurring tasks
        for (task in allTasks) {
            val isRecurring = task.recurringParentId != null || task.isGenerated == 1
            if (isRecurring && task.completed) {
                val parsedDate = FixedCalendarHelper.parseDateStr(task.associatedDate)
                if (parsedDate != null) {
                    val epochDay = toEpochDay(parsedDate)
                    activeEpochDays.add(epochDay)
                }
            }
        }

        // 2. Gather active days from completed habits
        for (habit in habits) {
            val anchorEpochDay = HabitCycleEngine.getEpochDay(habit.createdAtMs)
            val currentCycleIndex = HabitCycleEngine.getCycleIndex(todayEpochDay, anchorEpochDay)
            for (dayInCycle in habit.completedDays) {
                val epochDay = HabitCycleEngine.getEpochDay(currentCycleIndex, dayInCycle, anchorEpochDay)
                activeEpochDays.add(epochDay)
            }
        }

        if (activeEpochDays.isEmpty()) return 0

        // Streak check starting from today or yesterday
        val startDay = if (activeEpochDays.contains(todayEpochDay)) {
            todayEpochDay
        } else if (activeEpochDays.contains(todayEpochDay - 1)) {
            todayEpochDay - 1
        } else {
            return 0
        }

        var streak = 0
        var day = startDay
        while (activeEpochDays.contains(day)) {
            streak++
            day--
        }

        return streak
    }
}
