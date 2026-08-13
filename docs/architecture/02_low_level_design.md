# 2. Low-Level Design (LLD)

## Project Directory & Module Structure

The project follows a clean feature-by-layer structure under `com.l1khith.calender28`:

```
calender28/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/com/l1khith/calender28/
│       │   │   ├── Calender28Application.kt          # Application lifecycle class
│       │   │   ├── MainActivity.kt                     # Single Activity host
│       │   │   ├── di/                                 # Manual Dependency Injection
│       │   │   │   ├── AppModule.kt                    # Singleton providers container
│       │   │   │   ├── CoroutineScopes.kt              # App/IO coroutine scopes
│       │   │   │   └── DispatchersProvider.kt          # Testable dispatchers provider
│       │   │   ├── data/                               # Data layer implementations
│       │   │   │   ├── local/                          # Local Room database
│       │   │   │   │   ├── database/
│       │   │   │   │   │   ├── CalenderDatabase.kt      # Room Database setup
│       │   │   │   │   │   ├── Converters.kt            # Type converters
│       │   │   │   │   │   └── Migrations.kt            # DB migration logic
│       │   │   │   │   ├── dao/
│       │   │   │   │   │   ├── TaskDao.kt              # Task queries
│       │   │   │   │   │   ├── RecurringTaskDao.kt     # Recurring task queries
│       │   │   │   │   │   ├── HabitDao.kt             # Habit queries
│       │   │   │   │   │   └── HabitEntryDao.kt        # 28-day habit entry queries
│       │   │   │   │   └── entity/
│       │   │   │   │       ├── TaskEntity.kt           # Task table definition
│       │   │   │   │       ├── RecurringTaskEntity.kt  # Recurring task table
│       │   │   │   │       ├── HabitEntity.kt          # Habit master table
│       │   │   │   │       └── HabitEntryEntity.kt     # Habit 28-day entries
│       │   │   │   ├── preferences/                    # Jetpack DataStore
│       │   │   │   │   └── UserPreferences.kt         # User settings persistence
│       │   │   │   └── remote/                         # Ktor HTTP client (future sync)
│       │   │   │       └── ApiService.kt
│       │   │   ├── domain/                             # Domain layer (Pure Kotlin)
│       │   │   │   ├── model/                          # Business models
│       │   │   │   │   ├── Task.kt                     # Task domain model
│       │   │   │   │   ├── Habit.kt                    # Habit domain model
│       │   │   │   │   ├── HabitCycle.kt               # Habit 28-day cycle representation
│       │   │   │   │   └── CalendarDate.kt             # 28-day calendar date domain model
│       │   │   │   ├── repository/                     # Repository interfaces
│       │   │   │   │   ├── TaskRepository.kt
│       │   │   │   │   ├── HabitRepository.kt
│       │   │   │   │   └── UserRepository.kt
│       │   │   │   └── usecase/                        # Single-responsibility use cases
│       │   │   │       ├── task/
│       │   │   │       │   ├── CreateTaskUseCase.kt
│       │   │   │       │   ├── UpdateTaskUseCase.kt
│       │   │   │       │   ├── DeleteTaskUseCase.kt
│       │   │   │       │   ├── GetTasksForDateUseCase.kt
│       │   │   │       │   ├── ToggleTaskCompletionUseCase.kt
│       │   │   │       │   └── ScheduleTaskReminderUseCase.kt
│       │   │   │       ├── habit/
│       │   │   │       │   ├── CreateHabitUseCase.kt
│       │   │   │       │   ├── UpdateHabitUseCase.kt
│       │   │   │       │   ├── DeleteHabitUseCase.kt
│       │   │   │       │   ├── GetHabitsUseCase.kt
│       │   │   │       │   ├── ToggleHabitDayUseCase.kt
│       │   │   │       │   └── GetHabitCycleProgressUseCase.kt
│       │   │   │       └── sync/
│       │   │   │           ├── SyncCalendarUseCase.kt
│       │   │   │           └── ExportDataUseCase.kt
│       │   │   ├── presentation/                       # UI Layer (Jetpack Compose)
│       │   │   │   ├── common/                         # Reusable UI components
│       │   │   │   │   ├── LoadingIndicator.kt
│       │   │   │   │   ├── ErrorDialog.kt
│       │   │   │   │   └── EmptyState.kt
│       │   │   │   ├── theme/                          # Design Tokens & Theme
│       │   │   │   │   ├── Color.kt
│       │   │   │   │   ├── Type.kt
│       │   │   │   │   ├── Shape.kt
│       │   │   │   │   └── Theme.kt
│       │   │   │   ├── calendar/                       # Month Tab (28-day Grid)
│       │   │   │   │   ├── CalendarScreen.kt
│       │   │   │   │   ├── CalendarViewModel.kt
│       │   │   │   │   ├── CalendarGrid.kt
│       │   │   │   │   ├── DayCell.kt
│       │   │   │   │   └── AgendaList.kt
│       │   │   │   ├── tasks/                          # Tasks & Daily Agenda Tab
│       │   │   │   │   ├── TasksScreen.kt
│       │   │   │   │   ├── TasksViewModel.kt
│       │   │   │   │   ├── TaskList.kt
│       │   │   │   │   ├── TaskItem.kt
│       │   │   │   │   ├── CreateTaskBottomSheet.kt
│       │   │   │   │   └── HabitSection.kt
│       │   │   │   ├── habits/                         # Habit Detail & 28-day Tracker
│       │   │   │   │   ├── HabitDetailScreen.kt
│       │   │   │   │   ├── HabitDetailViewModel.kt
│       │   │   │   │   ├── HabitGrid.kt
│       │   │   │   │   └── HabitDayCell.kt
│       │   │   │   ├── profile/                        # User Profile & Settings Tab
│       │   │   │   │   ├── ProfileScreen.kt
│       │   │   │   │   └── ProfileViewModel.kt
│       │   │   │   └── navigation/                     # Navigation Graph
│       │   │   │       ├── CalenderNavHost.kt
│       │   │   │       └── Screen.kt
│       │   │   ├── service/                            # Background Alarm & Notifications
│       │   │   │   ├── AlarmReceiver.kt
│       │   │   │   ├── BootReceiver.kt
│       │   │   │   ├── NotificationHelper.kt
│       │   │   │   └── AlarmScheduler.kt
│       │   │   ├── billing/                            # RevenueCat In-App Purchases
│       │   │   │   ├── SubscriptionManager.kt
│       │   │   │   ├── PaywallScreen.kt
│       │   │   │   └── CustomerCenterScreen.kt
│       │   │   ├── widget/                             # Glance Launcher Widget
│       │   │   │   ├── TodayTaskWidget.kt
│       │   │   │   └── WidgetUpdater.kt
│       │   │   └── util/                               # Core Utilities
│       │   │       ├── DateTimeUtil.kt
│       │   │       ├── CalendarMath.kt
│       │   │       ├── PermissionHelper.kt
│       │   │       └── FileExporter.kt
│       │   └── res/                                    # Android Resources
│       └── test/                                       # Unit & Integration Tests
│           └── kotlin/com/l1khith/calender28/
│               ├── domain/
│               ├── data/
│               └── presentation/
```
