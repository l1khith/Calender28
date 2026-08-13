package com.l1khith.calender28.repository

import com.l1khith.calender28.data.AppTask

object IcsParserRepository {

    fun parseIcsContent(icsContent: String): List<AppTask> {
        val tasks = mutableListOf<AppTask>()
        val eventBlocks = icsContent.split("BEGIN:VEVENT")

        for (i in 1 until eventBlocks.size) {
            val block = eventBlocks[i].substringBefore("END:VEVENT")

            var summary = ""
            var description = ""
            var dtstartStr = ""
            var location = ""

            val lines = block.lines()
            for (line in lines) {
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("SUMMARY:") -> summary = trimmed.removePrefix("SUMMARY:").trim()
                    trimmed.startsWith("DESCRIPTION:") -> description = trimmed.removePrefix("DESCRIPTION:").trim()
                    trimmed.startsWith("DTSTART") -> dtstartStr = trimmed.substringAfter(":").trim()
                    trimmed.startsWith("LOCATION:") -> location = trimmed.removePrefix("LOCATION:").trim()
                }
            }

            if (summary.isNotBlank()) {
                val cleanTitle = summary.replace("\\,", ",").replace("\\;", ";")
                val cleanDesc = if (location.isNotBlank()) {
                    "$description [Location: $location]".trim()
                } else description

                val (dateStr, reminderTimeStr) = parseIcsDateTime(dtstartStr)
                val deterministicId = "ics_" + (cleanTitle + dateStr).hashCode()

                tasks.add(
                    AppTask(
                        id = deterministicId,
                        title = cleanTitle,
                        description = cleanDesc.ifEmpty { null },
                        associatedDate = dateStr,
                        isReminder = if (!reminderTimeStr.isNullOrEmpty()) 1 else 0,
                        reminderTime = reminderTimeStr,
                        utcTimestamp = null,
                        isCompleted = 0,
                        priority = 1,
                        recurringParentId = null,
                        isGenerated = 0
                    )
                )
            }
        }
        return tasks
    }

    private fun parseIcsDateTime(dtstart: String): Pair<String, String?> {
        val clean = dtstart.replace("Z", "").replace("T", "")
        if (clean.length >= 8) {
            val yyyy = clean.substring(0, 4)
            val mm = clean.substring(4, 6)
            val dd = clean.substring(6, 8)
            val dateStr = "$yyyy-$mm-$dd"

            var reminderTime: String? = null
            if (clean.length >= 12) {
                val hh = clean.substring(8, 10)
                val min = clean.substring(10, 12)
                reminderTime = "$hh:$min"
            }
            return Pair(dateStr, reminderTime)
        }
        return Pair("2026-08-11", null)
    }

    fun exportToIcs(tasks: List<AppTask>): String {
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//Calender28//Task Export//EN")

        for (task in tasks) {
            sb.appendLine("BEGIN:VEVENT")
            sb.appendLine("SUMMARY:${task.title}")
            if (!task.description.isNullOrEmpty()) {
                sb.appendLine("DESCRIPTION:${task.description}")
            }
            val dateFormatted = task.associatedDate.replace("-", "")
            val timeFormatted = task.reminderTime?.replace(":", "") ?: "0900"
            sb.appendLine("DTSTART:${dateFormatted}T${timeFormatted}00Z")
            sb.appendLine("END:VEVENT")
        }

        sb.appendLine("END:VCALENDAR")
        return sb.toString()
    }

    fun exportToCsv(tasks: List<AppTask>): String {
        val sb = StringBuilder()
        sb.appendLine("ID,Title,Description,Date,ReminderTime,Completed,Priority")
        for (task in tasks) {
            val titleEsc = "\"${task.title.replace("\"", "\"\"")}\""
            val descEsc = "\"${(task.description ?: "").replace("\"", "\"\"")}\""
            sb.appendLine("${task.id},$titleEsc,$descEsc,${task.associatedDate},${task.reminderTime ?: ""},${task.isCompleted},${task.priority}")
        }
        return sb.toString()
    }

    fun exportToJson(tasks: List<AppTask>): String {
        val sb = StringBuilder()
        sb.appendLine("[")
        for (i in tasks.indices) {
            val t = tasks[i]
            sb.append("  {")
            sb.append("\"id\":\"${t.id}\",")
            sb.append("\"title\":\"${t.title.replace("\"", "\\\"")}\",")
            sb.append("\"description\":\"${(t.description ?: "").replace("\"", "\\\"")}\",")
            sb.append("\"associatedDate\":\"${t.associatedDate}\",")
            sb.append("\"reminderTime\":${if (t.reminderTime != null) "\"${t.reminderTime}\"" else "null"},")
            sb.append("\"isCompleted\":${t.isCompleted},")
            sb.append("\"priority\":${t.priority}")
            sb.append("}")
            if (i < tasks.size - 1) sb.appendLine(",") else sb.appendLine()
        }
        sb.appendLine("]")
        return sb.toString()
    }
}

