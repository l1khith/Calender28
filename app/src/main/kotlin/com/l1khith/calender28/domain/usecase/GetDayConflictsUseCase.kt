package com.l1khith.calender28.domain.usecase

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetDayConflictsUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(dateStr: String): List<DayConflict> = withContext(Dispatchers.Default) {
        val allEvents = taskRepository.getTasksSpanningDate(dateStr)
        val timedEvents = allEvents.filter { !it.allDay && it.utcTimestamp != null }
            .sortedBy { it.utcTimestamp!! }
        val allDayEvents = allEvents.filter { it.allDay }

        val conflicts = mutableListOf<DayConflict>()

        // C1 & C5 — HARD OVERLAP & CROSS-DAY OVERLAP
        for (i in timedEvents.indices) {
            val a = timedEvents[i]
            val aStart = a.utcTimestamp!!
            val aEnd = a.endUtcTimestamp ?: (aStart + 60 * 60_000L)

            for (j in i + 1 until timedEvents.size) {
                val b = timedEvents[j]
                val bStart = b.utcTimestamp!!
                val bEnd = b.endUtcTimestamp ?: (bStart + 60 * 60_000L)

                if (bStart >= aEnd) break // Since list is sorted by start time

                val overlapStart = maxOf(aStart, bStart)
                val overlapEnd = minOf(aEnd, bEnd)

                if (overlapStart < overlapEnd) {
                    if (a.spansMidnight || b.spansMidnight) {
                        conflicts += DayConflict.CrossDayOverlap(
                            primaryEventId = a.id,
                            crossDayEventId = b.id,
                            overlapStartMs = overlapStart,
                            overlapEndMs = overlapEnd,
                            primaryTitle = a.title,
                            crossDayTitle = b.title
                        )
                    } else {
                        conflicts += DayConflict.HardOverlap(
                            primaryEventId = a.id,
                            secondaryEventId = b.id,
                            overlapStartMs = overlapStart,
                            overlapEndMs = overlapEnd,
                            primaryTitle = a.title,
                            secondaryTitle = b.title
                        )
                    }
                }
            }
        }

        // C2 — BACK-TO-BACK
        for (i in 0 until timedEvents.size - 1) {
            val a = timedEvents[i]
            val b = timedEvents[i + 1]
            val aStart = a.utcTimestamp!!
            val aEnd = a.endUtcTimestamp ?: (aStart + 60 * 60_000L)
            val bStart = b.utcTimestamp!!

            if (aEnd == bStart) {
                conflicts += DayConflict.BackToBack(
                    primaryEventId = a.id,
                    secondaryEventId = b.id,
                    boundaryMs = aEnd,
                    primaryTitle = a.title,
                    secondaryTitle = b.title
                )
            }
        }

        // C3 — SAME 30-MIN SLOT STACK
        val slotBuckets = timedEvents.groupBy {
            val startMs = it.utcTimestamp!!
            (startMs / (30 * 60 * 1000L)) * (30 * 60 * 1000L)
        }
        slotBuckets.filterValues { it.size >= 3 }.forEach { (slotStart, evs) ->
            conflicts += DayConflict.SameSlotStack(
                eventIds = evs.map { it.id },
                windowStartMs = slotStart,
                windowEndMs = slotStart + 30 * 60 * 1000L,
                eventTitles = evs.map { it.title }
            )
        }

        // C4 — ALL-DAY VS TIMED
        if (allDayEvents.isNotEmpty()) {
            for (timed in timedEvents) {
                for (ad in allDayEvents) {
                    conflicts += DayConflict.AllDayVsTimed(
                        primaryEventId = timed.id,
                        allDayEventId = ad.id,
                        timedTitle = timed.title,
                        allDayTitle = ad.title
                    )
                }
            }
        }

        conflicts.sortedBy { it.severity.ordinal }
    }
}
