package com.l1khith.calender28.data

import androidx.compose.runtime.Immutable
import com.l1khith.calender28.utils.HabitCycleEngine

@Immutable
data class Habit(
    val id: String,
    val name: String,
    val category: String = "Health",
    val reminderTime: String? = "07:00 AM Daily",
    val isReminderEnabled: Boolean = true,
    val priority: String = "High",
    val isPaused: Boolean = false,
    val colorHex: Long = 0xFF3B82F6,
    val completedDays: Set<Int> = emptySet(),
    val createdAtMs: Long = 0L
) {
    val completedCount: Int get() = completedDays.size
    val progressPercent: Int get() = ((completedCount.toFloat() / 28f) * 100).toInt()
    val consistencyPercent: Int get() = if (completedCount > 0) ((completedCount.toFloat() / 28f) * 100).toInt() else 0

    val currentDayInCycle: Int
        get() {
            val todayEpochDay = HabitCycleEngine.currentEpochDay()
            val anchorEpochDay = HabitCycleEngine.getEpochDay(createdAtMs)
            return HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay).safeDayInCycle
        }

    val streak: Int
        get() = HabitCycleEngine.computeCurrentStreak(completedDays, currentDayInCycle)

    val longestStreak: Int
        get() = HabitCycleEngine.computeBestStreak(completedDays)
}
