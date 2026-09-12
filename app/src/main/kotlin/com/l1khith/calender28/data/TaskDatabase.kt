package com.l1khith.calender28.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.HabitCycleEngine
import com.l1khith.calender28.utils.currentTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_recurring_parent_id` ON `tasks` (`recurring_parent_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_alarms_item_id` ON `scheduled_alarms` (`item_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_alarms_scheduled_time_utc` ON `scheduled_alarms` (`scheduled_time_utc`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coin_transactions_timestamp` ON `coin_transactions` (`timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_coin_transactions_reason_note` ON `coin_transactions` (`reason`, `note`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_linkedType_linkedId` ON `notes` (`linkedType`, `linkedId`)")
    }
}

@Database(
    entities = [
        AppTaskEntity::class,
        RecurringTaskEntity::class,
        HabitEntity::class,
        HabitEntryEntity::class,
        ScheduledAlarmEntity::class,
        FocusSessionEntity::class,
        CoinBalanceEntity::class,
        CoinTransactionEntity::class,
        SparkyEntity::class,
        NoteEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class RoomTaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun recurringTaskDao(): RecurringTaskDao
    abstract fun habitDao(): HabitDao
    abstract fun habitEntryDao(): HabitEntryDao
    abstract fun scheduledAlarmDao(): ScheduledAlarmDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun coinDao(): CoinDao
    abstract fun sparkyDao(): SparkyDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var instance: RoomTaskDatabase? = null

        fun getInstance(context: Context): RoomTaskDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RoomTaskDatabase::class.java,
                    "calender28_room.db"
                )
                .addMigrations(MIGRATION_8_9)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
            }
    }
}

class TaskDatabase(private val context: Context) {

    private val db: RoomTaskDatabase by lazy {
        RoomTaskDatabase.getInstance(context)
    }

    fun getScheduledAlarmDao(): ScheduledAlarmDao = db.scheduledAlarmDao()

    private fun notifyWidgetUpdate() {
        try {
            com.l1khith.calender28.widget.WidgetUpdater.updateWidget(context)
        } catch (_: Exception) {}
    }

    suspend fun insertTask(task: AppTask): Boolean = withContext(Dispatchers.IO) {
        if (task.id.startsWith("sys_")) {
            val sysTitles = db.taskDao().getSysTaskTitlesForDate(task.associatedDate)
            if (sysTitles.contains(task.title.lowercase().trim())) {
                return@withContext false
            }
        }
        val result = db.taskDao().insertTask(AppTaskEntity.fromAppTask(task))
        if (result != -1L) {
            notifyWidgetUpdate()
            true
        } else false
    }

    suspend fun updateTask(task: AppTask): Boolean = withContext(Dispatchers.IO) {
        val count = db.taskDao().updateTask(AppTaskEntity.fromAppTask(task))
        if (count > 0) {
            notifyWidgetUpdate()
            true
        } else false
    }

    suspend fun deleteTask(id: String): Boolean = withContext(Dispatchers.IO) {
        val count = db.taskDao().deleteTask(id)
        if (count > 0) {
            notifyWidgetUpdate()
            true
        } else false
    }

    suspend fun getTaskById(id: String): AppTask? = withContext(Dispatchers.IO) {
        db.taskDao().getTaskById(id)?.toAppTask()
    }

    suspend fun getTasksForDate(dateStr: String): List<AppTask> = withContext(Dispatchers.IO) {
        db.taskDao().getTasksForDate(dateStr).map { it.toAppTask() }
    }

    suspend fun getAllTasks(): List<AppTask> = withContext(Dispatchers.IO) {
        db.taskDao().getAllTasks().map { it.toAppTask() }
    }

    suspend fun getDatesWithActiveTasks(): Set<String> = withContext(Dispatchers.IO) {
        db.taskDao().getDatesWithActiveTasks().toSet()
    }

    suspend fun getTaskCountsPerDate(): Map<String, Int> = withContext(Dispatchers.IO) {
        val rawCounts = db.taskDao().getRawTaskCountsPerDate()
        val map = mutableMapOf<String, Int>()
        for (rc in rawCounts) {
            map[rc.associated_date] = rc.count
        }
        map
    }

    // --- Recurring Tasks ---

    suspend fun insertRecurringTask(task: RecurringTask): Boolean = withContext(Dispatchers.IO) {
        val result = db.recurringTaskDao().insertRecurringTask(RecurringTaskEntity.fromRecurringTask(task))
        result != -1L
    }

    suspend fun getAllRecurringTasks(): List<RecurringTask> = withContext(Dispatchers.IO) {
        db.recurringTaskDao().getAllRecurringTasks().map { it.toRecurringTask() }
    }

    suspend fun getActiveRecurringTasks(currentDateStr: String): List<RecurringTask> = withContext(Dispatchers.IO) {
        db.recurringTaskDao().getActiveRecurringTasks(currentDateStr).map { it.toRecurringTask() }
    }

    suspend fun deleteRecurringTask(id: String): Boolean = withContext(Dispatchers.IO) {
        val count = db.recurringTaskDao().deleteRecurringTask(id)
        count > 0
    }

    suspend fun insertGeneratedTask(task: AppTask): Boolean = withContext(Dispatchers.IO) {
        val result = db.taskDao().insertTask(AppTaskEntity.fromAppTask(task))
        if (result != -1L) {
            notifyWidgetUpdate()
            true
        } else false
    }

    suspend fun hasGeneratedInstanceForDate(dateStr: String, recurringParentId: String): Boolean = withContext(Dispatchers.IO) {
        db.taskDao().countGeneratedInstanceForDate(dateStr, recurringParentId) > 0
    }

    suspend fun deleteIncompleteGeneratedTasks(recurringParentId: String): Boolean = withContext(Dispatchers.IO) {
        db.taskDao().deleteIncompleteGeneratedTasks(recurringParentId) > 0
    }

    suspend fun catchUpRollover(targetDateStr: String) = withContext(Dispatchers.IO) {
        db.taskDao().purgePastUncompletedGeneratedTasks(targetDateStr)
        val targetFixedDate = FixedCalendarHelper.parseDateStr(targetDateStr) ?: return@withContext
        val oldCutoff = FixedCalendarHelper.addMonths(targetFixedDate, -3).toString()
        db.taskDao().purgeOldUncompletedTasks(oldCutoff)
    }

    // --- Habit Operations (28-day Habit Cycles) ---

    suspend fun insertHabit(habit: Habit): Boolean = withContext(Dispatchers.IO) {
        val result = db.habitDao().insertHabit(HabitEntity.fromHabit(habit))
        result != -1L
    }

    suspend fun updateHabit(habit: Habit): Boolean = withContext(Dispatchers.IO) {
        val count = db.habitDao().updateHabit(HabitEntity.fromHabit(habit))
        count > 0
    }

    suspend fun deleteHabit(id: String): Boolean = withContext(Dispatchers.IO) {
        val count = db.habitDao().deleteHabit(id)
        count > 0
    }

    suspend fun getAllHabits(): List<Habit> = withContext(Dispatchers.IO) {
        val habits = db.habitDao().getAllHabits()
        val todayEpochDay = HabitCycleEngine.currentEpochDay()

        val result = mutableListOf<Habit>()
        for (hEntity in habits) {
            val anchorEpochDay = HabitCycleEngine.getEpochDay(hEntity.created_at_ms)
            val currentPos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)

            val rawEntries = db.habitEntryDao().getHabitEntries(hEntity.id, currentPos.cycleIndex)
            val completedDaysSet = rawEntries.filter { it.is_completed == 1 }.map { it.day_in_cycle }.toSet()

            result.add(
                Habit(
                    id = hEntity.id,
                    name = hEntity.name,
                    category = hEntity.category,
                    reminderTime = hEntity.reminder_time,
                    isPaused = hEntity.is_paused == 1,
                    colorHex = hEntity.color_hex,
                    completedDays = completedDaysSet,
                    createdAtMs = hEntity.created_at_ms
                )
            )
        }
        result
    }

    suspend fun upsertHabitEntry(habitId: String, cycleIndex: Long, dayInCycle: Int, isCompleted: Boolean): Boolean = withContext(Dispatchers.IO) {
        val result = db.habitEntryDao().upsertHabitEntry(
            HabitEntryEntity(
                id = "${habitId}_${cycleIndex}_$dayInCycle",
                habit_id = habitId,
                cycle_index = cycleIndex,
                day_in_cycle = dayInCycle,
                epoch_day = 0L,
                is_completed = if (isCompleted) 1 else 0
            )
        )
        result != -1L
    }

    suspend fun getCurrentCycleProgress(habitId: String, cycleIndex: Long): Int = withContext(Dispatchers.IO) {
        db.habitEntryDao().getCurrentCycleProgress(habitId, cycleIndex)
    }

    companion object {
        fun getInstance(context: Context): RoomTaskDatabase = RoomTaskDatabase.getInstance(context)
    }
}
