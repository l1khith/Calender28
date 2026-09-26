package com.l1khith.calender28.utils

import android.content.Context
import android.text.format.DateFormat
import com.l1khith.calender28.data.AppTask
import java.util.Calendar

/**
 * Universal time formatting utility that respects device system 24h / 12h preferences.
 */
object TimeFormatter {

    /**
     * Parses any time string into (hour, minute), where hour is 0..23 and minute is 0..59.
     * Supports:
     * - "15:00", "09:30"
     * - "3:00 PM", "03:00 PM", "12:00 AM", "12:30 PM"
     * - "07:00 AM Daily", "15:00 Daily"
     * Returns null if unparseable or blank.
     */
    fun parseTimeToHourMinute(timeStr: String?): Pair<Int, Int>? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            val clean = timeStr.replace(" Daily", "", ignoreCase = true).trim()
            val isPM = clean.contains("PM", ignoreCase = true)
            val isAM = clean.contains("AM", ignoreCase = true)
            val digitsOnly = clean.replace("AM", "", ignoreCase = true)
                .replace("PM", "", ignoreCase = true)
                .trim()
            val parts = digitsOnly.split(":")
            var h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
            val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

            if (isPM && h < 12) h += 12
            if (isAM && h == 12) h = 0

            if (h in 0..23 && m in 0..59) {
                Pair(h, m)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Formats hour (0..23) and minute (0..59) according to device setting.
     * 24-hr device -> "15:00", "09:00"
     * 12-hr device -> "3:00 PM", "9:00 AM"
     */
    fun formatTime(context: Context, hour: Int, minute: Int): String {
        val is24Hour = DateFormat.is24HourFormat(context)
        return if (is24Hour) {
            "%02d:%02d".format(hour, minute)
        } else {
            val amPm = if (hour >= 12) "PM" else "AM"
            val h12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            "$h12:%02d %s".format(minute, amPm)
        }
    }

    /**
     * Formats any time string according to device setting.
     * If unparseable or blank, returns original string or empty.
     */
    fun formatTime(context: Context, timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return ""
        val parsed = parseTimeToHourMinute(timeStr) ?: return timeStr
        return formatTime(context, parsed.first, parsed.second)
    }

    /**
     * Formats an hour slot header (e.g. for timeline axis).
     * 24-hr device -> "15:00", "00:00"
     * 12-hr device -> "3:00 PM", "12:00 AM"
     */
    fun formatHourHeader(context: Context, hour: Int): String {
        return formatTime(context, hour, 0)
    }

    /**
     * Formats habit reminder time (e.g. "07:00 AM Daily" or "15:00 Daily").
     * Respects 24h / 12h, preserving the "Daily" label if present.
     */
    fun formatHabitReminderTime(context: Context, reminderTime: String?): String {
        if (reminderTime.isNullOrBlank()) return "Off"
        val isDaily = reminderTime.contains("Daily", ignoreCase = true)
        val formatted = formatTime(context, reminderTime)
        return if (isDaily) "$formatted Daily" else formatted
    }

    /**
     * Formats task time range (e.g. "15:00 – 16:00" or "3:00 PM – 4:00 PM" or "All Day").
     */
    fun formatTaskTimeRange(context: Context, task: AppTask): String {
        return when {
            task.allDay -> "All Day"
            !task.reminderTime.isNullOrEmpty() && !task.endTime.isNullOrEmpty() -> {
                "${formatTime(context, task.reminderTime)} – ${formatTime(context, task.endTime)}"
            }
            !task.reminderTime.isNullOrEmpty() -> formatTime(context, task.reminderTime)
            else -> ""
        }
    }

    /**
     * Formats a timestamp (epoch ms) to time string according to device setting.
     */
    fun formatTimestamp(context: Context, timestampMs: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = timestampMs }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        return formatTime(context, hour, minute)
    }
}
