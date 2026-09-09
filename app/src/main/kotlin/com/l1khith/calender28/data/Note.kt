package com.l1khith.calender28.data

import java.util.UUID

/**
 * Domain model representing a user note.
 *
 * @property linkedEntity Category/linkage: "NOTE", "HABIT", "TASK", or "CYCLE"
 * @property isMarkdown Format indicator: true for Markdown ("MD"), false for Plain Text ("TXT")
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val colorHex: String? = null,
    val associatedDate: String? = null,
    val linkedEntity: String = "NOTE",
    val isMarkdown: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)
