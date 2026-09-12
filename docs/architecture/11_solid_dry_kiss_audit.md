# 11. SOLID / DRY / KISS Audit & Compliance Guide

This is the definitive audit document.

## Section 1: Current Violations Found

| # | File | Principle | Severity | Problem | Target Fix |
|---|------|-----------|----------|---------|------------|
| 1 | TaskDatabase.kt | DRY/KISS | P0 | 21 runBlocking | Replace with coroutines/suspend functions |
| 2 | FocusSessionManager.kt | SRP | P0 | God object | Split into focused state/timer classes |
| 3 | FixedCalendarApp.kt | SRP | P1 | 2320-line monolith | Extract sub-composables to separate files |
| 4 | AppSettingsManager.kt | SRP/DI | P1 | Global singleton | Inject via DI, manage state efficiently |
| 5 | AppLockManager.kt | Missing State Machine | P1 | Boolean flags | Implement sealed interface state machine |
| 6 | SubscriptionManager.kt | SRP/DI | P1 | Singleton + tight coupling | Decouple and inject via DI |
| 7 | TaskRepositoryImpl.kt | DI | P1 | Self-resolving DAOs | Use constructor injection |
| 8 | AppContainer.kt | DI | P1 | Inconsistent injection | Standardize manual DI patterns |
| 9 | ThemeManager.kt | DI | P2 | Global mutable state | Expose flow/state via DI |
| 10 | Entities.kt | Performance | P2 | Missing indexes | Add appropriate database indexes |
| 11 | BootReceiver.kt | KISS | P2 | runBlocking in onReceive | Use goAsync() and coroutines |
| 12 | utils/AlarmScheduler.kt | DRY | P3 | Dead duplicate code | Consolidate and remove duplication |

## Section 2: SOLID Principles Reference

### S - Single Responsibility Principle
**What it means:** A class should have one, and only one, reason to change.
**How it applies here:** Managers and UI components shouldn't handle everything. `FixedCalendarApp.kt` handles too many UI states and business logic.
**Current violations:** `FocusSessionManager.kt`, `FixedCalendarApp.kt`, `AppSettingsManager.kt`, `SubscriptionManager.kt`.
**What compliance looks like:** Breaking down monoliths into smaller, focused classes or composables.

### O - Open/Closed Principle
**What it means:** Software entities should be open for extension, but closed for modification.
**How it applies here:** Features like new habit types or timer states shouldn't require modifying existing core logic.
**Current violations:** State management using booleans makes adding new states hard.
**What compliance looks like:** Using sealed classes/interfaces for state machines.

### L - Liskov Substitution Principle
**What it means:** Objects of a superclass shall be replaceable with objects of its subclasses without breaking the application.
**How it applies here:** Interfaces for repositories and managers should be properly abstracted.
**What compliance looks like:** Contract-first interfaces implementations in domain.

### I - Interface Segregation Principle
**What it means:** No client should be forced to depend on methods it does not use.
**How it applies here:** Avoid fat interfaces that mandate unimplemented methods.
**What compliance looks like:** Small, role-specific interfaces.

### D - Dependency Inversion Principle
**What it means:** Depend upon abstractions, not concretions.
**How it applies here:** Avoid singletons and self-instantiation.
**Current violations:** `TaskRepositoryImpl.kt` self-resolving DAOs, `ThemeManager.kt` global state.
**What compliance looks like:** Constructor injection for all dependencies in `AppContainer.kt`.

## Section 3: DRY Compliance Checklist
- [ ] Consolidate dead duplicate code in `utils/AlarmScheduler.kt`.
- [ ] Remove repeated `runBlocking` calls in `TaskDatabase.kt` and use centralized coroutine scopes/extensions.
- [ ] Centralize date math instead of repeating 28-day logic across components.

## Section 4: KISS Compliance Checklist
- [ ] Simplify `BootReceiver.kt` by replacing `runBlocking` with proper background execution (`goAsync()`).
- [ ] Simplify App Lock by replacing boolean flags (`_isLocked`, `_isAppLockEnabled`) with a unified State Machine.
- [ ] Break down `FixedCalendarApp.kt` to make UI logic straightforward and manageable.
