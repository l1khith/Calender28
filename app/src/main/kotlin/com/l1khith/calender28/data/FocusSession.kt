package com.l1khith.calender28.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Domain model representing a Focus session.
 */
@Immutable
data class FocusSession(
    val id: Long = 0,
    val taskId: String,
    val taskTitle: String,
    val mode: String, // "timer" or "stopwatch"
    val durationSeconds: Int,
    val completed: Boolean,
    val startedAt: Long,
    val endedAt: Long,
    val wasCancelled: Boolean = false
) {
    val durationMinutes: Int get() = durationSeconds / 60
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return if (minutes > 0) {
                "${minutes}m ${seconds}s"
            } else {
                "${seconds}s"
            }
        }
}

/**
 * Room Database entity for persisting focus sessions.
 */
@Entity(
    tableName = "focus_sessions",
    indices = [
        Index("taskId"),
        Index("startedAt"),
        Index("completed")
    ]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val taskTitle: String,
    val mode: String,
    val durationSeconds: Int,
    val completed: Boolean,
    val startedAt: Long,
    val endedAt: Long,
    val wasCancelled: Boolean = false
) {
    fun toDomain(): FocusSession = FocusSession(
        id = id,
        taskId = taskId,
        taskTitle = taskTitle,
        mode = mode,
        durationSeconds = durationSeconds,
        completed = completed,
        startedAt = startedAt,
        endedAt = endedAt,
        wasCancelled = wasCancelled
    )

    companion object {
        fun fromDomain(session: FocusSession): FocusSessionEntity = FocusSessionEntity(
            id = session.id,
            taskId = session.taskId,
            taskTitle = session.taskTitle,
            mode = session.mode,
            durationSeconds = session.durationSeconds,
            completed = session.completed,
            startedAt = session.startedAt,
            endedAt = session.endedAt,
            wasCancelled = session.wasCancelled
        )
    }
}
