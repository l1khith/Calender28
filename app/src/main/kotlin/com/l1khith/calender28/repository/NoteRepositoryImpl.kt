package com.l1khith.calender28.repository

import android.content.Context
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteDao
import com.l1khith.calender28.data.NoteEntity
import com.l1khith.calender28.data.RoomTaskDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {

    constructor(context: Context) : this(
        RoomTaskDatabase.getInstance(context.applicationContext).noteDao()
    )

    override fun observeAllNotes(): Flow<List<Note>> =
        noteDao.observeAllNotes()
            .map { list -> list.map { it.toNote() } }
            .flowOn(Dispatchers.IO)

    override fun observeNotesForDate(dateStr: String): Flow<List<Note>> =
        noteDao.observeNotesForDate(dateStr)
            .map { list -> list.map { it.toNote() } }
            .flowOn(Dispatchers.IO)

    override fun observeNotesByEntity(entityType: String): Flow<List<Note>> =
        noteDao.observeNotesByEntity(entityType)
            .map { list -> list.map { it.toNote() } }
            .flowOn(Dispatchers.IO)

    override suspend fun getNoteById(id: String): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)?.toNote()
    }

    override suspend fun saveNote(note: Note): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updated = note.copy(updatedAtMs = System.currentTimeMillis())
            noteDao.insertNote(NoteEntity.fromNote(updated))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun togglePin(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = noteDao.getNoteById(id)
                ?: return@withContext Result.failure(NoSuchElementException("Note not found with id=$id"))
            val newPinned = if (existing.is_pinned == 1) 0 else 1
            noteDao.updateNote(existing.copy(is_pinned = newPinned, updated_at_ms = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            noteDao.deleteNote(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query)
            .map { list -> list.map { it.toNote() } }
            .flowOn(Dispatchers.IO)
}
