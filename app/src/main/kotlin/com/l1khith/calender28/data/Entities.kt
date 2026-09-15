package com.l1khith.calender28.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index("associated_date"),
        Index("is_completed"),
        Index("recurring_parent_id"),
        Index("end_date")
    ]
)
data class AppTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val associated_date: String,
    val is_reminder: Int = 0,
    val reminder_time: String? = null,
    val utc_timestamp: Long? = null,
    val is_completed: Int = 0,
    val priority: Int = 1,
    val recurring_parent_id: String? = null,
    val is_generated: Int = 0,
    val last_focused_at: Long? = null,
    val total_focus_time: Int = 0,
    val focus_count: Int = 0,
    val last_focus_duration: Int = 0,
    val last_focus_mode: String? = null,
    val end_date: String? = null,
    val end_time: String? = null,
    val is_all_day: Int = 0,
    val reminder_offset_min: Int? = null,
    val end_utc_timestamp: Long? = null
) {
    fun toAppTask(): AppTask = AppTask(
        id = id,
        title = title,
        description = description,
        associatedDate = associated_date,
        isReminder = is_reminder,
        reminderTime = reminder_time,
        utcTimestamp = utc_timestamp,
        isCompleted = is_completed,
        priority = priority,
        recurringParentId = recurring_parent_id,
        isGenerated = is_generated,
        lastFocusedAt = last_focused_at,
        totalFocusTime = total_focus_time,
        focusCount = focus_count,
        lastFocusDuration = last_focus_duration,
        lastFocusMode = last_focus_mode,
        endDate = end_date,
        endTime = end_time,
        isAllDay = is_all_day,
        reminderOffsetMin = reminder_offset_min,
        endUtcTimestamp = end_utc_timestamp
    )

    companion object {
        fun fromAppTask(task: AppTask): AppTaskEntity = AppTaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            associated_date = task.associatedDate,
            is_reminder = task.isReminder,
            reminder_time = task.reminderTime,
            utc_timestamp = task.utcTimestamp,
            is_completed = task.isCompleted,
            priority = task.priority,
            recurring_parent_id = task.recurringParentId,
            is_generated = task.isGenerated,
            last_focused_at = task.lastFocusedAt,
            total_focus_time = task.totalFocusTime,
            focus_count = task.focusCount,
            last_focus_duration = task.lastFocusDuration,
            last_focus_mode = task.lastFocusMode,
            end_date = task.endDate,
            end_time = task.endTime,
            is_all_day = task.isAllDay,
            reminder_offset_min = task.reminderOffsetMin,
            end_utc_timestamp = task.endUtcTimestamp
        )
    }
}

@Entity(tableName = "recurring_tasks")
data class RecurringTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val recurrence_type: String,
    val recurrence_days: String?,
    val recurrence_interval: Int = 1,
    val priority: Int = 1,
    val is_active: Int = 1,
    val created_at: Long,
    val end_date: String? = null,
    val reminder_time: String? = null
) {
    fun toRecurringTask(): RecurringTask {
        val daysList = if (recurrence_days.isNullOrEmpty()) {
            emptyList()
        } else {
            recurrence_days.split(",").mapNotNull { it.toIntOrNull() }
        }
        val type = RecurrenceType.entries.find { it.name == recurrence_type } ?: RecurrenceType.DAILY

        return RecurringTask(
            id = id,
            title = title,
            description = description,
            recurrenceType = type,
            recurrenceDays = daysList,
            recurrenceInterval = recurrence_interval,
            priority = priority,
            isActive = is_active == 1,
            createdAt = created_at,
            endDate = end_date,
            reminderTime = reminder_time
        )
    }

    companion object {
        fun fromRecurringTask(task: RecurringTask): RecurringTaskEntity = RecurringTaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            recurrence_type = task.recurrenceType.name,
            recurrence_days = task.recurrenceDays.joinToString(","),
            recurrence_interval = task.recurrenceInterval,
            priority = task.priority,
            is_active = if (task.isActive) 1 else 0,
            created_at = task.createdAt,
            end_date = task.endDate,
            reminder_time = task.reminderTime
        )
    }
}

fun AppTask.toEntity(): AppTaskEntity = AppTaskEntity.fromAppTask(this)
fun RecurringTask.toEntity(): RecurringTaskEntity = RecurringTaskEntity.fromRecurringTask(this)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String = "Health",
    val reminder_time: String? = null,
    val is_paused: Int = 0,
    val color_hex: Long = 0xFF3B82F6,
    val created_at_ms: Long
) {
    companion object {
        fun fromHabit(habit: Habit): HabitEntity = HabitEntity(
            id = habit.id,
            name = habit.name,
            category = habit.category,
            reminder_time = habit.reminderTime,
            is_paused = if (habit.isPaused) 1 else 0,
            color_hex = habit.colorHex,
            created_at_ms = habit.createdAtMs
        )
    }
}

@Entity(
    tableName = "habit_entries",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("habit_id"),
        Index(value = ["habit_id", "cycle_index"]),
        Index(value = ["habit_id", "cycle_index", "day_in_cycle"], unique = true)
    ]
)
data class HabitEntryEntity(
    @PrimaryKey val id: String,
    val habit_id: String,
    val cycle_index: Long,
    val day_in_cycle: Int,
    val epoch_day: Long,
    val is_completed: Int = 0,
    val completed_at_ms: Long? = null
)

@Entity(
    tableName = "scheduled_alarms",
    indices = [
        Index("item_id"),
        Index("scheduled_time_utc")
    ]
)
data class ScheduledAlarmEntity(
    @PrimaryKey val alarm_id: Int,
    val item_id: String,
    val item_type: String,
    val scheduled_time_utc: Long,
    val is_recurring: Boolean,
    val recurrence_index: Int = 0,
    val created_at_ms: Long = System.currentTimeMillis()
)

