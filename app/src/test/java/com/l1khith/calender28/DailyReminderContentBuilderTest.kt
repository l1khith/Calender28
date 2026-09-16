package com.l1khith.calender28

import com.l1khith.calender28.domain.model.BetStatus
import com.l1khith.calender28.domain.model.ConfidenceTier
import com.l1khith.calender28.service.DailyReminderContentBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyReminderContentBuilderTest {

    @Test
    fun `title uses user name when provided and trimmed`() {
        val content = DailyReminderContentBuilder.build(
            userName = "  Alice  ",
            taskCount = 2
        )
        assertEquals("Good morning, Alice!", content.title)
    }

    @Test
    fun `title falls back to Morning Briefing when user name is null or blank`() {
        val contentNull = DailyReminderContentBuilder.build(userName = null, taskCount = 1)
        assertEquals("Morning Briefing", contentNull.title)

        val contentBlank = DailyReminderContentBuilder.build(userName = "   ", taskCount = 1)
        assertEquals("Morning Briefing", contentBlank.title)
    }

    @Test
    fun `zero tasks without recovery day gives standard breather message`() {
        val content = DailyReminderContentBuilder.build(
            userName = "Bob",
            taskCount = 0,
            isRecoveryDay = false
        )
        assertEquals("No tasks scheduled for today. Plan ahead or take a breather!", content.message)
        assertFalse(content.openBetSheet)
    }

    @Test
    fun `zero tasks with recovery day gives recovery message`() {
        val content = DailyReminderContentBuilder.build(
            userName = "Bob",
            taskCount = 0,
            isRecoveryDay = true
        )
        assertEquals("No tasks scheduled today. Enjoy your recovery day!", content.message)
        assertFalse(content.openBetSheet)
    }

    @Test
    fun `single task includes title when available`() {
        val content = DailyReminderContentBuilder.build(
            userName = null,
            taskCount = 1,
            firstTaskTitle = "Buy groceries"
        )
        assertTrue(content.message.contains("You have 1 task today: Buy groceries."))
        assertFalse(content.openBetSheet)
    }

    @Test
    fun `single task without title uses generic format`() {
        val content = DailyReminderContentBuilder.build(
            userName = null,
            taskCount = 1,
            firstTaskTitle = null
        )
        assertEquals("You have 1 task scheduled for today.", content.message)
        assertFalse(content.openBetSheet)
    }

    @Test
    fun `multiple tasks format message properly`() {
        val content = DailyReminderContentBuilder.build(
            userName = null,
            taskCount = 2
        )
        assertEquals("You have 2 tasks scheduled for today.", content.message)
        assertFalse(content.openBetSheet)
    }

    @Test
    fun `recovery day with tasks explains recovery mode`() {
        val content = DailyReminderContentBuilder.build(
            userName = null,
            taskCount = 4,
            isRecoveryDay = true
        )
        assertTrue(content.message.contains("You have 4 tasks scheduled for today."))
        assertTrue(content.message.contains("Recovery Day: Take it easy, no bets today."))
        assertFalse(content.openBetSheet)
    }

    @Test
    fun `active bet status displays tier and required tasks`() {
        val activeBet = BetStatus.Active(
            tier = ConfidenceTier.B,
            snapshotTaskCount = 5,
            currentCompletedCount = 1,
            requiredToWin = 4,
            reward = 7,
            penalty = 5,
            currentStreak = 0
        )
        val content = DailyReminderContentBuilder.build(
            userName = null,
            taskCount = 5,
            betStatus = activeBet
        )
        assertTrue(content.message.contains("Confidence bet active: B tier (4 to win)."))
        assertFalse(content.openBetSheet)
    }

    @Test
    fun `ready to bet status prompts user and enables openBetSheet`() {
        val readyStatus = BetStatus.ReadyToBet(
            tier = ConfidenceTier.A,
            taskCount = 4,
            requiredToWin = 2,
            reward = 3,
            penalty = 2,
            currentStreak = 0
        )
        val content = DailyReminderContentBuilder.build(
            userName = null,
            taskCount = 4,
            betStatus = readyStatus
        )
        assertTrue(content.message.contains("Place your confidence bet before 12:00 PM!"))
        assertTrue(content.openBetSheet)
    }

    @Test
    fun `taskCount greater than or equal to 3 with null betStatus prompts bet`() {
        val content = DailyReminderContentBuilder.build(
            userName = null,
            taskCount = 3,
            betStatus = null
        )
        assertTrue(content.message.contains("Place your confidence bet before 12:00 PM!"))
        assertTrue(content.openBetSheet)
    }
}
