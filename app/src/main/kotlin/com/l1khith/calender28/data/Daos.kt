package com.l1khith.calender28.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE associated_date = :dateStr ORDER BY priority DESC, is_reminder DESC, reminder_time ASC")
    suspend fun getTasksForDate(dateStr: String): List<AppTaskEntity>

    @Query("SELECT * FROM tasks WHERE associated_date = :dateStr ORDER BY priority DESC, is_reminder DESC, reminder_time ASC")
    fun observeTasksForDate(dateStr: String): Flow<List<AppTaskEntity>>

    @Query("SELECT * FROM tasks WHERE associated_date = :dateStr ORDER BY priority DESC, is_reminder DESC, reminder_time ASC")
    fun getTasksForDateSync(dateStr: String): List<AppTaskEntity>

    @Query("SELECT * FROM tasks ORDER BY priority DESC, is_reminder DESC, reminder_time ASC")
    suspend fun getAllTasks(): List<AppTaskEntity>

    @Query("SELECT * FROM tasks ORDER BY priority DESC, is_reminder DESC, reminder_time ASC")
    fun observeAllTasks(): Flow<List<AppTaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasksSync(): List<AppTaskEntity>

    @Query("SELECT DISTINCT associated_date FROM tasks WHERE is_completed = 0")
    suspend fun getDatesWithActiveTasks(): List<String>

    @Query("SELECT DISTINCT associated_date FROM tasks WHERE is_completed = 0")
    fun observeDatesWithActiveTasks(): Flow<List<String>>

    @Query("SELECT associated_date, COUNT(*) as count FROM tasks WHERE is_completed = 0 GROUP BY associated_date")
    suspend fun getRawTaskCountsPerDate(): List<DateTaskCount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AppTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<AppTaskEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTaskSync(task: AppTaskEntity): Long

    @Update
    suspend fun updateTask(task: AppTaskEntity): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String): Int

    @Query("DELETE FROM tasks WHERE recurring_parent_id = :parentId AND is_completed = 0")
    suspend fun deleteIncompleteGeneratedTasks(parentId: String): Int

    @Query("DELETE FROM tasks WHERE recurring_parent_id = :parentId OR id LIKE 'gen_' || :parentId || '_%'")
    suspend fun deleteAllGeneratedTasksForParent(parentId: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE associated_date = :dateStr AND recurring_parent_id = :parentId")
    suspend fun countGeneratedInstanceForDate(dateStr: String, parentId: String): Int

    @Query("SELECT LOWER(TRIM(title)) FROM tasks WHERE associated_date = :dateStr AND id LIKE 'sys_%'")
    suspend fun getSysTaskTitlesForDate(dateStr: String): List<String>

    @Query("DELETE FROM tasks WHERE associated_date < :currDate AND is_generated = 1 AND is_completed = 0")
    suspend fun purgePastUncompletedGeneratedTasks(currDate: String): Int

    @Query("DELETE FROM tasks WHERE associated_date < :cutoffDate AND is_completed = 0")
    suspend fun purgeOldUncompletedTasks(cutoffDate: String): Int

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): AppTaskEntity?

    @Query("UPDATE tasks SET last_focused_at = :timestamp, total_focus_time = total_focus_time + :durationSeconds, focus_count = focus_count + 1, last_focus_duration = :durationSeconds, last_focus_mode = :mode WHERE id = :id")
    suspend fun recordTaskFocus(id: String, durationSeconds: Int, mode: String, timestamp: Long): Int
}

data class DateTaskCount(
    val associated_date: String,
    val count: Int
)

@Dao
interface RecurringTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTask(task: RecurringTaskEntity): Long

    @Query("SELECT * FROM recurring_tasks ORDER BY created_at DESC")
    suspend fun getAllRecurringTasks(): List<RecurringTaskEntity>

    @Query("SELECT * FROM recurring_tasks ORDER BY created_at DESC")
    fun observeAllRecurringTasks(): Flow<List<RecurringTaskEntity>>

    @Query("SELECT * FROM recurring_tasks WHERE is_active = 1 AND (end_date IS NULL OR end_date = '' OR end_date >= :currentDateStr)")
    suspend fun getActiveRecurringTasks(currentDateStr: String): List<RecurringTaskEntity>

    @Query("DELETE FROM recurring_tasks WHERE id = :id")
    suspend fun deleteRecurringTask(id: String): Int
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY created_at_ms DESC")
    suspend fun getAllHabits(): List<HabitEntity>

    @Query("SELECT DISTINCT h.* FROM habits h LEFT JOIN habit_entries e ON h.id = e.habit_id ORDER BY h.created_at_ms DESC")
    fun observeAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity): Int

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String): Int
}

@Dao
interface HabitEntryDao {
    @Query("SELECT day_in_cycle, is_completed FROM habit_entries WHERE habit_id = :habitId AND cycle_index = :cycleIndex ORDER BY day_in_cycle ASC")
    suspend fun getHabitEntries(habitId: String, cycleIndex: Long): List<HabitEntryPair>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabitEntry(entry: HabitEntryEntity): Long

    @Query("SELECT COUNT(*) FROM habit_entries WHERE habit_id = :habitId AND cycle_index = :cycleIndex AND is_completed = 1")
    suspend fun getCurrentCycleProgress(habitId: String, cycleIndex: Long): Int
}

data class HabitEntryPair(
    val day_in_cycle: Int,
    val is_completed: Int
)

@Dao
interface ScheduledAlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: ScheduledAlarmEntity): Long

    @Query("DELETE FROM scheduled_alarms WHERE alarm_id = :alarmId")
    suspend fun deleteAlarm(alarmId: Int): Int

    @Query("DELETE FROM scheduled_alarms WHERE item_id = :itemId")
    suspend fun deleteAlarmsForItem(itemId: String): Int

    @Query("SELECT * FROM scheduled_alarms WHERE item_id = :itemId")
    suspend fun getAlarmsForItem(itemId: String): List<ScheduledAlarmEntity>

    @Query("SELECT * FROM scheduled_alarms WHERE scheduled_time_utc > :now")
    suspend fun getActiveAlarms(now: Long = System.currentTimeMillis()): List<ScheduledAlarmEntity>
}

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun observeAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    suspend fun getAllSessions(): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_sessions WHERE taskId = :taskId ORDER BY startedAt DESC")
    fun observeSessionsForTask(taskId: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT SUM(durationSeconds) FROM focus_sessions WHERE completed = 1")
    fun observeTotalFocusSeconds(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1")
    fun observeCompletedSessionCount(): Flow<Int>

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long): Int

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions(): Int
}
