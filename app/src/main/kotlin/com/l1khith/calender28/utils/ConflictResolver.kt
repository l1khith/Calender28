package com.l1khith.calender28.utils

import com.l1khith.calender28.data.AppTask

object ConflictResolver {

    /**
     * Finds the earliest conflict-free start time ("HH:mm") on candidateDate for a task of the given duration.
     * Searches between startHour (default 8) and endHour (default 22) in 15-minute increments.
     * If no slot is found in standard hours, scans 00:00 to 23:59.
     */
    fun findConflictFreeSlot(
        candidateDate: String,
        durationMinutes: Long,
        existingTasks: List<AppTask>,
        ignoreTaskId: String? = null
    ): String? {
        val fixedDate = FixedCalendarHelper.parseDateStr(candidateDate) ?: return null
        val durationMs = (if (durationMinutes > 0) durationMinutes else 60L) * 60_000L

        // Filter and sort active, non-all-day tasks with timestamps
        val timedTasks = existingTasks
            .filter { it.id != ignoreTaskId && !it.allDay && it.utcTimestamp != null }
            .map { task ->
                val startMs = task.utcTimestamp!!
                val endMs = task.endUtcTimestamp ?: (startMs + 60 * 60_000L)
                startMs to endMs
            }
            .sortedBy { it.first }

        // Preferred window: 08:00 to 21:00
        val preferredSlot = scanWindow(fixedDate, 8, 21, durationMs, timedTasks)
        if (preferredSlot != null) return preferredSlot

        // Fallback: 00:00 to 23:00
        return scanWindow(fixedDate, 0, 23, durationMs, timedTasks)
    }

    private fun scanWindow(
        fixedDate: FixedDate,
        startHour: Int,
        endHour: Int,
        durationMs: Long,
        sortedIntervals: List<Pair<Long, Long>>
    ): String? {
        for (hour in startHour..endHour) {
            for (minute in intArrayOf(0, 15, 30, 45)) {
                val timeStr = "%02d:%02d".format(hour, minute)
                val candidateStartMs = FixedCalendarHelper.toTimestamp(fixedDate, timeStr)
                val candidateEndMs = candidateStartMs + durationMs

                // Check for overlap against all existing intervals
                val hasOverlap = sortedIntervals.any { (otherStart, otherEnd) ->
                    candidateStartMs < otherEnd && otherStart < candidateEndMs
                }

                if (!hasOverlap) {
                    return timeStr
                }
            }
        }
        return null
    }
}
