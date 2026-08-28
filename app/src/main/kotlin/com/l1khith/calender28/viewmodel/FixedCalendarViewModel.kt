package com.l1khith.calender28.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.repository.HabitRepositoryImpl
import com.l1khith.calender28.repository.TaskRepositoryImpl
import com.l1khith.calender28.utils.CalendarSyncHelper
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "FixedCalendarVM"

class FixedCalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val taskRepository = TaskRepositoryImpl(context)
    private val habitRepository = HabitRepositoryImpl(context)
    private val coinRepository = com.l1khith.calender28.repository.CoinRepositoryImpl(context)

    // Calendar navigation state
    private val _selectedDate = MutableStateFlow(FixedCalendarHelper.fromTimestamp(System.currentTimeMillis()))
    val selectedDate: StateFlow<FixedDate> = _selectedDate.asStateFlow()

    // Task list and indicator state
    private val _tasksForSelectedDay = MutableStateFlow<List<AppTask>>(emptyList())
    val tasksForSelectedDay: StateFlow<List<AppTask>> = _tasksForSelectedDay.asStateFlow()

    val datesWithActiveTasks: StateFlow<Set<String>> = taskRepository.getDatesWithActiveTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val taskCountsPerDate: StateFlow<Map<String, Int>> = taskRepository.getTaskCountsPerDateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val recurringTasks: StateFlow<List<RecurringTask>> = taskRepository.getRecurringTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<AppTask>> = taskRepository.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Habit state for 28-day Habit Cycles
    val habits: StateFlow<List<Habit>> = habitRepository.getAllHabitsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        Log.d(TAG, "init: Initializing FixedCalendarViewModel with selected date ${_selectedDate.value}")
        val currentSelDate = _selectedDate.value
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.catchUpRollover(currentSelDate)
            val systemEvents = CalendarSyncHelper.importSystemCalendarEvents(context)
            for (task in systemEvents) {
                taskRepository.saveTask(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    associatedDate = FixedCalendarHelper.parseDateStr(task.associatedDate) ?: currentSelDate,
                    isReminder = task.reminder,
                    reminderTime = task.reminderTime,
                    priority = task.priority
                )
            }
            coinRepository.rewardDailyLogin(currentSelDate.toString())
            loadState(currentSelDate)
        }
    }

    fun selectDate(date: FixedDate) {
        Log.d(TAG, "selectDate: Called with date=$date")
        onDateSelected(date)
    }

    fun onDateSelected(date: FixedDate) {
        Log.d(TAG, "onDateSelected: Date changed from ${_selectedDate.value} to $date")
        _selectedDate.value = date
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.catchUpRollover(date)
            loadState(date)
        }
    }

    fun selectToday() {
        val today = FixedCalendarHelper.fromTimestamp(System.currentTimeMillis())
        Log.d(TAG, "selectToday: Selecting today's date $today")
        onDateSelected(today)
    }

    fun nextMonth() {
        val next = FixedCalendarHelper.addMonths(_selectedDate.value, 1)
        Log.d(TAG, "nextMonth: Navigating to month $next")
        _selectedDate.value = next
        onDateSelected(next)
    }

    fun previousMonth() {
        val prev = FixedCalendarHelper.addMonths(_selectedDate.value, -1)
        Log.d(TAG, "previousMonth: Navigating to month $prev")
        _selectedDate.value = prev
        onDateSelected(prev)
    }

    fun onTaskSelected(taskId: String) {
        Log.d(TAG, "onTaskSelected: Selected taskId=$taskId")
        viewModelScope.launch(Dispatchers.Default) {
            val task = taskRepository.getAllTasks().find { it.id == taskId }
            if (task != null) {
                val parsed = FixedCalendarHelper.parseDateStr(task.associatedDate)
                if (parsed != null) {
                    _selectedDate.value = parsed
                    loadState(parsed)
                }
            }
        }
    }

    // --- Task Actions ---

    fun saveTask(
        id: String?,
        title: String,
        description: String?,
        isReminder: Boolean,
        reminderTime: String?,
        priority: Int = 1,
        associatedDateStr: String? = null
    ) {
        val targetDate = if (associatedDateStr != null) {
            FixedCalendarHelper.parseDateStr(associatedDateStr) ?: _selectedDate.value
        } else {
            _selectedDate.value
        }
        Log.d(TAG, "saveTask: id=$id, title=$title, date=$targetDate, isReminder=$isReminder, reminderTime=$reminderTime")

        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.saveTask(
                id = id,
                title = title,
                description = description,
                associatedDate = targetDate,
                isReminder = isReminder,
                reminderTime = reminderTime,
                priority = priority
            )
            loadState(targetDate)
        }
    }

    fun deleteTask(task: AppTask) {
        Log.d(TAG, "deleteTask: Deleting taskId=${task.id}, title=${task.title}")
        val targetDate = _selectedDate.value
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.deleteTask(task.id, task.recurringParentId)
            loadState(targetDate)
        }
    }

    fun toggleTaskCompletion(task: AppTask) {
        Log.d(TAG, "toggleTaskCompletion: Toggling taskId=${task.id}, currentCompleted=${task.completed}")
        val targetDate = _selectedDate.value
        val isNowCompleting = !task.completed
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.toggleTaskCompletion(task)
            if (isNowCompleting) {
                val isRecurring = task.recurringParentId != null || task.isGenerated == 1
                coinRepository.rewardTaskCompletion(isRecurring = isRecurring, streakDays = 1, taskTitle = task.title)
            }
            loadState(targetDate)
        }
    }

    // --- Recurring Task Actions ---

    fun saveRecurringTask(
        id: String?,
        title: String,
        description: String?,
        recurrenceType: RecurrenceType,
        recurrenceDays: List<Int>,
        recurrenceInterval: Int,
        priority: Int,
        isActive: Boolean,
        endDate: String?,
        reminderTime: String? = null
    ) {
        val targetDate = _selectedDate.value
        Log.d(TAG, "saveRecurringTask: id=$id, title=$title, type=$recurrenceType, isActive=$isActive")
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.saveRecurringTask(
                id = id,
                title = title,
                description = description,
                recurrenceType = recurrenceType,
                recurrenceDays = recurrenceDays,
                recurrenceInterval = recurrenceInterval,
                priority = priority,
                isActive = isActive,
                endDate = endDate,
                reminderTime = reminderTime,
                targetDate = targetDate
            )
            loadState(targetDate)
        }
    }

    fun deleteRecurringTask(id: String) {
        Log.d(TAG, "deleteRecurringTask: Deleting recurring taskId=$id")
        val targetDate = _selectedDate.value
        viewModelScope.launch(Dispatchers.Default) {
            taskRepository.deleteRecurringTask(id)
            loadState(targetDate)
        }
    }

    // --- Habit Actions (28-day Habit Cycles) ---

    fun saveHabit(
        id: String?,
        name: String,
        category: String = "Health",
        reminderTime: String? = null,
        colorHex: Long = 0xFF3B82F6
    ) {
        Log.d(TAG, "saveHabit: id=$id, name=$name, category=$category, reminderTime=$reminderTime")
        val targetDate = _selectedDate.value
        viewModelScope.launch(Dispatchers.Default) {
            habitRepository.saveHabit(id, name, category, reminderTime, colorHex)
            loadState(targetDate)
        }
    }

    fun deleteHabit(id: String) {
        Log.d(TAG, "deleteHabit: Deleting habitId=$id")
        val targetDate = _selectedDate.value
        viewModelScope.launch(Dispatchers.Default) {
            habitRepository.deleteHabit(id)
            loadState(targetDate)
        }
    }

    fun toggleHabitDay(habitId: String, cycleIndex: Long, dayInCycle: Int, currentCompletedState: Boolean) {
        Log.d(TAG, "toggleHabitDay: habitId=$habitId, cycleIndex=$cycleIndex, dayInCycle=$dayInCycle, currentCompleted=$currentCompletedState")
        val targetDate = _selectedDate.value
        val isNowCompleted = !currentCompletedState
        viewModelScope.launch(Dispatchers.Default) {
            habitRepository.toggleHabitDay(habitId, cycleIndex, dayInCycle, currentCompletedState)
            if (isNowCompleted) {
                val allHabits = habitRepository.getAllHabits()
                val targetHabit = allHabits.find { it.id == habitId }
                val habitName = targetHabit?.name ?: "Habit"
                coinRepository.rewardPartialHabitProgress(habitName)

                val progress = habitRepository.getCurrentCycleProgress(habitId, cycleIndex)
                if (progress >= 28) {
                    coinRepository.rewardHabitCycleComplete(habitName)
                }
            }
            loadState(targetDate)
        }
    }

    // --- State Refresh ---

    private suspend fun loadState(date: FixedDate) {
        val dateStr = date.toString()
        Log.d(TAG, "loadState: Loading tasks for date=$dateStr")
        val tasksForDay = taskRepository.getTasksForDate(dateStr)

        withContext(Dispatchers.Main) {
            _tasksForSelectedDay.value = tasksForDay
            Log.d(TAG, "loadState: Loaded ${tasksForDay.size} tasks for date=$dateStr")
        }
    }

    fun refresh() {
        val date = _selectedDate.value
        Log.d(TAG, "refresh: Refreshing state for date=$date")
        viewModelScope.launch(Dispatchers.Default) {
            loadState(date)
        }
    }

    fun loadHabits() {
        Log.d(TAG, "loadHabits: Loading habits state")
        refresh()
    }

    fun insertHabit(habit: Habit) {
        Log.d(TAG, "insertHabit: Inserting habit id=${habit.id}, name=${habit.name}")
        saveHabit(habit.id, habit.name, habit.category, habit.reminderTime, habit.colorHex)
    }

    fun updateHabit(habit: Habit) {
        Log.d(TAG, "updateHabit: Updating habit id=${habit.id}, name=${habit.name}")
        saveHabit(habit.id, habit.name, habit.category, habit.reminderTime, habit.colorHex)
    }

    fun toggleRecurringTaskActive(task: RecurringTask) {
        Log.d(TAG, "toggleRecurringTaskActive: Toggling active state for taskId=${task.id}, newIsActive=${!task.isActive}")
        saveRecurringTask(
            id = task.id,
            title = task.title,
            description = task.description,
            recurrenceType = task.recurrenceType,
            recurrenceDays = task.recurrenceDays,
            recurrenceInterval = task.recurrenceInterval,
            priority = task.priority,
            isActive = !task.isActive,
            endDate = task.endDate,
            reminderTime = task.reminderTime
        )
    }

    fun importSystemCalendar() {
        Log.d(TAG, "importSystemCalendar: Importing system calendar events")
        val targetDate = _selectedDate.value
        viewModelScope.launch(Dispatchers.Default) {
            val events = CalendarSyncHelper.importSystemCalendarEvents(context)
            Log.d(TAG, "importSystemCalendar: Found ${events.size} system calendar events")
            for (task in events) {
                taskRepository.saveTask(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    associatedDate = FixedCalendarHelper.parseDateStr(task.associatedDate) ?: targetDate,
                    isReminder = task.reminder,
                    reminderTime = task.reminderTime,
                    priority = task.priority
                )
            }
            loadState(targetDate)
        }
    }

    fun exportTasksToDownloads(
        format: String,
        year: Int = _selectedDate.value.year,
        month: Int = _selectedDate.value.month,
        currentMonthOnly: Boolean = true,
        onResult: (Boolean, String, String) -> Unit = { _, _, _ -> }
    ) {
        Log.d(TAG, "exportTasksToDownloads: Exporting year=$year, month=$month, format=$format, currentMonthOnly=$currentMonthOnly")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allTasks = taskRepository.getAllTasks()
                val monthStr = month.toString().padStart(2, '0')
                val prefix = "$year-$monthStr"

                val filteredTasks = if (currentMonthOnly) {
                    allTasks.filter { it.associatedDate.startsWith(prefix) }
                } else {
                    allTasks
                }

                val content = when (format.lowercase()) {
                    "csv" -> com.l1khith.calender28.repository.IcsParserRepository.exportToCsv(filteredTasks)
                    "json" -> com.l1khith.calender28.repository.IcsParserRepository.exportToJson(filteredTasks)
                    else -> com.l1khith.calender28.repository.IcsParserRepository.exportToIcs(filteredTasks)
                }

                val monthName = FixedCalendarHelper.getMonthName(month)
                val ext = format.lowercase()
                val fileName = if (currentMonthOnly) {
                    "Calender28_${monthName}_${year}_Tasks.$ext"
                } else {
                    "Calender28_AllTasks_${year}.$ext"
                }

                val mimeType = when (ext) {
                    "csv" -> "text/csv"
                    "json" -> "application/json"
                    else -> "text/calendar"
                }

                val uri = com.l1khith.calender28.utils.TaskExportHelper.saveToDownloads(
                    context = context,
                    fileName = fileName,
                    content = content,
                    mimeType = mimeType
                )

                withContext(Dispatchers.Main) {
                    if (uri != null) {
                        onResult(true, fileName, "📁 Exported ${filteredTasks.size} tasks to Downloads/Calender28/$fileName")
                    } else {
                        onResult(false, fileName, "❌ Failed to save file to Downloads folder")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Export failed", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "", "❌ Export error: ${e.localizedMessage}")
                }
            }
        }
    }

    fun exportDataString(format: String): String {
        return ""
    }

    fun importFromIcsContent(content: String) {
        Log.d(TAG, "importFromIcsContent: Importing ICS content length=${content.length}")
        val targetDate = _selectedDate.value
        viewModelScope.launch(Dispatchers.Default) {
            val tasks = CalendarSyncHelper.importFromIcs(content)
            Log.d(TAG, "importFromIcsContent: Imported ${tasks.size} tasks")
            for (t in tasks) {
                taskRepository.saveTask(
                    id = t.id,
                    title = t.title,
                    description = t.description,
                    associatedDate = FixedCalendarHelper.parseDateStr(t.associatedDate) ?: targetDate,
                    isReminder = t.reminder,
                    reminderTime = t.reminderTime,
                    priority = t.priority
                )
            }
            loadState(targetDate)
        }
    }
}
