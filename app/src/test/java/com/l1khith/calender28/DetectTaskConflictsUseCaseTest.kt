package com.l1khith.calender28

import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.domain.usecase.DetectTaskConflictsUseCase
import com.l1khith.calender28.test.FakeTaskRepository
import com.l1khith.calender28.test.TestDataFactory
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DetectTaskConflictsUseCaseTest {

    private lateinit var fakeRepo: FakeTaskRepository
    private lateinit var useCase: DetectTaskConflictsUseCase
    private val testDate = FixedDate(2028, 1, 14)
    private val dateStr = testDate.toString()

    @Before
    fun setup() {
        fakeRepo = FakeTaskRepository()
        useCase = DetectTaskConflictsUseCase(fakeRepo)
    }

    @Test
    fun testHardOverlap_detected() = runBlocking {
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
        fakeRepo.tasks.add(task1)

        // Overlaps 10:30 - 11:30
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

        val conflicts = useCase(task2)
        assertTrue(conflicts.any { it is DayConflict.HardOverlap })
        val hardOverlap = conflicts.first { it is DayConflict.HardOverlap } as DayConflict.HardOverlap
        val overlapDurationMinutes = (hardOverlap.overlapEndMs - hardOverlap.overlapStartMs) / 60_000L
        assertEquals(30L, overlapDurationMinutes)
    }

    @Test
    fun testBackToBack_detected() = runBlocking {
        val start1 = FixedCalendarHelper.toTimestamp(testDate, "09:00")
        val end1 = FixedCalendarHelper.toTimestamp(testDate, "10:00")
        val task1 = TestDataFactory.createAppTask(
            id = "task_1",
            title = "Task 1",
            associatedDate = dateStr,
            reminderTime = "09:00",
            utcTimestamp = start1,
            endTime = "10:00",
            endUtcTimestamp = end1
        )
        fakeRepo.tasks.add(task1)

        // Starts exactly when task1 ends
        val start2 = FixedCalendarHelper.toTimestamp(testDate, "10:00")
        val end2 = FixedCalendarHelper.toTimestamp(testDate, "11:00")
        val task2 = TestDataFactory.createAppTask(
            id = "task_2",
            title = "Task 2",
            associatedDate = dateStr,
            reminderTime = "10:00",
            utcTimestamp = start2,
            endTime = "11:00",
            endUtcTimestamp = end2
        )

        val conflicts = useCase(task2)
        assertTrue(conflicts.any { it is DayConflict.BackToBack })
    }

    @Test
    fun testAllDayVsTimed_detected() = runBlocking {
        val allDayTask = TestDataFactory.createAppTask(
            id = "all_day_1",
            title = "All Day Event",
            associatedDate = dateStr,
            isAllDay = 1
        )
        fakeRepo.tasks.add(allDayTask)

        val start = FixedCalendarHelper.toTimestamp(testDate, "10:00")
        val end = FixedCalendarHelper.toTimestamp(testDate, "11:00")
        val timedTask = TestDataFactory.createAppTask(
            id = "timed_1",
            title = "Timed Meeting",
            associatedDate = dateStr,
            reminderTime = "10:00",
            utcTimestamp = start,
            endTime = "11:00",
            endUtcTimestamp = end
        )

        val conflicts = useCase(timedTask)
        assertTrue(conflicts.any { it is DayConflict.AllDayVsTimed })
    }

    @Test
    fun testNoConflict_whenDisjointWithBuffer() = runBlocking {
        val start1 = FixedCalendarHelper.toTimestamp(testDate, "09:00")
        val end1 = FixedCalendarHelper.toTimestamp(testDate, "10:00")
        val task1 = TestDataFactory.createAppTask(
            id = "task_1",
            title = "Task 1",
            associatedDate = dateStr,
            reminderTime = "09:00",
            utcTimestamp = start1,
            endTime = "10:00",
            endUtcTimestamp = end1
        )
        fakeRepo.tasks.add(task1)

        // 30 minute gap
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

        val conflicts = useCase(task2)
        assertTrue(conflicts.isEmpty())
    }
}
