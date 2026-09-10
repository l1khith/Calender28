package com.l1khith.calender28.data

import java.util.UUID

/**
 * Domain model representing a note.
 *
 * @property id Unique identifier (UUID).
 * @property content Raw note content (the sole source of truth for the note).
 * @property format Choice between plain text ([NoteFormat.TXT]) or markdown ([NoteFormat.MD]).
 * @property isPinned Whether note is pinned to top.
 * @property createdAt Creation Unix timestamp in milliseconds.
 * @property updatedAt Last modified Unix timestamp in milliseconds.
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "",
    val format: NoteFormat = NoteFormat.TXT,
    val isPinned: Boolean = false,
    val title: String = "",
    val linkedType: String? = null,
    val linkedId: String? = null,
    val colorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isMarkdown: Boolean get() = format == NoteFormat.MD

    /**
     * Extracts the first non-empty line as the headline for card previews,
     * stripping leading Markdown symbols if present.
     */
    val displayTitle: String
        get() {
            if (title.isNotBlank()) return title
            val firstLine = content.lines().firstOrNull { it.isNotBlank() }?.trim()
            return if (!firstLine.isNullOrBlank()) {
                firstLine.replace(Regex("^#+\\s*"), "")
            } else {
                "Untitled Note"
            }
        }

    /**
     * Extracts the content following the first line for snippet preview.
     */
    val snippetPreview: String
        get() {
            val nonBlankLines = content.lines().filter { it.isNotBlank() }
            return if (nonBlankLines.size > 1) {
                nonBlankLines.drop(1).joinToString(" ")
            } else {
                ""
            }
        }
}
