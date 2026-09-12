# CALENDER28 — ENTERPRISE ARCHITECTURE BLUEPRINT

Welcome to the official architecture documentation for **Calender28**, a modern 28-day calendar, habit tracking, and task management application built for Android.

This documentation suite serves as the principal blueprint for engineering design, system architecture, database schema, concurrency rules, and development standards.

---

## 📚 Architecture Documentation Index

| # | Document | Description |
|---|----------|-------------|
| **01** | [High-Level Design (HLD)](./01_high_level_design.md) | Presentation, Domain, Data, and Platform layers & System Architecture Diagram |
| **02** | [Low-Level Design (LLD)](./02_low_level_design.md) | Package structure, file layout, and component responsibility mapping |
| **03** | [Database Design](./03_database_design.md) | Room schema, ERD, Entities (`Task`, `Habit`, `HabitEntry`), DAOs, and Migrations |
| **04** | [Coroutines Architecture](./04_coroutines_architecture.md) | Concurrency model, Dispatchers strategy, custom scopes, testable provider, and ViewModel pattern |
| **05** | [Dependency Injection](./05_dependency_injection.md) | Lightweight manual DI container without framework overhead (No Hilt/Koin) |
| **06** | [28-Day Calendar Math](./06_calendar_math.md) | 13-month × 28-day calendar math engine, Gregorian conversion, and epoch calculation |
| **07** | [Habit Cycle Engine](./07_habit_cycle_engine.md) | 28-day habit cycle tracking, streak calculation algorithm, and progress evaluation |
| **08** | [Notification & Alarm System](./08_notification_system.md) | Local Notification Channels, `AlarmManager` integration, receivers, and exact alarms |
| **09** | [Coding Standards](./09_coding_standards.md) | Naming conventions, package rules, error handling standard (`Result<T>`), and layer boundaries |
| **10** | [Development Roadmap](./10_development_roadmap.md) | 5-phase engineering roadmap from project foundation to 14-dev closed beta testing |
| **11** | [SOLID/DRY/KISS Audit](./11_solid_dry_kiss_audit.md) | The definitive audit document outlining core architectural violations and fixes |
| **12** | [State Machine Patterns](./12_state_machines.md) | Specifications for AppLock and Focus Timer state machines |
| **13** | [Compose Performance](./13_compose_performance.md) | Jetpack Compose performance rules, recomposition strategies, and anti-patterns |
| **14** | [Performance Benchmark Suite](../../benchmark/README.md) | Official performance budgets, empirical benchmarks, and profiling recipes |

---

## 🎯 Key Architectural Principles

1. **Clean Architecture & Unidirectional Data Flow (UDF)**
   - Strict separation of concerns between Presentation, Domain, Data, and Platform layers.
   - UI components observe immutable `StateFlow` and send user intent actions to ViewModels.

2. **Framework Independence & Zero Unnecessary Overhead**
   - Manual Dependency Injection eliminates annotation processing overhead and slow build times.
   - Pure Kotlin Domain Layer without Android framework dependencies.

3. **Offline-First & Local Persistence**
   - Room DB serves as the single source of truth for app state.
   - Asynchronous reactive data streams (`Flow`) update UI automatically on database changes.

4. **Deterministic Concurrency & Testability**
   - Injectable `DispatchersProvider` ensures zero non-deterministic async code in unit tests.
   - Comprehensive test coverage for Calendar Math, Habit Cycle algorithms, and Use Cases.
