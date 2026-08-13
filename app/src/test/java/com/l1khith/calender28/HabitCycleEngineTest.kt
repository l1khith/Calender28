package com.l1khith.calender28

import com.l1khith.calender28.utils.HabitCycleEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitCycleEngineTest {

    @Test
    fun testComputePosition() {
        val posDay1 = HabitCycleEngine.computePosition(0L, 0L)
        assertEquals(0L, posDay1.cycleIndex)
        assertEquals(1, posDay1.dayInCycle)

        val posDay28 = HabitCycleEngine.computePosition(27L, 0L)
        assertEquals(0L, posDay28.cycleIndex)
        assertEquals(28, posDay28.dayInCycle)

        val posDay29 = HabitCycleEngine.computePosition(28L, 0L)
        assertEquals(1L, posDay29.cycleIndex)
        assertEquals(1, posDay29.dayInCycle)
    }

    @Test
    fun testComputeCurrentStreak() {
        val completed = setOf(1, 2, 3, 4, 5)
        val streak = HabitCycleEngine.computeCurrentStreak(completed, 5)
        assertEquals(5, streak)
    }

    @Test
    fun testComputeBestStreak() {
        val completed = setOf(1, 2, 3, 7, 8, 9, 10, 11, 15)
        val best = HabitCycleEngine.computeBestStreak(completed)
        assertEquals(5, best)
    }

    @Test
    fun testComputePerfectWeeksCount() {
        val completed = setOf(1, 2, 3, 4, 5, 6, 7, 15, 16, 17, 18, 19, 20, 21)
        val perfectWeeks = HabitCycleEngine.computePerfectWeeksCount(completed)
        assertEquals(2, perfectWeeks)
    }
}
