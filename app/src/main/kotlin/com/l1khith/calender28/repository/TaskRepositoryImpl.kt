package com.l1khith.calender28.repository

import android.content.Context
import android.util.Log
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.AppTaskEntity
import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.data.RecurringTaskEntity
import com.l1khith.calender28.data.RoomTaskDatabase
import com.l1khith.calender28.data.toEntity
import com.l1khith.calender28.service.AlarmScheduler as ServiceAlarmScheduler
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TAG = "TaskRepoImpl"

class TaskRepositoryImpl(
    private val taskDao: com.l1khith.calender28.data.TaskDao,
    private val recurringTaskDao: com.l1khith.calender28.data.RecurringTaskDao,
    private val serviceAlarmScheduler: ServiceAlarmScheduler
) : TaskRepository {

    constructor(context: Context) : this(
        taskDao = RoomTaskDatabase.getInstance(context).taskDao(),
        recurringTaskDao = RoomTaskDatabase.getInstance(context).recurringTaskDao(),
        serviceAlarmScheduler = ServiceAlarmScheduler(context)
    )

    override fun getTasksForDateFlow(dateStr: String): Flow<List<AppTask>> {
        Log.d(TAG, "getTasksForDateFlow: Observing tasks for date=$dateStr")
        return taskDao.observeTasksForDate(dateStr).map { list: List<AppTaskEntity> ->
            list.map { it.toAppTask() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getAllTasksFlow(): Flow<List<AppTask>> {
        Log.d(TAG, "getAllTasksFlow: Observing all tasks")
        return taskDao.observeAllTasks().map { list: List<AppTaskEntity> ->
            list.map { it.toAppTask() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getDatesWithActiveTasksFlow(): Flow<Set<String>> {
        Log.d(TAG, "getDatesWithActiveTasksFlow: Observing dates with active tasks")
        return taskDao.observeDatesWithActiveTasks().map { list: List<String> ->
            list.toSet()
        }.flowOn(Dispatchers.IO)
    }

    override fun getTaskCountsPerDateFlow(): Flow<Map<String, Int>> {
        Log.d(TAG, "getTaskCountsPerDateFlow: Observing task counts per date")
        return taskDao.observeAllTasks().map { list: List<AppTaskEntity> ->
            val map = mutableMapOf<String, Int>()
            for (item in list) {
                if (item.is_completed == 0) {
                    val count = map.getOrDefault(item.associated_date, 0)
                    map[item.associated_date] = count + 1
                }
            }
            map
        }.flowOn(Dispatchers.IO)
    }

    override fun getRecurringTasksFlow(): Flow<List<RecurringTask>> {
        Log.d(TAG, "getRecurringTasksFlow: Observing recurring tasks flow")
        return recurringTaskDao.observeAllRecurringTasks().map { list: List<RecurringTaskEntity> ->
            list.map { it.toRecurringTask() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getTasksForDate(dateStr: String): List<AppTask> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getTasksForDate: Querying tasks for date=$dateStr")
        val list: List<AppTaskEntity> = taskDao.getTasksForDate(dateStr)
        list.map { it.toAppTask() }
    }

    override suspend fun getAllTasks(): List<AppTask> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getAllTasks: Querying all tasks")
        val list: List<AppTaskEntity> = taskDao.getAllTasks()
        list.map { it.toAppTask() }
    }

    override suspend fun getDatesWithActiveTasks(): Set<String> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getDatesWithActiveTasks: Querying dates with active tasks")
        val list: List<String> = taskDao.getDatesWithActiveTasks()
        list.toSet()
    }

    override suspend fun getTaskCountsPerDate(): Map<String, Int> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getTaskCountsPerDate: Querying task counts per date")
        val list: List<AppTaskEntity> = taskDao.getAllTasks()
        val map = mutableMapOf<String, Int>()
        for (item in list) {
            if (item.is_completed == 0) {
                val count = map.getOrDefault(item.associated_date, 0)
                map[item.associated_date] = count + 1
            }
        }
        map
    }

    override suspend fun getAllRecurringTasks(): List<RecurringTask> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getAllRecurringTasks: Querying all recurring tasks")
        val list: List<RecurringTaskEntity> = recurringTaskDao.getAllRecurringTasks()
        list.map { it.toRecurringTask() }
    }

    override suspend fun saveTask(
        id: String?,
        title: String,
        description: String?,
        associatedDate: FixedDate,
        isReminder: Boolean,
        reminderTime: String?,
        priority: Int
    ): AppTask = withContext(Dispatchers.IO) {
        Log.d(TAG, "saveTask: Saving task id=$id, title=$title, date=$associatedDate, isReminder=$isReminder")
        val isRem = if (isReminder) 1 else 0
        val utcTimestamp = if (isReminder && reminderTime != null) {
            FixedCalendarHelper.toTimestamp(associatedDate, reminderTime)
        } else null

        val task = AppTask(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            description = description?.ifEmpty { null },
            associatedDate = associatedDate.toString(),
            isReminder = isRem,
            reminderTime = if (isReminder) reminderTime else null,
            utcTimestamp = utcTimestamp,
            isCompleted = 0,
            priority = priority,
            recurringParentId = null,
            isGenerated = 0
        )

        if (id != null) {
            serviceAlarmScheduler.cancelTaskAlarm(task)
        }
        taskDao.insertTask(task.toEntity())

        if (isReminder) {
            serviceAlarmScheduler.scheduleTaskReminder(task)
        }

        task
    }

    override suspend fun importSystemCalendarTasks(tasks: List<AppTask>): Unit = withContext(Dispatchers.IO) {
        if (tasks.isEmpty()) return@withContext
        Log.d(TAG, "importSystemCalendarTasks: Batch inserting ${tasks.size} system calendar events")
        val entities = tasks.map { it.toEntity() }
        taskDao.insertTasks(entities)
    }

    override suspend fun updateTask(task: AppTask): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "updateTask: Updating taskId=${task.id}")
        taskDao.updateTask(task.toEntity())
        Unit
    }

    override suspend fun deleteTask(taskId: String, recurringParentId: String?): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "deleteTask: Deleting taskId=$taskId, recurringParentId=$recurringParentId")
        val actualParentId = recurringParentId ?: if (taskId.startsWith("gen_")) {
            val parts = taskId.split("_")
            if (parts.size >= 3) parts[1] else null
        } else null

        if (actualParentId != null) {
            deleteRecurringTask(actualParentId)
        } else {
            val list: List<AppTaskEntity> = taskDao.getAllTasks()
            for (entity in list) {
                if (entity.id == taskId) {
                    serviceAlarmScheduler.cancelTaskAlarm(entity.toAppTask())
                    break
                }
            }
            taskDao.deleteTask(taskId)
        }
        Unit
    }

    override suspend fun toggleTaskCompletion(task: AppTask): AppTask = withContext(Dispatchers.IO) {
        val newStatus = if (task.completed) 0 else 1
        Log.d(TAG, "toggleTaskCompletion: Toggling taskId=${task.id} to isCompleted=$newStatus")
        val updatedTask = task.copy(isCompleted = newStatus)
        taskDao.updateTask(updatedTask.toEntity())

        if (updatedTask.completed) {
            serviceAlarmScheduler.cancelTaskAlarm(updatedTask)
        } else if (updatedTask.reminder) {
            serviceAlarmScheduler.scheduleTaskReminder(updatedTask)
        }

        updatedTask
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
    ): RecurringTask = withContext(Dispatchers.IO) {
        val recurringId = if (!id.isNullOrEmpty() && id.startsWith("gen_")) {
            val parts = id.split("_")
            if (parts.size >= 3) parts[1] else id
        } else {
            id ?: UUID.randomUUID().toString()
        }
        Log.d(TAG, "saveRecurringTask: recurringId=$recurringId, title=$title, isActive=$isActive")

        val targetDateStr = targetDate.toString()

        val existingRecurring = recurringTaskDao.getAllRecurringTasks().find { it.id == recurringId }
        val createdAtTime = existingRecurring?.created_at ?: System.currentTimeMillis()

        if (existingRecurring != null || id != null) {
            val list: List<AppTaskEntity> = taskDao.getAllTasks()
            for (entity in list) {
                if (entity.recurring_parent_id == recurringId && entity.is_completed == 0) {
                    serviceAlarmScheduler.cancelTaskAlarm(entity.toAppTask())
                }
            }
            taskDao.deleteIncompleteGeneratedTasks(recurringId)
        }

        val recurringTask = RecurringTask(
            id = recurringId,
            title = title,
            description = description?.ifEmpty { null },
            recurrenceType = recurrenceType,
            recurrenceDays = recurrenceDays,
            recurrenceInterval = recurrenceInterval,
            priority = priority,
            isActive = isActive,
            createdAt = createdAtTime,
            endDate = endDate?.ifEmpty { null },
            reminderTime = reminderTime
        )

        recurringTaskDao.insertRecurringTask(recurringTask.toEntity())
        // No scheduleRecurringTask() — recurring tasks work via generated instances

        if (isActive && FixedCalendarHelper.shouldGenerateInstance(recurringTask, targetDate)) {
            val isRem = if (reminderTime != null) 1 else 0
            val utcTs = if (reminderTime != null) {
                FixedCalendarHelper.toTimestamp(targetDate, reminderTime)
            } else null

            val genTask = AppTask(
                id = "gen_${recurringId}_$targetDateStr",
                title = title,
                description = description?.ifEmpty { null },
                associatedDate = targetDateStr,
                isReminder = isRem,
                reminderTime = reminderTime,
                utcTimestamp = utcTs,
                isCompleted = 0,
                priority = priority,
                recurringParentId = recurringId,
                isGenerated = 1
            )
            taskDao.insertTask(genTask.toEntity())
            if (isRem == 1) {
                serviceAlarmScheduler.scheduleTaskReminder(genTask)
                Log.d(TAG, "saveRecurringTask: Scheduled alarm for generated task ${genTask.id}")
            }
        }

        recurringTask
    }

    override suspend fun deleteRecurringTask(id: String): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "deleteRecurringTask: Deleting recurring taskId=$id")
        val list: List<AppTaskEntity> = taskDao.getAllTasks()
        for (entity in list) {
            if (entity.recurring_parent_id == id || entity.id.startsWith("gen_${id}_")) {
                serviceAlarmScheduler.cancelTaskAlarm(entity.toAppTask())
            }
        }
        taskDao.deleteAllGeneratedTasksForParent(id)
        recurringTaskDao.deleteRecurringTask(id)
        Unit
    }

    override suspend fun catchUpRollover(targetDate: FixedDate) = withContext(Dispatchers.IO) {
        val dateStr = targetDate.toString()
        Log.d(TAG, "catchUpRollover: Running catch-up rollover for date=$dateStr")
        val activeRecurring: List<RecurringTaskEntity> = recurringTaskDao.getActiveRecurringTasks(dateStr)
        for (entity in activeRecurring) {
            val rec = entity.toRecurringTask()
            if (FixedCalendarHelper.shouldGenerateInstance(rec, targetDate)) {
                val hasGen = taskDao.countGeneratedInstanceForDate(dateStr, rec.id) > 0
                if (!hasGen) {
                    val isRem = if (rec.reminderTime != null) 1 else 0
                    val utcTs = if (rec.reminderTime != null) {
                        FixedCalendarHelper.toTimestamp(targetDate, rec.reminderTime)
                    } else null

                    val genTask = AppTask(
                        id = "gen_${rec.id}_$dateStr",
                        title = rec.title,
                        description = rec.description,
                        associatedDate = dateStr,
                        isReminder = isRem,
                        reminderTime = rec.reminderTime,
                        utcTimestamp = utcTs,
                        isCompleted = 0,
                        priority = rec.priority,
                        recurringParentId = rec.id,
                        isGenerated = 1
                    )
                    taskDao.insertTask(genTask.toEntity())
                    if (isRem == 1) {
                        serviceAlarmScheduler.scheduleTaskReminder(genTask)
                        Log.d(TAG, "catchUpRollover: Scheduled alarm for generated task ${genTask.id}")
                    }
                }
            }
        }
    }
}
