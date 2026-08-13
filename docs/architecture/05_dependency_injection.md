# 5. Dependency Injection (Manual DI)

## Design Decision
Calender28 deliberately uses **Pure Kotlin Manual Dependency Injection** instead of external framework libraries (such as Dagger/Hilt or Koin).

### Advantages:
1. **Zero Annotation Processor / KSP Overhead**: Eliminates code generation build delays.
2. **Compile-Time Verification**: Simple Kotlin constructor instantiation verified by standard compiler.
3. **No Reflection**: Instant startup times without dynamic dependency graph resolution.
4. **Complete Control**: Straightforward singleton containment and easy test double substitution.

---

## AppModule Implementation

```kotlin
package com.l1khith.calender28.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.l1khith.calender28.data.local.database.CalenderDatabase
import com.l1khith.calender28.data.repository.HabitRepositoryImpl
import com.l1khith.calender28.data.repository.TaskRepositoryImpl
import com.l1khith.calender28.data.repository.UserRepositoryImpl
import com.l1khith.calender28.domain.repository.HabitRepository
import com.l1khith.calender28.domain.repository.TaskRepository
import com.l1khith.calender28.domain.repository.UserRepository
import com.l1khith.calender28.domain.usecase.habit.*
import com.l1khith.calender28.domain.usecase.task.*
import com.l1khith.calender28.presentation.calendar.CalendarViewModel
import com.l1khith.calender28.presentation.tasks.TasksViewModel

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class AppModule private constructor(context: Context) {

    val database: CalenderDatabase = CalenderDatabase.getInstance(context)
    val dispatchers: DispatchersProvider = DefaultDispatchersProvider()
    val appScopes: AppCoroutineScopes = AppCoroutineScopes()

    // Repositories
    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(
            taskDao = database.taskDao(),
            recurringTaskDao = database.recurringTaskDao(),
            dispatchers = dispatchers
        )
    }

    val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitEntryDao = database.habitEntryDao(),
            dispatchers = dispatchers
        )
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(
            dataStore = context.dataStore,
            dispatchers = dispatchers
        )
    }

    // Task Use Cases
    val createTaskUseCase by lazy { CreateTaskUseCase(taskRepository) }
    val getTasksForDateUseCase by lazy { GetTasksForDateUseCase(taskRepository) }
    val toggleTaskUseCase by lazy { ToggleTaskCompletionUseCase(taskRepository) }

    // Habit Use Cases
    val createHabitUseCase by lazy { CreateHabitUseCase(habitRepository) }
    val getHabitsUseCase by lazy { GetHabitsUseCase(habitRepository) }
    val toggleHabitDayUseCase by lazy { ToggleHabitDayUseCase(habitRepository) }

    // ViewModel Factory Helpers
    fun provideTasksViewModel() = TasksViewModel(
        getTasksUseCase = getTasksForDateUseCase,
        createTaskUseCase = createTaskUseCase,
        toggleTaskUseCase = toggleTaskUseCase,
        dispatchers = dispatchers
    )

    fun provideCalendarViewModel() = CalendarViewModel(
        getTasksForDateUseCase = getTasksForDateUseCase,
        dispatchers = dispatchers
    )

    companion object {
        @Volatile private var instance: AppModule? = null

        fun getInstance(context: Context): AppModule =
            instance ?: synchronized(this) {
                instance ?: AppModule(context.applicationContext).also { instance = it }
            }
    }
}

// Extension property for clean syntax across Context / Composables
val Context.appModule: AppModule
    get() = AppModule.getInstance(this)
```
