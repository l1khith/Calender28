package com.l1khith.calender28.ui.daydetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.domain.model.ConflictResolutionResult
import com.l1khith.calender28.domain.usecase.GetDayConflictsUseCase
import com.l1khith.calender28.domain.usecase.GetDayDetailUseCase
import com.l1khith.calender28.domain.usecase.conflicts.ResolveConflictUseCase
import com.l1khith.calender28.domain.usecase.conflicts.SuggestFreeSlotsUseCase
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "DayDetailViewModel"

class DayDetailViewModel(
    application: Application,
    private val getDayDetailUseCase: GetDayDetailUseCase,
    private val getDayConflictsUseCase: GetDayConflictsUseCase,
    private val taskRepository: TaskRepository,
    private val resolveConflictUseCase: ResolveConflictUseCase,
    private val suggestFreeSlotsUseCase: SuggestFreeSlotsUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        DayDetailUiState(selectedDate = FixedCalendarHelper.currentFixedDate())
    )
    val uiState: StateFlow<DayDetailUiState> = _uiState.asStateFlow()

    fun loadDate(date: FixedDate) {
        val dateStr = date.toString()
        Log.d(TAG, "loadDate: Loading detail for $dateStr")

        _uiState.value = _uiState.value.copy(selectedDate = date, isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val detail = getDayDetailUseCase(dateStr)
                val conflicts = getDayConflictsUseCase(dateStr)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    timedTasks = detail.timedTasks,
                    unscheduledTasks = detail.unscheduledTasks,
                    allDayTasks = detail.allDayTasks,
                    crossDayTasks = detail.crossDayTasks,
                    recurringInstances = detail.recurringInstances,
                    habits = detail.habitReminders,
                    focusSessions = detail.focusSessions,
                    scheduledAlarms = detail.scheduledAlarms,
                    conflicts = conflicts
                )
            } catch (e: Exception) {
                Log.e(TAG, "loadDate: Error loading day detail", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun toggleTaskComplete(task: AppTask) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.toggleTaskCompletion(task)
            loadDate(_uiState.value.selectedDate)
        }
    }

    fun moveEvent(
        eventId: String,
        conflictEventId: String,
        isEventA: Boolean,
        newDateStr: String,
        newStartTime: String,
        conflictType: String = "OVERLAP"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = resolveConflictUseCase.moveEvent(
                    eventId = eventId,
                    conflictEventId = conflictEventId,
                    isEventA = isEventA,
                    newDateStr = newDateStr,
                    newStartTime = newStartTime,
                    conflictType = conflictType
                )
                _uiState.value = _uiState.value.copy(lastResolutionResult = result)
                loadDate(_uiState.value.selectedDate)
            } catch (e: Exception) {
                Log.e(TAG, "Error moving event $eventId", e)
            }
        }
    }

    fun deleteEvent(
        eventId: String,
        conflictEventId: String,
        isEventA: Boolean,
        conflictType: String = "OVERLAP"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = resolveConflictUseCase.deleteEvent(
                    eventId = eventId,
                    conflictEventId = conflictEventId,
                    isEventA = isEventA,
                    conflictType = conflictType
                )
                _uiState.value = _uiState.value.copy(lastResolutionResult = result)
                loadDate(_uiState.value.selectedDate)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting event $eventId", e)
            }
        }
    }

    fun mergeEvents(
        eventAId: String,
        eventBId: String,
        conflictType: String = "HARD_OVERLAP"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = resolveConflictUseCase.mergeEvents(
                    eventAId = eventAId,
                    eventBId = eventBId,
                    conflictType = conflictType
                )
                _uiState.value = _uiState.value.copy(lastResolutionResult = result)
                loadDate(_uiState.value.selectedDate)
            } catch (e: Exception) {
                Log.e(TAG, "Error merging events $eventAId and $eventBId", e)
            }
        }
    }

    fun undoLastResolution() {
        val lastResult = _uiState.value.lastResolutionResult ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                resolveConflictUseCase.undo(lastResult)
                _uiState.value = _uiState.value.copy(lastResolutionResult = null)
                loadDate(_uiState.value.selectedDate)
            } catch (e: Exception) {
                Log.e(TAG, "Error undoing resolution", e)
            }
        }
    }

    fun clearLastResolution() {
        _uiState.value = _uiState.value.copy(lastResolutionResult = null)
    }

    fun addBuffer(eventId: String, bufferMinutes: Int = 15) {
        viewModelScope.launch(Dispatchers.IO) {
            val allTasks = taskRepository.getAllTasks()
            val task = allTasks.find { it.id == eventId } ?: return@launch
            val currentStartMs = task.utcTimestamp ?: return@launch
            val fixedDate = FixedCalendarHelper.parseDateStr(task.associatedDate) ?: return@launch

            val newStartMs = currentStartMs + bufferMinutes * 60_000L
            val newEndMs = (task.endUtcTimestamp ?: (currentStartMs + 60 * 60_000L)) + bufferMinutes * 60_000L

            val startMinutesTotal = (newStartMs / 60_000L) % 1440L
            val newStartTime = "%02d:%02d".format((startMinutesTotal / 60).toInt(), (startMinutesTotal % 60).toInt())

            val endMinutesTotal = (newEndMs / 60_000L) % 1440L
            val newEndTime = "%02d:%02d".format((endMinutesTotal / 60).toInt(), (endMinutesTotal % 60).toInt())

            val updatedTask = task.copy(
                reminderTime = newStartTime,
                utcTimestamp = newStartMs,
                endTime = newEndTime,
                endUtcTimestamp = newEndMs
            )

            taskRepository.updateTask(updatedTask)
            loadDate(_uiState.value.selectedDate)
        }
    }

    fun dismissConflict(conflict: DayConflict) {
        val key = "${conflict.primaryEventId}_${conflict.severity}"
        _uiState.value = _uiState.value.copy(
            dismissedConflictKeys = _uiState.value.dismissedConflictKeys + key
        )
    }

    fun setReviewSheetOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isReviewSheetOpen = open)
    }
}
