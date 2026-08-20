package com.l1khith.calender28.repository

import com.l1khith.calender28.data.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    suspend fun saveFocusSession(session: FocusSession): Long
    suspend fun recordTaskFocus(taskId: String, durationSeconds: Int, mode: String, timestamp: Long = System.currentTimeMillis())
    fun getAllFocusSessionsFlow(): Flow<List<FocusSession>>
    fun getFocusSessionsForTaskFlow(taskId: String): Flow<List<FocusSession>>
    fun getTotalFocusSecondsFlow(): Flow<Long>
    fun getCompletedSessionCountFlow(): Flow<Int>
    suspend fun deleteFocusSession(sessionId: Long): Int
    suspend fun clearAllFocusSessions(): Int
}
