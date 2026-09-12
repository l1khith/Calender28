# 5. Dependency Injection (Manual DI)

## Design Decision
Calender28 deliberately uses **Pure Kotlin Manual Dependency Injection** instead of external framework libraries (such as Dagger/Hilt or Koin).

### Advantages:
1. **Zero Annotation Processor / KSP Overhead**: Eliminates code generation build delays.
2. **Compile-Time Verification**: Simple Kotlin constructor instantiation verified by standard compiler.
3. **No Reflection**: Instant startup times without dynamic dependency graph resolution.
4. **Complete Control**: Straightforward singleton containment and easy test double substitution.

---

## Current State (What We Have)

The actual `AppContainer` interface and `DefaultAppContainer` are located in `di/AppContainer.kt`. Note the current architectural issues:
- Some repositories get DAOs injected (e.g., `CoinRepositoryImpl` ✅)
- Some repositories take a raw Context and self-resolve their DAOs (`TaskRepositoryImpl` ❌)
- Global singletons (`AppSettingsManager`, `AppLockManager`, `SubscriptionManager`) bypass DI entirely ❌

## Target State (After Refactoring)

- ALL repositories receive DAOs via constructor parameters
- ALL singletons converted to container-managed classes
- Container explicitly provides: database, dispatchers, repositories, and managers

**Target AppContainer Structure:**
```kotlin
interface AppContainer {
    val database: CalenderDatabase
    val dispatchers: DispatchersProvider
    
    // DAOs
    val taskDao: TaskDao
    val habitDao: HabitDao
    
    // Repositories
    val taskRepository: TaskRepository
    val habitRepository: HabitRepository
    
    // Managers
    val appSettingsManager: AppSettingsManager
    val appLockManager: AppLockManager
    val subscriptionManager: SubscriptionManager
}
```

## Migration Checklist

- [ ] Fix `TaskRepositoryImpl`: take `TaskDao` not `Context`
- [ ] Fix `HabitRepositoryImpl`: take `HabitDao` not `Context`
- [ ] Fix `FocusRepositoryImpl`: take `FocusSessionDao` not `Context`
- [ ] Migrate `AppSettingsManager` to class
- [ ] Migrate `AppLockManager` to class
- [ ] Migrate `SubscriptionManager` to class
- [ ] Migrate `ThemeManager` to `CompositionLocal`
