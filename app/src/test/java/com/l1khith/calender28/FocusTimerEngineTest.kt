package com.l1khith.calender28

import com.l1khith.calender28.service.FocusTimerEngine
import org.junit.Assert.*
import org.junit.Test

class FocusTimerEngineTest {

    @Test
    fun `test stopwatch elapsed time progression with mock clock`() {
        var currentTime = 1_000_000L
        val engine = FocusTimerEngine(
            mode = "stopwatch",
            clock = { currentTime }
        )

        assertEquals(0, engine.currentElapsedSeconds())
        assertFalse(engine.isFinished())

        // Advance clock by 15 seconds
        currentTime += 15_000L
        assertEquals(15, engine.currentElapsedSeconds())
        assertFalse(engine.isFinished())
    }

    @Test
    fun `test timer mode countdown and completion`() {
        var currentTime = 5_000_000L
        val engine = FocusTimerEngine(
            mode = "timer",
            targetDurationSeconds = 60,
            clock = { currentTime }
        )

        assertEquals(0, engine.currentElapsedSeconds())
        assertEquals(60, engine.remainingSeconds())
        assertFalse(engine.isFinished())

        // Halfway
        currentTime += 30_000L
        assertEquals(30, engine.currentElapsedSeconds())
        assertEquals(30, engine.remainingSeconds())
        assertFalse(engine.isFinished())

        // Finished
        currentTime += 30_000L
        assertEquals(60, engine.currentElapsedSeconds())
        assertEquals(0, engine.remainingSeconds())
        assertTrue(engine.isFinished())

        // Overtime should still be finished and remaining clamped to 0
        currentTime += 10_000L
        assertEquals(70, engine.currentElapsedSeconds())
        assertEquals(0, engine.remainingSeconds())
        assertTrue(engine.isFinished())
    }

    @Test
    fun `test pause and resume drift-free calculation`() {
        var currentTime = 10_000_000L
        val engine = FocusTimerEngine(
            mode = "timer",
            targetDurationSeconds = 300,
            clock = { currentTime }
        )

        // Run for 10 seconds
        currentTime += 10_000L
        assertEquals(10, engine.currentElapsedSeconds())

        // Pause
        val pausedSeconds = engine.pause()
        assertEquals(10, pausedSeconds)
        assertTrue(engine.isPaused)

        // 50 seconds elapse while paused (simulating device sleep or user pause)
        currentTime += 50_000L
        assertEquals(10, engine.currentElapsedSeconds()) // Must still be 10

        // Calling pause again while already paused is a no-op
        assertEquals(10, engine.pause())

        // Resume
        engine.resume()
        assertFalse(engine.isPaused)

        // Run for 15 more seconds
        currentTime += 15_000L
        assertEquals(25, engine.currentElapsedSeconds())
        assertEquals(275, engine.remainingSeconds())
    }
}
