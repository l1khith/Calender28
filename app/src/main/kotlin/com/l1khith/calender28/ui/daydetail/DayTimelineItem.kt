package com.l1khith.calender28.ui.daydetail

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.utils.TimeFormatter
import java.util.Calendar

/**
 * Unified model for all items displayed in the Day Detail timeline.
 */
sealed class DayTimelineItem {
    abstract val timeMinutes: Int? // Minutes from midnight (0..1439), null if no time

    data class HourHeader(
        val hour: Int
    ) : DayTimelineItem() {
        override val timeMinutes: Int = hour * 60
    }

    data class OneOffTask(
        val task: AppTask
    ) : DayTimelineItem() {
        override val timeMinutes: Int? = TimeFormatter.parseTimeToHourMinute(task.reminderTime)?.let {
            it.first * 60 + it.second
        }
    }

    data class RecurringTaskInstance(
        val task: RecurringTask,
        val isCompleted: Boolean,
        val generatedTask: AppTask? = null
    ) : DayTimelineItem() {
        override val timeMinutes: Int? = TimeFormatter.parseTimeToHourMinute(task.reminderTime)?.let {
            it.first * 60 + it.second
        }
    }

    data class HabitReminder(
        val habit: Habit,
        val isCompleted: Boolean
    ) : DayTimelineItem() {
        override val timeMinutes: Int? = TimeFormatter.parseTimeToHourMinute(habit.reminderTime)?.let {
            it.first * 60 + it.second
        }
    }

    data class NoteItem(
        val note: Note
    ) : DayTimelineItem() {
        override val timeMinutes: Int? = run {
            val cal = Calendar.getInstance().apply { timeInMillis = note.createdAt }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }
    }
}
