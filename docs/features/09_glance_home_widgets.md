# Feature Spec 09: Jetpack Glance Home Screen Widgets

## 1. Overview
The **Jetpack Glance Widget** brings today's 28-day fixed calendar agenda and Eisenhower tasks directly to the user's Android home screen. Built on Jetpack Glance with Compose syntax, it offers interactive task completion directly from the launcher.

---

## 2. Core Components & Architecture

### Source Files
* **Widget Provider**: [`TodayTaskWidget.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/widget/TodayTaskWidget.kt)
* **Updater & Receiver**: [`WidgetUpdater.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/widget/WidgetUpdater.kt)
* **Manifest Receiver**: `GlanceAppWidgetReceiver` in [`AndroidManifest.xml`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/AndroidManifest.xml)

---

## 3. Technical Implementation

### A. Glance Composable Layout
* Uses `androidx.glance` layout primitives (`GlanceModifier`, `Column`, `Row`, `LazyColumn`, `Text`, `Button`).
* Adapts to multiple widget sizes:
  * **Compact (2x2)**: Displays current Fixed Date (e.g. `Sol 14`), total pending task count, and primary Q1 urgent item.
  * **Expanded (4x2 / 4x4)**: Displays full scrollable task list categorized by priority with interactive check-boxes.

### B. Interactive Action Handling (`ActionCallback`)
* Tapping a task's completion checkbox fires a Glance `ActionCallback` (`ToggleTaskCompletedActionCallback`).
* The callback executes on `Dispatchers.IO`, updating the task's completion flag in `TaskDatabase` and triggering `TodayTaskWidget().update(context, glanceId)` to immediately refresh the widget UI.

### C. Automatic Widget Sync
* `WidgetUpdater.updateAllWidgets(context)` is called automatically whenever:
  * A task is created, modified, or deleted.
  * Midnight date rollover occurs.
  * External system calendar events are synced.
  * Recurring task templates are generated.

---

## 4. Performance & Memory
* Operates with minimal memory footprint by leveraging Glance RemoteViews compilation.
* No long-running background services required for widget rendering.
