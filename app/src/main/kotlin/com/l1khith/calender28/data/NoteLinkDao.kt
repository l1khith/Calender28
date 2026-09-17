package com.l1khith.calender28.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for note link relationships.
 */
@Dao
interface NoteLinkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: NoteLinkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<NoteLinkEntity>)

    @Query("DELETE FROM note_links WHERE source_note_id = :sourceNoteId")
    suspend fun deleteLinksForSource(sourceNoteId: String)

    @Query("DELETE FROM note_links WHERE source_note_id = :noteId OR target_note_id = :noteId")
    suspend fun deleteLinksForNote(noteId: String)

    @Query("SELECT * FROM note_links")
    fun getAllLinks(): Flow<List<NoteLinkEntity>>

    @Query("SELECT * FROM note_links")
    suspend fun getAllLinksList(): List<NoteLinkEntity>

    @Query("SELECT * FROM note_links WHERE source_note_id = :noteId OR target_note_id = :noteId")
    fun getLinksForNote(noteId: String): Flow<List<NoteLinkEntity>>

    @Query("SELECT * FROM note_links WHERE source_note_id = :noteId OR target_note_id = :noteId")
    suspend fun getLinksForNoteSync(noteId: String): List<NoteLinkEntity>

    @Query("SELECT * FROM note_links WHERE source_note_id IN (:noteIds) AND target_note_id IN (:noteIds)")
    suspend fun getLinksBetween(noteIds: List<String>): List<NoteLinkEntity>
}
