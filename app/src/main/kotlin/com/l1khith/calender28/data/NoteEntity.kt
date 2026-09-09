package com.l1khith.calender28.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index("is_pinned"),
        Index("updated_at_ms"),
        Index("associated_date"),
        Index("linked_entity")
    ]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val is_pinned: Int = 0,
    val color_hex: String? = null,
    val associated_date: String? = null,
    val linked_entity: String = "NOTE",
    val is_markdown: Int = 0,
    val created_at_ms: Long = System.currentTimeMillis(),
    val updated_at_ms: Long = System.currentTimeMillis()
) {
    fun toNote(): Note = Note(
        id = id,
        title = title,
        content = content,
        isPinned = is_pinned == 1,
        colorHex = color_hex,
        associatedDate = associated_date,
        linkedEntity = linked_entity,
        isMarkdown = is_markdown == 1,
        createdAtMs = created_at_ms,
        updatedAtMs = updated_at_ms
    )

    companion object {
        fun fromNote(note: Note): NoteEntity = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            is_pinned = if (note.isPinned) 1 else 0,
            color_hex = note.colorHex,
            associated_date = note.associatedDate,
            linked_entity = note.linkedEntity,
            is_markdown = if (note.isMarkdown) 1 else 0,
            created_at_ms = note.createdAtMs,
            updated_at_ms = note.updatedAtMs
        )
    }
}
