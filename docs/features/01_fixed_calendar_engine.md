# Feature Spec 01: Fixed 28-Day Calendar Engine

## 1. Overview & Conceptual Model
The **Fixed 28-Day Calendar Engine** implements the Cotsworth / International Fixed Calendar system. Instead of the irregular Gregorian calendar (28 to 31 days per month), Calender28 organizes the year into:
* **13 Months** of exactly **28 Days** (4 weeks each: 7 × 4 = 28).
* **Sol**: A 13th month inserted between June and July (Month index 7).
* **Year Day**: An extra intercalary day (Day 365) outside the weekly cycle at the end of December.
* **Leap Day**: An intercalary day (Day 366 in leap years) inserted after June / before Sol.
* **Every Month Starts on Sunday** and ends on Saturday, making every date fall on the exact same day of the week in every month, every year.

```
Month Structure (28 Days):
 Sun  Mon  Tue  Wed  Thu  Fri  Sat
   1    2    3    4    5    6    7
   8    9   10   11   12   13   14
  15   16   17   18   19   20   21
  22   23   24   25   26   27   28
```

---

## 2. Core Components & Architecture

### Source Files
* **Helper**: [`FixedCalendarHelper.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/utils/FixedCalendarHelper.kt)
* **Model**: [`FixedDate.kt` / `FixedMonth`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/utils/FixedCalendarHelper.kt)
* **ViewModel**: [`FixedCalendarViewModel.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/viewmodel/FixedCalendarViewModel.kt)
* **UI**: [`FixedCalendarApp.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/FixedCalendarApp.kt)

### Month Definitions
```kotlin
val FIXED_MONTHS = listOf(
    "January", "February", "March", "April", "May", "June",
    "Sol", // Intercalary 13th Month
    "July", "August", "September", "October", "November", "December"
)
```

---

## 3. Mathematical Conversion Logic

### Gregorian Timestamp → Fixed Date
1. Calculate the day of the year (`dayOfYear` ∈ [1, 365/366]).
2. Determine leap year status (`year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)`).
3. Handle special days:
   * **Leap Day**: If `isLeapYear` and `dayOfYear == 169` (after June 28), it is treated as Leap Day.
   * **Year Day**: If `dayOfYear == 365` (or `366` in leap years), it is Year Day.
4. Calculate standard month and day:
   ```kotlin
   val zeroIndexedDay = dayOfYear - 1
   val month = (zeroIndexedDay / 28) + 1
   val day = (zeroIndexedDay % 28) + 1
   ```

### Fixed Date → Gregorian Milliseconds
Converts a `FixedDate(year, month, day)` back to epoch milliseconds for interoperability with Android system alarms, notification managers, and calendar providers.

---

## 4. UI Representation & Navigation
* **Horizontal Month Pager**: Allows scrolling across all 13 months with year wrap-around.
* **7x4 Grid Matrix**: Shows 28 days with indicator dots for scheduled tasks, urgent priorities, and completed habit marks.
* **Current Day Highlight**: Visual badge identifying today's date in the fixed system with one-tap "Go to Today" button.
* **Gregorian Overlay Subtitle**: Displays the corresponding Gregorian date (e.g., `"Sol 14, 2026 • Jul 01, 2026"`) for easy real-world alignment.

---

## 5. Edge Cases & Safety
* **Leap Year Calculations**: Fully conforms to standard Gregorian 400-year leap cycles.
* **String Serialization Format**: Standardized as `YYYY-MM-DD` (where MM ranges from `01` to `13`, and DD ranges from `01` to `28`, plus `YD` for Year Day).
* **Deterministic Fallback**: If an invalid date string is parsed, defaults gracefully to the current calculated fixed date.
