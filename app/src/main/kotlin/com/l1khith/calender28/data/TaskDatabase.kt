package com.l1khith.calender28.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.HabitCycleEngine
import com.l1khith.calender28.utils.currentTimeMillis
import kotlinx.coroutines.runBlocking

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
    version = 8,
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

    fun insertTask(task: AppTask): Boolean = runBlocking {
        if (task.id.startsWith("sys_")) {
            val sysTitles = db.taskDao().getSysTaskTitlesForDate(task.associatedDate)
            if (sysTitles.contains(task.title.lowercase().trim())) {
                return@runBlocking false
            }
        }
        val result = db.taskDao().insertTask(AppTaskEntity.fromAppTask(task))
        if (result != -1L) {
            notifyWidgetUpdate()
            true
        } else false
    }

    fun updateTask(task: AppTask): Boolean = runBlocking {
        val count = db.taskDao().updateTask(AppTaskEntity.fromAppTask(task))
        if (count > 0) {
            notifyWidgetUpdate()
            true
        } else false
    }

    fun deleteTask(id: String): Boolean = runBlocking {
        val count = db.taskDao().deleteTask(id)
        if (count > 0) {
            notifyWidgetUpdate()
            true
        } else false
    }

    fun getTasksForDate(dateStr: String): List<AppTask> = runBlocking {
        db.taskDao().getTasksForDate(dateStr).map { it.toAppTask() }
    }

    fun getAllTasks(): List<AppTask> = runBlocking {
        db.taskDao().getAllTasks().map { it.toAppTask() }
    }

    fun getDatesWithActiveTasks(): Set<String> = runBlocking {
        db.taskDao().getDatesWithActiveTasks().toSet()
    }

    fun getTaskCountsPerDate(): Map<String, Int> = runBlocking {
        val rawCounts = db.taskDao().getRawTaskCountsPerDate()
        val map = mutableMapOf<String, Int>()
        for (rc in rawCounts) {
            map[rc.associated_date] = rc.count
        }
        map
    }

    // --- Recurring Tasks ---

    fun insertRecurringTask(task: RecurringTask): Boolean = runBlocking {
        val result = db.recurringTaskDao().insertRecurringTask(RecurringTaskEntity.fromRecurringTask(task))
        result != -1L
    }

    fun getAllRecurringTasks(): List<RecurringTask> = runBlocking {
        db.recurringTaskDao().getAllRecurringTasks().map { it.toRecurringTask() }
    }

    fun getActiveRecurringTasks(currentDateStr: String): List<RecurringTask> = runBlocking {
        db.recurringTaskDao().getActiveRecurringTasks(currentDateStr).map { it.toRecurringTask() }
    }

    fun deleteRecurringTask(id: String): Boolean = runBlocking {
        val count = db.recurringTaskDao().deleteRecurringTask(id)
        count > 0
    }

    fun insertGeneratedTask(task: AppTask): Boolean = runBlocking {
        val result = db.taskDao().insertTask(AppTaskEntity.fromAppTask(task))
        if (result != -1L) {
            notifyWidgetUpdate()
            true
        } else false
    }

    fun hasGeneratedInstanceForDate(dateStr: String, recurringParentId: String): Boolean = runBlocking {
        db.taskDao().countGeneratedInstanceForDate(dateStr, recurringParentId) > 0
    }

    fun deleteIncompleteGeneratedTasks(recurringParentId: String): Boolean = runBlocking {
        db.taskDao().deleteIncompleteGeneratedTasks(recurringParentId) > 0
    }

    fun catchUpRollover(targetDateStr: String) = runBlocking {
        db.taskDao().purgePastUncompletedGeneratedTasks(targetDateStr)
        val targetFixedDate = FixedCalendarHelper.parseDateStr(targetDateStr) ?: return@runBlocking
        val oldCutoff = FixedCalendarHelper.addMonths(targetFixedDate, -3).toString()
        db.taskDao().purgeOldUncompletedTasks(oldCutoff)
    }

    // --- Habit Operations (28-day Habit Cycles) ---

    fun insertHabit(habit: Habit): Boolean = runBlocking {
        val result = db.habitDao().insertHabit(HabitEntity.fromHabit(habit))
        result != -1L
    }

    fun updateHabit(habit: Habit): Boolean = runBlocking {
        val count = db.habitDao().updateHabit(HabitEntity.fromHabit(habit))
        count > 0
    }

    fun deleteHabit(id: String): Boolean = runBlocking {
        val count = db.habitDao().deleteHabit(id)
        count > 0
    }

    fun getAllHabits(): List<Habit> = runBlocking {
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

    fun upsertHabitEntry(habitId: String, cycleIndex: Long, dayInCycle: Int, isCompleted: Boolean): Boolean = runBlocking {
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

    fun getCurrentCycleProgress(habitId: String, cycleIndex: Long): Int = runBlocking {
        db.habitEntryDao().getCurrentCycleProgress(habitId, cycleIndex)
    }

    companion object {
        fun getInstance(context: Context): RoomTaskDatabase = RoomTaskDatabase.getInstance(context)
    }
}
