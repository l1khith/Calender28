package com.l1khith.calender28.domain.usecase.notes

import com.l1khith.calender28.domain.model.NoteContext
import com.l1khith.calender28.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GenerateDefaultNoteTitleUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(
        context: NoteContext = NoteContext.Standalone,
        timeMs: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String = withContext(Dispatchers.IO) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).apply {
            this.timeZone = timeZone
        }
        val dateStr = sdf.format(Date(timeMs))

        val baseTitle = when (context) {
            is NoteContext.Standalone -> dateStr
            is NoteContext.FromTask -> {
                val sanitized = context.title.replace(":", "-").trim().take(40)
                "Task: $sanitized — $dateStr"
            }
            is NoteContext.FromHabit -> {
                val sanitized = context.name.replace(":", "-").trim().take(40)
                "Habit: $sanitized — $dateStr"
            }
            is NoteContext.FromCycle -> {
                "Cycle ${context.cycleIndex} — $dateStr"
            }
        }

        val existingTitles = noteRepository.getTitlesStartingWith(baseTitle).toSet()

        if (!existingTitles.contains(baseTitle)) {
            return@withContext baseTitle
        }

        var counter = 2
        while (existingTitles.contains("$baseTitle ($counter)")) {
            counter++
        }

        "$baseTitle ($counter)"
    }
}
