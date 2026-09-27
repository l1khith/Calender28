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
import com.l1khith.calender28.utils.HabitCycleEngine
import com.l1khith.calender28.utils.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.l1khith.calender28.Calender28Application
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.toEntity
import com.l1khith.calender28.repository.HabitRepository
import com.l1khith.calender28.repository.NoteRepository
import java.util.Calendar

private const val TAG = "DayDetailViewModel"

data class TimelineItem(
    val hour: Int,              // 0-23
    val minute: Int,            // 0-59
    val type: TimelineItemType,
    val note: Note? = null
)

sealed class TimelineItemType {
    object HourHeader : TimelineItemType()
    object NoteItem : TimelineItemType()
}

class DayDetailViewModel(
    application: Application,
    private val getDayDetailUseCase: GetDayDetailUseCase,
    private val getDayConflictsUseCase: GetDayConflictsUseCase,
    private val taskRepository: TaskRepository,
    private val resolveConflictUseCase: ResolveConflictUseCase,
    private val suggestFreeSlotsUseCase: SuggestFreeSlotsUseCase,
    private val noteRepository: NoteRepository = (application as Calender28Application).container.noteRepository,
    private val habitRepository: HabitRepository = (application as Calender28Application).container.habitRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        DayDetailUiState(selectedDate = FixedCalendarHelper.currentFixedDate())
    )
    val uiState: StateFlow<DayDetailUiState> = _uiState.asStateFlow()

    private val _notesForDate = MutableStateFlow<List<Note>>(emptyList())
    val notesForDate: StateFlow<List<Note>> = _notesForDate.asStateFlow()

    val hourRangeFlow: StateFlow<IntRange> = MutableStateFlow(0..23).asStateFlow()

    val timelineData: StateFlow<Pair<List<DayTimelineItem>, List<DayTimelineItem>>> = combine(
        _uiState,
        _notesForDate
    ) { state, notes ->
        buildUnifiedTimeline(state, notes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(emptyList(), emptyList())
    )

    val dayTimelineItems: StateFlow<List<DayTimelineItem>> = timelineData
        .map { it.first }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unscheduledItems: StateFlow<List<DayTimelineItem>> = timelineData
        .map { it.second }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val timelineItems: StateFlow<List<TimelineItem>> = combine(
        notesForDate,
        hourRangeFlow
    ) { notes, range ->
        buildTimelineItems(notes, range)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var notesJob: Job? = null

    fun loadDate(date: FixedDate) {
        val dateStr = date.toString()
        Log.d(TAG, "loadDate: Loading detail for $dateStr")

        _uiState.value = _uiState.value.copy(selectedDate = date, isLoading = true)

        notesJob?.cancel()
        notesJob = viewModelScope.launch(Dispatchers.IO) {
            val startOfDayMs = FixedCalendarHelper.toTimestamp(date, "00:00")
            val endOfDayMs = FixedCalendarHelper.toTimestamp(date, "23:59") + 59_999L

            noteRepository.getAllNotes().collect { allNotes ->
                val filtered = allNotes.filter { note ->
                    (note.linkedType == "DATE" && note.linkedId == dateStr) ||
                    (note.createdAt in startOfDayMs..endOfDayMs) ||
                    FixedCalendarHelper.fromTimestamp(note.createdAt).toString() == dateStr
                }
                _notesForDate.value = filtered
            }
        }

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

    fun toggleRecurringComplete(recurringTask: RecurringTask, generatedTask: AppTask?, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = _uiState.value.selectedDate
            val dateStr = date.toString()
            if (generatedTask != null) {
                taskRepository.toggleTaskCompletion(generatedTask)
            } else {
                val isRem = if (recurringTask.reminderTime != null) 1 else 0
                val utcTs = if (recurringTask.reminderTime != null) {
                    FixedCalendarHelper.toTimestamp(date, recurringTask.reminderTime)
                } else null
                val newStatus = if (!isCompleted) 1 else 0
                val genTask = AppTask(
                    id = "gen_${recurringTask.id}_$dateStr",
                    title = recurringTask.title,
                    description = recurringTask.description,
                    associatedDate = dateStr,
                    isReminder = isRem,
                    reminderTime = recurringTask.reminderTime,
                    utcTimestamp = utcTs,
                    isCompleted = newStatus,
                    priority = recurringTask.priority,
                    recurringParentId = recurringTask.id,
                    isGenerated = 1
                )
                (getApplication<Application>() as Calender28Application).container.database.taskDao().insertTask(genTask.toEntity())
            }
            loadDate(date)
        }
    }

    fun toggleHabitComplete(habit: Habit) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = _uiState.value.selectedDate
            val epochDay = HabitCycleEngine.getEpochDay(FixedCalendarHelper.toTimestamp(date, "12:00"))
            val anchorEpochDay = HabitCycleEngine.getEpochDay(habit.createdAtMs)
            val pos = HabitCycleEngine.computePosition(epochDay, anchorEpochDay)
            val isCompleted = habit.completedDays.contains(pos.safeDayInCycle)
            habitRepository.toggleHabitDay(habit.id, pos.cycleIndex, pos.safeDayInCycle, isCompleted)
            loadDate(date)
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

    fun saveNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(note)
        }
    }

    companion object {
        fun buildTimelineItems(
            notes: List<Note>,
            hourRange: IntRange
        ): List<TimelineItem> {
            val items = mutableListOf<TimelineItem>()

            // Group notes by hour
            val notesByHour = notes.groupBy { note ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = note.createdAt
                }
                cal.get(Calendar.HOUR_OF_DAY)
            }

            // Build the timeline
            for (hour in hourRange) {
                // Add hour header
                items.add(
                    TimelineItem(
                        hour = hour,
                        minute = 0,
                        type = TimelineItemType.HourHeader
                    )
                )

                // Add notes for this hour, sorted by minute
                notesByHour[hour]
                    ?.sortedBy { note ->
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = note.createdAt
                        }
                        cal.get(Calendar.MINUTE)
                    }
                    ?.forEach { note ->
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = note.createdAt
                        }
                        items.add(
                            TimelineItem(
                                hour = hour,
                                minute = cal.get(Calendar.MINUTE),
                                type = TimelineItemType.NoteItem,
                                note = note
                            )
                        )
                    }
            }

            return items
        }

        fun buildUnifiedTimeline(
            uiState: DayDetailUiState,
            notes: List<Note>
        ): Pair<List<DayTimelineItem>, List<DayTimelineItem>> {
            val selectedDate = uiState.selectedDate
            val scheduledItems = mutableListOf<DayTimelineItem>()
            val unscheduledItems = mutableListOf<DayTimelineItem>()

            // 1. One-off tasks vs Generated Recurring tasks in timedTasks
            val generatedParentIds = mutableSetOf<String>()

            uiState.timedTasks.forEach { task ->
                if (task.recurringParentId != null || task.isGenerated == 1) {
                    val parentId = task.recurringParentId ?: task.id.removePrefix("gen_").substringBefore("_")
                    generatedParentIds.add(parentId)
                    val parentTemplate = uiState.recurringInstances.find { it.id == parentId }
                    if (parentTemplate != null) {
                        scheduledItems.add(
                            DayTimelineItem.RecurringTaskInstance(
                                task = parentTemplate,
                                isCompleted = task.completed,
                                generatedTask = task
                            )
                        )
                    } else {
                        scheduledItems.add(
                            DayTimelineItem.RecurringTaskInstance(
                                task = RecurringTask(
                                    id = parentId,
                                    title = task.title,
                                    description = task.description,
                                    recurrenceType = RecurrenceType.DAILY,
                                    createdAt = System.currentTimeMillis(),
                                    reminderTime = task.reminderTime,
                                    priority = task.priority,
                                    isActive = true
                                ),
                                isCompleted = task.completed,
                                generatedTask = task
                            )
                        )
                    }
                } else {
                    scheduledItems.add(DayTimelineItem.OneOffTask(task))
                }
            }

            // 2. Recurring tasks from recurringInstances that don't have generated tasks in timedTasks
            uiState.recurringInstances.forEach { rec ->
                if (!generatedParentIds.contains(rec.id)) {
                    val parsedTime = TimeFormatter.parseTimeToHourMinute(rec.reminderTime)
                    if (parsedTime != null) {
                        scheduledItems.add(
                            DayTimelineItem.RecurringTaskInstance(
                                task = rec,
                                isCompleted = false,
                                generatedTask = null
                            )
                        )
                    } else {
                        unscheduledItems.add(
                            DayTimelineItem.RecurringTaskInstance(
                                task = rec,
                                isCompleted = false,
                                generatedTask = null
                            )
                        )
                    }
                }
            }

            // 3. Habit reminders
            uiState.habits.forEach { habit ->
                if (!habit.reminderTime.isNullOrBlank() && habit.reminderTime != "Off") {
                    val epochDay = HabitCycleEngine.getEpochDay(FixedCalendarHelper.toTimestamp(selectedDate, "12:00"))
                    val anchorEpochDay = HabitCycleEngine.getEpochDay(habit.createdAtMs)
                    val pos = HabitCycleEngine.computePosition(epochDay, anchorEpochDay)
                    val isCompleted = habit.completedDays.contains(pos.safeDayInCycle)
                    scheduledItems.add(
                        DayTimelineItem.HabitReminder(
                            habit = habit,
                            isCompleted = isCompleted
                        )
                    )
                }
            }

            // 4. Notes
            notes.forEach { note ->
                scheduledItems.add(DayTimelineItem.NoteItem(note))
            }

            // 5. Unscheduled & All-Day Tasks
            uiState.unscheduledTasks.forEach { task ->
                unscheduledItems.add(DayTimelineItem.OneOffTask(task))
            }
            uiState.allDayTasks.forEach { task ->
                unscheduledItems.add(DayTimelineItem.OneOffTask(task))
            }

            // Group scheduled items by hour (0..23)
            val itemsByHour = scheduledItems.groupBy { item ->
                val mins = item.timeMinutes ?: 0
                (mins / 60).coerceIn(0, 23)
            }

            val result = mutableListOf<DayTimelineItem>()
            for (hour in 0..23) {
                result.add(DayTimelineItem.HourHeader(hour))
                val inThisHour = itemsByHour[hour] ?: emptyList()
                result.addAll(inThisHour.sortedBy { it.timeMinutes ?: 0 })
            }

            return Pair(result, unscheduledItems)
        }
    }
}
