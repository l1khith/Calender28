# Feature Spec 06: Device Calendar Sync & ICS Import/Export

## 1. Overview
The **Calendar Sync & ICS Engine** enables two-way interoperability between the on-device 28-day fixed calendar and standard Gregorian calendar tools. It supports importing from `.ics` (iCalendar) files, syncing with device calendars (Google Calendar, Outlook, local calendars via Android `CalendarContract`), and exporting tasks to `.ics` or `.csv`.

---

## 2. Core Components & Architecture

### Source Files
* **ICS Parser**: [`IcsParserRepository.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/repository/IcsParserRepository.kt)
* **Sync Helper**: [`CalendarSyncHelper.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/utils/CalendarSyncHelper.kt)
* **Export Helper**: [`TaskExportHelper.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/utils/TaskExportHelper.kt), [`ExportUtils.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/utils/ExportUtils.kt)
* **UI**: [`SyncDialogs.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/SyncDialogs.kt), [`SyncOptionsBottomSheet.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/SyncOptionsBottomSheet.kt), [`FilePicker.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/FilePicker.kt)

---

## 3. Key Functional Capabilities

### A. Android Device Calendar Sync (`CalendarContract`)
* Queries `CalendarContract.Events` with `READ_CALENDAR` / `WRITE_CALENDAR` runtime permissions.
* Maps external Gregorian events:
  * Reads `DTSTART`, `TITLE`, `DESCRIPTION`, `ALL_DAY`.
  * Converts Gregorian epoch timestamp into corresponding `FixedDate` (`YYYY-MM-DD`).
  * Ingests as tasks with priority weighting.

### B. ICS (RFC 5545) File Import
* Uses standard Android Storage Access Framework (`ActivityResultContracts.OpenDocument`).
* Parses `BEGIN:VEVENT`, `SUMMARY`, `DESCRIPTION`, `DTSTART`, `DTEND`, `RRULE`.
* Batch-inserts imported events into Room database on a background coroutine dispatcher (`Dispatchers.IO`).

### C. Data Export (.ICS & .CSV Backup)
* **ICS Export**: Exports all tasks and scheduled events into RFC 5545 `.ics` format for importing into Apple Calendar, Google Calendar, or Thunderbird.
* **CSV Export**: Exports tasks, habits, and focus logs with headers for spreadsheet analysis in Excel or Google Sheets.
* Utilizes `Intent.ACTION_SEND` or `ActivityResultContracts.CreateDocument` to write files securely without requiring legacy storage permissions.

---

## 4. Privacy & Offline Guarantees
* All calendar sync operations execute **100% on-device**.
* No external server endpoints, analytics trackers, or third-party cloud bridges are involved.
