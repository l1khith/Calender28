package com.l1khith.calender28.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.l1khith.calender28.data.AppTask
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

object CalendarSyncHelper {

    fun importSystemCalendarEvents(context: Context): List<AppTask> {
        val tasks = mutableListOf<AppTask>()
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }

            val now = System.currentTimeMillis()
            val startRange = now - (30L * 24 * 60 * 60 * 1000L)
            val endRange = now + (365L * 24 * 60 * 60 * 1000L)

            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startRange)
            ContentUris.appendId(builder, endRange)

            val projection = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.ALL_DAY
            )

            val cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            val todayStr = FixedCalendarHelper.fromTimestamp(now).toString()

            cursor?.use { c ->
                val titleIndex = c.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val descIndex = c.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
                val beginIndex = c.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val allDayIndex = c.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)

                while (c.moveToNext()) {
                    val title = c.getString(titleIndex) ?: "System Event"
                    val description = c.getString(descIndex)
                    val startMillis = c.getLong(beginIndex)
                    val isAllDay = c.getInt(allDayIndex) == 1

                    val fixedDate = FixedCalendarHelper.fromTimestamp(startMillis)
                    val associatedDate = fixedDate.toString()

                    if (associatedDate < todayStr) {
                        continue
                    }

                    val reminderTime = if (isAllDay) {
                        null
                    } else {
                        val localDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneId.systemDefault())
                        val h = localDateTime.hour.toString().padStart(2, '0')
                        val m = localDateTime.minute.toString().padStart(2, '0')
                        "$h:$m"
                    }

                    val cleanTitle = title.trim().lowercase()
                    val hashId = (cleanTitle + "_" + associatedDate).hashCode().let { if (it < 0) -it else it }
                    val deterministicId = "sys_${hashId}"

                    tasks.add(
                        AppTask(
                            id = deterministicId,
                            title = title.trim(),
                            description = description,
                            associatedDate = associatedDate,
                            isReminder = 0,
                            reminderTime = reminderTime,
                            utcTimestamp = null,
                            isCompleted = 0,
                            priority = 1
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tasks.distinctBy { "${it.title.trim().lowercase()}_${it.associatedDate}" }
    }

    fun exportToIcs(tasks: List<AppTask>): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\r\n")
        sb.append("VERSION:2.0\r\n")
        sb.append("PRODID:-//Calender28Planner//NONSGML v1.0//EN\r\n")

        for (task in tasks) {
            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:${task.id}\r\n")

            val timestamp = if (task.isReminder == 1 && task.utcTimestamp != null) {
                task.utcTimestamp
            } else {
                val fixedDate = FixedCalendarHelper.parseDateStr(task.associatedDate)
                if (fixedDate != null) {
                    FixedCalendarHelper.toTimestamp(fixedDate, "00:00")
                } else {
                    System.currentTimeMillis()
                }
            }

            val utcStr = formatUtcIcs(timestamp)
            sb.append("DTSTART:$utcStr\r\n")
            sb.append("SUMMARY:${task.title.replace("\n", " ").replace(",", "\\,")}\r\n")
            if (!task.description.isNullOrEmpty()) {
                sb.append("DESCRIPTION:${task.description.replace("\n", "\\n").replace(",", "\\,")}\r\n")
            }
            sb.append("END:VEVENT\r\n")
        }

        sb.append("END:VCALENDAR\r\n")
        return sb.toString()
    }

    fun importFromIcs(icsContent: String): List<AppTask> {
        val tasks = mutableListOf<AppTask>()
        val lines = icsContent.lines()
        var currentTitle = ""
        var currentDesc = ""
        var currentUid = ""
        var currentFixedDate: FixedDate = FixedCalendarHelper.fromTimestamp(System.currentTimeMillis())
        var currentTimestamp: Long? = null
        var inEvent = false

        val isNative13Month = icsContent.contains("X-13MONTH-CALENDAR") || icsContent.contains("PRODID:-//Calender28Planner")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed == "BEGIN:VEVENT") {
                inEvent = true
                currentTitle = "Untitled Event"
                currentDesc = ""
                currentUid = UUID.randomUUID().toString()
                currentFixedDate = FixedCalendarHelper.fromTimestamp(System.currentTimeMillis())
                currentTimestamp = null
            } else if (trimmed == "END:VEVENT") {
                if (inEvent) {
                    val isReminder = 0
                    val reminderTime: String? = null

                    tasks.add(
                        AppTask(
                            id = "ics_$currentUid",
                            title = currentTitle,
                            description = currentDesc.ifEmpty { null },
                            associatedDate = currentFixedDate.toString(),
                            isReminder = isReminder,
                            reminderTime = reminderTime,
                            utcTimestamp = currentTimestamp,
                            isCompleted = 0,
                            priority = 1
                        )
                    )
                    inEvent = false
                }
            } else if (inEvent) {
                val colonIndex = trimmed.indexOf(':')
                if (colonIndex != -1) {
                    val key = trimmed.substring(0, colonIndex)
                    val value = trimmed.substring(colonIndex + 1)
                    when {
                        key.startsWith("SUMMARY") -> currentTitle = value.replace("\\,", ",")
                        key.startsWith("DESCRIPTION") -> currentDesc = value.replace("\\n", "\n").replace("\\,", ",")
                        key.startsWith("UID") -> currentUid = value
                        key.startsWith("DTSTART") -> {
                            val cleanVal = value.replace(";", "").replace(":", "")
                            val parsed = parseIcsDtStart(cleanVal, isNative13Month)
                            currentFixedDate = parsed.first
                            currentTimestamp = parsed.second
                        }
                    }
                }
            }
        }
        return tasks
    }

    private fun formatUtcIcs(timestampMs: Long): String {
        val totalSeconds = timestampMs / 1000L
        val seconds = (totalSeconds % 60).toInt()
        val totalMinutes = totalSeconds / 60L
        val minutes = (totalMinutes % 60).toInt()
        val totalHours = totalMinutes / 60L
        val hours = (totalHours % 24).toInt()

        val fixedDate = FixedCalendarHelper.fromTimestamp(timestampMs)
        val y = fixedDate.year.toString().padStart(4, '0')
        val m = fixedDate.month.toString().padStart(2, '0')
        val d = fixedDate.day.toString().padStart(2, '0')
        val h = hours.toString().padStart(2, '0')
        val min = minutes.toString().padStart(2, '0')
        val s = seconds.toString().padStart(2, '0')
        return "${y}${m}${d}T${h}${min}${s}Z"
    }

    private fun parseIcsDtStart(value: String, isNative13Month: Boolean): Pair<FixedDate, Long?> {
        val now = System.currentTimeMillis()
        val defaultDate = FixedCalendarHelper.fromTimestamp(now)
        return try {
            val datePart = value.take(8)
            if (datePart.length == 8) {
                val y = datePart.substring(0, 4).toInt()
                val m = datePart.substring(4, 6).toInt()
                val d = datePart.substring(6, 8).toInt()

                val timePart = if (value.contains("T")) value.substringAfter("T").take(6) else "000000"
                val h = timePart.substring(0, 2).toIntOrNull() ?: 0
                val min = timePart.substring(2, 4).toIntOrNull() ?: 0
                val timeStr = "$h:$min"

                if (isNative13Month) {
                    val fixedDate = FixedDate(y, m.coerceIn(1, 13), d.coerceIn(1, 29))
                    val ts = FixedCalendarHelper.toTimestamp(fixedDate, timeStr)
                    Pair(fixedDate, ts)
                } else {
                    val fixedDate = FixedCalendarHelper.gregorianToFixed(y, m, d)
                    val ts = FixedCalendarHelper.toTimestamp(fixedDate, timeStr)
                    Pair(fixedDate, ts)
                }
            } else {
                Pair(defaultDate, now)
            }
        } catch (e: Exception) {
            Pair(defaultDate, now)
        }
    }
}

fun importSystemCalendarEvents(context: Context): List<AppTask> = CalendarSyncHelper.importSystemCalendarEvents(context)
fun importSystemCalendarEvents(): List<AppTask> = emptyList()

