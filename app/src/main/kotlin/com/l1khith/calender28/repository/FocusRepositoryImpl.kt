package com.l1khith.calender28.repository

import android.content.Context
import android.util.Log
import com.l1khith.calender28.data.FocusSession
import com.l1khith.calender28.data.FocusSessionEntity
import com.l1khith.calender28.data.RoomTaskDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val TAG = "FocusRepoImpl"

class FocusRepositoryImpl(
    private val focusSessionDao: com.l1khith.calender28.data.FocusSessionDao,
    private val taskDao: com.l1khith.calender28.data.TaskDao
) : FocusRepository {

    constructor(context: Context) : this(
        focusSessionDao = RoomTaskDatabase.getInstance(context).focusSessionDao(),
        taskDao = RoomTaskDatabase.getInstance(context).taskDao()
    )

    override suspend fun saveFocusSession(session: FocusSession): Long = withContext(Dispatchers.IO) {
        Log.d(TAG, "saveFocusSession: Saving session for task=${session.taskTitle}, duration=${session.durationSeconds}s, mode=${session.mode}")
        val entity = FocusSessionEntity.fromDomain(session)
        val id = focusSessionDao.insertSession(entity)
        if (session.completed && session.taskId.isNotBlank()) {
            taskDao.recordTaskFocus(
                id = session.taskId,
                durationSeconds = session.durationSeconds,
                mode = session.mode,
                timestamp = session.endedAt
            )
        }
        id
    }

    override suspend fun recordTaskFocus(
        taskId: String,
        durationSeconds: Int,
        mode: String,
        timestamp: Long
    ) = withContext(Dispatchers.IO) {
        if (taskId.isNotBlank()) {
            taskDao.recordTaskFocus(
                id = taskId,
                durationSeconds = durationSeconds,
                mode = mode,
                timestamp = timestamp
            )
        }
    }

    override fun getAllFocusSessionsFlow(): Flow<List<FocusSession>> {
        return focusSessionDao.observeAllSessions().map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getFocusSessionsForTaskFlow(taskId: String): Flow<List<FocusSession>> {
        return focusSessionDao.observeSessionsForTask(taskId).map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getTotalFocusSecondsFlow(): Flow<Long> {
        return focusSessionDao.observeTotalFocusSeconds().map { it ?: 0L }.flowOn(Dispatchers.IO)
    }

    override fun getCompletedSessionCountFlow(): Flow<Int> {
        return focusSessionDao.observeCompletedSessionCount().flowOn(Dispatchers.IO)
    }

    override suspend fun getSessionsInRange(startMs: Long, endMs: Long): List<FocusSession> = withContext(Dispatchers.IO) {
        focusSessionDao.getSessionsInRange(startMs, endMs).map { it.toDomain() }
    }

    override suspend fun deleteFocusSession(sessionId: Long): Int = withContext(Dispatchers.IO) {
        focusSessionDao.deleteSession(sessionId)
    }

    override suspend fun clearAllFocusSessions(): Int = withContext(Dispatchers.IO) {
        focusSessionDao.clearAllSessions()
    }
}
