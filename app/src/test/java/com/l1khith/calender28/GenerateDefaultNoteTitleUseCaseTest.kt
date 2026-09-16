package com.l1khith.calender28

import com.l1khith.calender28.data.Note
import com.l1khith.calender28.domain.model.NoteContext
import com.l1khith.calender28.domain.usecase.notes.GenerateDefaultNoteTitleUseCase
import com.l1khith.calender28.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class GenerateDefaultNoteTitleUseCaseTest {

    private class FakeNoteRepositoryImpl : NoteRepository {
        val titles = mutableListOf<String>()

        override suspend fun insertNote(note: Note): Result<Unit> {
            titles.add(note.title)
            return Result.success(Unit)
        }
        override suspend fun updateNote(note: Note): Result<Unit> = Result.success(Unit)
        override suspend fun deleteNote(note: Note): Result<Unit> = Result.success(Unit)
        override suspend fun deleteNoteById(id: String): Result<Unit> = Result.success(Unit)
        override fun getAllNotes(): Flow<List<Note>> = emptyFlow()
        override fun searchNotes(query: String): Flow<List<Note>> = emptyFlow()
        override suspend fun getNoteById(id: String): Note? = null
        override fun getNotesForEntity(type: String, id: String): Flow<List<Note>> = emptyFlow()
        override fun getNotesByType(type: String): Flow<List<Note>> = emptyFlow()
        override suspend fun getTitlesStartingWith(prefix: String): List<String> {
            return titles.filter { it.startsWith(prefix) }
        }
    }

    private lateinit var fakeRepo: FakeNoteRepositoryImpl
    private lateinit var useCase: GenerateDefaultNoteTitleUseCase

    private val fixedUtc: TimeZone = TimeZone.getTimeZone("UTC")
    // Fixed timestamp: 2026-10-15 08:30:00 UTC
    private val fixedTimeMs: Long = Calendar.getInstance(fixedUtc).apply {
        set(Calendar.YEAR, 2026)
        set(Calendar.MONTH, Calendar.OCTOBER)
        set(Calendar.DAY_OF_MONTH, 15)
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 30)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Before
    fun setUp() {
        fakeRepo = FakeNoteRepositoryImpl()
        useCase = GenerateDefaultNoteTitleUseCase(fakeRepo)
    }

    @Test
    fun `standalone note generates date-time string in target timezone`() = runBlocking {
        val title = useCase(
            context = NoteContext.Standalone,
            timeMs = fixedTimeMs,
            timeZone = fixedUtc
        )
        assertEquals("2026-10-15 08:30", title)
    }

    @Test
    fun `fromTask formats prefix and date`() = runBlocking {
        val title = useCase(
            context = NoteContext.FromTask(taskId = "t1", title = "Write quarterly report"),
            timeMs = fixedTimeMs,
            timeZone = fixedUtc
        )
        assertEquals("Task: Write quarterly report — 2026-10-15 08:30", title)
    }

    @Test
    fun `fromTask sanitizes colons and truncates title to 40 characters`() = runBlocking {
        val longTitle = "Project: Deep Dive: Reviewing Architecture & System Designs For The Q4 Release"
        val title = useCase(
            context = NoteContext.FromTask(taskId = "t1", title = longTitle),
            timeMs = fixedTimeMs,
            timeZone = fixedUtc
        )
        // Sanitized: "Project- Deep Dive- Reviewing Architecture & System Designs For The Q4 Release"
        // take(40): "Project- Deep Dive- Reviewing Architectu"
        assertEquals("Task: Project- Deep Dive- Reviewing Architectu — 2026-10-15 08:30", title)
    }

    @Test
    fun `fromHabit formats prefix and sanitizes colons`() = runBlocking {
        val title = useCase(
            context = NoteContext.FromHabit(habitId = "h1", name = "Morning Routine: Stretches"),
            timeMs = fixedTimeMs,
            timeZone = fixedUtc
        )
        assertEquals("Habit: Morning Routine- Stretches — 2026-10-15 08:30", title)
    }

    @Test
    fun `fromCycle formats cycle index and date`() = runBlocking {
        val title = useCase(
            context = NoteContext.FromCycle(cycleIndex = 4),
            timeMs = fixedTimeMs,
            timeZone = fixedUtc
        )
        assertEquals("Cycle 4 — 2026-10-15 08:30", title)
    }

    @Test
    fun `increments duplicate counter when title already exists`() = runBlocking {
        fakeRepo.titles.add("2026-10-15 08:30")

        val title = useCase(
            context = NoteContext.Standalone,
            timeMs = fixedTimeMs,
            timeZone = fixedUtc
        )
        assertEquals("2026-10-15 08:30 (2)", title)
    }

    @Test
    fun `increments duplicate counter past existing numbered titles`() = runBlocking {
        fakeRepo.titles.add("2026-10-15 08:30")
        fakeRepo.titles.add("2026-10-15 08:30 (2)")

        val title = useCase(
            context = NoteContext.Standalone,
            timeMs = fixedTimeMs,
            timeZone = fixedUtc
        )
        assertEquals("2026-10-15 08:30 (3)", title)
    }
}
