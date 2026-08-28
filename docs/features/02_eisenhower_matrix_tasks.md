# Feature Spec 02: Eisenhower Matrix Task & Priority Management

## 1. Overview
The **Eisenhower Matrix Task Management** feature enables users to organize tasks into four actionable quadrants based on urgency and importance. Tasks are bound to dates in the 28-day fixed calendar, support reminders, recurrences, priority weighting, and automatic overdue rollover.

```
       URGENT                   NOT URGENT
┌─────────────────────────┬─────────────────────────┐
│     Q1: DO FIRST        │      Q2: SCHEDULE       │
│  (Urgent & Important)   │  (Not Urgent & Import.) │
├─────────────────────────┼─────────────────────────┤
│     Q3: DELEGATE        │       Q4: DON'T DO      │
│  (Urgent & Not Import.) │ (Not Urgent & Not Imp.) │
└─────────────────────────┴─────────────────────────┘
```

---

## 2. Core Components & Architecture

### Source Files
* **Entity**: [`AppTask.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/AppTask.kt), [`RecurringTask.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/RecurringTask.kt)
* **DAO**: [`TaskDao`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/Daos.kt) in `Daos.kt`
* **Repository**: [`TaskRepositoryImpl.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/repository/TaskRepositoryImpl.kt)
* **ViewModel**: [`FixedCalendarViewModel.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/viewmodel/FixedCalendarViewModel.kt)
* **UI**: [`TasksScreen.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/TasksScreen.kt), [`CreateTaskScreen.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/CreateTaskScreen.kt), [`RecurringDialogs.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/RecurringDialogs.kt)

---

## 3. Data Model

### `AppTask` Schema
| Field | Type | Description |
|---|---|---|
| `id` | `String` (UUID / Timestamp) | Primary Key |
| `title` | `String` | Task Title |
| `description` | `String?` | Optional details or notes |
| `associatedDate` | `String` | Format: `YYYY-MM-DD` (Fixed Calendar date) |
| `priority` | `Int` | Priority Flag: `0 = Low`, `1 = Medium`, `2 = High / Urgent` |
| `completed` | `Boolean` | Completion status |
| `isReminder` | `Int` | `1` if exact alarm reminder is enabled, `0` otherwise |
| `reminderTime` | `String?` | Format: `HH:mm` |
| `isGenerated` | `Int` | `1` if generated automatically from a recurring template |
| `recurringTaskId` | `Long?` | Foreign key reference to recurring rule |

---

## 4. Key Functional Capabilities

### A. Quadrant Categorization & Filtering
* **Q1 (Urgent & Important)**: High priority (`priority = 2`) due today or overdue.
* **Q2 (Schedule / Focus)**: Medium/High priority scheduled for upcoming dates.
* **Q3 (Delegate / Low Focus)**: Low priority tasks with imminent reminder deadlines.
* **Q4 (Backlog / Low Priority)**: Standard tasks without immediate time pressure.

### B. Search & Dynamic Filtering
* Real-time search query filtering over titles and descriptions.
* Filter chips for: **All**, **Pending**, **Completed**, **Urgent/Overdue**.

### C. Overdue Rollover Engine
* On app launch or date transition, [`MidnightRolloverWorker`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/service/MidnightRolloverWorker.kt) and `catchUpRollover()` identify uncompleted tasks from past dates and aggregate them into the **Overdue & Attention** section on the current day's agenda.

### D. Recurring Tasks & Templates
* Supports recurrence rules: **Daily**, **Weekly** (specific days), **Monthly** (every 28-day cycle), and **Yearly**.
* Automatic task instance materialization into the Room database without duplication.

---

## 5. UI Interactions
* **Check to Complete**: Instant strike-through with database update.
* **Swipe Actions**: Quick delete and edit actions.
* **Start Deep Focus**: Direct action on any task item to launch a dedicated Focus Timer with that task's context.
* **Creation Dialog**: Supports date picking in the 28-day calendar, exact time picker, recurrence setup, and priority tagging.
