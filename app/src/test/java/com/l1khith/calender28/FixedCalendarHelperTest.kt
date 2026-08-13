package com.l1khith.calender28

import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedCalendarHelperTest {

    @Test
    fun testIsLeapYear() {
        assertTrue(FixedCalendarHelper.isLeapYear(2024))
        assertTrue(FixedCalendarHelper.isLeapYear(2000))
        assertFalse(FixedCalendarHelper.isLeapYear(2025))
        assertFalse(FixedCalendarHelper.isLeapYear(1900))
    }

    @Test
    fun testGetMonthName() {
        assertEquals("January", FixedCalendarHelper.getMonthName(1))
        assertEquals("Sol", FixedCalendarHelper.getMonthName(7))
        assertEquals("December", FixedCalendarHelper.getMonthName(13))
        assertEquals("Unknown", FixedCalendarHelper.getMonthName(14))
    }

    @Test
    fun testAddMonths() {
        val baseDate = FixedDate(2026, 5, 15)
        val nextMonth = FixedCalendarHelper.addMonths(baseDate, 2)
        assertEquals(2026, nextMonth.year)
        assertEquals(7, nextMonth.month)
        assertEquals(15, nextMonth.day)

        val overflowYear = FixedCalendarHelper.addMonths(baseDate, 10)
        assertEquals(2027, overflowYear.year)
        assertEquals(2, overflowYear.month)
    }

    @Test
    fun testShouldGenerateInstanceDaily() {
        val task = RecurringTask(
            id = "rec1",
            title = "Gym",
            description = null,
            recurrenceType = RecurrenceType.DAILY,
            recurrenceDays = emptyList(),
            recurrenceInterval = 1,
            priority = 1,
            isActive = true,
            createdAt = 1700000000000L
        )
        val testDate = FixedCalendarHelper.fromTimestamp(1710000000000L)
        assertTrue(FixedCalendarHelper.shouldGenerateInstance(task, testDate))
    }
}
