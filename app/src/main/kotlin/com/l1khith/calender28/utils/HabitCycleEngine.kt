package com.l1khith.calender28.utils

data class HabitCyclePosition(
    val cycleIndex: Long,
    val dayInCycle: Int
) {
    val safeDayInCycle: Int get() = dayInCycle.coerceIn(1, 28)

    init {
        require(dayInCycle in 1..28) {
            "dayInCycle must be between 1 and 28 inclusive, was $dayInCycle"
        }
    }
}

object HabitCycleEngine {

    const val DAYS_IN_CYCLE = 28

    fun getEpochDay(timestampMs: Long): Long {
        val tz = java.util.TimeZone.getDefault()
        val localMs = timestampMs + tz.getOffset(timestampMs)
        return (localMs / 86400000L).coerceAtLeast(0L)
    }

    fun currentEpochDay(): Long = getEpochDay(System.currentTimeMillis())

    fun computePosition(epochDay: Long, anchorEpochDay: Long = 0L): HabitCyclePosition {
        val delta = epochDay - anchorEpochDay
        val cycleIndex: Long
        val dayInCycle: Int

        if (delta >= 0) {
            cycleIndex = delta / DAYS_IN_CYCLE
            dayInCycle = ((delta % DAYS_IN_CYCLE) + 1).toInt()
        } else {
            val adjustedDelta = delta + 1
            cycleIndex = (adjustedDelta / DAYS_IN_CYCLE) - 1
            val rem = delta % DAYS_IN_CYCLE
            dayInCycle = (if (rem == 0L) 1L else rem + DAYS_IN_CYCLE + 1).toInt()
        }

        return HabitCyclePosition(cycleIndex, dayInCycle)
    }

    fun getCycleIndex(epochDay: Long, anchorEpochDay: Long = 0L): Long {
        return computePosition(epochDay, anchorEpochDay).cycleIndex
    }

    fun getDayInCycle(epochDay: Long, anchorEpochDay: Long = 0L): Int {
        return computePosition(epochDay, anchorEpochDay).dayInCycle
    }

    fun getEpochDay(cycleIndex: Long, dayInCycle: Int, anchorEpochDay: Long = 0L): Long {
        require(dayInCycle in 1..28) { "dayInCycle must be in 1..28" }
        return anchorEpochDay + (cycleIndex * DAYS_IN_CYCLE) + (dayInCycle - 1)
    }

    fun computeCurrentStreak(completedDays: Set<Int>, currentDayInCycle: Int): Int {
        if (completedDays.isEmpty() || currentDayInCycle < 1) return 0
        val startDay = if (completedDays.contains(currentDayInCycle)) {
            currentDayInCycle
        } else if (currentDayInCycle > 1 && completedDays.contains(currentDayInCycle - 1)) {
            currentDayInCycle - 1
        } else {
            return 0
        }

        var streak = 0
        var day = startDay
        while (day >= 1 && completedDays.contains(day)) {
            streak++
            day--
        }
        return streak
    }

    fun computeBestStreak(completedDays: Set<Int>): Int {
        var best = 0
        var current = 0
        for (day in 1..28) {
            if (completedDays.contains(day)) {
                current++
                if (current > best) best = current
            } else {
                current = 0
            }
        }
        return best
    }

    fun computePerfectWeeksCount(completedDays: Set<Int>): Int {
        var count = 0
        for (week in 0..3) {
            val startDay = week * 7 + 1
            var isPerfect = true
            for (d in startDay until (startDay + 7)) {
                if (!completedDays.contains(d)) {
                    isPerfect = false
                    break
                }
            }
            if (isPerfect) count++
        }
        return count
    }
}
