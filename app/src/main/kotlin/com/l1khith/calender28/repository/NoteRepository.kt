package com.l1khith.calender28.repository

import com.l1khith.calender28.data.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notes data access and search abstraction.
 */
interface NoteRepository {
    suspend fun insertNote(note: Note): Result<Unit>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(note: Note): Result<Unit>
    suspend fun deleteNoteById(id: String): Result<Unit>
    fun getAllNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    fun getNotesForEntity(type: String, id: String): Flow<List<Note>>
    fun getNotesByType(type: String): Flow<List<Note>>
}
