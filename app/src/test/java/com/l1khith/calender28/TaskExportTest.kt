package com.l1khith.calender28

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.repository.IcsParserRepository
import org.junit.Assert.*
import org.junit.Test

class TaskExportTest {

    private val sampleTasks = listOf(
        AppTask(
            id = "task_aug_1",
            title = "August Budget Review",
            description = "Monthly financial check",
            associatedDate = "2026-08-05",
            reminderTime = "10:00 AM",
            isCompleted = 0,
            priority = 2
        ),
        AppTask(
            id = "task_aug_2",
            title = "August Matrix Planning",
            description = "Quarterly alignment",
            associatedDate = "2026-08-20",
            reminderTime = "02:30 PM",
            isCompleted = 1,
            priority = 3
        ),
        AppTask(
            id = "task_jul_1",
            title = "July Retrospective",
            description = "Previous month task",
            associatedDate = "2026-07-15",
            reminderTime = "11:00 AM",
            isCompleted = 1,
            priority = 1
        )
    )

    @Test
    fun `test filtering tasks for current month only`() {
        val year = 2026
        val month = 8
        val monthStr = month.toString().padStart(2, '0')
        val prefix = "$year-$monthStr"

        val currentMonthTasks = sampleTasks.filter { it.associatedDate.startsWith(prefix) }
        assertEquals(2, currentMonthTasks.size)
        assertTrue(currentMonthTasks.all { it.associatedDate.startsWith("2026-08") })
    }

    @Test
    fun `test export current month tasks to CSV`() {
        val augustTasks = sampleTasks.filter { it.associatedDate.startsWith("2026-08") }
        val csv = IcsParserRepository.exportToCsv(augustTasks)

        assertTrue(csv.startsWith("ID,Title,Description,Date,ReminderTime,Completed,Priority"))
        assertTrue(csv.contains("August Budget Review"))
        assertTrue(csv.contains("August Matrix Planning"))
        assertFalse(csv.contains("July Retrospective"))
    }

    @Test
    fun `test export current month tasks to ICS`() {
        val augustTasks = sampleTasks.filter { it.associatedDate.startsWith("2026-08") }
        val ics = IcsParserRepository.exportToIcs(augustTasks)

        assertTrue(ics.contains("BEGIN:VCALENDAR"))
        assertTrue(ics.contains("SUMMARY:August Budget Review"))
        assertTrue(ics.contains("SUMMARY:August Matrix Planning"))
        assertFalse(ics.contains("July Retrospective"))
        assertTrue(ics.contains("END:VCALENDAR"))
    }

    @Test
    fun `test export current month tasks to JSON`() {
        val augustTasks = sampleTasks.filter { it.associatedDate.startsWith("2026-08") }
        val json = IcsParserRepository.exportToJson(augustTasks)

        assertTrue(json.contains("\"title\":\"August Budget Review\""))
        assertTrue(json.contains("\"associatedDate\":\"2026-08-05\""))
        assertFalse(json.contains("July Retrospective"))
    }
}
