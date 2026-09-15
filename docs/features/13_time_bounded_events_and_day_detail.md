# Feature 13: Time-Bounded Events, Day Detail Screen & Conflict Detection

## Overview
Calender28 transforms from a point-in-time task reminder system into a Google Calendar-grade scheduling platform. Users can schedule events with precise start and end times, multi-day cross-midnight spans, and all-day duration. Tapping any day cell in the 28-day calendar grid navigates to a dedicated full-screen **Day Detail View** with a 24-hour timeline, conflict detection engine, side-by-side overlap rendering, and 7-source daily aggregation.

---

## 1. User Requirements Summary
1. **Tap single day cell** -> Navigate to a **new full-screen view** featuring an hourly timeline (00:00 - 23:59).
2. **Time-bounded tasks** -> Create tasks with both start (upper bound) and end (lower bound) times.
3. **Cross-day scheduling** -> Default end date is same day as start, but user can change to any future day.
4. **Comprehensive Daily Aggregation** -> Aggregate 7 daily sources:
   - One-off timed tasks
   - Cross-day spanning tasks
   - All-day tasks
   - Recurring task instances
   - Habit reminders & streak checkpoints
   - Past & planned focus sessions
   - Scheduled system alarms
5. **Conflict Surface & Resolution** -> Detect and visually indicate 5 types of schedule conflicts on the timeline with side-by-side rendering and auto-suggested conflict-free resolution slots.

---

## 2. Data Model & Room Schema

### Database Version: 10 (`calender28_room.db`)
Additive migration `MIGRATION_9_10` adds the following columns to the `tasks` table:
- `end_date TEXT`: "YYYY-MM-DD" representation of the end date (null = same as `associated_date`).
- `end_time TEXT`: "HH:mm" representation of the end time (null = point-in-time task).
- `is_all_day INTEGER NOT NULL DEFAULT 0`: 1 = all-day task (ignores time bounds).
- `reminder_offset_min INTEGER`: Reminder trigger offset in minutes before start.
- `end_utc_timestamp INTEGER`: Unix epoch milliseconds of end time.
- Index created: `index_tasks_end_date` on `tasks(end_date)`.

### Backward Compatibility
Existing rows have `NULL` values for end fields. The system interprets `end_time == null` as a legacy point-in-time task, guaranteeing 100% data and behavior preservation with zero data loss.

---

## 3. Conflict Detection Engine

### Conflict Types
| Type | Trigger Condition | Severity | Visual Treatment |
|---|---|---|---|
| **HARD_OVERLAP** | `startA < endB && startB < endA` | CRITICAL | Blocks split 50/50 horizontally, red dotted `OverlapOverlay`, `⚠️` badge |
| **BACK_TO_BACK** | `endA == startB` | WARNING | Amber `⟷` boundary marker, zero buffer warning |
| **SAME_SLOT_STACK** | 3+ events within 30-min window | WARNING | Stack chip indicator, tap to expand |
| **ALL_DAY_VS_TIMED**| All-day task + timed task on same date | INFO | Info badge `ℹ️` on timed block |
| **CROSS_DAY_OVERLAP**| Multi-day event overlaps same-day event | CRITICAL | Red continuation banner & conflict badge |

### Conflict Resolution
The `ConflictReviewSheet` computes the earliest conflict-free slot of equal duration using `findConflictFreeSlot()` and presents 1-tap "Move to [HH:mm]" actions.

---

## 4. UI Specifications

### 4.1 Day Detail View
- **TopAppBar**: Displays Fixed 28-day date (e.g., "Sol 14, 2026"), Weekday, Cycle Week, Back navigation, and "Today" quick-jump.
- **Summary Strip**: Horizontal metric badges for tasks, habits, focus, and available free time.
- **Conflict Banner**: Red-tinted alert card appearing when conflicts are present; tapping opens `ConflictReviewSheet`.
- **Hour Timeline (`HourTimeline.kt`)**:
  - 24-hour vertical scrollable list (64.dp per hour).
  - Current time indicator (`NowIndicator.kt`) showing red dotted line and pulse dot when viewing today.
  - Category-tinted blocks (`TimeBlockItem.kt`) with 4dp accent border.
  - Side-by-side layout for concurrent tasks.
  - Continuation headers (`▶ Continues from [Day]`, `◀ Continues to [Day]`) for cross-day tasks.
  - Empty hour slot tap creates a new task pre-populated with start = hour and end = hour + 1.
- **Collapsible Footer Sections**: All-Day, Recurring, Habits, Focus sessions.
- **FAB `+`**: Quick create button pre-filled with the active day.

### 4.2 Create Task Sheet Updates
- Start date/time and End date/time pickers.
- "Same day as start" checkbox (default checked; unchecking enables cross-day picker).
- "All-day event" toggle.
- Live duration chip (e.g. `1h 30m`).
- Reminder offset picker (At time, 10m before, 15m before, 30m before, 1h before).
- Live conflict warning with "Adjust Time" and "Save Anyway" actions.

---

## 5. Performance SLAs
- First composition of `DayDetailScreen`: **< 100ms**
- Timeline scrolling: **60 fps**
- Conflict detection compute: **< 15ms** on `Dispatchers.Default`
- 7-source aggregation compute: **< 50ms** on `Dispatchers.IO`
