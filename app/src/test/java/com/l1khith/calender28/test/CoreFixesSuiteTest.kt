package com.l1khith.calender28.test

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.domain.model.GraphData
import com.l1khith.calender28.domain.model.ResolutionAction
import com.l1khith.calender28.domain.usecase.notes.BuildBatchGraphUseCase
import com.l1khith.calender28.domain.usecase.notes.BuildGraphUseCase
import com.l1khith.calender28.domain.usecase.notes.ParseNoteLinksUseCase
import com.l1khith.calender28.ui.notes.graph.ForceDirectedLayout
import com.l1khith.calender28.utils.ConflictResolver
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureTimeMillis

class CoreFixesSuiteTest {

    // ── F1: Notes Graph Tests (Markdown Only Constraint) ──

    @Test
    fun parseNoteLinks_markdownNote_extractsWikilinksAndMarkdownLinksAndTags() {
        val useCase = ParseNoteLinksUseCase()
        val mdNote = Note(
            id = "n1",
            content = "# Title\nSee [[Design Docs]] and [Roadmap](doc2). Also #matrix #v1",
            format = NoteFormat.MD
        )

        val result = useCase(mdNote)
        assertEquals(2, result.links.size)
        assertEquals("Design Docs", result.links[0].target)
        assertEquals("WIKILINK", result.links[0].type)
        assertEquals("doc2", result.links[1].target)
        assertEquals("MD_LINK", result.links[1].type)

        assertEquals(listOf("matrix", "v1"), result.tags)
    }

    @Test
    fun parseNoteLinks_plainTextNote_ignoredCompletely() {
        val useCase = ParseNoteLinksUseCase()
        val txtNote = Note(
            id = "n2",
            content = "This is a plain text note with [[IgnoreMe]] and [not a link](foo) #txt",
            format = NoteFormat.TXT
        )

        val result = useCase(txtNote)
        // Strictly zero links and zero tags extracted for non-MD notes
        assertTrue(result.links.isEmpty())
        assertTrue(result.tags.isEmpty())
    }

    @Test
    fun forceDirectedLayout_500NodesBenchmark_convergesUnder2Seconds() = runBlocking {
        val layout = ForceDirectedLayout()
        val nodes = (1..500).map { i ->
            com.l1khith.calender28.domain.model.GraphNode(
                id = "node_$i",
                title = "Node $i",
                x = (i % 20) * 15f,
                y = (i / 20) * 15f
            )
        }
        val edges = (1..400).map { i ->
            com.l1khith.calender28.domain.model.GraphEdge(
                id = "edge_$i",
                sourceId = "node_$i",
                targetId = "node_${i + 1}"
            )
        }
        val graphData = GraphData(nodes = nodes, edges = edges)

        val timeMs = measureTimeMillis {
            // Run 50 physics steps (typical animation frame window)
            repeat(50) {
                layout.step(graphData)
            }
        }

        println("⚡ [BENCHMARK] 500-node force-directed graph 50 steps: ${timeMs}ms")
        assertTrue("500-node physics benchmark exceeded 2000ms: was ${timeMs}ms", timeMs < 2000)
    }

    // ── F4: Conflict Resolution Free Slot Suggestion Tests ──

    @Test
    fun findNearestFreeSlots_suggestsThreeConflictFreeSlots() {
        val existingTasks = listOf(
            AppTask(
                id = "t1",
                title = "Meeting 1",
                associatedDate = "2026-01-15",
                reminderTime = "09:00",
                endTime = "10:00"
            ),
            AppTask(
                id = "t2",
                title = "Meeting 2",
                associatedDate = "2026-01-15",
                reminderTime = "10:00",
                endTime = "11:00"
            )
        )

        val slots = ConflictResolver.findNearestFreeSlots(
            candidateDate = "2026-01-15",
            durationMinutes = 60,
            existingTasks = existingTasks,
            ignoreTaskId = "t1",
            count = 3
        )

        assertEquals(3, slots.size)
        // All suggestions must not overlap with t2 (10:00-11:00)
        slots.forEach { slot ->
            val startMin = slot.startTime.substringBefore(":").toInt() * 60 + slot.startTime.substringAfter(":").toInt()
            val endMin = startMin + 60
            assertFalse(
                "Slot overlaps with t2",
                startMin < 11 * 60 && endMin > 10 * 60 && slot.dateStr == "2026-01-15"
            )
        }
    }

    // ── F5: Multi-Alarm Reminder Offsets Tests ──

    @Test
    fun appTask_effectiveReminderOffsets_fallbacksAndNormalizes() {
        val taskWithMulti = AppTask(
            id = "t1",
            title = "Task 1",
            associatedDate = "2026-01-15",
            reminderOffsetMin = 15,
            reminderOffsets = listOf(0, 10, 30)
        )
        // Multi-offsets take precedence
        assertEquals(listOf(0, 10, 30), taskWithMulti.effectiveReminderOffsets)

        val taskLegacyOnly = AppTask(
            id = "t2",
            title = "Legacy",
            associatedDate = "2026-01-15",
            reminderOffsetMin = 45,
            reminderOffsets = emptyList()
        )
        // Falls back to reminderOffsetMin
        assertEquals(listOf(45), taskLegacyOnly.effectiveReminderOffsets)

        val taskDefaults = AppTask(
            id = "t3",
            title = "Default",
            associatedDate = "2026-01-15",
            reminderOffsetMin = null,
            reminderOffsets = emptyList()
        )
        // Default is empty list (or at-event when reminder is scheduled)
        assertEquals(emptyList<Int>(), taskDefaults.effectiveReminderOffsets)
    }
}
