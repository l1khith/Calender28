package com.l1khith.calender28.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index("updatedAt"),
        Index("title"),
        Index("isPinned"),
        Index(value = ["linkedType", "linkedId"])
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val title: String = "",
    val content: String = "",
    val format: String = "TXT",
    val linkedType: String? = null,
    val linkedId: String? = null,
    @ColumnInfo(name = "isPinned", defaultValue = "0")
    val isPinned: Boolean = false,
    val colorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toNote(): Note = Note(
        id = id,
        content = content,
        format = NoteFormat.fromString(format),
        isPinned = isPinned,
        title = title,
        linkedType = linkedType,
        linkedId = linkedId,
        colorHex = colorHex,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromNote(note: Note): NoteEntity = NoteEntity(
            id = note.id,
            title = note.displayTitle,
            content = note.content,
            format = note.format.name,
            linkedType = note.linkedType,
            linkedId = note.linkedId,
            isPinned = note.isPinned,
            colorHex = note.colorHex,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
    }
}
