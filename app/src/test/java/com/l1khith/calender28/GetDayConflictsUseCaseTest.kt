package com.l1khith.calender28

import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.domain.usecase.GetDayConflictsUseCase
import com.l1khith.calender28.test.FakeTaskRepository
import com.l1khith.calender28.test.TestDataFactory
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetDayConflictsUseCaseTest {

    private lateinit var fakeRepo: FakeTaskRepository
    private lateinit var getDayConflictsUseCase: GetDayConflictsUseCase
    private val testDate = FixedDate(2028, 1, 14)
    private val dateStr = testDate.toString()

    @Before
    fun setup() {
        fakeRepo = FakeTaskRepository()
        getDayConflictsUseCase = GetDayConflictsUseCase(fakeRepo)
    }

    @Test
    fun testGetDayConflicts_multipleOverlappingTasks_findsAllConflicts() = runBlocking {
        val start1 = FixedCalendarHelper.toTimestamp(testDate, "10:00")
        val end1 = FixedCalendarHelper.toTimestamp(testDate, "11:00")
        val task1 = TestDataFactory.createAppTask(
            id = "task_1",
            title = "Task 1",
            associatedDate = dateStr,
            reminderTime = "10:00",
            utcTimestamp = start1,
            endTime = "11:00",
            endUtcTimestamp = end1
        )

        val start2 = FixedCalendarHelper.toTimestamp(testDate, "10:30")
        val end2 = FixedCalendarHelper.toTimestamp(testDate, "11:30")
        val task2 = TestDataFactory.createAppTask(
            id = "task_2",
            title = "Task 2",
            associatedDate = dateStr,
            reminderTime = "10:30",
            utcTimestamp = start2,
            endTime = "11:30",
            endUtcTimestamp = end2
        )

        val start3 = FixedCalendarHelper.toTimestamp(testDate, "11:00")
        val end3 = FixedCalendarHelper.toTimestamp(testDate, "12:00")
        val task3 = TestDataFactory.createAppTask(
            id = "task_3",
            title = "Task 3",
            associatedDate = dateStr,
            reminderTime = "11:00",
            utcTimestamp = start3,
            endTime = "12:00",
            endUtcTimestamp = end3
        )

        fakeRepo.tasks.addAll(listOf(task1, task2, task3))

        val conflicts = getDayConflictsUseCase(dateStr)

        // task1 and task2: HardOverlap (10:30 - 11:00)
        // task2 and task3: HardOverlap (11:00 - 11:30)
        assertTrue("Should detect hard overlap",
            conflicts.any { it is DayConflict.HardOverlap && (it.primaryEventId == "task_1" || it.secondaryEventId == "task_1") })
        assertTrue("Should detect hard overlap between task2 and task3",
            conflicts.any { it is DayConflict.HardOverlap && (it.primaryEventId == "task_2" || it.secondaryEventId == "task_2") })
    }

    @Test
    fun testGetDayConflicts_noConflicts_returnsEmpty() = runBlocking {
        val start1 = FixedCalendarHelper.toTimestamp(testDate, "08:00")
        val end1 = FixedCalendarHelper.toTimestamp(testDate, "09:00")
        val task1 = TestDataFactory.createAppTask(
            id = "task_1",
            title = "Morning Sync",
            associatedDate = dateStr,
            reminderTime = "08:00",
            utcTimestamp = start1,
            endTime = "09:00",
            endUtcTimestamp = end1
        )

        val start2 = FixedCalendarHelper.toTimestamp(testDate, "14:00")
        val end2 = FixedCalendarHelper.toTimestamp(testDate, "15:00")
        val task2 = TestDataFactory.createAppTask(
            id = "task_2",
            title = "Afternoon Workshop",
            associatedDate = dateStr,
            reminderTime = "14:00",
            utcTimestamp = start2,
            endTime = "15:00",
            endUtcTimestamp = end2
        )

        fakeRepo.tasks.addAll(listOf(task1, task2))

        val conflicts = getDayConflictsUseCase(dateStr)
        assertTrue("Should have no conflicts for tasks hours apart", conflicts.isEmpty())
    }
}
