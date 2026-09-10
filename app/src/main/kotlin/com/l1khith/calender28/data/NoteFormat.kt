package com.l1khith.calender28.data

/**
 * Supported file format types for Calender28 notes.
 *
 * @property extension The standard file extension (without dot).
 * @property mimeType The standard Android MIME type for Intent sharing.
 */
enum class NoteFormat(val extension: String, val mimeType: String) {
    TXT("txt", "text/plain"),
    MD("md", "text/markdown");

    companion object {
        fun fromString(value: String?): NoteFormat =
            if (value.equals("MD", ignoreCase = true)) MD else TXT
    }
}
