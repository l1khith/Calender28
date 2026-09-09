package com.l1khith.calender28

import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteDao
import com.l1khith.calender28.data.NoteEntity
import com.l1khith.calender28.repository.NoteRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeNoteDao : NoteDao {
    private val notes = mutableMapOf<String, NoteEntity>()
    private val notesFlow = MutableStateFlow<List<NoteEntity>>(emptyList())

    private fun emit() {
        val sorted = notes.values.sortedWith(
            compareByDescending<NoteEntity> { it.is_pinned }
                .thenByDescending { it.updated_at_ms }
        )
        notesFlow.value = sorted
    }

    override fun observeAllNotes(): Flow<List<NoteEntity>> = notesFlow

    override suspend fun getAllNotes(): List<NoteEntity> = notesFlow.value

    override suspend fun getNoteById(id: String): NoteEntity? = notes[id]

    override fun observeNotesForDate(dateStr: String): Flow<List<NoteEntity>> =
        notesFlow.map { list -> list.filter { it.associated_date == dateStr } }

    override fun observeNotesByEntity(entityType: String): Flow<List<NoteEntity>> =
        notesFlow.map { list -> list.filter { it.linked_entity.equals(entityType, ignoreCase = true) } }

    override suspend fun insertNote(note: NoteEntity): Long {
        notes[note.id] = note
        emit()
        return 1L
    }

    override suspend fun updateNote(note: NoteEntity): Int {
        notes[note.id] = note
        emit()
        return 1
    }

    override suspend fun deleteNote(id: String): Int {
        val removed = notes.remove(id) != null
        if (removed) emit()
        return if (removed) 1 else 0
    }

    override fun searchNotes(query: String): Flow<List<NoteEntity>> =
        notesFlow.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
}

class NoteSystemTest {

    private lateinit var fakeDao: FakeNoteDao
    private lateinit var repository: NoteRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeNoteDao()
        repository = NoteRepositoryImpl(fakeDao)
    }

    @Test
    fun `saveNote persists note with linkedEntity and isMarkdown`() = runBlocking {
        val note = Note(
            id = "note_1",
            title = "Morning Reflection",
            content = "Completed my 7-day streak today.",
            isPinned = false,
            colorHex = "#3B82F6",
            linkedEntity = "HABIT",
            isMarkdown = true
        )

        val result = repository.saveNote(note)
        assertTrue(result.isSuccess)

        val retrieved = repository.getNoteById("note_1")
        assertNotNull(retrieved)
        assertEquals("Morning Reflection", retrieved?.title)
        assertEquals("Completed my 7-day streak today.", retrieved?.content)
        assertEquals("#3B82F6", retrieved?.colorHex)
        assertEquals("HABIT", retrieved?.linkedEntity)
        assertTrue(retrieved?.isMarkdown == true)
        assertFalse(retrieved?.isPinned ?: true)
    }

    @Test
    fun `togglePin toggles note pinned state`() = runBlocking {
        val note = Note(
            id = "note_pin_test",
            title = "Important Checklist",
            content = "Keep this pinned at the top",
            isPinned = false
        )
        repository.saveNote(note)

        val toggleResult1 = repository.togglePin("note_pin_test")
        assertTrue(toggleResult1.isSuccess)

        val pinnedNote = repository.getNoteById("note_pin_test")
        assertTrue(pinnedNote?.isPinned == true)

        val toggleResult2 = repository.togglePin("note_pin_test")
        assertTrue(toggleResult2.isSuccess)

        val unpinnedNote = repository.getNoteById("note_pin_test")
        assertFalse(unpinnedNote?.isPinned == true)
    }

    @Test
    fun `deleteNote removes note from repository`() = runBlocking {
        val note = Note(
            id = "note_to_delete",
            title = "Delete Me",
            content = "Temporary scratchpad"
        )
        repository.saveNote(note)
        assertNotNull(repository.getNoteById("note_to_delete"))

        val deleteResult = repository.deleteNote("note_to_delete")
        assertTrue(deleteResult.isSuccess)

        assertNull(repository.getNoteById("note_to_delete"))
    }

    @Test
    fun `searchNotes filters by title or content`() = runBlocking {
        repository.saveNote(Note(id = "1", title = "Grocery List", content = "Apples, bananas, oats"))
        repository.saveNote(Note(id = "2", title = "Workout Plan", content = "Leg day: squats, lunges"))
        repository.saveNote(Note(id = "3", title = "App Architecture", content = "Review Room migration"))

        val searchApples = repository.searchNotes("apples").first()
        assertEquals(1, searchApples.size)
        assertEquals("Grocery List", searchApples[0].title)

        val searchPlan = repository.searchNotes("Plan").first()
        assertEquals(1, searchPlan.size)
        assertEquals("Workout Plan", searchPlan[0].title)

        val searchNotFound = repository.searchNotes("NonExistentKeyword").first()
        assertTrue(searchNotFound.isEmpty())
    }

    @Test
    fun `observeNotesByEntity filters by linked entity`() = runBlocking {
        repository.saveNote(Note(id = "1", title = "Habit note", content = "Read 20 mins", linkedEntity = "HABIT"))
        repository.saveNote(Note(id = "2", title = "Task note", content = "File taxes", linkedEntity = "TASK"))
        repository.saveNote(Note(id = "3", title = "Cycle note", content = "End of month", linkedEntity = "CYCLE"))

        val habitNotes = repository.observeNotesByEntity("HABIT").first()
        assertEquals(1, habitNotes.size)
        assertEquals("Habit note", habitNotes[0].title)

        val taskNotes = repository.observeNotesByEntity("TASK").first()
        assertEquals(1, taskNotes.size)
        assertEquals("Task note", taskNotes[0].title)
    }

    @Test
    fun `pinned notes appear before unpinned notes in observed flow`() = runBlocking {
        repository.saveNote(Note(id = "1", title = "Unpinned 1", content = "First", isPinned = false, updatedAtMs = 1000L))
        repository.saveNote(Note(id = "2", title = "Unpinned 2", content = "Second", isPinned = false, updatedAtMs = 2000L))
        repository.saveNote(Note(id = "3", title = "Pinned Note", content = "Crucial", isPinned = true, updatedAtMs = 500L))

        val notesList = repository.observeAllNotes().first()
        assertEquals(3, notesList.size)
        assertEquals("Pinned Note", notesList[0].title)
        assertTrue(notesList[0].isPinned)
    }
}
