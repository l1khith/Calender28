package com.l1khith.calender28

import com.l1khith.calender28.test.TestDataFactory
import com.l1khith.calender28.utils.ConflictResolver
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import org.junit.Assert.*
import org.junit.Test

class ConflictResolverTest {

    private val testDate = FixedDate(2028, 1, 14)
    private val dateStr = testDate.toString()

    @Test
    fun testFindConflictFreeSlot_noExistingTasks_returnsFirstPreferredSlot() {
        val slot = ConflictResolver.findConflictFreeSlot(
            candidateDate = dateStr,
            durationMinutes = 60L,
            existingTasks = emptyList()
        )

        // Preferred window starts at 08:00
        assertEquals("08:00", slot)
    }

    @Test
    fun testFindConflictFreeSlot_overlappingSlot_findsNextAvailableSlot() {
        // Existing task from 08:00 to 09:00
        val startMs = FixedCalendarHelper.toTimestamp(testDate, "08:00")
        val endMs = FixedCalendarHelper.toTimestamp(testDate, "09:00")
        val existingTask = TestDataFactory.createAppTask(
            id = "task_1",
            title = "Morning Sync",
            associatedDate = dateStr,
            reminderTime = "08:00",
            utcTimestamp = startMs,
            endTime = "09:00",
            endUtcTimestamp = endMs
        )

        val slot = ConflictResolver.findConflictFreeSlot(
            candidateDate = dateStr,
            durationMinutes = 60L,
            existingTasks = listOf(existingTask)
        )

        assertNotNull(slot)
        assertEquals("09:00", slot)
    }

    @Test
    fun testFindConflictFreeSlot_multipleChainedTasks_skipsToFirstOpenSlot() {
        // Task 1: 08:00 - 09:00
        val task1 = TestDataFactory.createAppTask(
            id = "task_1",
            associatedDate = dateStr,
            utcTimestamp = FixedCalendarHelper.toTimestamp(testDate, "08:00"),
            endUtcTimestamp = FixedCalendarHelper.toTimestamp(testDate, "09:00")
        )
        // Task 2: 09:00 - 10:30
        val task2 = TestDataFactory.createAppTask(
            id = "task_2",
            associatedDate = dateStr,
            utcTimestamp = FixedCalendarHelper.toTimestamp(testDate, "09:00"),
            endUtcTimestamp = FixedCalendarHelper.toTimestamp(testDate, "10:30")
        )

        val slot = ConflictResolver.findConflictFreeSlot(
            candidateDate = dateStr,
            durationMinutes = 30L,
            existingTasks = listOf(task1, task2)
        )

        assertNotNull(slot)
        assertEquals("10:30", slot)
    }

    @Test
    fun testFindConflictFreeSlot_ignoresSpecifiedTaskId() {
        // Task 1: 08:00 - 09:00 (the task being rescheduled)
        val task1 = TestDataFactory.createAppTask(
            id = "reschedule_me",
            associatedDate = dateStr,
            utcTimestamp = FixedCalendarHelper.toTimestamp(testDate, "08:00"),
            endUtcTimestamp = FixedCalendarHelper.toTimestamp(testDate, "09:00")
        )

        val slot = ConflictResolver.findConflictFreeSlot(
            candidateDate = dateStr,
            durationMinutes = 60L,
            existingTasks = listOf(task1),
            ignoreTaskId = "reschedule_me"
        )

        assertEquals("08:00", slot)
    }
}
