# 7. Habit Cycle Engine

## Overview
Habit tracking in Calender28 is structured around **28-day cycles**. Each habit entry maps to a specific day (`1..28`) within a specific cycle (`cycleIndex`).

The Habit Cycle Engine evaluates completion progress, streaks, perfect weeks, and historical statistics.

---

## Domain Models

```kotlin
package com.l1khith.calender28.domain.model

data class Habit(
    val id: String,
    val name: String,
    val category: String = "Health",
    val reminderTime: String? = null,
    val isPaused: Boolean = false,
    val colorHex: Long = 0xFF6B7280
)

data class HabitEntry(
    val id: String,
    val habitId: String,
    val cycleIndex: Long,
    val dayInCycle: Int,     // 1..28
    val epochDay: Long,
    val isCompleted: Boolean,
    val completedAtMs: Long?
)

data class HabitCycleProgress(
    val cycleIndex: Long,
    val totalDays: Int = 28,
    val completedDays: Int,
    val completedDayNumbers: Set<Int>,
    val streak: Int,
    val isPerfectWeek: Boolean
)

data class HabitCycle(
    val habitId: String,
    val cycleIndex: Long,
    val monthName: String,
    val year: Int
)
```

---

## HabitCycleEngine Implementation

```kotlin
package com.l1khith.calender28.domain.engine

import com.l1khith.calender28.domain.model.*
import com.l1khith.calender28.util.CalendarDate
import com.l1khith.calender28.util.CalendarMath

class HabitCycleEngine(
    private val calendarMath: CalendarMath = CalendarMath
) {
    fun getCurrentCycleProgress(
        habit: Habit,
        entries: List<HabitEntry>
    ): HabitCycleProgress {
        val today = calendarMath.today()
        val currentCycleIndex = calendarMath.getCycleIndex(today)
        
        val completedDays = entries
            .filter { it.cycleIndex == currentCycleIndex && it.isCompleted }
            .map { it.dayInCycle }
            .toSet()
        
        return HabitCycleProgress(
            cycleIndex = currentCycleIndex,
            totalDays = 28,
            completedDays = completedDays.size,
            completedDayNumbers = completedDays,
            streak = calculateStreak(habit, entries),
            isPerfectWeek = completedDays.size >= 7
        )
    }
    
    fun calculateStreak(habit: Habit, allEntries: List<HabitEntry>): Int {
        val today = calendarMath.today()
        var streak = 0
        var checkDate = today
        
        while (true) {
            val cycleIndex = calendarMath.getCycleIndex(checkDate)
            val dayInCycle = calendarMath.getDayInCycle(checkDate)
            
            val wasCompleted = allEntries.any { entry ->
                entry.habitId == habit.id && 
                entry.cycleIndex == cycleIndex && 
                entry.dayInCycle == dayInCycle && 
                entry.isCompleted
            }
            
            if (!wasCompleted) break
            streak++
            
            // Move backward by 1 day
            val prevDay = checkDate.day - 1
            checkDate = if (prevDay >= 1) {
                checkDate.copy(day = prevDay)
            } else {
                val prevMonth = if (checkDate.month > 1) checkDate.month - 1 else 13
                val prevYear = if (checkDate.month > 1) checkDate.year else checkDate.year - 1
                CalendarDate(prevYear, prevMonth, 28)
            }
        }
        
        return streak
    }
    
    fun getCycleForDate(habit: Habit, date: CalendarDate): HabitCycle {
        val cycleIndex = calendarMath.getCycleIndex(date)
        return HabitCycle(
            habitId = habit.id,
            cycleIndex = cycleIndex,
            monthName = calendarMath.getMonthName(date.month),
            year = date.year
        )
    }
}
```
