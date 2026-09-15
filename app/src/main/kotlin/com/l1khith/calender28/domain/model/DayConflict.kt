package com.l1khith.calender28.domain.model

enum class ConflictSeverity {
    CRITICAL,
    WARNING,
    INFO
}

sealed class DayConflict {
    abstract val primaryEventId: String
    abstract val severity: ConflictSeverity

    data class HardOverlap(
        override val primaryEventId: String,
        val secondaryEventId: String,
        val overlapStartMs: Long,
        val overlapEndMs: Long,
        val primaryTitle: String = "",
        val secondaryTitle: String = ""
    ) : DayConflict() {
        override val severity = ConflictSeverity.CRITICAL
    }

    data class BackToBack(
        override val primaryEventId: String,
        val secondaryEventId: String,
        val boundaryMs: Long,
        val primaryTitle: String = "",
        val secondaryTitle: String = ""
    ) : DayConflict() {
        override val severity = ConflictSeverity.WARNING
    }

    data class SameSlotStack(
        val eventIds: List<String>,
        val windowStartMs: Long,
        val windowEndMs: Long,
        val eventTitles: List<String> = emptyList()
    ) : DayConflict() {
        override val primaryEventId = eventIds.firstOrNull() ?: ""
        override val severity = ConflictSeverity.WARNING
    }

    data class AllDayVsTimed(
        override val primaryEventId: String,
        val allDayEventId: String,
        val timedTitle: String = "",
        val allDayTitle: String = ""
    ) : DayConflict() {
        override val severity = ConflictSeverity.INFO
    }

    data class CrossDayOverlap(
        override val primaryEventId: String,
        val crossDayEventId: String,
        val overlapStartMs: Long,
        val overlapEndMs: Long,
        val primaryTitle: String = "",
        val crossDayTitle: String = ""
    ) : DayConflict() {
        override val severity = ConflictSeverity.CRITICAL
    }
}
