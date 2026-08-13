# 9. Coding Standards & Conventions

## Naming Conventions Table

| Code Type | Style Convention | Example |
|-----------|------------------|---------|
| **Classes & Interfaces** | `PascalCase` | `TaskRepository`, `CreateTaskUseCase`, `HabitCycleEngine` |
| **Functions & Methods** | `camelCase` | `getTasksForDate()`, `toggleTaskCompletion()`, `calculateStreak()` |
| **Variables & Parameters** | `camelCase` | `isLoading`, `selectedDate`, `habitEntryList` |
| **Constants** | `UPPER_SNAKE_CASE` | `DAYS_PER_CYCLE`, `CHANNEL_TASK_REMINDERS` |
| **Composable Functions** | `PascalCase` | `TaskItem()`, `HabitGrid()`, `CalendarCell()` |
| **XML / Asset Files** | `snake_case` | `ic_notification.xml`, `bg_card.png` |
| **Database Tables / Columns** | `snake_case` | `tasks`, `habit_entries`, `cycle_index` |

---

## File Organization Rules

1. **One Top-Level Component per File**: Each class, interface, or core UI component gets its own dedicated file (except sealed class hierarchies).
2. **Composable Screen Layouts**: UI composables belong in `*Screen.kt` or `*Component.kt` files under `presentation/<feature>/`.
3. **ViewModels**: ViewModels are placed in `*ViewModel.kt` files corresponding to their feature screen.
4. **Use Cases**: Use cases are stored in `domain/usecase/<feature>/` directories and follow the naming convention `<Action><Entity>UseCase.kt`.
5. **Data Layer Separation**: Strict model distinction between Room Database `*Entity`, pure business `DomainModel`, and UI state `UiState`.

---

## Standard Error Handling Pattern

Use Kotlin's standard `Result<T>` pattern in Use Cases and Repositories to enforce explicit error handling:

```kotlin
// UseCase implementation returning Result<T>
class CreateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Task> = try {
        if (task.title.isBlank()) {
            Result.failure(IllegalArgumentException("Task title cannot be blank"))
        } else {
            repository.insertTask(task)
            Result.success(task)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// In ViewModel (fold pattern)
viewModelScope.launch {
    createTaskUseCase(task).fold(
        onSuccess = { newTask ->
            _events.tryEmit(TasksEvent.TaskCreated)
        },
        onFailure = { throwable ->
            _events.tryEmit(TasksEvent.Error(throwable.message ?: "Failed to create task"))
        }
    )
}
```
