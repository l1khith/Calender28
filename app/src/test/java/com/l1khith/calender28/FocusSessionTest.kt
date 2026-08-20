package com.l1khith.calender28

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.FocusSession
import com.l1khith.calender28.data.FocusSessionEntity
import com.l1khith.calender28.service.FocusState
import org.junit.Assert.*
import org.junit.Test

class FocusSessionTest {

    @Test
    fun `test AppTask formattedFocusDuration with zero seconds`() {
        val task = AppTask(
            id = "task1",
            title = "Read Book",
            associatedDate = "2026-08-20",
            totalFocusTime = 0
        )
        assertFalse(task.hasEverFocused)
        assertEquals("", task.formattedFocusDuration)
    }

    @Test
    fun `test AppTask formattedFocusDuration with minutes and hours`() {
        val taskMins = AppTask(
            id = "task2",
            title = "Study Kotlin",
            associatedDate = "2026-08-20",
            totalFocusTime = 1500, // 25 mins
            focusCount = 1
        )
        assertTrue(taskMins.hasEverFocused)
        assertEquals("25m", taskMins.formattedFocusDuration)

        val taskHours = AppTask(
            id = "task3",
            title = "Deep Coding",
            associatedDate = "2026-08-20",
            totalFocusTime = 5400, // 1h 30m
            focusCount = 3
        )
        assertTrue(taskHours.hasEverFocused)
        assertEquals("1h 30m", taskHours.formattedFocusDuration)
    }

    @Test
    fun `test FocusSession to and from entity mapping`() {
        val session = FocusSession(
            id = 42L,
            taskId = "task_xyz",
            taskTitle = "Math Assignment",
            mode = "timer",
            durationSeconds = 1800,
            completed = true,
            startedAt = 100000L,
            endedAt = 101800L,
            wasCancelled = false
        )

        val entity = FocusSessionEntity.fromDomain(session)
        assertEquals(42L, entity.id)
        assertEquals("task_xyz", entity.taskId)
        assertEquals("Math Assignment", entity.taskTitle)
        assertEquals("timer", entity.mode)
        assertEquals(1800, entity.durationSeconds)
        assertTrue(entity.completed)
        assertFalse(entity.wasCancelled)

        val reconstructed = entity.toDomain()
        assertEquals(session.id, reconstructed.id)
        assertEquals(session.taskId, reconstructed.taskId)
        assertEquals(session.taskTitle, reconstructed.taskTitle)
        assertEquals(session.durationSeconds, reconstructed.durationSeconds)
        assertEquals(session.formattedDuration, "30m 0s")
    }

    @Test
    fun `test FocusState Active timer calculations`() {
        val dummyTask = AppTask(id = "1", title = "Write Article", associatedDate = "2026-08-20")
        val activeTimer = FocusState.Active(
            task = dummyTask,
            mode = "timer",
            targetDurationSeconds = 1500, // 25 min
            elapsedSeconds = 300, // 5 min
            isPaused = false,
            startedAtMs = 10000L
        )

        assertEquals(1200, activeTimer.remainingSeconds) // 20 min remaining
        assertEquals(1200, activeTimer.displaySeconds)
        assertEquals("20:00", activeTimer.formattedTime)
        assertEquals(0.2f, activeTimer.progressFraction, 0.001f)
    }

    @Test
    fun `test FocusState Active stopwatch calculations`() {
        val dummyTask = AppTask(id = "2", title = "Open Research", associatedDate = "2026-08-20")
        val activeStopwatch = FocusState.Active(
            task = dummyTask,
            mode = "stopwatch",
            targetDurationSeconds = 0,
            elapsedSeconds = 125, // 2m 5s
            isPaused = true,
            startedAtMs = 5000L
        )

        assertEquals(125, activeStopwatch.displaySeconds)
        assertEquals("02:05", activeStopwatch.formattedTime)
        assertEquals(0f, activeStopwatch.progressFraction, 0.001f)
    }

    @Test
    fun `test FocusState Completed duration formatting`() {
        val dummyTask = AppTask(id = "3", title = "Sprint", associatedDate = "2026-08-20")
        val completedShort = FocusState.Completed(
            task = dummyTask,
            mode = "stopwatch",
            durationSeconds = 45
        )
        assertEquals("45s", completedShort.formattedDuration)

        val completedLong = FocusState.Completed(
            task = dummyTask,
            mode = "timer",
            durationSeconds = 1530 // 25m 30s
        )
        assertEquals("25m 30s", completedLong.formattedDuration)
    }
}
