package com.l1khith.calender28.repository

import com.l1khith.calender28.data.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAllNotes(): Flow<List<Note>>
    fun observeNotesForDate(dateStr: String): Flow<List<Note>>
    fun observeNotesByEntity(entityType: String): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun saveNote(note: Note): Result<Unit>
    suspend fun togglePin(id: String): Result<Unit>
    suspend fun deleteNote(id: String): Result<Unit>
    fun searchNotes(query: String): Flow<List<Note>>
}
