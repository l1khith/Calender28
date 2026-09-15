# 04. Profiling & Trace Recipes for Calender28

Practical recipes for diagnosing performance, memory allocations, thread contention, and frame drops on Android devices.

---

## 1. Quick Terminal Profiling Recipes

### A. Quick Startup Latency Measurement
Executes 3 consecutive cold launches and outputs `ThisTime`, `TotalTime`, and `WaitTime`:

```powershell
adb shell am force-stop com.l1khith.calender28
adb shell am start-activity -W -n com.l1khith.calender28/.MainActivity -c android.intent.category.LAUNCHER -a android.intent.action.MAIN
```

### B. Frame Rate & Dropped Frame Analysis (`dumpsys gfxinfo`)
Collects frame rendering histograms, total frames rendered, and janky frame percentages:

```powershell
# Reset stats
adb shell dumpsys gfxinfo com.l1khith.calender28 reset

# Perform interactions (scroll calendar, flip coin, toggle habits)...

# Dump rendering metrics
adb shell dumpsys gfxinfo com.l1khith.calender28 framestats
```

Look for:
- `Total frames rendered`
- `Janky frames: X (Y%)` -> Target is `< 1.0%`
- `Number Missed Vsync: 0`
- `Number High Input Latency: 0`

---

## 2. Perfetto Trace Recording Recipe

Perfetto captures kernel-level scheduler events, thread state changes, and Compose recomposition slices.

### Run 10-Second Perfetto Trace:
```powershell
adb shell perfetto \
  -c - --txt \
  -o /data/misc/perfetto-traces/calender28_trace.perfetto-trace <<EOF
buffers: {
    size_kb: 65536
    fill_policy: RING_BUFFER
}
data_sources: {
    config {
        name: "linux.ftrace"
        ftrace_config {
            ftrace_events: "sched_switch"
            ftrace_events: "power/suspend_resume"
            atrace_categories: "am"
            atrace_categories: "view"
            atrace_categories: "dalvik"
            atrace_apps: "com.l1khith.calender28"
        }
    }
}
duration_ms: 10000
EOF
```

Pull trace to desktop:
```powershell
adb pull /data/misc/perfetto-traces/calender28_trace.perfetto-trace ./trace.perfetto-trace
```
Open in [ui.perfetto.dev](https://ui.perfetto.dev).

---

## 3. SQLite Query Plan Inspection Recipe

Ensure all queries use indices and avoid full table scans:

### Step 1: Open SQLite shell inside app data directory
```bash
adb shell "run-as com.l1khith.calender28 sqlite3 /data/data/com.l1khith.calender28/databases/matrix28_database"
```

### Step 2: Verify Query Plans
```sql
-- 1. Verify Task Primary Key Lookup uses index
EXPLAIN QUERY PLAN SELECT * FROM tasks WHERE id = 'sample_id';
-- Expected Output: SEARCH tasks USING INDEX sqlite_autoindex_tasks_1 (id=?)

-- 2. Verify Recurring Parent lookup uses index (added in MIGRATION_8_9)
EXPLAIN QUERY PLAN SELECT * FROM tasks WHERE recurring_parent_id = 'parent_123';
-- Expected Output: SEARCH tasks USING INDEX index_tasks_recurring_parent_id (recurring_parent_id=?)

-- 3. Verify Date filter uses index
EXPLAIN QUERY PLAN SELECT * FROM tasks WHERE associated_date = '2026-08-20';
-- Expected Output: SEARCH tasks USING INDEX index_tasks_associated_date (associated_date=?)
```

---

## 4. Compose Recomposition Tracking (Layout Inspector)

1. Open Android Studio -> **View** -> **Tool Windows** -> **App Inspection**.
2. Select **Layout Inspector** and attach to `com.l1khith.calender28`.
3. Check the **Recomposition Counts** column:
   - `MatrixTopAppBar`: Counts should remain static except when coin count changes.
   - `CalendarMonthGrid`: Should show **0 recompositions** during top bar coin wiggles or bottom bar clicks.
   - `FixedCalendarApp`: Root composable recompositions should be strictly gated.
