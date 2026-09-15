# 01. Performance Budgets & Service-Level Agreements (SLAs)

Performance budgets define non-negotiable quantitative limits for **Calender28**. Any pull request or refactor that exceeds these budgets without explicit approval must be blocked.

---

## 1. App Startup Latencies

Startup is measured from process creation to the first frame rendered (TTID - Time to Initial Display) and when user interaction is fully enabled (TTFD - Time to Full Display).

| Startup Type | Target Budget | Warning Threshold | Critical Failure | Measurement Method |
|---|---|---|---|---|
| **Cold Startup (TTID)** | `< 400 ms` | `500 ms` | `> 700 ms` | `adb shell am start-activity -W` |
| **Cold Startup (TTFD)** | `< 550 ms` | `700 ms` | `> 1000 ms` | `reportFullyDrawn()` / Macrobenchmark |
| **Warm Startup** | `< 180 ms` | `250 ms` | `> 350 ms` | Background-to-foreground transition |
| **Hot Startup** | `< 80 ms` | `120 ms` | `> 180 ms` | Activity re-entry from back stack |

### Startup Constraints:
1. **Zero Main-Thread Network / Disk IO:** All database creation, DataStore initialization, and SDK setup (MobileAds, RevenueCat) must occur off the main UI thread.
2. **Lazy Initialization:** Dependencies must be resolved lazily via `AppContainer` only when their respective screens are navigated to.

---

## 2. Frame Rendering & Jank Budgets

Compose UI rendering must maintain a locked 60 FPS (16.6ms per frame) on standard displays and 120 FPS (8.3ms per frame) on high-refresh rate displays.

| Metric | Budget | Alert Threshold | Description |
|---|---|---|---|
| **P50 Frame Time** | `< 6.0 ms` | `> 8.0 ms` | Median frame rendering time. |
| **P90 Frame Time** | `< 12.0 ms` | `> 14.0 ms` | 90th percentile frame time during scrolling. |
| **P99 Frame Time** | `< 16.6 ms` | `> 24.0 ms` | 99th percentile frame time. |
| **Janky Frames Rate** | `< 1.0%` | `> 2.5%` | Percentage of frames exceeding 16.6ms deadline. |
| **Frozen Frames (>700ms)** | `0.0%` | `> 0.0%` | Zero tolerance for frozen frames. |

### Scrolling & Animation SLAs:
- **Calendar Month Grid Switch:** Fast swipe transition between months must drop 0 frames.
- **Flame Streak Animation:** Top app bar flame breathing effect must execute purely in the draw phase via `Modifier.graphicsLayer { }` with **0 recompositions per second**.
- **CalCoin 3D Flip Animation:** Flip rotation must execute with **0 recompositions of the calendar grid**.

---

## 3. Database Operation Latency Budgets

Measured against the Room SQLite database on `Dispatchers.IO`.

| Operation | Target Budget | Maximum Allowed | SLA Guarantee |
|---|---|---|---|
| **Single Row Lookup (`getTaskById`)** | `< 0.8 ms` | `2.5 ms` | Indexed query (O(1) B-tree lookup). |
| **Month Tasks Query (28 days)** | `< 3.5 ms` | `8.0 ms` | Filtered by `associated_date` index. |
| **Full Task Insert / Update** | `< 4.0 ms` | `10.0 ms` | Write with transaction wrapper. |
| **Batch Rollover (28-day catch-up)** | `< 15.0 ms` | `35.0 ms` | Background worker catch-up execution. |
| **Main-Thread Disk Access** | **0.0 ms** | **0.0 ms** | **Zero `runBlocking` calls permitted anywhere in codebase.** |

---

## 4. Memory & Resource Footprint

| Metric | Budget | Warning Threshold | Measurement Tool |
|---|---|---|---|
| **Baseline Heap Allocation** | `< 45 MB` | `65 MB` | Android Studio Memory Profiler |
| **Peak Heap (Active Calendar + Tasks)** | `< 75 MB` | `95 MB` | During image/avatar rendering & list scrolls |
| **Retained Activity / ViewModel Leaks** | **0 leaks** | **> 0 leaks** | LeakCanary / Memory Profiler Heap Dump |
| **Draw Loop Allocations** | **0 allocs / frame** | **> 0 allocs** | No `Path()`, `Paint()`, or `Brush()` inside `onDrawBehind` |

---

## 5. Background Services & Battery Quotas

| Background Service | Budget / Quota | Previous Legacy Cost | Optimization Strategy |
|---|---|---|---|
| **Focus Service Steady-State IPC** | **0 calls / min** | 60 calls / min | Native Android `Chronometer` count-up/down in notification. |
| **Focus Service State Transitions** | ≤ 1 call per pause/resume | Continuous polling | Push updates only on state change. |
| **Midnight Rollover Wakeup** | Exactly 1 per 24h | 1 per 24h | WorkManager unique one-time request with exact delay. |
| **Widget Update IPC** | Event-driven (≤ 5 / day) | Polling | Triggered only on task state mutation or date rollover. |
