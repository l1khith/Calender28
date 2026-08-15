package com.l1khith.calender28

import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class FixedCalendarHelperTest {

    // ═════════════════════════════════════════════════════════════════
    // 1. isLeapYear() Tests (4 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testIsLeapYear_standardLeapYear() {
        assertTrue(FixedCalendarHelper.isLeapYear(2024))
    }

    @Test
    fun testIsLeapYear_centuryLeapYear() {
        assertTrue(FixedCalendarHelper.isLeapYear(2000))
    }

    @Test
    fun testIsLeapYear_nonLeapYear() {
        assertFalse(FixedCalendarHelper.isLeapYear(2025))
    }

    @Test
    fun testIsLeapYear_centuryNonLeapYear() {
        assertFalse(FixedCalendarHelper.isLeapYear(1900))
    }

    // ═════════════════════════════════════════════════════════════════
    // 2. getMonthName() Tests (15 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testGetMonthName_all13Months() {
        val expected = listOf(
            "January", "February", "March", "April", "May", "June",
            "Sol", "July", "August", "September", "October", "November", "December"
        )
        expected.forEachIndexed { index, name ->
            assertEquals(name, FixedCalendarHelper.getMonthName(index + 1))
        }
    }

    @Test
    fun testGetMonthName_outOfBoundsLow() {
        assertEquals("Unknown", FixedCalendarHelper.getMonthName(0))
    }

    @Test
    fun testGetMonthName_outOfBoundsHigh() {
        assertEquals("Unknown", FixedCalendarHelper.getMonthName(14))
    }

    // ═════════════════════════════════════════════════════════════════
    // 3. addMonths() Tests (8 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testAddMonths_sameYear() {
        val baseDate = FixedDate(2026, 5, 15)
        val nextMonth = FixedCalendarHelper.addMonths(baseDate, 2)
        assertEquals(2026, nextMonth.year)
        assertEquals(7, nextMonth.month)
        assertEquals(15, nextMonth.day)
    }

    @Test
    fun testAddMonths_overflowYear() {
        val baseDate = FixedDate(2026, 5, 15)
        val overflowYear = FixedCalendarHelper.addMonths(baseDate, 10)
        assertEquals(2027, overflowYear.year)
        assertEquals(2, overflowYear.month)
    }

    @Test
    fun testAddMonths_cross13thMonthBoundary() {
        val decDate = FixedDate(2026, 13, 1)
        val nextDate = FixedCalendarHelper.addMonths(decDate, 1)
        assertEquals(2027, nextDate.year)
        assertEquals(1, nextDate.month)
        assertEquals(1, nextDate.day)
    }

    @Test
    fun testAddMonths_negativeDelta() {
        val baseDate = FixedDate(2026, 3, 10)
        val prevDate = FixedCalendarHelper.addMonths(baseDate, -2)
        assertEquals(2026, prevDate.year)
        assertEquals(1, prevDate.month)
        assertEquals(10, prevDate.day)
    }

    @Test
    fun testAddMonths_negativeDeltaCrossYear() {
        val janDate = FixedDate(2026, 1, 15)
        val prevYearDate = FixedCalendarHelper.addMonths(janDate, -1)
        assertEquals(2025, prevYearDate.year)
        assertEquals(13, prevYearDate.month)
        assertEquals(15, prevYearDate.day)
    }

    @Test
    fun testAddMonths_preserveDay28() {
        val date = FixedDate(2026, 1, 28)
        val result = FixedCalendarHelper.addMonths(date, 3)
        assertEquals(28, result.day)
    }

    @Test
    fun testAddMonths_leapDayHandling() {
        val leapDate = FixedDate(2024, 6, 29, isLeapDay = true)
        val result = FixedCalendarHelper.addMonths(leapDate, 1)
        assertEquals(7, result.month)
        assertEquals(28, result.day)
    }

    @Test
    fun testAddMonths_yearDayHandling() {
        val yearDate = FixedDate(2026, 13, 29, isYearDay = true)
        val result = FixedCalendarHelper.addMonths(yearDate, 1)
        assertEquals(2027, result.year)
        assertEquals(1, result.month)
        assertEquals(28, result.day)
    }

    // ═════════════════════════════════════════════════════════════════
    // 4. fromTimestamp() & toTimestamp() Tests (12 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testFromTimestamp_epochZero() {
        val result = FixedCalendarHelper.fromTimestamp(0L)
        assertNotNull(result)
        assertTrue(result.year in 1969..1970)
    }

    @Test
    fun testFromTimestamp_knownDate() {
        val date = FixedDate(2026, 8, 14)
        val ts = FixedCalendarHelper.toTimestamp(date)
        val roundtrip = FixedCalendarHelper.fromTimestamp(ts)
        assertEquals(date.year, roundtrip.year)
        assertEquals(date.month, roundtrip.month)
        assertEquals(date.day, roundtrip.day)
    }

    @Test
    fun testToTimestamp_withTimeAM() {
        val date = FixedDate(2026, 1, 1)
        val morningTs = FixedCalendarHelper.toTimestamp(date, "09:00 AM")
        val eveningTs = FixedCalendarHelper.toTimestamp(date, "09:00 PM")
        assertNotNull(morningTs)
        assertNotNull(eveningTs)
        assertTrue(eveningTs > morningTs)
        assertEquals(12 * 60 * 60 * 1000L, eveningTs - morningTs)
    }

    @Test
    fun testToTimestamp_invalidTimeFormat() {
        val date = FixedDate(2026, 1, 1)
        val ts = FixedCalendarHelper.toTimestamp(date, "invalid_time")
        assertNotNull(ts) // falls back to start of day
    }

    @Test
    fun testToTimestamp_nullTime() {
        val date = FixedDate(2026, 1, 1)
        val ts = FixedCalendarHelper.toTimestamp(date, null)
        assertNotNull(ts)
    }

    @Test
    fun testFromTimestamp_all28Days() {
        for (day in 1..28) {
            val date = FixedDate(2026, 1, day)
            val ts = FixedCalendarHelper.toTimestamp(date)
            val result = FixedCalendarHelper.fromTimestamp(ts)
            assertEquals(day, result.day)
        }
    }

    @Test
    fun testFromTimestamp_all13Months() {
        for (month in 1..13) {
            val date = FixedDate(2026, month, 1)
            val ts = FixedCalendarHelper.toTimestamp(date)
            val result = FixedCalendarHelper.fromTimestamp(ts)
            assertEquals(month, result.month)
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // 5. parseDateStr() Tests (4 cases)
    // ═════════════════════════════════════════════════════════════════

    @Test
    fun testParseDateStr_validISO() {
        val date = FixedCalendarHelper.parseDateStr("2026-08-14")
        assertNotNull(date)
        assertEquals(2026, date!!.year)
        assertEquals(8, date.month)
        assertEquals(14, date.day)
    }

    @Test
    fun testParseDateStr_invalidFormat() {
        assertNull(FixedCalendarHelper.parseDateStr("14/08/2026"))
        assertNull(FixedCalendarHelper.parseDateStr("invalid"))
        assertNull(FixedCalendarHelper.parseDateStr(""))
    }

    // ═════════════════════════════════════════════════════════════════
    // 6. Recurrence Logic Tests (4 cases)
    // ═════════════════════════════════════════════════════════════════

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

    @Test
    fun testShouldGenerateInstanceInactiveTask() {
        val task = RecurringTask(
            id = "rec2",
            title = "Gym Inactive",
            description = null,
            recurrenceType = RecurrenceType.DAILY,
            recurrenceDays = emptyList(),
            recurrenceInterval = 1,
            priority = 1,
            isActive = false,
            createdAt = 1700000000000L
        )
        val testDate = FixedCalendarHelper.fromTimestamp(1710000000000L)
        assertFalse(FixedCalendarHelper.shouldGenerateInstance(task, testDate))
    }

    @Test
    fun testShouldGenerateInstanceWeekly() {
        val task = RecurringTask(
            id = "rec3",
            title = "Weekly Meeting",
            description = null,
            recurrenceType = RecurrenceType.WEEKLY,
            recurrenceDays = listOf(1), // Monday
            recurrenceInterval = 1,
            priority = 1,
            isActive = true,
            createdAt = 1700000000000L
        )
        val mondayDate = FixedDate(2026, 1, 2) // Day 2 is Monday in 13-month calendar
        assertTrue(FixedCalendarHelper.shouldGenerateInstance(task, mondayDate))
    }

    @Test
    fun testShouldGenerateInstanceMonthly() {
        val task = RecurringTask(
            id = "rec4",
            title = "Rent Payment",
            description = null,
            recurrenceType = RecurrenceType.MONTHLY,
            recurrenceDays = listOf(1),
            recurrenceInterval = 1,
            priority = 1,
            isActive = true,
            createdAt = 1700000000000L
        )
        val testDate = FixedCalendarHelper.fromTimestamp(1710000000000L)
        val beforeCreatedDate = FixedCalendarHelper.fromTimestamp(1690000000000L)
        assertTrue(FixedCalendarHelper.shouldGenerateInstance(task, testDate))
        assertFalse(FixedCalendarHelper.shouldGenerateInstance(task, beforeCreatedDate))
    }
}
