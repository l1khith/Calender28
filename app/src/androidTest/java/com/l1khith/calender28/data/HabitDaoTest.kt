package com.l1khith.calender28.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.l1khith.calender28.test.TestDataFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitDaoTest {

    private lateinit var database: RoomTaskDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var habitEntryDao: HabitEntryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RoomTaskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitDao = database.habitDao()
        habitEntryDao = database.habitEntryDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ═════════════════════════════════════════════════════════════════
    // 1. HABIT DAO TESTS (12 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun insertHabit_savesToDatabase() = runBlocking {
        val habit = TestDataFactory.createHabitEntity(id = "h1", name = "Drink Water")
        habitDao.insertHabit(habit)

        val habits = habitDao.getAllHabits()
        assertEquals(1, habits.size)
        assertEquals("Drink Water", habits[0].name)
    }

    @Test
    fun insertHabit_duplicateReplaces() = runBlocking {
        val original = TestDataFactory.createHabitEntity(id = "h2", name = "Original")
        habitDao.insertHabit(original)

        val updated = original.copy(name = "Updated Habit")
        habitDao.insertHabit(updated)

        val habits = habitDao.getAllHabits()
        assertEquals(1, habits.size)
        assertEquals("Updated Habit", habits[0].name)
    }

    @Test
    fun getHabitById_returnsMatchingHabit() = runBlocking {
        val habit = TestDataFactory.createHabitEntity(id = "h3", name = "Read 30 mins")
        habitDao.insertHabit(habit)

        val result = habitDao.getHabitById("h3")
        assertNotNull(result)
        assertEquals("Read 30 mins", result!!.name)
    }

    @Test
    fun getHabitById_returnsNullForMissing() = runBlocking {
        val result = habitDao.getHabitById("nonexistent")
        assertNull(result)
    }

    @Test
    fun observeAllHabits_emitsFlow() = runBlocking {
        val habit = TestDataFactory.createHabitEntity(id = "h4", name = "Workout")
        habitDao.insertHabit(habit)

        val flowResult = habitDao.observeAllHabits().first()
        assertEquals(1, flowResult.size)
        assertEquals("Workout", flowResult[0].name)
    }

    @Test
    fun updateHabit_modifiesMetadata() = runBlocking {
        val habit = TestDataFactory.createHabitEntity(id = "h5", name = "Before", reminder_time = "08:00 AM")
        habitDao.insertHabit(habit)

        habitDao.updateHabit(habit.copy(name = "After", reminder_time = "09:00 AM"))

        val result = habitDao.getHabitById("h5")
        assertEquals("After", result?.name)
        assertEquals("09:00 AM", result?.reminder_time)
    }

    @Test
    fun deleteHabit_removesHabit() = runBlocking {
        val habit = TestDataFactory.createHabitEntity(id = "h6")
        habitDao.insertHabit(habit)

        habitDao.deleteHabit("h6")

        val result = habitDao.getHabitById("h6")
        assertNull(result)
    }

    @Test
    fun getHabitCount_returnsTotal() = runBlocking {
        habitDao.insertHabit(TestDataFactory.createHabitEntity(id = "h-cnt-1"))
        habitDao.insertHabit(TestDataFactory.createHabitEntity(id = "h-cnt-2"))

        val count = habitDao.getHabitCount()
        assertEquals(2, count)
    }

    // ═════════════════════════════════════════════════════════════════
    // 2. HABIT ENTRY DAO TESTS (13 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun upsertHabitEntry_insertsNewEntry() = runBlocking {
        val entry = TestDataFactory.createHabitEntryEntity(id = "e1", habit_id = "h1", cycle_index = 0L, day_in_cycle = 1, is_completed = 1)
        habitEntryDao.upsertHabitEntry(entry)

        val entries = habitEntryDao.getHabitEntries("h1", 0L)
        assertEquals(1, entries.size)
        assertEquals(1, entries[0].day_in_cycle)
        assertEquals(1, entries[0].is_completed)
    }

    @Test
    fun upsertHabitEntry_togglesExistingEntry() = runBlocking {
        val entry = TestDataFactory.createHabitEntryEntity(id = "e2", habit_id = "h1", cycle_index = 0L, day_in_cycle = 2, is_completed = 1)
        habitEntryDao.upsertHabitEntry(entry)

        val toggled = entry.copy(is_completed = 0)
        habitEntryDao.upsertHabitEntry(toggled)

        val entries = habitEntryDao.getHabitEntries("h1", 0L)
        assertEquals(1, entries.size)
        assertEquals(0, entries[0].is_completed)
    }

    @Test
    fun getHabitEntries_filtersByCycleIndex() = runBlocking {
        val entryCycle0 = TestDataFactory.createHabitEntryEntity(id = "e-c0", habit_id = "h1", cycle_index = 0L, day_in_cycle = 1)
        val entryCycle1 = TestDataFactory.createHabitEntryEntity(id = "e-c1", habit_id = "h1", cycle_index = 1L, day_in_cycle = 1)
        habitEntryDao.upsertHabitEntry(entryCycle0)
        habitEntryDao.upsertHabitEntry(entryCycle1)

        val cycle0Result = habitEntryDao.getHabitEntries("h1", 0L)
        val cycle1Result = habitEntryDao.getHabitEntries("h1", 1L)

        assertEquals(1, cycle0Result.size)
        assertEquals(0L, cycle0Result[0].cycle_index)

        assertEquals(1, cycle1Result.size)
        assertEquals(1L, cycle1Result[0].cycle_index)
    }

    @Test
    fun deleteEntriesForHabit_cleansAllEntries() = runBlocking {
        habitEntryDao.upsertHabitEntry(TestDataFactory.createHabitEntryEntity(id = "e-del-1", habit_id = "h-del", cycle_index = 0L, day_in_cycle = 1))
        habitEntryDao.upsertHabitEntry(TestDataFactory.createHabitEntryEntity(id = "e-del-2", habit_id = "h-del", cycle_index = 0L, day_in_cycle = 2))

        habitEntryDao.deleteEntriesForHabit("h-del")

        val entries = habitEntryDao.getHabitEntries("h-del", 0L)
        assertTrue(entries.isEmpty())
    }

    @Test
    fun countCompletedDays_returnsCount() = runBlocking {
        habitEntryDao.upsertHabitEntry(TestDataFactory.createHabitEntryEntity(id = "cnt-1", habit_id = "h-cnt", cycle_index = 0L, day_in_cycle = 1, is_completed = 1))
        habitEntryDao.upsertHabitEntry(TestDataFactory.createHabitEntryEntity(id = "cnt-2", habit_id = "h-cnt", cycle_index = 0L, day_in_cycle = 2, is_completed = 1))
        habitEntryDao.upsertHabitEntry(TestDataFactory.createHabitEntryEntity(id = "cnt-3", habit_id = "h-cnt", cycle_index = 0L, day_in_cycle = 3, is_completed = 0))

        val entries = habitEntryDao.getHabitEntries("h-cnt", 0L)
        val completedCount = entries.count { it.is_completed == 1 }
        assertEquals(2, completedCount)
    }
}
