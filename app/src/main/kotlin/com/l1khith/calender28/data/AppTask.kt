package com.l1khith.calender28.data

data class AppTask(
    val id: String,
    val title: String,
    val description: String?,
    val associatedDate: String,      // "YYYY-MM-DD" in 13-month format (e.g., "2026-07-12")
    val isReminder: Int,             // 1 = Active Reminder, 0 = Task Only
    val reminderTime: String?,       // Local time (e.g., "14:30")
    val utcTimestamp: Long?,         // Absolute Unix Milliseconds for system alarms
    val isCompleted: Int,            // 0 = Pending, 1 = Done
    val priority: Int = 1,           // 1 = Low, 2 = Medium, 3 = High
    val recurringParentId: String? = null,
    val isGenerated: Int = 0         // 1 = generated from recurring, 0 = regular task
) {
    val completed: Boolean get() = isCompleted == 1
    val reminder: Boolean get() = isReminder == 1
}

