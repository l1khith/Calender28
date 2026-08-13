# 1. High-Level Design (HLD)

## Overview
The **Calender28** application is designed following Clean Architecture guidelines combined with Modern Android Architecture standards (Jetpack Compose, Coroutines, Flow, Room, and Material 3).

The system is split into four distinct layers:
1. **Presentation Layer**: UI rendering via Jetpack Compose, ViewModel management, and Glance Home Screen Widgets.
2. **Domain Layer**: Pure Kotlin business logic, Use Cases (Interactors), Domain Models, and Repository interfaces.
3. **Data Layer**: Room SQLite Database, DataStore preferences, and potential Ktor remote clients.
4. **Platform Layer**: Native Android OS integration (AlarmManager, NotificationManager, Biometric Prompt, RevenueCat / AdMob).

---

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION LAYER                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │  Month Tab  │  │  Tasks Tab  │  │ Profile Tab │  │  Widget (Glance)    │ │
│  │  (Calendar) │  │ (Habits+Task)│  │  (Settings) │  │  (Home Screen)     │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘ │
│         │                │                │                   │            │
│         └────────────────┴────────────────┴───────────────────┘            │
│                              │                                               │
│                    ┌─────────┴─────────┐                                     │
│                    │   Compose UI      │  ← StateFlow observers             │
│                    │   (Stateless)     │  ← Event handlers → ViewModel        │
│                    └─────────┬─────────┘                                     │
└──────────────────────────────┼───────────────────────────────────────────────┘
                               │
┌──────────────────────────────┼───────────────────────────────────────────────┐
│                         DOMAIN LAYER                                         │
│                    ┌─────────┴─────────┐                                     │
│                    │   ViewModel       │  ← Business logic orchestration   │
│                    │   (Android VM)    │  ← CoroutineScope (viewModelScope) │
│                    │                   │  ← Exposes: StateFlow, SharedFlow   │
│                    └─────────┬─────────┘                                     │
│                              │                                               │
│         ┌────────────────────┼────────────────────┐                         │
│         │                    │                    │                         │
│    ┌────┴────┐         ┌────┴────┐         ┌────┴────┐                      │
│    │ UseCase │         │ UseCase │         │ UseCase │                      │
│    │  Task   │         │  Habit  │         │  Sync   │                      │
│    └────┬────┘         └────┬────┘         └────┬────┘                      │
│         │                    │                    │                         │
└─────────┼────────────────────┼────────────────────┼───────────────────────────┘
          │                    │                    │
┌─────────┼────────────────────┼────────────────────┼───────────────────────────┐
│    ┌────┴────┐         ┌────┴────┐         ┌────┴────┐                        │
│    │Repository│        │Repository│        │Repository│                        │
│    │  Task   │         │  Habit  │         │  User   │                        │
│    │ (Interface)│      │ (Interface)│      │ (Interface)│                    │
│    └────┬────┘         └────┬────┘         └────┬────┘                        │
│         │                    │                    │                           │
│    ┌────┴────┐         ┌────┴────┐         ┌────┴────┐                      │
│    │  Impl   │         │  Impl   │         │  Impl   │                      │
│    │(Room+DAO)│        │(Room+DAO)│        │(DataStore)│                    │
│    └────┬────┘         └────┴────┘         └────┬────┘                      │
└─────────┼─────────────────────────────────────────┼───────────────────────────┘
          │                                         │
┌─────────┼─────────────────────────────────────────┼───────────────────────────┐
│    ┌────┴───────────────────────────────────────────┴────┐                        │
│    │              DATA LAYER                            │                        │
│    │  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │                        │
│    │  │  Room DB     │  │  DataStore   │  │  Remote  │ │                        │
│    │  │  (SQLite)    │  │  (Prefs)     │  │  (Ktor)  │ │                        │
│    │  └──────────────┘  └──────────────┘  └──────────┘ │                        │
│    └─────────────────────────────────────────────────────┘                        │
└───────────────────────────────────────────────────────────────────────────────────┘
                               │
┌──────────────────────────────┼─────────────────────────────────────────────────┐
│                         PLATFORM LAYER                                           │
│    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│    │ AlarmManager │  │Notification  │  │  Biometric   │  │  AdMob / Revenue │  │
│    │  (Local)     │  │  Manager     │  │   Prompt     │  │      Cat         │  │
│    └──────────────┘  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Layer Responsibilities

### 1. Presentation Layer
- **Jetpack Compose Screen Composables**: Fully declarative, state-driven UI elements.
- **ViewModels**: Retain UI state across configuration changes, execute use cases, and emit one-shot user events (e.g. snackbars, dialogs).
- **Glance App Widgets**: Home screen widgets built using Glance to render today's agenda directly on the launcher.

### 2. Domain Layer
- **Pure Kotlin Domain Models**: Entities independent of database or network schemas (`Task`, `Habit`, `HabitCycle`, `CalendarDate`).
- **Use Cases**: Single-responsibility interactors (e.g. `CreateTaskUseCase`, `ToggleHabitDayUseCase`).
- **Repository Contracts**: Interfaces declaring data access operations.

### 3. Data Layer
- **Room Local Data Sources**: Entities, DAOs, and database configuration enforcing thread-safe SQLite persistence.
- **Preferences Data Source**: Proto/Preferences DataStore for storing user settings and app preferences.
- **Repository Implementations**: Map data layer entities to domain models and enforce caching or dispatching rules.

### 4. Platform Layer
- **AlarmManager & NotificationManager**: Schedule exact local alarms and format system tray notifications.
- **Biometric Integration**: Provide app locking using Android BiometricPrompt APIs.
- **Billing**: Manage subscriptions and in-app purchases via RevenueCat SDK.
