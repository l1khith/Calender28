package com.l1khith.calender28.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at_ms DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY is_pinned DESC, updated_at_ms DESC")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE associated_date = :dateStr ORDER BY is_pinned DESC, updated_at_ms DESC")
    fun observeNotesForDate(dateStr: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE linked_entity = :entityType ORDER BY is_pinned DESC, updated_at_ms DESC")
    fun observeNotesByEntity(entityType: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String): Int

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY is_pinned DESC, updated_at_ms DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
}
