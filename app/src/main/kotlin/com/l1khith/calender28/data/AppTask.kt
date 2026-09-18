package com.l1khith.calender28.data

import androidx.compose.runtime.Immutable

@Immutable
data class AppTask(
    val id: String,
    val title: String,
    val description: String? = null,
    val associatedDate: String,      // "YYYY-MM-DD" in 13-month format (e.g., "2026-07-12")
    val isReminder: Int = 0,         // 1 = Active Reminder, 0 = Task Only
    val reminderTime: String? = null,// Local time (e.g., "14:30")
    val utcTimestamp: Long? = null,  // Absolute Unix Milliseconds for system alarms
    val isCompleted: Int = 0,        // 0 = Pending, 1 = Done
    val priority: Int = 1,           // 1 = Low, 2 = Medium, 3 = High
    val recurringParentId: String? = null,
    val isGenerated: Int = 0,        // 1 = generated from recurring, 0 = regular task
    val lastFocusedAt: Long? = null,
    val totalFocusTime: Int = 0,     // Total focus duration in seconds
    val focusCount: Int = 0,         // Total focus sessions completed
    val lastFocusDuration: Int = 0,  // Last session duration in seconds
    val lastFocusMode: String? = null, // "timer" or "stopwatch"
    val endDate: String? = null,     // "YYYY-MM-DD" in 13-month format (null = same as associatedDate)
    val endTime: String? = null,     // "HH:mm" (null = point-in-time)
    val isAllDay: Int = 0,           // 1 = all-day task
    val reminderOffsetMin: Int? = null, // Legacy single reminder offset in minutes before start
    val endUtcTimestamp: Long? = null, // Absolute Unix Milliseconds for end
    val reminderOffsets: List<Int> = emptyList() // Multi-alarm reminder offsets in minutes before start
) {
    val effectiveReminderOffsets: List<Int>
        get() = when {
            reminderOffsets.isNotEmpty() -> reminderOffsets
            reminderOffsetMin != null -> listOf(reminderOffsetMin)
            else -> emptyList()
        }
    val completed: Boolean get() = isCompleted == 1
    val reminder: Boolean get() = isReminder == 1
    val allDay: Boolean get() = isAllDay == 1
    val isTimeBounded: Boolean get() = !reminderTime.isNullOrEmpty() && !endTime.isNullOrEmpty() && !allDay
    val spansMidnight: Boolean get() = endDate != null && endDate != associatedDate
    val hasEverFocused: Boolean get() = totalFocusTime > 0 || focusCount > 0

    val durationMinutes: Long
        get() {
            if (utcTimestamp != null && endUtcTimestamp != null && endUtcTimestamp > utcTimestamp) {
                return (endUtcTimestamp - utcTimestamp) / 60_000L
            }
            return 0L
        }

    val formattedTimeRange: String
        get() = when {
            allDay -> "All Day"
            !reminderTime.isNullOrEmpty() && !endTime.isNullOrEmpty() -> "$reminderTime → $endTime"
            !reminderTime.isNullOrEmpty() -> reminderTime
            else -> ""
        }

    val formattedFocusDuration: String
        get() {
            if (totalFocusTime <= 0) return ""
            val minutes = totalFocusTime / 60
            val hours = minutes / 60
            val remMinutes = minutes % 60
            return when {
                hours > 0 && remMinutes > 0 -> "${hours}h ${remMinutes}m"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "${totalFocusTime}s"
            }
        }
}
