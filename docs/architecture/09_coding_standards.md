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

---

## Composable Rules
- Max 200 lines per composable file — extract sub-composables
- Single `@Stable` state parameter per screen composable
- `remember { }` all callback lambdas passed to children
- Lambda `graphicsLayer { }` exclusively — NEVER `Modifier.graphicsLayer(param = value)` or `Modifier.scale(value)`. WHY: Non-lambda reads state during composition phase, causing 60fps recomposition storms
- `collectAsStateWithLifecycle()` always — NEVER `collectAsState()`. WHY: Lifecycle-aware collection prevents zombie collectors
- No star imports except `androidx.compose.*`

## ViewModel Rules
- Single `StateFlow<ScreenState>` — use sealed interface, not flat booleans. WHY: Flat booleans create 2^N impossible states
- `viewModelScope.launch(Dispatchers.IO)` for all repository calls
- Constructor injection via ViewModelProvider.Factory — never `getInstance()` or `object` access
- No Android framework imports in ViewModel — pure Kotlin + kotlinx.coroutines

## Repository Rules
- Accept DAOs via constructor parameters — never resolve internally. WHY: Testability + Dependency Inversion
- Return `Flow<T>` for observable data, `suspend fun` for one-shot
- Use `flowOn(Dispatchers.IO)` and `distinctUntilChanged()` on all database flows

## Database Rules (CRITICAL)
- ZERO `runBlocking` anywhere in the codebase. WHY: `runBlocking` on main thread = ANR. Even on IO thread, it wastes a thread pool slot.
- `@Entity` with explicit `indices` on all frequently-queried columns
- `exportSchema = true` — schema JSON committed to VCS
- Explicit `Migration(from, to)` objects — NEVER `fallbackToDestructiveMigration()` in production. WHY: Destroys all user data
- `@Transaction` on multi-write operations

## Singleton Rules
- NO global `object` singletons holding mutable state (MutableStateFlow, MutableState)
- Use `class` instances managed by AppContainer
- WHY: Singletons bypass DI, are untestable, create hidden global coupling
- EXCEPTIONS: Pure utility objects with no state (e.g., `FixedCalendarHelper`, `Constants`)

## Background Component Rules
- BroadcastReceivers: Use `goAsync()` + `CoroutineScope(Dispatchers.IO).launch { }`. WHY: `onReceive()` runs on main thread, must not block
- WorkManager: Use `CoroutineWorker`, never `Worker` with `runBlocking`
- Services: Create notification channels in `onCreate()`, not per-notification. WHY: Channel creation is a Binder IPC
