package com.l1khith.calender28.repository

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.utils.FixedDate
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForDateFlow(dateStr: String): Flow<List<AppTask>>
    fun getAllTasksFlow(): Flow<List<AppTask>>
    fun getDatesWithActiveTasksFlow(): Flow<Set<String>>
    fun getTaskCountsPerDateFlow(): Flow<Map<String, Int>>
    fun getRecurringTasksFlow(): Flow<List<RecurringTask>>
    fun getTodayCountFlow(dateStr: String): Flow<Int>
    fun getTodayPendingCountFlow(dateStr: String): Flow<Int>
    suspend fun getTodayCount(dateStr: String): Int
    suspend fun getTodayPendingCount(dateStr: String): Int

    suspend fun getTasksForDate(dateStr: String): List<AppTask>
    fun getTasksSpanningDateFlow(dateStr: String): Flow<List<AppTask>>
    suspend fun getTasksSpanningDate(dateStr: String): List<AppTask>
    suspend fun getTasksOverlapping(startDate: String, endDate: String): List<AppTask>
    suspend fun getAllDayTasksForDate(dateStr: String): List<AppTask>
    suspend fun getAllTasks(): List<AppTask>
    suspend fun getDatesWithActiveTasks(): Set<String>
    suspend fun getTaskCountsPerDate(): Map<String, Int>
    suspend fun getAllRecurringTasks(): List<RecurringTask>

    suspend fun saveTask(
        id: String?,
        title: String,
        description: String?,
        associatedDate: FixedDate,
        isReminder: Boolean,
        reminderTime: String?,
        priority: Int = 1,
        endDate: String? = null,
        endTime: String? = null,
        isAllDay: Boolean = false,
        reminderOffsetMin: Int? = null
    ): AppTask

    suspend fun importSystemCalendarTasks(tasks: List<AppTask>)

    suspend fun updateTask(task: AppTask)
    suspend fun deleteTask(taskId: String, recurringParentId: String?)
    suspend fun toggleTaskCompletion(task: AppTask): AppTask

    suspend fun saveRecurringTask(
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
    ): RecurringTask

    suspend fun deleteRecurringTask(id: String)
    suspend fun catchUpRollover(targetDate: FixedDate)
}
