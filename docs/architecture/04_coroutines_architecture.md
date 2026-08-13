# 4. Coroutines & Concurrency Architecture

## Dispatchers Strategy Matrix

To ensure crisp 60/120fps UI performance and non-blocking background operations, thread dispatching is strictly segregated across architectural boundaries:

| Architectural Layer | Dispatcher | Scope | Purpose |
|---------------------|------------|-------|---------|
| **UI / Compose** | `Dispatchers.Main` | `viewModelScope` | UI rendering, state mutations, Jetpack Navigation |
| **Database (Room)** | `Dispatchers.IO` | `viewModelScope` | Async SQLite read/write operations (Room dispatches automatically, explicit IO for transactions) |
| **Network (Ktor)** | `Dispatchers.IO` | `viewModelScope` | External REST API requests |
| **File I/O** | `Dispatchers.IO` | `viewModelScope` | JSON/CSV data export and backup file access |
| **Background (Alarms)** | `Dispatchers.Default` | `applicationScope` | Alarm rescheduling, widget rendering, heavy state evaluation |
| **Heavy Computation** | `Dispatchers.Default` | `viewModelScope` | 28-day calendar math, habit cycle streak evaluation |

---

## Injectable Dispatchers Provider

To eliminate non-deterministic thread behavior in unit tests, dispatchers are provided via an abstraction interface:

```kotlin
package com.l1khith.calender28.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher

interface DispatchersProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatchersProvider : DispatchersProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}

class TestDispatchersProvider(
    override val main: CoroutineDispatcher = StandardTestDispatcher(),
    override val io: CoroutineDispatcher = StandardTestDispatcher(),
    override val default: CoroutineDispatcher = StandardTestDispatcher()
) : DispatchersProvider
```

---

## Application Scope Lifecycle

```kotlin
package com.l1khith.calender28.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AppCoroutineScopes {
    // Survives configuration changes; bound to Application process lifecycle
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    fun cancel() {
        applicationScope.cancel()
    }
}
```

---

## ViewModel Concurrency Pattern (Unidirectional Data Flow)

ViewModels maintain state using `StateFlow` and handle transient one-shot events with `SharedFlow`:

```kotlin
package com.l1khith.calender28.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.di.DispatchersProvider
import com.l1khith.calender28.domain.model.Habit
import com.l1khith.calender28.domain.model.Task
import com.l1khith.calender28.domain.usecase.task.CreateTaskUseCase
import com.l1khith.calender28.domain.usecase.task.GetTasksForDateUseCase
import com.l1khith.calender28.domain.usecase.task.ToggleTaskCompletionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel(
    private val getTasksUseCase: GetTasksForDateUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val toggleTaskUseCase: ToggleTaskCompletionUseCase,
    private val dispatchers: DispatchersProvider
) : ViewModel() {

    // Immutable Single Source of Truth for UI State
    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    // One-shot side-effect events (toasts, navigation triggers)
    private val _events = MutableSharedFlow<TasksEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<TasksEvent> = _events.asSharedFlow()

    fun loadTasksForDate(date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getTasksUseCase(date)
                .flowOn(dispatchers.io)
                .catch { e ->
                    _events.tryEmit(TasksEvent.Error(e.message ?: "Unknown error occurred"))
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { tasks ->
                    _uiState.update { 
                        it.copy(tasks = tasks, selectedDate = date, isLoading = false) 
                    }
                }
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch(dispatchers.io) {
            val result = createTaskUseCase(task)
            result.fold(
                onSuccess = { 
                    _events.tryEmit(TasksEvent.TaskCreated)
                    loadTasksForDate(task.dateStr)
                },
                onFailure = { e ->
                    _events.tryEmit(TasksEvent.Error(e.message ?: "Failed to create task"))
                }
            )
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch(dispatchers.io) {
            toggleTaskUseCase(taskId)
            _uiState.value.selectedDate?.let { loadTasksForDate(it) }
        }
    }
}

// Presentation Models
data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val selectedDate: String? = null,
    val isLoading: Boolean = false,
    val isProUser: Boolean = false
)

sealed class TasksEvent {
    data class Error(val message: String) : TasksEvent()
    object TaskCreated : TasksEvent()
    object TaskDeleted : TasksEvent()
}
```
