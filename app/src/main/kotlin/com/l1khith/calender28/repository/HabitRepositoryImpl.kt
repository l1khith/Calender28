package com.l1khith.calender28.repository

import android.content.Context
import android.util.Log
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.HabitEntity
import com.l1khith.calender28.data.HabitEntryEntity
import com.l1khith.calender28.data.RoomTaskDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.HabitCycleEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TAG = "HabitRepoImpl"

class HabitRepositoryImpl(private val context: Context) : HabitRepository {

    private val db = RoomTaskDatabase.getInstance(context)
    private val habitDao = db.habitDao()
    private val habitEntryDao = db.habitEntryDao()

    override fun getAllHabitsFlow(): Flow<List<Habit>> {
        Log.d(TAG, "getAllHabitsFlow: Observing habits flow")
        return habitDao.observeAllHabits().map { entities ->
            Log.d(TAG, "getAllHabitsFlow: Flow emitted ${entities.size} habit entities")
            val todayEpochDay = HabitCycleEngine.currentEpochDay()
            val result = mutableListOf<Habit>()
            for (hEntity in entities) {
                val anchorEpochDay = HabitCycleEngine.getEpochDay(hEntity.created_at_ms)
                val currentPos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)
                val rawEntries = try {
                    habitEntryDao.getHabitEntries(hEntity.id, currentPos.cycleIndex)
                } catch (e: Exception) {
                    Log.e(TAG, "getAllHabitsFlow: Failed fetching entries for habitId=${hEntity.id}", e)
                    emptyList()
                }
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
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getAllHabits(): List<Habit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getAllHabits: Querying all habits from DB")
        val habits = habitDao.getAllHabits()
        Log.d(TAG, "getAllHabits: Query returned ${habits.size} habits")
        val todayEpochDay = HabitCycleEngine.currentEpochDay()

        val result = mutableListOf<Habit>()
        for (hEntity in habits) {
            val anchorEpochDay = HabitCycleEngine.getEpochDay(hEntity.created_at_ms)
            val currentPos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)

            val rawEntries = try {
                habitEntryDao.getHabitEntries(hEntity.id, currentPos.cycleIndex)
            } catch (e: Exception) {
                Log.e(TAG, "getAllHabits: Failed fetching entries for habitId=${hEntity.id}", e)
                emptyList()
            }
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

    override suspend fun saveHabit(
        id: String?,
        name: String,
        category: String,
        reminderTime: String?,
        colorHex: Long
    ): Habit = withContext(Dispatchers.IO) {
        val habitId = id ?: UUID.randomUUID().toString()
        Log.d(TAG, "saveHabit: Saving habitId=$habitId, name=$name, category=$category")
        val habitEntity = HabitEntity(
            id = habitId,
            name = name,
            category = category,
            reminder_time = reminderTime,
            is_paused = 0,
            color_hex = colorHex,
            created_at_ms = System.currentTimeMillis()
        )
        habitDao.insertHabit(habitEntity)

        val savedHabit = Habit(
            id = habitId,
            name = name,
            category = category,
            reminderTime = reminderTime,
            isPaused = false,
            colorHex = colorHex,
            createdAtMs = System.currentTimeMillis()
        )
        if (!reminderTime.isNullOrEmpty()) {
            com.l1khith.calender28.service.AlarmScheduler(context).scheduleHabitReminder(savedHabit)
        }
        savedHabit
    }

    override suspend fun deleteHabit(id: String): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "deleteHabit: Deleting habitId=$id")
        habitDao.deleteHabit(id)
        Unit
    }

    override suspend fun toggleHabitDay(
        habitId: String,
        cycleIndex: Long,
        dayInCycle: Int,
        currentCompletedState: Boolean
    ): Unit = withContext(Dispatchers.IO) {
        val newState = if (!currentCompletedState) 1 else 0
        Log.d(TAG, "toggleHabitDay: habitId=$habitId, cycleIndex=$cycleIndex, dayInCycle=$dayInCycle, newState=$newState")
        habitEntryDao.upsertHabitEntry(
            HabitEntryEntity(
                id = "${habitId}_${cycleIndex}_$dayInCycle",
                habit_id = habitId,
                cycle_index = cycleIndex,
                day_in_cycle = dayInCycle,
                epoch_day = 0L,
                is_completed = newState
            )
        )
        Unit
    }

    override suspend fun getCurrentCycleProgress(habitId: String, cycleIndex: Long): Int = withContext(Dispatchers.IO) {
        try {
            habitEntryDao.getCurrentCycleProgress(habitId, cycleIndex)
        } catch (e: Exception) {
            0
        }
    }
}
