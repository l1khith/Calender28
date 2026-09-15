package com.l1khith.calender28.domain.usecase

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetectTaskConflictsUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(candidate: AppTask): List<DayConflict> = withContext(Dispatchers.Default) {
        val startDate = candidate.associatedDate
        val endDate = candidate.endDate ?: candidate.associatedDate

        val overlappingEntities = taskRepository.getTasksOverlapping(startDate, endDate)
        val candidateStartMs = candidate.utcTimestamp ?: return@withContext emptyList()
        val candidateEndMs = candidate.endUtcTimestamp ?: (candidateStartMs + 60 * 60_000L)

        val conflicts = mutableListOf<DayConflict>()

        for (other in overlappingEntities) {
            if (other.id == candidate.id) continue

            // All-day vs Timed check
            if (candidate.allDay || other.allDay) {
                if (candidate.associatedDate == other.associatedDate) {
                    conflicts += DayConflict.AllDayVsTimed(
                        primaryEventId = if (!candidate.allDay) candidate.id else other.id,
                        allDayEventId = if (candidate.allDay) candidate.id else other.id,
                        timedTitle = if (!candidate.allDay) candidate.title else other.title,
                        allDayTitle = if (candidate.allDay) candidate.title else other.title
                    )
                }
                continue
            }

            val otherStartMs = other.utcTimestamp ?: continue
            val otherEndMs = other.endUtcTimestamp ?: (otherStartMs + 60 * 60_000L)

            // Hard Overlap check
            val overlapStart = maxOf(candidateStartMs, otherStartMs)
            val overlapEnd = minOf(candidateEndMs, otherEndMs)

            if (overlapStart < overlapEnd) {
                if (candidate.spansMidnight || other.spansMidnight) {
                    conflicts += DayConflict.CrossDayOverlap(
                        primaryEventId = candidate.id,
                        crossDayEventId = other.id,
                        overlapStartMs = overlapStart,
                        overlapEndMs = overlapEnd,
                        primaryTitle = candidate.title,
                        crossDayTitle = other.title
                    )
                } else {
                    conflicts += DayConflict.HardOverlap(
                        primaryEventId = candidate.id,
                        secondaryEventId = other.id,
                        overlapStartMs = overlapStart,
                        overlapEndMs = overlapEnd,
                        primaryTitle = candidate.title,
                        secondaryTitle = other.title
                    )
                }
            } else if (candidateEndMs == otherStartMs || otherEndMs == candidateStartMs) {
                conflicts += DayConflict.BackToBack(
                    primaryEventId = candidate.id,
                    secondaryEventId = other.id,
                    boundaryMs = if (candidateEndMs == otherStartMs) candidateEndMs else otherEndMs,
                    primaryTitle = candidate.title,
                    secondaryTitle = other.title
                )
            }
        }

        conflicts.sortedBy { it.severity.ordinal }
    }
}
