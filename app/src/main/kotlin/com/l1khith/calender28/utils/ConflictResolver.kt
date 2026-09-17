package com.l1khith.calender28.utils

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.domain.model.FreeSlot

object ConflictResolver {

    /**
     * Finds the earliest conflict-free start time ("HH:mm") on candidateDate for a task of the given duration.
     */
    fun findConflictFreeSlot(
        candidateDate: String,
        durationMinutes: Long,
        existingTasks: List<AppTask>,
        ignoreTaskId: String? = null
    ): String? {
        val slots = findNearestFreeSlots(candidateDate, durationMinutes, existingTasks, ignoreTaskId, count = 1)
        return slots.firstOrNull()?.startTime
    }

    /**
     * Finds up to [count] nearest conflict-free slots of [durationMinutes] duration.
     * Scans candidateDate during preferred hours (08:00 - 21:00), then full day (00:00 - 23:00).
     * If more slots are needed, rolls forward to tomorrow.
     */
    fun findNearestFreeSlots(
        candidateDate: String,
        durationMinutes: Long,
        existingTasks: List<AppTask>,
        ignoreTaskId: String? = null,
        count: Int = 3
    ): List<FreeSlot> {
        val fixedDate = FixedCalendarHelper.parseDateStr(candidateDate) ?: return emptyList()
        val durationMs = (if (durationMinutes > 0) durationMinutes else 60L) * 60_000L
        val results = mutableListOf<FreeSlot>()

        // 1. Existing tasks for candidate date
        val timedTasksToday = existingTasks
            .filter { it.associatedDate == candidateDate && it.id != ignoreTaskId && !it.allDay && it.utcTimestamp != null }
            .map { task ->
                val startMs = task.utcTimestamp!!
                val endMs = task.endUtcTimestamp ?: (startMs + 60 * 60_000L)
                startMs to endMs
            }
            .sortedBy { it.first }

        collectSlotsInWindow(fixedDate, 8, 21, durationMs, timedTasksToday, results, count, isTomorrow = false)
        if (results.size < count) {
            collectSlotsInWindow(fixedDate, 0, 23, durationMs, timedTasksToday, results, count, isTomorrow = false)
        }

        // 2. If fewer than count found, search tomorrow
        if (results.size < count) {
            val tomorrowFixed = FixedCalendarHelper.fromTimestamp(FixedCalendarHelper.toTimestamp(fixedDate) + 86400000L)
            val tomorrowStr = tomorrowFixed.toString()
            val timedTasksTomorrow = existingTasks
                .filter { it.associatedDate == tomorrowStr && it.id != ignoreTaskId && !it.allDay && it.utcTimestamp != null }
                .map { task ->
                    val startMs = task.utcTimestamp!!
                    val endMs = task.endUtcTimestamp ?: (startMs + 60 * 60_000L)
                    startMs to endMs
                }
                .sortedBy { it.first }

            collectSlotsInWindow(tomorrowFixed, 8, 21, durationMs, timedTasksTomorrow, results, count, isTomorrow = true)
        }

        return results.distinctBy { it.dateStr to it.startTime }.take(count)
    }

    private fun collectSlotsInWindow(
        fixedDate: FixedDate,
        startHour: Int,
        endHour: Int,
        durationMs: Long,
        sortedIntervals: List<Pair<Long, Long>>,
        accumulator: MutableList<FreeSlot>,
        targetCount: Int,
        isTomorrow: Boolean
    ) {
        val dateStr = fixedDate.toString()
        for (hour in startHour..endHour) {
            for (minute in intArrayOf(0, 15, 30, 45)) {
                if (accumulator.size >= targetCount) return

                val startTimeStr = "%02d:%02d".format(hour, minute)
                val candidateStartMs = FixedCalendarHelper.toTimestamp(fixedDate, startTimeStr)
                val candidateEndMs = candidateStartMs + durationMs

                // Check overlap
                val hasOverlap = sortedIntervals.any { (otherStart, otherEnd) ->
                    candidateStartMs < otherEnd && otherStart < candidateEndMs
                }

                if (!hasOverlap) {
                    val endMinTotal = (candidateEndMs / 60_000L) % 1440L
                    val endTimeStr = "%02d:%02d".format((endMinTotal / 60).toInt(), (endMinTotal % 60).toInt())
                    accumulator.add(
                        FreeSlot(
                            dateStr = dateStr,
                            startTime = startTimeStr,
                            endTime = endTimeStr,
                            isTomorrow = isTomorrow
                        )
                    )
                }
            }
        }
    }
}
