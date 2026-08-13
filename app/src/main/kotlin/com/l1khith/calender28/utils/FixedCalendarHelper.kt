package com.l1khith.calender28.utils

import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask

data class FixedDate(
    val year: Int,
    val month: Int, // 1 to 13 (Sol is 7)
    val day: Int,   // 1 to 28 (or 29 for Leap Day / Year Day)
    val isLeapDay: Boolean = false,
    val isYearDay: Boolean = false
) {
    fun toEpochDay(): Long {
        val ms = FixedCalendarHelper.toTimestamp(this)
        return ms / 86400000L
    }

    override fun toString(): String {
        val monthStr = month.toString().padStart(2, '0')
        val dayStr = day.toString().padStart(2, '0')
        return "$year-$monthStr-$dayStr"
    }
}

object FixedCalendarHelper {

    private val MONTH_NAMES = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "Sol", "July", "August", "September", "October", "November", "December"
    )

    private val WEEKDAYS = arrayOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun currentFixedDate(): FixedDate = fromTimestamp(currentTimeMillis())

    fun getMonthName(monthIndex: Int): String {
        if (monthIndex in 1..13) {
            return MONTH_NAMES[monthIndex - 1]
        }
        return "Unknown"
    }

    /**
     * Converts a 12-month Gregorian date (Year, Month 1-12, Day 1-31) to 13-month FixedDate
     */
    fun gregorianToFixed(year: Int, month: Int, day: Int): FixedDate {
        val daysInMonths = if (isLeapYear(year)) {
            intArrayOf(0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        } else {
            intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        }

        val safeMonth = month.coerceIn(1, 12)
        val maxDayInM = daysInMonths[safeMonth]
        val safeDay = day.coerceIn(1, maxDayInM)

        var dayOfYear = safeDay
        for (m in 1 until safeMonth) {
            dayOfYear += daysInMonths[m]
        }

        return fromDayOfYear(year, dayOfYear)
    }

    /**
     * Converts a Gregorian day of year (1-365/366) to 13-month FixedDate
     */
    fun fromDayOfYear(year: Int, dayOfYear: Int): FixedDate {
        val isLeap = isLeapYear(year)
        return if (isLeap) {
            when {
                dayOfYear == 169 -> FixedDate(year, 6, 29, isLeapDay = true)
                dayOfYear == 366 -> FixedDate(year, 13, 29, isYearDay = true)
                dayOfYear > 169 -> {
                    val shiftedDay = dayOfYear - 1
                    val month = ((shiftedDay - 1) / 28) + 1
                    val day = ((shiftedDay - 1) % 28) + 1
                    FixedDate(year, month, day)
                }
                else -> {
                    val month = ((dayOfYear - 1) / 28) + 1
                    val day = ((dayOfYear - 1) % 28) + 1
                    FixedDate(year, month, day)
                }
            }
        } else {
            if (dayOfYear == 365) {
                FixedDate(year, 13, 29, isYearDay = true)
            } else {
                val month = ((dayOfYear - 1) / 28) + 1
                val day = ((dayOfYear - 1) % 28) + 1
                FixedDate(year, month, day)
            }
        }
    }

    /**
     * Converts a 13-month FixedDate to a Gregorian day of year (1-365/366)
     */
    fun toDayOfYear(fixedDate: FixedDate): Int {
        val isLeap = isLeapYear(fixedDate.year)
        return if (isLeap) {
            when {
                fixedDate.isLeapDay || (fixedDate.month == 6 && fixedDate.day == 29) -> 169
                fixedDate.isYearDay || (fixedDate.month == 13 && fixedDate.day == 29) -> 366
                fixedDate.month > 6 -> {
                    val baseDay = (fixedDate.month - 1) * 28 + fixedDate.day
                    baseDay + 1
                }
                else -> {
                    (fixedDate.month - 1) * 28 + fixedDate.day
                }
            }
        } else {
            if (fixedDate.isYearDay || (fixedDate.month == 13 && fixedDate.day == 29)) {
                365
            } else {
                (fixedDate.month - 1) * 28 + fixedDate.day
            }
        }
    }

    private fun floorDiv(a: Long, b: Long): Int {
        val res = a / b
        val rem = a % b
        return (if (rem != 0L && ((a < 0) xor (b < 0))) res - 1 else res).toInt()
    }

    fun fromEpochDay(epochDay: Long): FixedDate {
        return fromTimestamp(epochDay * 86400000L)
    }

    /**
     * Maps a UTC timestamp (milliseconds since epoch) to local FixedDate using local timezone offset
     */
    fun fromTimestamp(timestampMs: Long): FixedDate {
        val offset = getTimeZoneOffset(timestampMs)
        val localMs = timestampMs + offset
        val totalDays = floorDiv(localMs, 86400000L)

        var year = 1970
        var remainingDays = totalDays

        if (remainingDays >= 0) {
            while (true) {
                val daysInYear = if (isLeapYear(year)) 366 else 365
                if (remainingDays < daysInYear) {
                    break
                }
                remainingDays -= daysInYear
                year++
            }
        } else {
            while (remainingDays < 0) {
                year--
                val daysInYear = if (isLeapYear(year)) 366 else 365
                remainingDays += daysInYear
            }
        }

        val dayOfYear = remainingDays + 1
        return fromDayOfYear(year, dayOfYear)
    }

    /**
     * Converts local FixedDate and Optional time string ("HH:mm") back to UTC epoch milliseconds
     */
    fun toTimestamp(fixedDate: FixedDate, timeStr: String? = null): Long {
        val dayOfYear = toDayOfYear(fixedDate)
        var daysCount = 0L
        if (fixedDate.year >= 1970) {
            for (y in 1970 until fixedDate.year) {
                daysCount += if (isLeapYear(y)) 366 else 365
            }
        } else {
            for (y in fixedDate.year until 1970) {
                daysCount -= if (isLeapYear(y)) 366 else 365
            }
        }
        daysCount += (dayOfYear - 1)

        var localMs = daysCount * 86400000L
        if (!timeStr.isNullOrEmpty()) {
            try {
                val clean = timeStr.replace(" Daily", "", ignoreCase = true).trim()
                val isPM = clean.contains("PM", ignoreCase = true)
                val isAM = clean.contains("AM", ignoreCase = true)
                val digitsOnly = clean.replace("AM", "", ignoreCase = true).replace("PM", "", ignoreCase = true).trim()
                val parts = digitsOnly.split(":")
                var h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 0
                val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

                if (isPM && h < 12) h += 12
                if (isAM && h == 12) h = 0

                localMs += (h * 3600L + m * 60L) * 1000L
            } catch (_: Exception) {}
        }

        val approxUtc = localMs - getTimeZoneOffset(localMs)
        val offset = getTimeZoneOffset(approxUtc)
        return localMs - offset
    }

    fun getDayOfWeek(fixedDate: FixedDate): String {
        if (fixedDate.isLeapDay || (fixedDate.month == 6 && fixedDate.day == 29)) return "Leap Day (Leave)"
        if (fixedDate.isYearDay || (fixedDate.month == 13 && fixedDate.day == 29)) return "Sol Day (Leave Day)"
        val index = (fixedDate.day - 1) % 7
        return WEEKDAYS[index]
    }

    fun getDayOfWeekIndex(fixedDate: FixedDate): Int {
        if (fixedDate.isLeapDay || fixedDate.isYearDay) return -1
        return (fixedDate.day - 1) % 7
    }

    fun parseDateStr(dateStr: String): FixedDate? {
        return try {
            val parts = dateStr.split("-")
            if (parts.size != 3) return null
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val isLeap = isLeapYear(y)
            val isLD = isLeap && m == 6 && d == 29
            val isYD = m == 13 && d == 29
            FixedDate(y, m, d, isLeapDay = isLD, isYearDay = isYD)
        } catch (e: Exception) {
            null
        }
    }

    fun getPreviousDateStr(dateStr: String): String {
        val current = parseDateStr(dateStr) ?: return ""
        val (year, month, day) = Triple(current.year, current.month, current.day)

        return if (day > 1) {
            val prevDay = day - 1
            FixedDate(year, month, prevDay, isLeapDay = (isLeapYear(year) && month == 6 && prevDay == 29), isYearDay = (month == 13 && prevDay == 29)).toString()
        } else if (month > 1) {
            val prevMonth = month - 1
            val prevDay = if (prevMonth == 6 && isLeapYear(year)) 29 else 28
            FixedDate(year, prevMonth, prevDay, isLeapDay = (isLeapYear(year) && month == 6 && prevDay == 29), isYearDay = (month == 13 && prevDay == 29)).toString()
        } else {
            val prevYear = year - 1
            FixedDate(prevYear, 13, 29, isYearDay = true).toString()
        }
    }

    fun daysBetween(dateStr1: String, dateStr2: String): Int {
        val d1 = parseDateStr(dateStr1) ?: return 0
        val d2 = parseDateStr(dateStr2) ?: return 0
        val t1 = toTimestamp(d1)
        val t2 = toTimestamp(d2)
        return ((t2 - t1) / 86400000L).toInt()
    }

    fun addMonths(date: FixedDate, monthsToAdd: Int): FixedDate {
        var newMonth = date.month + monthsToAdd
        var newYear = date.year
        while (newMonth > 13) {
            newMonth -= 13
            newYear++
        }
        while (newMonth < 1) {
            newMonth += 13
            newYear--
        }
        val maxDays = if (newMonth == 6 && isLeapYear(newYear)) 29 else if (newMonth == 13) 29 else 28
        val newDay = date.day.coerceAtMost(maxDays)
        return FixedDate(newYear, newMonth, newDay, isLeapDay = (isLeapYear(newYear) && newMonth == 6 && newDay == 29), isYearDay = (newMonth == 13 && newDay == 29))
    }

    fun shouldGenerateInstance(recurringTask: RecurringTask, date: FixedDate): Boolean {
        if (!recurringTask.isActive) return false

        val createdFixed = fromTimestamp(recurringTask.createdAt)

        if (date.year < createdFixed.year) return false
        if (date.year == createdFixed.year && date.month < createdFixed.month) return false
        if (date.year == createdFixed.year && date.month == createdFixed.month && date.day < createdFixed.day) return false

        if (!recurringTask.endDate.isNullOrEmpty()) {
            val endFixed = parseDateStr(recurringTask.endDate)
            if (endFixed != null) {
                if (date.year > endFixed.year) return false
                if (date.year == endFixed.year && date.month > endFixed.month) return false
                if (date.year == endFixed.year && date.month == endFixed.month && date.day > endFixed.day) return false
            }
        }

        return true
    }
}
