package com.l1khith.calender28.test

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.utils.FixedDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTaskRepository(
    val tasks: MutableList<AppTask> = mutableListOf()
) : TaskRepository {

    override fun getTasksForDateFlow(dateStr: String): Flow<List<AppTask>> =
        flowOf(tasks.filter { it.associatedDate == dateStr })

    override fun getAllTasksFlow(): Flow<List<AppTask>> = flowOf(tasks)

    override fun getDatesWithActiveTasksFlow(): Flow<Set<String>> =
        flowOf(tasks.map { it.associatedDate }.toSet())

    override fun getTaskCountsPerDateFlow(): Flow<Map<String, Int>> =
        flowOf(tasks.groupBy { it.associatedDate }.mapValues { it.value.size })

    override fun getRecurringTasksFlow(): Flow<List<RecurringTask>> = flowOf(emptyList())

    override suspend fun getTasksForDate(dateStr: String): List<AppTask> =
        tasks.filter { it.associatedDate == dateStr }

    override fun getTasksSpanningDateFlow(dateStr: String): Flow<List<AppTask>> =
        flowOf(tasks.filter { task ->
            val start = task.associatedDate
            val end = task.endDate ?: task.associatedDate
            start <= dateStr && dateStr <= end
        })

    override suspend fun getTasksSpanningDate(dateStr: String): List<AppTask> {
        return tasks.filter { task ->
            val start = task.associatedDate
            val end = task.endDate ?: task.associatedDate
            start <= dateStr && dateStr <= end
        }
    }

    override suspend fun getTasksOverlapping(startDate: String, endDate: String): List<AppTask> {
        return tasks.filter { task ->
            val taskStart = task.associatedDate
            val taskEnd = task.endDate ?: task.associatedDate
            taskStart <= endDate && startDate <= taskEnd
        }
    }

    override suspend fun getAllDayTasksForDate(dateStr: String): List<AppTask> {
        return tasks.filter { it.associatedDate == dateStr && it.allDay }
    }

    override suspend fun getAllTasks(): List<AppTask> = tasks

    override suspend fun getDatesWithActiveTasks(): Set<String> =
        tasks.map { it.associatedDate }.toSet()

    override suspend fun getTaskCountsPerDate(): Map<String, Int> =
        tasks.groupBy { it.associatedDate }.mapValues { it.value.size }

    override suspend fun getAllRecurringTasks(): List<RecurringTask> = emptyList()

    override suspend fun saveTask(
        id: String?,
        title: String,
        description: String?,
        associatedDate: FixedDate,
        isReminder: Boolean,
        reminderTime: String?,
        priority: Int,
        endDate: String?,
        endTime: String?,
        isAllDay: Boolean,
        reminderOffsetMin: Int?,
        reminderOffsets: List<Int>
    ): AppTask {
        val newTask = AppTask(
            id = id ?: java.util.UUID.randomUUID().toString(),
            title = title,
            description = description,
            associatedDate = associatedDate.toString(),
            isReminder = if (isReminder) 1 else 0,
            reminderTime = reminderTime,
            priority = priority,
            endDate = endDate,
            endTime = endTime,
            isAllDay = if (isAllDay) 1 else 0,
            reminderOffsetMin = reminderOffsetMin,
            reminderOffsets = reminderOffsets
        )
        tasks.add(newTask)
        return newTask
    }

    override suspend fun importSystemCalendarTasks(tasks: List<AppTask>) {
        this.tasks.addAll(tasks)
    }

    override suspend fun updateTask(task: AppTask) {
        val idx = tasks.indexOfFirst { it.id == task.id }
        if (idx >= 0) tasks[idx] = task else tasks.add(task)
    }

    override suspend fun deleteTask(taskId: String, recurringParentId: String?) {
        tasks.removeAll { it.id == taskId }
    }

    override suspend fun toggleTaskCompletion(task: AppTask): AppTask {
        val updated = task.copy(isCompleted = if (task.completed) 0 else 1)
        updateTask(updated)
        return updated
    }

    override suspend fun saveRecurringTask(
        id: String?,
        title: String,
        description: String?,
        recurrenceType: RecurrenceType,
        recurrenceDays: List<Int>,
        recurrenceInterval: Int,
        priority: Int,
        isActive: Boolean,
        endDate: String?,
        reminderTime: String?,
        targetDate: FixedDate
    ): RecurringTask {
        throw UnsupportedOperationException("Not needed in conflict tests")
    }

    override suspend fun deleteRecurringTask(id: String) {}

    override suspend fun catchUpRollover(targetDate: FixedDate) {}

    override fun getTodayCountFlow(dateStr: String): Flow<Int> =
        flowOf(tasks.count { task ->
            val start = task.associatedDate
            val end = task.endDate ?: task.associatedDate
            start <= dateStr && dateStr <= end
        })

    override fun getTodayPendingCountFlow(dateStr: String): Flow<Int> =
        flowOf(tasks.count { task ->
            val start = task.associatedDate
            val end = task.endDate ?: task.associatedDate
            start <= dateStr && dateStr <= end && !task.completed
        })

    override suspend fun getTodayCount(dateStr: String): Int =
        tasks.count { task ->
            val start = task.associatedDate
            val end = task.endDate ?: task.associatedDate
            start <= dateStr && dateStr <= end
        }

    override suspend fun getTodayPendingCount(dateStr: String): Int =
        tasks.count { task ->
            val start = task.associatedDate
            val end = task.endDate ?: task.associatedDate
            start <= dateStr && dateStr <= end && !task.completed
        }
}
