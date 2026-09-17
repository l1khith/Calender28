package com.l1khith.calender28.domain.model

import com.l1khith.calender28.data.AppTask

/**
 * Resolution actions supported by the conflict resolution engine.
 */
enum class ResolutionAction {
    MOVE_A,
    MOVE_B,
    DELETE_A,
    DELETE_B,
    MERGE
}

/**
 * Represents a conflict-free time slot candidate.
 */
data class FreeSlot(
    val dateStr: String,
    val startTime: String,
    val endTime: String,
    val isTomorrow: Boolean = false
) {
    val displayRange: String
        get() = if (isTomorrow) "Tomorrow at $startTime" else "$startTime → $endTime"
}

/**
 * Outcome of a conflict resolution action, preserving previous states for Undo.
 */
data class ConflictResolutionResult(
    val action: ResolutionAction,
    val eventAId: String,
    val eventBId: String,
    val message: String,
    val previousEventA: AppTask?,
    val previousEventB: AppTask?
)
