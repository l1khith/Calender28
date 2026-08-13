# 6. 28-Day Calendar Math Engine

## Mathematical Foundation
The core calendar system of **Calender28** splits a solar year into **13 equal months of exactly 28 days** (13 × 28 = 364 days).

- Days per month: **28 days** (4 perfect 7-day weeks per month).
- Months per year: **13 months** (Month 1 to 13, where Month 13 is designated as the "Matrix" month).
- Remaining 1-2 days per solar year: Handled as "Day Zero" intercalary non-weekday event days.

---

## CalendarMath Engine Implementation

```kotlin
package com.l1khith.calender28.util

import java.time.LocalDate

data class CalendarDate(
    val year: Int,
    val month: Int,  // 1 to 13
    val day: Int     // 1 to 28
) {
    fun toDateString(): String = "%04d-%02d-%02d".format(year, month, day)
}

object CalendarMath {
    const val DAYS_PER_CYCLE = 28
    const val MONTHS_PER_YEAR = 13  // 13 months × 28 days = 364 days
    
    fun today(): CalendarDate {
        val localDate = LocalDate.now()
        return fromGregorian(localDate)
    }
    
    fun fromGregorian(date: LocalDate): CalendarDate {
        val dayOfYear = date.dayOfYear
        val year = date.year
        val month = ((dayOfYear - 1) / DAYS_PER_CYCLE) + 1
        val day = ((dayOfYear - 1) % DAYS_PER_CYCLE) + 1
        return CalendarDate(year, month.coerceAtMost(13), day)
    }
    
    fun toGregorian(cal: CalendarDate): LocalDate {
        val dayOfYear = (cal.month - 1) * DAYS_PER_CYCLE + cal.day
        return LocalDate.ofYearDay(cal.year, dayOfYear.coerceAtMost(365))
    }
    
    fun toEpochDay(cal: CalendarDate): Long {
        return toGregorian(cal).toEpochDay()
    }
    
    fun getCycleIndex(cal: CalendarDate): Long {
        // Absolute total 28-day cycles elapsed since year 0
        return cal.year * MONTHS_PER_YEAR.toLong() + cal.month
    }
    
    fun getDayInCycle(cal: CalendarDate): Int = cal.day
    
    fun getMonthName(month: Int): String = when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        13 -> "Matrix"
        else -> "Unknown"
    }
}
```
