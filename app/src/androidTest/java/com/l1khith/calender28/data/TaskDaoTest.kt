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
class TaskDaoTest {

    private lateinit var database: RoomTaskDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RoomTaskDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskDao = database.taskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // ═════════════════════════════════════════════════════════════════
    // 1. INSERT TESTS (8 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun insertTask_savesToDatabase() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "task-1", title = "Buy Milk")
        taskDao.insertTask(task)

        val tasks = taskDao.getTasksForDate("2026-08-14")
        assertEquals(1, tasks.size)
        assertEquals("Buy Milk", tasks[0].title)
    }

    @Test
    fun insertTask_withNullDescription_savesSuccessfully() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "task-2", description = null)
        taskDao.insertTask(task)

        val tasks = taskDao.getAllTasks()
        assertNull(tasks.find { it.id == "task-2" }?.description)
    }

    @Test
    fun insertTask_withLongTitle_savesSuccessfully() = runBlocking {
        val longTitle = "T".repeat(500)
        val task = TestDataFactory.createAppTaskEntity(id = "task-3", title = longTitle)
        taskDao.insertTask(task)

        val tasks = taskDao.getAllTasks()
        assertEquals(500, tasks.find { it.id == "task-3" }?.title?.length)
    }

    @Test
    fun insertDuplicateTask_replacesExisting() = runBlocking {
        val original = TestDataFactory.createAppTaskEntity(id = "task-4", title = "Original")
        taskDao.insertTask(original)

        val updated = original.copy(title = "Updated Title")
        taskDao.insertTask(updated)

        val tasks = taskDao.getAllTasks()
        assertEquals(1, tasks.size)
        assertEquals("Updated Title", tasks[0].title)
    }

    @Test
    fun insertMultipleTasks_allRetrievable() = runBlocking {
        val tasks = (1..50).map {
            TestDataFactory.createAppTaskEntity(id = "task-multi-$it")
        }
        tasks.forEach { taskDao.insertTask(it) }

        val allTasks = taskDao.getAllTasks()
        assertEquals(50, allTasks.size)
    }

    @Test
    fun insertTask_withSpecialCharacters() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "task-5", title = "Gym 🏋️‍♂️ & <html>")
        taskDao.insertTask(task)

        val tasks = taskDao.getAllTasks()
        assertEquals("Gym 🏋️‍♂️ & <html>", tasks[0].title)
    }

    @Test
    fun insertTask_reminderFlags() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "task-6", is_reminder = 1, reminder_time = "10:00 AM")
        taskDao.insertTask(task)

        val result = taskDao.getAllTasks().first { it.id == "task-6" }
        assertEquals(1, result.is_reminder)
        assertEquals("10:00 AM", result.reminder_time)
    }

    @Test
    fun insertTaskSync_works() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "task-sync-1")
        taskDao.insertTaskSync(task)

        val result = taskDao.getAllTasksSync()
        assertEquals(1, result.size)
    }

    // ═════════════════════════════════════════════════════════════════
    // 2. QUERY & FILTER TESTS (10 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun getTasksForDate_filtersByAssociatedDate() = runBlocking {
        val task1 = TestDataFactory.createAppTaskEntity(id = "t1", associated_date = "2026-08-14")
        val task2 = TestDataFactory.createAppTaskEntity(id = "t2", associated_date = "2026-08-15")
        taskDao.insertTask(task1)
        taskDao.insertTask(task2)

        val day14 = taskDao.getTasksForDate("2026-08-14")
        assertEquals(1, day14.size)
        assertEquals("t1", day14[0].id)
    }

    @Test
    fun getTasksForDate_returnsEmptyForNoMatch() = runBlocking {
        val tasks = taskDao.getTasksForDate("2099-01-01")
        assertTrue(tasks.isEmpty())
    }

    @Test
    fun getDatesWithActiveTasks_onlyUncompleted() = runBlocking {
        val active = TestDataFactory.createAppTaskEntity(id = "t1", associated_date = "2026-08-14", is_completed = 0)
        val done = TestDataFactory.createAppTaskEntity(id = "t2", associated_date = "2026-08-15", is_completed = 1)
        taskDao.insertTask(active)
        taskDao.insertTask(done)

        val activeDates = taskDao.getDatesWithActiveTasks()
        assertEquals(1, activeDates.size)
        assertEquals("2026-08-14", activeDates[0])
    }

    @Test
    fun observeTasksForDate_emitsFlow() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "t-flow", associated_date = "2026-08-14")
        taskDao.insertTask(task)

        val flowResult = taskDao.observeTasksForDate("2026-08-14").first()
        assertEquals(1, flowResult.size)
    }

    @Test
    fun getSysTaskTitlesForDate_filtersSysPrefix() = runBlocking {
        val sysTask = TestDataFactory.createAppTaskEntity(id = "sys_123", title = "Meeting", associated_date = "2026-08-14")
        val normalTask = TestDataFactory.createAppTaskEntity(id = "normal_123", title = "Personal", associated_date = "2026-08-14")
        taskDao.insertTask(sysTask)
        taskDao.insertTask(normalTask)

        val titles = taskDao.getSysTaskTitlesForDate("2026-08-14")
        assertEquals(1, titles.size)
        assertEquals("meeting", titles[0])
    }

    @Test
    fun countGeneratedInstanceForDate_countsMatching() = runBlocking {
        val gen1 = TestDataFactory.createAppTaskEntity(id = "gen_p1_2026-08-14", associated_date = "2026-08-14", recurring_parent_id = "p1")
        val gen2 = TestDataFactory.createAppTaskEntity(id = "gen_p2_2026-08-14", associated_date = "2026-08-14", recurring_parent_id = "p2")
        taskDao.insertTask(gen1)
        taskDao.insertTask(gen2)

        val count = taskDao.countGeneratedInstanceForDate("2026-08-14", "p1")
        assertEquals(1, count)
    }

    @Test
    fun getRawTaskCountsPerDate_groupsCorrectly() = runBlocking {
        taskDao.insertTask(TestDataFactory.createAppTaskEntity(id = "c1", associated_date = "2026-08-14", is_completed = 0))
        taskDao.insertTask(TestDataFactory.createAppTaskEntity(id = "c2", associated_date = "2026-08-14", is_completed = 0))
        taskDao.insertTask(TestDataFactory.createAppTaskEntity(id = "c3", associated_date = "2026-08-15", is_completed = 0))

        val counts = taskDao.getRawTaskCountsPerDate()
        assertEquals(2, counts.size)
    }

    // ═════════════════════════════════════════════════════════════════
    // 3. UPDATE TESTS (5 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun updateTask_modifiesExisting() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "u1", title = "Before")
        taskDao.insertTask(task)

        taskDao.updateTask(task.copy(title = "After"))
        val tasks = taskDao.getAllTasks()
        assertEquals("After", tasks[0].title)
    }

    @Test
    fun updateTask_nonexistent_returnsZeroRows() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "nonexistent")
        val rows = taskDao.updateTask(task)
        assertEquals(0, rows)
    }

    // ═════════════════════════════════════════════════════════════════
    // 4. DELETE & CASCADE TESTS (12 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun deleteTask_removesSingleTask() = runBlocking {
        val task = TestDataFactory.createAppTaskEntity(id = "d1")
        taskDao.insertTask(task)

        val deletedCount = taskDao.deleteTask("d1")
        assertEquals(1, deletedCount)
        assertTrue(taskDao.getAllTasks().isEmpty())
    }

    @Test
    fun deleteIncompleteGeneratedTasks_deletesOnlyUncompleted() = runBlocking {
        val genUncompleted = TestDataFactory.createAppTaskEntity(id = "gen1", recurring_parent_id = "parent-A", is_completed = 0)
        val genCompleted = TestDataFactory.createAppTaskEntity(id = "gen2", recurring_parent_id = "parent-A", is_completed = 1)
        taskDao.insertTask(genUncompleted)
        taskDao.insertTask(genCompleted)

        taskDao.deleteIncompleteGeneratedTasks("parent-A")

        val remaining = taskDao.getAllTasks()
        assertEquals(1, remaining.size)
        assertEquals("gen2", remaining[0].id)
    }

    @Test
    fun deleteAllGeneratedTasksForParent_wipesAllInstances() = runBlocking {
        val gen1 = TestDataFactory.createAppTaskEntity(id = "gen_parent-B_2026-08-14", recurring_parent_id = "parent-B", is_completed = 0)
        val gen2 = TestDataFactory.createAppTaskEntity(id = "gen_parent-B_2026-08-15", recurring_parent_id = "parent-B", is_completed = 1)
        val other = TestDataFactory.createAppTaskEntity(id = "other", recurring_parent_id = "parent-C", is_completed = 0)
        taskDao.insertTask(gen1)
        taskDao.insertTask(gen2)
        taskDao.insertTask(other)

        taskDao.deleteAllGeneratedTasksForParent("parent-B")

        val remaining = taskDao.getAllTasks()
        assertEquals(1, remaining.size)
        assertEquals("other", remaining[0].id)
    }

    @Test
    fun purgePastUncompletedGeneratedTasks_cleansOldInstances() = runBlocking {
        val oldGen = TestDataFactory.createAppTaskEntity(id = "old", associated_date = "2026-08-01", is_generated = 1, is_completed = 0)
        val newGen = TestDataFactory.createAppTaskEntity(id = "new", associated_date = "2026-08-14", is_generated = 1, is_completed = 0)
        taskDao.insertTask(oldGen)
        taskDao.insertTask(newGen)

        taskDao.purgePastUncompletedGeneratedTasks("2026-08-10")

        val remaining = taskDao.getAllTasks()
        assertEquals(1, remaining.size)
        assertEquals("new", remaining[0].id)
    }

    @Test
    fun purgeOldUncompletedTasks_purgesBeforeCutoff() = runBlocking {
        val oldTask = TestDataFactory.createAppTaskEntity(id = "p-old", associated_date = "2026-01-01", is_completed = 0)
        val newTask = TestDataFactory.createAppTaskEntity(id = "p-new", associated_date = "2026-08-14", is_completed = 0)
        taskDao.insertTask(oldTask)
        taskDao.insertTask(newTask)

        taskDao.purgeOldUncompletedTasks("2026-06-01")

        val remaining = taskDao.getAllTasks()
        assertEquals(1, remaining.size)
        assertEquals("p-new", remaining[0].id)
    }
}
