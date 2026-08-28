# Feature Spec 04: Deep Focus & Pomodoro Foreground Service

## 1. Overview
The **Deep Focus & Pomodoro Mode** provides a distraction-free productivity timer backed by an Android Foreground Service. It allows users to execute timed focus intervals (Pomodoro technique), associate sessions with specific Eisenhower tasks or tags, and control running timers via lock-screen media notifications.

---

## 2. Core Components & Architecture

### Source Files
* **Entity**: [`FocusSession.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/FocusSession.kt)
* **DAO**: [`FocusSessionDao`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/data/Daos.kt) in `Daos.kt`
* **Repository**: [`FocusRepositoryImpl.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/repository/FocusRepositoryImpl.kt)
* **Foreground Service**: [`FocusService.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/service/FocusService.kt)
* **Manager**: [`FocusSessionManager.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/service/FocusSessionManager.kt)
* **Receiver**: [`FocusActionReceiver.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/service/FocusActionReceiver.kt)
* **ViewModel**: [`FocusViewModel.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/viewmodel/FocusViewModel.kt)
* **UI**: [`FocusScreens.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/FocusScreens.kt)

---

## 3. Data Model

### `FocusSession` Entity
| Field | Type | Description |
|---|---|---|
| `id` | `Long` (Auto-increment) | Primary Key |
| `taskId` | `String?` | Linked `AppTask` ID (optional) |
| `taskTitle` | `String?` | Title snapshot for historical display |
| `durationMinutes` | `Int` | Planned duration (e.g. 25, 45, 60 min) |
| `actualSecondsSpent`| `Int` | Exact time logged before completion or stop |
| `completed` | `Boolean` | True if full duration elapsed |
| `timestamp` | `Long` | Epoch timestamp of session start |
| `tag` | `String?` | Optional category tag (e.g., "Deep Work", "Study") |

---

## 4. Technical Implementation & Foreground Lifecycle

### A. Android Foreground Service (`FocusService`)
* Starts with `ServiceCompat.startForeground()` with `FOREGROUND_SERVICE_TYPE_SPECIAL_USE` or `MEDIA_PLAYBACK` compatibility.
* Continues ticking accurately in the background without being killed by Android Doze mode or aggressive battery optimizers.
* Emits a countdown timer loop on `Dispatchers.Default` updating remaining seconds every 1000ms.

### B. Persistent Notification with Direct Actions
* Features an ongoing, non-dismissible notification displaying:
  * Running timer countdown (`MM:SS`).
  * Current task / session name.
  * **Pause / Resume** action button.
  * **Stop / Finish** action button.
* Intent actions routed securely through [`FocusActionReceiver`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/service/FocusActionReceiver.kt).

### C. State Machine & Sound / Haptic Cues
* States: `IDLE`, `RUNNING`, `PAUSED`, `COMPLETED`.
* On timer completion:
  * Triggers notification sound and vibration pattern.
  * Auto-persists completed `FocusSession` into Room database.
  * Optionally prompts user to start a Break Session (5 or 15 mins).

---

## 5. UI Features
* **Radial / Circular Progress Ring**: Smooth visual countdown timer rendered in Jetpack Compose.
* **Duration Presets**: Quick buttons for 15m, 25m (Pomodoro), 45m, 60m, and custom duration sliders.
* **Focus Analytics**: Visual statistics showing total focus minutes logged today and across the 28-day month.
