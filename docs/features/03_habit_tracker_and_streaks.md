# Feature Spec 03: 28-Day Habit Cycles & Streak Tracking

## 1. Overview
The **Habit Tracker & 28-Day Cycle Engine** leverages the fixed 28-day calendar architecture to provide structured behavioral tracking. Unlike Gregorian habit trackers with varying month lengths (28, 30, 31 days), every habit in Calender28 operates on a deterministic **28-Day Cycle Grid** divided into four 7-day micro-cycles.

```
28-Day Habit Cycle Grid:
Week 1: [●] [●] [●] [●] [●] [●] [●]  (Days 1-7)
Week 2: [●] [●] [●] [○] [●] [●] [●]  (Days 8-14)
Week 3: [●] [●] [●] [●] [●] [●] [●]  (Days 15-21)
Week 4: [●] [●] [●] [●] [●] [●] [●]  (Days 22-28)
```

---

## 2. Core Components & Architecture

### Source Files
* **Entities**: [`Habit.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/Habit.kt), [`HabitEntry`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/Entities.kt)
* **DAO**: [`HabitDao`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/Daos.kt), [`HabitEntryDao`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/Daos.kt) in `Daos.kt`
* **Repository**: [`HabitRepositoryImpl.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/repository/HabitRepositoryImpl.kt)
* **Algorithm**: [`HabitCycleEngine.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/utils/HabitCycleEngine.kt)
* **UI**: [`HabitScreen.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/HabitScreen.kt)

---

## 3. Data Model

### `Habit` Entity
| Field | Type | Description |
|---|---|---|
| `id` | `Long` (Auto-increment) | Primary Key |
| `name` | `String` | Habit Title (e.g., "Morning Run", "Code 1 Hour") |
| `description` | `String?` | Optional motivation or rules |
| `colorHex` | `String` | Accent color token for visualization |
| `targetDaysPerCycle`| `Int` | Target days out of 28 (default: 28) |
| `reminderTime` | `String?` | Optional daily prompt (`HH:mm`) |
| `currentStreak` | `Int` | Active consecutive days completed |
| `longestStreak` | `Int` | All-time best consecutive streak |
| `createdAt` | `Long` | Epoch timestamp of creation |

### `HabitEntry` Entity
Stores daily completion states (`habitId`, `fixedDateStr`, `completed`, `notes`).

---

## 4. Algorithmic Capabilities

### A. 28-Day Cycle Grid Calculation
`HabitCycleEngine` maps any fixed date to its cycle offset:
```kotlin
val dayInCycle = fixedDate.day // 1..28
val weekInCycle = ((dayInCycle - 1) / 7) + 1 // 1..4
```

### B. Streak Computation Engine
* Iterates backward through consecutive calendar days.
* Tolerates the transition across year boundaries and Year Day without losing active streak momentum.
* Automatically recalculates `currentStreak` and updates `longestStreak` on every toggle.

### C. Cycle Completion Percentage
* Computes real-time progress for the active 28-day cycle:
  $$\text{Completion Rate} = \frac{\text{Completed Days in Current 28-Day Month}}{\text{Target Days}} \times 100\%$$

---

## 5. UI Features
* **Interactive 28-Dot Grid**: Visual 4x7 matrix per habit allowing users to view and toggle past/current days directly.
* **Streak Counter Badges**: Flame/fire streak icons showing active streak count.
* **Habit Analytics Sheet**: Breakdown of weekly adherence and total completions.
* **Creation Dialog**: Color palette picker, custom targets, and reminder time picker.
