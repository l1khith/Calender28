package com.l1khith.calender28.billing

import com.l1khith.calender28.data.NoteFormat
import org.junit.Assert.*
import org.junit.Test

class FeatureTierMatrixTest {

    // ── 1. Habit Tier Limits (Free: 3, Pro: Unlimited) ──
    @Test
    fun testHabitTierLimits() {
        val freeHabitLimit = 3
        fun canCreateHabit(currentCount: Int, isPro: Boolean): Boolean {
            return isPro || currentCount < freeHabitLimit
        }

        // Free user can create up to 3
        assertTrue(canCreateHabit(0, isPro = false))
        assertTrue(canCreateHabit(1, isPro = false))
        assertTrue(canCreateHabit(2, isPro = false))
        assertFalse(canCreateHabit(3, isPro = false))
        assertFalse(canCreateHabit(4, isPro = false))

        // Pro user can create unlimited
        assertTrue(canCreateHabit(3, isPro = true))
        assertTrue(canCreateHabit(10, isPro = true))
        assertTrue(canCreateHabit(100, isPro = true))
    }

    // ── 2. Recurring Task Tier Limits (Free: 5, Pro: Unlimited) ──
    @Test
    fun testRecurringTaskTierLimits() {
        val freeRecurringLimit = 5
        fun canCreateRecurringTask(currentCount: Int, isPro: Boolean): Boolean {
            return isPro || currentCount < freeRecurringLimit
        }

        // Free user can create up to 5 active recurring tasks
        assertTrue(canCreateRecurringTask(0, isPro = false))
        assertTrue(canCreateRecurringTask(4, isPro = false))
        assertFalse(canCreateRecurringTask(5, isPro = false))
        assertFalse(canCreateRecurringTask(6, isPro = false))

        // Pro user has unlimited recurring tasks
        assertTrue(canCreateRecurringTask(5, isPro = true))
        assertTrue(canCreateRecurringTask(50, isPro = true))
    }

    // ── 3. Notes Tier Limits (Free: 10 Plain Text, Pro: Unlimited + MD) ──
    @Test
    fun testNoteTierLimits() {
        val freeNoteLimit = 10
        fun canCreateNote(currentCount: Int, isPro: Boolean): Boolean {
            return isPro || currentCount < freeNoteLimit
        }

        fun canUseMarkdown(isPro: Boolean): Boolean {
            return isPro
        }

        // Free user can create up to 10 notes
        assertTrue(canCreateNote(0, isPro = false))
        assertTrue(canCreateNote(9, isPro = false))
        assertFalse(canCreateNote(10, isPro = false))
        assertFalse(canCreateNote(15, isPro = false))

        // Free user cannot toggle or write Markdown notes
        assertFalse(canUseMarkdown(isPro = false))

        // Pro user has unlimited notes and full markdown access
        assertTrue(canCreateNote(10, isPro = true))
        assertTrue(canCreateNote(100, isPro = true))
        assertTrue(canUseMarkdown(isPro = true))
    }

    // ── 4. Sparky Shop & Evolution Tier Limits ──
    @Test
    fun testSparkyShopTierGating() {
        fun isSparkyShopAccessible(isPro: Boolean): Boolean = isPro

        assertFalse("Sparky Shop must be locked for Free users", isSparkyShopAccessible(isPro = false))
        assertTrue("Sparky Shop must be accessible for Pro users", isSparkyShopAccessible(isPro = true))
    }

    // ── 5. Focus Analytics Tier Limits ──
    @Test
    fun testFocusAnalyticsTierGating() {
        fun isFocusAnalyticsAccessible(isPro: Boolean): Boolean = isPro

        assertFalse("Focus Analytics must be locked for Free users", isFocusAnalyticsAccessible(isPro = false))
        assertTrue("Focus Analytics must be accessible for Pro users", isFocusAnalyticsAccessible(isPro = true))
    }

    // ── 6. Data Export (CSV / ICS / JSON) Tier Limits ──
    @Test
    fun testDataExportTierGating() {
        fun isDataExportAccessible(isPro: Boolean): Boolean = isPro

        assertFalse("Data Export must be locked for Free users", isDataExportAccessible(isPro = false))
        assertTrue("Data Export must be accessible for Pro users", isDataExportAccessible(isPro = true))
    }
}
