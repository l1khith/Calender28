package com.l1khith.calender28.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing an explicit or inferred link between two notes.
 * Used for knowledge graph visualization and backlink tracking.
 */
@Entity(
    tableName = "note_links",
    indices = [
        Index("source_note_id"),
        Index("target_note_id")
    ]
)
data class NoteLinkEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    @ColumnInfo(name = "source_note_id")
    val sourceNoteId: String,
    @ColumnInfo(name = "target_note_id")
    val targetNoteId: String,
    @ColumnInfo(name = "link_type")
    val linkType: String, // "wiki", "markdown", "backlink"
    @ColumnInfo(name = "created_at_ms")
    val createdAtMs: Long = System.currentTimeMillis()
)
