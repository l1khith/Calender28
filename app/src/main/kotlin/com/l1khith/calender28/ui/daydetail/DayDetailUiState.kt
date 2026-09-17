package com.l1khith.calender28.ui.daydetail

import androidx.compose.runtime.Immutable
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.FocusSession
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.data.ScheduledAlarmEntity
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.utils.FixedDate

@Immutable
data class DayDetailUiState(
    val selectedDate: FixedDate,
    val isLoading: Boolean = false,
    val timedTasks: List<AppTask> = emptyList(),
    val unscheduledTasks: List<AppTask> = emptyList(),
    val allDayTasks: List<AppTask> = emptyList(),
    val crossDayTasks: List<AppTask> = emptyList(),
    val recurringInstances: List<RecurringTask> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val focusSessions: List<FocusSession> = emptyList(),
    val scheduledAlarms: List<ScheduledAlarmEntity> = emptyList(),
    val conflicts: List<DayConflict> = emptyList(),
    val dismissedConflictKeys: Set<String> = emptySet(),
    val isReviewSheetOpen: Boolean = false,
    val lastResolutionResult: com.l1khith.calender28.domain.model.ConflictResolutionResult? = null
) {
    val activeConflicts: List<DayConflict>
        get() = conflicts.filter { conflict ->
            val key = "${conflict.primaryEventId}_${conflict.severity}"
            !dismissedConflictKeys.contains(key)
        }

    val totalEventsCount: Int
        get() = timedTasks.size + allDayTasks.size + crossDayTasks.size

    val freeHoursEstimated: Double
        get() {
            val busyMinutes = timedTasks.sumOf {
                if (it.durationMinutes > 0) it.durationMinutes else 60L
            }
            val remaining = 1440L - busyMinutes
            return maxOf(0.0, remaining / 60.0)
        }
}
