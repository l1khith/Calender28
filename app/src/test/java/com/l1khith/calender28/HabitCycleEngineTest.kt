package com.l1khith.calender28

import com.l1khith.calender28.utils.FixedDate
import com.l1khith.calender28.utils.HabitCycleEngine
import org.junit.Assert.*
import org.junit.Test

class HabitCycleEngineTest {

    // ═════════════════════════════════════════════════════════════════
    // 1. computePosition() Tests (10 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testComputePosition_sameDay() {
        val posDay1 = HabitCycleEngine.computePosition(0L, 0L)
        assertEquals(0L, posDay1.cycleIndex)
        assertEquals(1, posDay1.dayInCycle)
    }

    @Test
    fun testComputePosition_day28() {
        val posDay28 = HabitCycleEngine.computePosition(27L, 0L)
        assertEquals(0L, posDay28.cycleIndex)
        assertEquals(28, posDay28.dayInCycle)
    }

    @Test
    fun testComputePosition_day29StartsCycle1() {
        val posDay29 = HabitCycleEngine.computePosition(28L, 0L)
        assertEquals(1L, posDay29.cycleIndex)
        assertEquals(1, posDay29.dayInCycle)
    }

    @Test
    fun testComputePosition_56DaysLaterCycle2() {
        val pos = HabitCycleEngine.computePosition(56L, 0L)
        assertEquals(2L, pos.cycleIndex)
        assertEquals(1, pos.dayInCycle)
    }

    @Test
    fun testComputePosition_65DaysLater() {
        val pos = HabitCycleEngine.computePosition(65L, 0L) // 65 = 2*28 + 9 -> day 10
        assertEquals(2L, pos.cycleIndex)
        assertEquals(10, pos.dayInCycle)
    }

    @Test
    fun testComputePosition_withNonZeroAnchor() {
        val anchor = 1000L
        val today = 1010L // 10 days after anchor -> day 11
        val pos = HabitCycleEngine.computePosition(today, anchor)
        assertEquals(0L, pos.cycleIndex)
        assertEquals(11, pos.dayInCycle)
    }

    @Test
    fun testComputePosition_beforeAnchorNegativeCycle() {
        val anchor = 1000L
        val today = 999L // 1 day before anchor
        val pos = HabitCycleEngine.computePosition(today, anchor)
        assertTrue(pos.cycleIndex < 0L)
    }

    @Test
    fun testComputePosition_safeDayInCycle() {
        val pos = HabitCycleEngine.computePosition(0L, 0L)
        assertEquals(1, pos.safeDayInCycle)
    }

    @Test
    fun testComputePosition_largeEpochDay() {
        val pos = HabitCycleEngine.computePosition(2800L, 0L) // cycle 100, day 1
        assertEquals(100L, pos.cycleIndex)
        assertEquals(1, pos.dayInCycle)
    }

    @Test
    fun testComputePosition_getEpochDayReverse() {
        val epochDay = 150L
        val pos = HabitCycleEngine.computePosition(epochDay, 0L)
        val reversed = HabitCycleEngine.getEpochDay(pos.cycleIndex, pos.dayInCycle, 0L)
        assertEquals(epochDay, reversed)
    }

    // ═════════════════════════════════════════════════════════════════
    // 2. getEpochDay() & currentEpochDay() Tests (5 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testGetEpochDay_nonNegative() {
        val ts = System.currentTimeMillis()
        val epochDay = HabitCycleEngine.getEpochDay(ts)
        assertTrue(epochDay >= 0L)
    }

    @Test
    fun testCurrentEpochDay_returnsValidDay() {
        val current = HabitCycleEngine.currentEpochDay()
        assertTrue(current > 0L)
    }

    @Test
    fun testGetEpochDay_sameTimeSameResult() {
        val ts = 1755129600000L
        val day1 = HabitCycleEngine.getEpochDay(ts)
        val day2 = HabitCycleEngine.getEpochDay(ts)
        assertEquals(day1, day2)
    }

    @Test
    fun testGetEpochDay_24HoursLater() {
        val ts1 = 1755129600000L
        val ts2 = ts1 + 86400000L
        val day1 = HabitCycleEngine.getEpochDay(ts1)
        val day2 = HabitCycleEngine.getEpochDay(ts2)
        assertEquals(1L, day2 - day1)
    }

    @Test
    fun testGetEpochDay_zeroTimestamp() {
        val day = HabitCycleEngine.getEpochDay(0L)
        assertTrue(day >= 0L)
    }

    // ═════════════════════════════════════════════════════════════════
    // 3. Streak & Progress Calculations (8 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testComputeCurrentStreak_perfect5Days() {
        val completed = setOf(1, 2, 3, 4, 5)
        val streak = HabitCycleEngine.computeCurrentStreak(completed, 5)
        assertEquals(5, streak)
    }

    @Test
    fun testComputeCurrentStreak_brokenStreak() {
        val completed = setOf(1, 2, 4, 5) // day 3 missed
        val streak = HabitCycleEngine.computeCurrentStreak(completed, 5)
        assertEquals(2, streak) // only days 4 and 5 count
    }

    @Test
    fun testComputeCurrentStreak_todayNotCompleted_yesterdayCompleted() {
        val completed = setOf(1, 2, 3)
        // Today is day 4, not logged yet, but days 1..3 were completed
        val streak = HabitCycleEngine.computeCurrentStreak(completed, 4)
        assertEquals(3, streak)
    }

    @Test
    fun testComputeCurrentStreak_todayNotCompleted_yesterdayMissed() {
        val completed = setOf(1, 2)
        // Today is day 4, day 3 was missed
        val streak = HabitCycleEngine.computeCurrentStreak(completed, 4)
        assertEquals(0, streak)
    }

    @Test
    fun testComputeBestStreak_mixedDays() {
        val completed = setOf(1, 2, 3, 7, 8, 9, 10, 11, 15)
        val best = HabitCycleEngine.computeBestStreak(completed)
        assertEquals(5, best)
    }

    @Test
    fun testComputeBestStreak_emptySet() {
        val best = HabitCycleEngine.computeBestStreak(emptySet())
        assertEquals(0, best)
    }

    @Test
    fun testComputeBestStreak_all28Days() {
        val completed = (1..28).toSet()
        val best = HabitCycleEngine.computeBestStreak(completed)
        assertEquals(28, best)
    }

    @Test
    fun testComputePerfectWeeksCount_twoWeeks() {
        val completed = setOf(1, 2, 3, 4, 5, 6, 7, 15, 16, 17, 18, 19, 20, 21)
        val perfectWeeks = HabitCycleEngine.computePerfectWeeksCount(completed)
        assertEquals(2, perfectWeeks)
    }

    @Test
    fun testComputePerfectWeeksCount_empty() {
        val perfectWeeks = HabitCycleEngine.computePerfectWeeksCount(emptySet())
        assertEquals(0, perfectWeeks)
    }

    @Test
    fun testHabitModel_brokenStreakDoesNotCountMissedDays() {
        val habit = com.l1khith.calender28.data.Habit(
            id = "test-1",
            name = "Morning Run",
            completedDays = setOf(1, 2, 4, 5), // Day 3 missed
            createdAtMs = System.currentTimeMillis() // Day 1 today
        )
        // Longest streak should be 2, NOT 4!
        assertEquals(2, habit.longestStreak)
    }
}
