package com.l1khith.calender28

import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteDao
import com.l1khith.calender28.data.NoteEntity
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.repository.NoteRepository
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
    private val notesMap = mutableMapOf<String, NoteEntity>()
    private val notesFlow = MutableStateFlow<List<NoteEntity>>(emptyList())

    private fun emit() {
        notesFlow.value = notesMap.values
            .sortedWith(compareByDescending<NoteEntity> { it.isPinned }.thenByDescending { it.updatedAt })
    }

    override suspend fun insert(note: NoteEntity) {
        notesMap[note.id] = note
        emit()
    }

    override suspend fun update(note: NoteEntity) {
        notesMap[note.id] = note
        emit()
    }

    override suspend fun delete(note: NoteEntity) {
        notesMap.remove(note.id)
        emit()
    }

    override suspend fun deleteById(id: String) {
        notesMap.remove(id)
        emit()
    }

    override fun getAllNotes(): Flow<List<NoteEntity>> = notesFlow

    override fun searchNotes(query: String): Flow<List<NoteEntity>> =
        notesFlow.map { list ->
            list.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
        }

    override suspend fun getNoteById(id: String): NoteEntity? = notesMap[id]

    override fun getNotesForEntity(type: String, id: String): Flow<List<NoteEntity>> =
        notesFlow.map { list ->
            list.filter { it.linkedType.equals(type, ignoreCase = true) && it.linkedId == id }
        }

    override fun getNotesByType(type: String): Flow<List<NoteEntity>> =
        notesFlow.map { list ->
            list.filter { it.linkedType.equals(type, ignoreCase = true) }
        }

    override suspend fun getTitlesStartingWith(prefix: String): List<String> =
        notesMap.values.map { it.title }.filter { it.startsWith(prefix) }
}

class NoteSystemTest {

    private lateinit var fakeNoteDao: FakeNoteDao
    private lateinit var noteRepository: NoteRepository

    @Before
    fun setUp() {
        fakeNoteDao = FakeNoteDao()
        noteRepository = NoteRepositoryImpl(fakeNoteDao)
    }

    @Test
    fun `default note format is TXT`() {
        val note = Note(content = "Milk, Bread")
        assertEquals(NoteFormat.TXT, note.format)
        assertFalse(note.isMarkdown)
        assertEquals("text/plain", note.format.mimeType)
    }

    @Test
    fun `markdown note has MD format and markdown flag`() {
        val note = Note(
            content = "# Reflection\nGreat day!",
            format = NoteFormat.MD
        )
        assertEquals(NoteFormat.MD, note.format)
        assertTrue(note.isMarkdown)
        assertEquals("text/markdown", note.format.mimeType)
    }

    @Test
    fun `displayTitle derives from first line and strips markdown heading`() {
        val markdownNote = Note(content = "### Meeting Agenda\n1. Review goals\n2. Next steps", format = NoteFormat.MD)
        assertEquals("Meeting Agenda", markdownNote.displayTitle)
        assertEquals("1. Review goals 2. Next steps", markdownNote.snippetPreview)

        val plainNote = Note(content = "Grocery Shopping\nApples\nBananas", format = NoteFormat.TXT)
        assertEquals("Grocery Shopping", plainNote.displayTitle)
        assertEquals("Apples Bananas", plainNote.snippetPreview)

        val emptyNote = Note(content = "")
        assertEquals("Untitled Note", emptyNote.displayTitle)
        assertEquals("", emptyNote.snippetPreview)
    }

    @Test
    fun `save and retrieve note from repository`() = runBlocking {
        val note = Note(
            content = "# Task notes\nDetails about project",
            format = NoteFormat.MD
        )
        noteRepository.insertNote(note)

        val retrieved = noteRepository.getNoteById(note.id)
        assertNotNull(retrieved)
        assertEquals("Task notes", retrieved!!.displayTitle)
        assertEquals(NoteFormat.MD, retrieved.format)
    }

    @Test
    fun `search notes filters by content`() = runBlocking {
        val note1 = Note(content = "Morning Reflection\nFeeling energized")
        val note2 = Note(content = "Gym Schedule\nLeg day today")
        val note3 = Note(content = "Book Notes\nQuotes on discipline")

        noteRepository.insertNote(note1)
        noteRepository.insertNote(note2)
        noteRepository.insertNote(note3)

        val titleMatch = noteRepository.searchNotes("Morning").first()
        assertEquals(1, titleMatch.size)
        assertEquals("Morning Reflection", titleMatch[0].displayTitle)

        val contentMatch = noteRepository.searchNotes("discipline").first()
        assertEquals(1, contentMatch.size)
        assertEquals("Book Notes", contentMatch[0].displayTitle)
    }

    @Test
    fun `delete note removes it from repository`() = runBlocking {
        val note = Note(content = "Delete me")
        noteRepository.insertNote(note)

        var allNotes = noteRepository.getAllNotes().first()
        assertEquals(1, allNotes.size)

        noteRepository.deleteNote(note)
        allNotes = noteRepository.getAllNotes().first()
        assertTrue(allNotes.isEmpty())
    }

    @Test
    fun `pinned notes appear before unpinned notes`() = runBlocking {
        val unpinned = Note(id = "1", content = "Unpinned Note", isPinned = false, updatedAt = 1000)
        val pinned = Note(id = "2", content = "Pinned Note", isPinned = true, updatedAt = 500)

        noteRepository.insertNote(unpinned)
        noteRepository.insertNote(pinned)

        val list = noteRepository.getAllNotes().first()
        assertEquals(2, list.size)
        assertEquals("Pinned Note", list[0].displayTitle)
        assertEquals("Unpinned Note", list[1].displayTitle)
    }
}
