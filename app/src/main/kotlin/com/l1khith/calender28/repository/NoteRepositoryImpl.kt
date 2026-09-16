package com.l1khith.calender28.repository

import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteDao
import com.l1khith.calender28.data.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of [NoteRepository] delegating to [NoteDao].
 */
class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {

    override suspend fun insertNote(note: Note): Result<Unit> = runCatching {
        noteDao.insert(NoteEntity.fromNote(note))
    }

    override suspend fun updateNote(note: Note): Result<Unit> = runCatching {
        noteDao.update(NoteEntity.fromNote(note))
    }

    override suspend fun deleteNote(note: Note): Result<Unit> = runCatching {
        noteDao.delete(NoteEntity.fromNote(note))
    }

    override suspend fun deleteNoteById(id: String): Result<Unit> = runCatching {
        noteDao.deleteById(id)
    }

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { list -> list.map { it.toNote() } }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { list -> list.map { it.toNote() } }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toNote()

    override fun getNotesForEntity(type: String, id: String): Flow<List<Note>> =
        noteDao.getNotesForEntity(type, id).map { list -> list.map { it.toNote() } }

    override fun getNotesByType(type: String): Flow<List<Note>> =
        noteDao.getNotesByType(type).map { list -> list.map { it.toNote() } }

    override suspend fun getTitlesStartingWith(prefix: String): List<String> =
        noteDao.getTitlesStartingWith(prefix)
}
