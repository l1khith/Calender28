package com.l1khith.calender28.test

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.AppTaskEntity
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.HabitEntity
import com.l1khith.calender28.data.HabitEntryEntity
import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.data.RecurringTaskEntity
import com.l1khith.calender28.utils.FixedDate
import java.util.UUID

object TestDataFactory {

    fun createAppTask(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Task",
        description: String? = null,
        associatedDate: String = "2026-08-14",
        isReminder: Int = 1,
        reminderTime: String? = "09:00",
        utcTimestamp: Long? = System.currentTimeMillis() + 3600000,
        isCompleted: Int = 0,
        priority: Int = 1,
        recurringParentId: String? = null,
        isGenerated: Int = 0,
        endDate: String? = null,
        endTime: String? = null,
        isAllDay: Int = 0,
        reminderOffsetMin: Int? = null,
        endUtcTimestamp: Long? = null
    ) = AppTask(
        id = id,
        title = title,
        description = description,
        associatedDate = associatedDate,
        isReminder = isReminder,
        reminderTime = reminderTime,
        utcTimestamp = utcTimestamp,
        isCompleted = isCompleted,
        priority = priority,
        recurringParentId = recurringParentId,
        isGenerated = isGenerated,
        endDate = endDate,
        endTime = endTime,
        isAllDay = isAllDay,
        reminderOffsetMin = reminderOffsetMin,
        endUtcTimestamp = endUtcTimestamp
    )

    fun createAppTaskEntity(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Task Entity",
        description: String? = null,
        associated_date: String = "2026-08-14",
        is_reminder: Int = 1,
        reminder_time: String? = "09:00",
        utc_timestamp: Long? = System.currentTimeMillis() + 3600000,
        is_completed: Int = 0,
        priority: Int = 1,
        recurring_parent_id: String? = null,
        is_generated: Int = 0,
        end_date: String? = null,
        end_time: String? = null,
        is_all_day: Int = 0,
        reminder_offset_min: Int? = null,
        end_utc_timestamp: Long? = null
    ) = AppTaskEntity(
        id = id,
        title = title,
        description = description,
        associated_date = associated_date,
        is_reminder = is_reminder,
        reminder_time = reminder_time,
        utc_timestamp = utc_timestamp,
        is_completed = is_completed,
        priority = priority,
        recurring_parent_id = recurring_parent_id,
        is_generated = is_generated,
        end_date = end_date,
        end_time = end_time,
        is_all_day = is_all_day,
        reminder_offset_min = reminder_offset_min,
        end_utc_timestamp = end_utc_timestamp
    )

    fun createHabit(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Habit",
        category: String = "Health",
        reminderTime: String? = "07:00 AM",
        isPaused: Boolean = false,
        colorHex: Long = 0xFF3B82F6,
        completedDays: Set<Int> = emptySet(),
        createdAtMs: Long = System.currentTimeMillis()
    ) = Habit(
        id = id,
        name = name,
        category = category,
        reminderTime = reminderTime,
        isPaused = isPaused,
        colorHex = colorHex,
        completedDays = completedDays,
        createdAtMs = createdAtMs
    )

    fun createHabitEntity(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Habit Entity",
        category: String = "Health",
        reminder_time: String? = "07:00 AM",
        is_paused: Int = 0,
        color_hex: Long = 0xFF3B82F6,
        created_at_ms: Long = System.currentTimeMillis()
    ) = HabitEntity(
        id = id,
        name = name,
        category = category,
        reminder_time = reminder_time,
        is_paused = is_paused,
        color_hex = color_hex,
        created_at_ms = created_at_ms
    )

    fun createHabitEntryEntity(
        id: String = UUID.randomUUID().toString(),
        habit_id: String = "habit-1",
        cycle_index: Long = 0L,
        day_in_cycle: Int = 1,
        epoch_day: Long = 19800L,
        is_completed: Int = 1,
        completed_at_ms: Long = System.currentTimeMillis()
    ) = HabitEntryEntity(
        id = id,
        habit_id = habit_id,
        cycle_index = cycle_index,
        day_in_cycle = day_in_cycle,
        epoch_day = epoch_day,
        is_completed = is_completed,
        completed_at_ms = completed_at_ms
    )

    fun createRecurringTask(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Recurring Task",
        description: String? = null,
        recurrenceType: RecurrenceType = RecurrenceType.DAILY,
        recurrenceDays: List<Int> = emptyList(),
        recurrenceInterval: Int = 1,
        priority: Int = 1,
        isActive: Boolean = true,
        createdAt: Long = System.currentTimeMillis(),
        endDate: String? = null,
        reminderTime: String? = "08:00 AM"
    ) = RecurringTask(
        id = id,
        title = title,
        description = description,
        recurrenceType = recurrenceType,
        recurrenceDays = recurrenceDays,
        recurrenceInterval = recurrenceInterval,
        priority = priority,
        isActive = isActive,
        createdAt = createdAt,
        endDate = endDate,
        reminderTime = reminderTime
    )

    fun createRecurringTaskEntity(
        id: String = UUID.randomUUID().toString(),
        title: String = "Test Recurring Task Entity",
        description: String? = null,
        recurrence_type: String = "DAILY",
        recurrence_days: String = "",
        recurrence_interval: Int = 1,
        priority: Int = 1,
        is_active: Int = 1,
        created_at: Long = System.currentTimeMillis(),
        end_date: String? = null,
        reminder_time: String? = "08:00 AM"
    ) = RecurringTaskEntity(
        id = id,
        title = title,
        description = description,
        recurrence_type = recurrence_type,
        recurrence_days = recurrence_days,
        recurrence_interval = recurrence_interval,
        priority = priority,
        is_active = is_active,
        created_at = created_at,
        end_date = end_date,
        reminder_time = reminder_time
    )

    fun createFixedDate(
        year: Int = 2026,
        month: Int = 8,
        day: Int = 14
    ) = FixedDate(year = year, month = month, day = day)
}
