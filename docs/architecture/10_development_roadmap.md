# 10. Development Roadmap

## Phase 1: Project Foundation (Week 1)
- [x] Create Android Studio project structure with Jetpack Compose.
- [x] Configure `build.gradle.kts` dependencies (Room, Coroutines, Navigation, DataStore, Glance).
- [x] Create Room database schema, Entities (`Task`, `RecurringTask`, `Habit`, `HabitEntry`), DAOs, and Type Converters.
- [x] Implement lightweight Manual Dependency Injection container (`AppModule`).
- [x] Implement 28-day `CalendarMath` engine & unit tests.

---

## Phase 2: Core Task Features (Week 2)
- [x] Implement Task CRUD Use Cases & Repositories (`TaskRepositoryImpl`).
- [x] Build 28-day Month Tab Calendar Grid (`CalendarGrid`, `DayCell`).
- [x] Build Tasks Tab Agenda List & Date Picker (`TasksScreen`, `AgendaList`).
- [x] Build Task Creation Bottom Sheet (`CreateTaskBottomSheet`).
- [x] Implement task completion toggles and database reactive Flow bindings.

---

## Phase 3: Habit Tracking Engine (Week 3)
- [x] Implement Habit CRUD & Habit Entry Repository (`HabitRepositoryImpl`).
- [x] Implement 28-day Habit Cycle Tracking Engine (`HabitCycleEngine`).
- [x] Build Habit Detail Screen & 28-day visual completion grid (`HabitGrid`, `HabitDayCell`).
- [x] Build consecutive streak calculation & perfect week badge logic.
- [x] Ensure seamless habit state persistence across Month, Tasks, and Habit tabs.

---

## Phase 4: Enterprise Polish & Platform Integration (Week 4)
- [x] Integrate RevenueCat In-App Purchases with developer toggle mode.
- [x] Build Paywall Screen & Customer Center Screen.
- [x] Build Local Notification System (`NotificationHelper`, `AlarmScheduler`, `AlarmReceiver`).
- [x] Build Glance Home Screen Widget (`TodayTaskWidget`).
- [x] Implement Biometric App Lock & Data Export/Import features (JSON/CSV).

---

## Phase 5: Testing & Release (Week 5)
- [x] Write Unit Tests for `CalendarMath`, `HabitCycleEngine`, and Use Cases.
- [x] Write Integration Tests for Room Database DAOs and Migrations.
- [x] Run Jetpack Compose UI Flow & E2E Navigation tests.
- [x] Launch Closed Beta Testing program with 14 verified developers.

---

## Refactoring Roadmap (Post-Audit)
| Phase | Focus | Files | Risk | Status |
|---|---|---|---|---|
| 1 | Database Safety | TaskDatabase, Entities, DAOs, Receivers | Low | Complete |
| 2 | DI Cleanup | AppContainer, all Repositories | Low | Complete |
| 3 | Composable Decomposition | FixedCalendarApp → 5+ files | Medium | Complete |
| 4 | Singleton Elimination | Settings, Lock, Billing, Theme managers | Medium | Complete |
| 5 | Background Hardening | FocusSessionManager, dead code | Low | Complete |
