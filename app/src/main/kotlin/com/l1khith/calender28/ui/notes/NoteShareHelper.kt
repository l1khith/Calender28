package com.l1khith.calender28.ui.notes

import android.content.Context
import android.content.Intent
import com.l1khith.calender28.data.Note

/**
 * Pure Android framework Intent helper for sharing notes via system share sheet.
 */
object NoteShareHelper {

    fun shareNote(context: Context, note: Note) {
        val firstLine = note.content.lines().firstOrNull { it.isNotBlank() }?.trim() ?: "Untitled Note"
        val subject = firstLine.replace(Regex("^#+\\s*"), "")

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = note.format.mimeType
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, note.content)
        }

        val chooserIntent = Intent.createChooser(sendIntent, "Share note via...")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}
