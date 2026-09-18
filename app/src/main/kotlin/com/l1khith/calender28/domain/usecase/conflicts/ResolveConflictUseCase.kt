package com.l1khith.calender28.domain.usecase.conflicts

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.ConflictResolutionDao
import com.l1khith.calender28.data.ConflictResolutionEntity
import com.l1khith.calender28.domain.model.ConflictResolutionResult
import com.l1khith.calender28.domain.model.ResolutionAction
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.utils.FixedCalendarHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * UseCase executing conflict resolution actions and managing undo capabilities.
 */
class ResolveConflictUseCase(
    private val taskRepository: TaskRepository,
    private val conflictResolutionDao: ConflictResolutionDao
) {

    suspend fun moveEvent(
        eventId: String,
        conflictEventId: String,
        isEventA: Boolean,
        newDateStr: String,
        newStartTime: String,
        conflictType: String
    ): ConflictResolutionResult = withContext(Dispatchers.IO) {
        val allTasks = taskRepository.getAllTasks()
        val targetTask = allTasks.find { it.id == eventId }
            ?: throw IllegalArgumentException("Task $eventId not found")
        val otherTask = allTasks.find { it.id == conflictEventId }

        val previousTarget = targetTask.copy()
        val previousOther = otherTask?.copy()

        val fixedDate = FixedCalendarHelper.parseDateStr(newDateStr)
            ?: FixedCalendarHelper.currentFixedDate()
        val durationMs = if (targetTask.durationMinutes > 0) targetTask.durationMinutes * 60_000L else 60 * 60_000L
        val newStartMs = FixedCalendarHelper.toTimestamp(fixedDate, newStartTime)
        val newEndMs = newStartMs + durationMs

        val endMinTotal = (newEndMs / 60_000L) % 1440L
        val newEndTimeStr = "%02d:%02d".format((endMinTotal / 60).toInt(), (endMinTotal % 60).toInt())

        val updated = targetTask.copy(
            associatedDate = newDateStr,
            reminderTime = newStartTime,
            utcTimestamp = newStartMs,
            endDate = if (newDateStr == targetTask.associatedDate) targetTask.endDate else newDateStr,
            endTime = newEndTimeStr,
            endUtcTimestamp = newEndMs
        )
        taskRepository.updateTask(updated)

        val action = if (isEventA) ResolutionAction.MOVE_A else ResolutionAction.MOVE_B
        val resId = UUID.randomUUID().toString()
        conflictResolutionDao.insertResolution(
            ConflictResolutionEntity(
                id = resId,
                conflictType = conflictType,
                eventAId = if (isEventA) eventId else conflictEventId,
                eventBId = if (isEventA) conflictEventId else eventId,
                action = action.name,
                oldStartA = if (isEventA) previousTarget.utcTimestamp else previousOther?.utcTimestamp,
                newStartA = if (isEventA) newStartMs else previousOther?.utcTimestamp,
                oldStartB = if (!isEventA) previousTarget.utcTimestamp else previousOther?.utcTimestamp,
                newStartB = if (!isEventA) newStartMs else previousOther?.utcTimestamp
            )
        )

        ConflictResolutionResult(
            action = action,
            eventAId = if (isEventA) eventId else conflictEventId,
            eventBId = if (isEventA) conflictEventId else eventId,
            message = "Moved '${targetTask.title}' to $newStartTime",
            previousEventA = if (isEventA) previousTarget else previousOther,
            previousEventB = if (isEventA) previousOther else previousTarget
        )
    }

    suspend fun deleteEvent(
        eventId: String,
        conflictEventId: String,
        isEventA: Boolean,
        conflictType: String
    ): ConflictResolutionResult = withContext(Dispatchers.IO) {
        val allTasks = taskRepository.getAllTasks()
        val targetTask = allTasks.find { it.id == eventId }
            ?: throw IllegalArgumentException("Task $eventId not found")
        val otherTask = allTasks.find { it.id == conflictEventId }

        val previousTarget = targetTask.copy()
        val previousOther = otherTask?.copy()

        taskRepository.deleteTask(targetTask.id, targetTask.recurringParentId)

        val action = if (isEventA) ResolutionAction.DELETE_A else ResolutionAction.DELETE_B
        val resId = UUID.randomUUID().toString()
        conflictResolutionDao.insertResolution(
            ConflictResolutionEntity(
                id = resId,
                conflictType = conflictType,
                eventAId = if (isEventA) eventId else conflictEventId,
                eventBId = if (isEventA) conflictEventId else eventId,
                action = action.name,
                oldStartA = previousTarget.utcTimestamp,
                newStartA = null,
                oldStartB = previousOther?.utcTimestamp,
                newStartB = previousOther?.utcTimestamp
            )
        )

        ConflictResolutionResult(
            action = action,
            eventAId = if (isEventA) eventId else conflictEventId,
            eventBId = if (isEventA) conflictEventId else eventId,
            message = "Deleted '${targetTask.title}'",
            previousEventA = if (isEventA) previousTarget else previousOther,
            previousEventB = if (isEventA) previousOther else previousTarget
        )
    }

    suspend fun mergeEvents(
        eventAId: String,
        eventBId: String,
        conflictType: String
    ): ConflictResolutionResult = withContext(Dispatchers.IO) {
        val allTasks = taskRepository.getAllTasks()
        val taskA = allTasks.find { it.id == eventAId }
            ?: throw IllegalArgumentException("Task $eventAId not found")
        val taskB = allTasks.find { it.id == eventBId }
            ?: throw IllegalArgumentException("Task $eventBId not found")

        val previousA = taskA.copy()
        val previousB = taskB.copy()

        val unionStartMs = minOf(taskA.utcTimestamp ?: 0L, taskB.utcTimestamp ?: 0L)
        val endA = taskA.endUtcTimestamp ?: (taskA.utcTimestamp ?: 0L) + 3600000L
        val endB = taskB.endUtcTimestamp ?: (taskB.utcTimestamp ?: 0L) + 3600000L
        val unionEndMs = maxOf(endA, endB)

        val mergedTitle = "${taskA.title} + ${taskB.title}"
        val mergedDescription = listOfNotNull(taskA.description, taskB.description)
            .filter { it.isNotBlank() }
            .joinToString("\n---\n")

        val fixedDate = FixedCalendarHelper.parseDateStr(taskA.associatedDate)
            ?: FixedCalendarHelper.currentFixedDate()
        val startMinTotal = (unionStartMs / 60_000L) % 1440L
        val endMinTotal = (unionEndMs / 60_000L) % 1440L

        val startTimeStr = "%02d:%02d".format((startMinTotal / 60).toInt(), (startMinTotal % 60).toInt())
        val endTimeStr = "%02d:%02d".format((endMinTotal / 60).toInt(), (endMinTotal % 60).toInt())

        val updatedA = taskA.copy(
            title = mergedTitle,
            description = mergedDescription.ifEmpty { null },
            reminderTime = startTimeStr,
            utcTimestamp = unionStartMs,
            endTime = endTimeStr,
            endUtcTimestamp = unionEndMs
        )

        taskRepository.updateTask(updatedA)
        taskRepository.deleteTask(taskB.id, taskB.recurringParentId)

        val resId = UUID.randomUUID().toString()
        conflictResolutionDao.insertResolution(
            ConflictResolutionEntity(
                id = resId,
                conflictType = conflictType,
                eventAId = eventAId,
                eventBId = eventBId,
                action = ResolutionAction.MERGE.name,
                oldStartA = taskA.utcTimestamp,
                newStartA = unionStartMs,
                oldStartB = taskB.utcTimestamp,
                newStartB = null
            )
        )

        ConflictResolutionResult(
            action = ResolutionAction.MERGE,
            eventAId = eventAId,
            eventBId = eventBId,
            message = "Merged events into '$mergedTitle'",
            previousEventA = previousA,
            previousEventB = previousB
        )
    }

    suspend fun undo(result: ConflictResolutionResult) = withContext(Dispatchers.IO) {
        when (result.action) {
            ResolutionAction.MOVE_A -> {
                result.previousEventA?.let { taskRepository.updateTask(it) }
            }
            ResolutionAction.MOVE_B -> {
                result.previousEventB?.let { taskRepository.updateTask(it) }
            }
            ResolutionAction.DELETE_A -> {
                result.previousEventA?.let { taskRepository.updateTask(it) }
            }
            ResolutionAction.DELETE_B -> {
                result.previousEventB?.let { taskRepository.updateTask(it) }
            }
            ResolutionAction.MERGE -> {
                result.previousEventA?.let { taskRepository.updateTask(it) }
                result.previousEventB?.let { taskRepository.updateTask(it) }
            }
        }
    }
}
