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
class RecurringTaskDaoTest {

    private lateinit var database: RoomTaskDatabase
    private lateinit var recurringTaskDao: RecurringTaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RoomTaskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recurringTaskDao = database.recurringTaskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ═════════════════════════════════════════════════════════════════
    // 1. RECURRING TASK DAO TESTS (20 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun saveRecurringTask_insertsNewTask() = runBlocking {
        val recurring = TestDataFactory.createRecurringTaskEntity(id = "r1", title = "Daily Standup")
        recurringTaskDao.saveRecurringTask(recurring)

        val tasks = recurringTaskDao.getAllRecurringTasks()
        assertEquals(1, tasks.size)
        assertEquals("Daily Standup", tasks[0].title)
    }

    @Test
    fun saveRecurringTask_replacesDuplicate() = runBlocking {
        val original = TestDataFactory.createRecurringTaskEntity(id = "r2", title = "Original Meeting")
        recurringTaskDao.saveRecurringTask(original)

        val updated = original.copy(title = "Updated Meeting")
        recurringTaskDao.saveRecurringTask(updated)

        val tasks = recurringTaskDao.getAllRecurringTasks()
        assertEquals(1, tasks.size)
        assertEquals("Updated Meeting", tasks[0].title)
    }

    @Test
    fun getRecurringTaskById_returnsMatching() = runBlocking {
        val recurring = TestDataFactory.createRecurringTaskEntity(id = "r3", title = "Weekly Review")
        recurringTaskDao.saveRecurringTask(recurring)

        val result = recurringTaskDao.getRecurringTaskById("r3")
        assertNotNull(result)
        assertEquals("Weekly Review", result!!.title)
    }

    @Test
    fun getRecurringTaskById_returnsNullForMissing() = runBlocking {
        val result = recurringTaskDao.getRecurringTaskById("nonexistent")
        assertNull(result)
    }

    @Test
    fun observeAllRecurringTasks_emitsFlow() = runBlocking {
        val recurring = TestDataFactory.createRecurringTaskEntity(id = "r4", title = "Flow Meeting")
        recurringTaskDao.saveRecurringTask(recurring)

        val flowResult = recurringTaskDao.observeAllRecurringTasks().first()
        assertEquals(1, flowResult.size)
        assertEquals("Flow Meeting", flowResult[0].title)
    }

    @Test
    fun deleteRecurringTask_removesTemplate() = runBlocking {
        val recurring = TestDataFactory.createRecurringTaskEntity(id = "r5")
        recurringTaskDao.saveRecurringTask(recurring)

        val deletedCount = recurringTaskDao.deleteRecurringTask("r5")
        assertEquals(1, deletedCount)

        val result = recurringTaskDao.getRecurringTaskById("r5")
        assertNull(result)
    }

    @Test
    fun getActiveRecurringTasks_filtersInactive() = runBlocking {
        val active = TestDataFactory.createRecurringTaskEntity(id = "r-active", is_active = 1, created_at = 1000L)
        val inactive = TestDataFactory.createRecurringTaskEntity(id = "r-inactive", is_active = 0, created_at = 1000L)
        recurringTaskDao.saveRecurringTask(active)
        recurringTaskDao.saveRecurringTask(inactive)

        val activeList = recurringTaskDao.getActiveRecurringTasks("2026-08-14")
        assertEquals(1, activeList.size)
        assertEquals("r-active", activeList[0].id)
    }

    @Test
    fun getActiveRecurringTasks_respectsEndDate() = runBlocking {
        val endedTask = TestDataFactory.createRecurringTaskEntity(id = "r-ended", is_active = 1, created_at = 1000L, end_date = "2026-08-10")
        val ongoingTask = TestDataFactory.createRecurringTaskEntity(id = "r-ongoing", is_active = 1, created_at = 1000L, end_date = null)
        recurringTaskDao.saveRecurringTask(endedTask)
        recurringTaskDao.saveRecurringTask(ongoingTask)

        val activeList = recurringTaskDao.getActiveRecurringTasks("2026-08-14")
        assertEquals(1, activeList.size)
        assertEquals("r-ongoing", activeList[0].id)
    }

    @Test
    fun toggleRecurringTaskActive_updatesStatus() = runBlocking {
        val recurring = TestDataFactory.createRecurringTaskEntity(id = "r-toggle", is_active = 1)
        recurringTaskDao.saveRecurringTask(recurring)

        recurringTaskDao.toggleRecurringTaskActive("r-toggle", 0)

        val result = recurringTaskDao.getRecurringTaskById("r-toggle")
        assertEquals(0, result?.is_active)
    }

    @Test
    fun getRecurringTaskCount_returnsTotal() = runBlocking {
        recurringTaskDao.saveRecurringTask(TestDataFactory.createRecurringTaskEntity(id = "cnt-1"))
        recurringTaskDao.saveRecurringTask(TestDataFactory.createRecurringTaskEntity(id = "cnt-2"))

        val count = recurringTaskDao.getRecurringTaskCount()
        assertEquals(2, count)
    }
}
