package com.l1khith.calender28.repository

import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteDao
import com.l1khith.calender28.data.NoteEntity
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.data.NoteLinkDao
import com.l1khith.calender28.data.NoteLinkEntity
import com.l1khith.calender28.domain.usecase.notes.ParseNoteLinksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of [NoteRepository] delegating to [NoteDao] and [NoteLinkDao].
 */
class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val noteLinkDao: NoteLinkDao? = null,
    private val parseNoteLinksUseCase: ParseNoteLinksUseCase = ParseNoteLinksUseCase()
) : NoteRepository {

    private suspend fun syncLinksForNote(note: Note) {
        val linkDao = noteLinkDao ?: return
        linkDao.deleteLinksForSource(note.id)

        // Strictly Markdown notes only
        if (note.format != NoteFormat.MD) return

        val parsed = parseNoteLinksUseCase(note)
        if (parsed.links.isNotEmpty()) {
            val entities = parsed.links.map { link ->
                NoteLinkEntity(
                    sourceNoteId = note.id,
                    targetNoteId = link.target,
                    linkType = link.type,
                    createdAtMs = System.currentTimeMillis()
                )
            }
            linkDao.insertLinks(entities)
        }
    }

    override suspend fun insertNote(note: Note): Result<Unit> = runCatching {
        noteDao.insert(NoteEntity.fromNote(note))
        syncLinksForNote(note)
    }

    override suspend fun updateNote(note: Note): Result<Unit> = runCatching {
        noteDao.update(NoteEntity.fromNote(note))
        syncLinksForNote(note)
    }

    override suspend fun deleteNote(note: Note): Result<Unit> = runCatching {
        noteDao.delete(NoteEntity.fromNote(note))
        noteLinkDao?.deleteLinksForNote(note.id)
    }

    override suspend fun deleteNoteById(id: String): Result<Unit> = runCatching {
        noteDao.deleteById(id)
        noteLinkDao?.deleteLinksForNote(id)
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
