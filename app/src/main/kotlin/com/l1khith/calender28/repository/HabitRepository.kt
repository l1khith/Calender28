package com.l1khith.calender28.repository

import com.l1khith.calender28.data.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabitsFlow(): Flow<List<Habit>>
    suspend fun getAllHabits(): List<Habit>
    suspend fun saveHabit(
        id: String?,
        name: String,
        category: String,
        reminderTime: String?,
        colorHex: Long
    ): Habit
    suspend fun deleteHabit(id: String)
    suspend fun toggleHabitDay(habitId: String, cycleIndex: Long, dayInCycle: Int, currentCompletedState: Boolean)
    suspend fun getCurrentCycleProgress(habitId: String, cycleIndex: Long): Int
}
