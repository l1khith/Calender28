# 12. Customizable & Reorderable Bottom Navigation System

## Overview
The Customizable Bottom Navigation feature gives users full free will to personalize both the **visibility** and **order** of the core bottom navigation tabs in Calender28. Users can reorder tabs via smooth vertical drag-and-drop handles or shortcut controls, enable/disable tabs (Month, Tasks, Habit, Notes), and see their changes reflected instantly across a real-time live preview dock and the primary bottom bar.

---

## 1. Core Rules & Constraints

| Rule | Description |
|------|-------------|
| **Minimum 1 Tab** | User cannot disable all tabs. If only 1 tab is enabled, its switch is locked to prevent empty dock states. |
| **Free-Form Tab Ordering** | Tabs can be reordered in any sequence via drag-and-drop handles (`Icons.Default.DragHandle`) or directional move buttons. |
| **DataStore Persistence** | Tab visibility is persisted in `enabled_bottom_tabs` (`Set<String>`); tab ordering is persisted in `bottom_tab_order` (comma-separated string, e.g. `"tasks,notes,month,habit"`). |
| **Automatic Fallback Route** | If the user disables the tab they are currently viewing, navigation automatically redirects to the first available enabled tab in their custom order. |
| **Corrupted Data Recovery** | If stored order contains duplicates, invalid names, or missing tabs, `BottomTab.parseOrder` safely sanitizes the list and appends any missing tabs. |
| **Zero Emojis** | UI strings and controls strictly use Material vector icons (`Icons.Default.DragHandle`, `Icons.Default.Tune`, `Icons.Default.Restore`, etc.) and theme design tokens. |

---

## 2. Available Tabs

| Tab ID | Default Index | Label | Vector Icon | Description | Default |
|--------|---------------|-------|-------------|-------------|---------|
| `month` | 0 | Month | `Icons.Default.CalendarMonth` | Calendar view and overview | Enabled |
| `tasks` | 1 | Tasks | `Icons.Default.Checklist` | Task list and agenda items | Enabled |
| `habit` | 2 | Habit | `Icons.Default.LocalFireDepartment` | 28-day habit cycles & streaks | Enabled |
| `notes` | 3 | Notes | `Icons.Default.EditNote` | Personal notes & Markdown editor | Enabled |

---

## 3. Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       ProfileScreen                         │
│             (Appearance > Customize Navigation)             │
└──────────────────────────────┬──────────────────────────────┘
                               │ Opens
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    CustomizeNavScreen                       │
│     ┌─────────────────────────────────────────────────┐     │
│     │  Drag Handle + Drag-to-Reorder Gestures        │     │
│     │  Interactive Switches with Minimum Lock        │     │
│     │  Live Preview Dock (Ordered + Filtered)         │     │
│     │  Reset to Defaults (Restores Order + Visibility)│     │
│     └─────────────────────────────────────────────────┘     │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   CustomizeNavViewModel                     │
│         (Handles onMove, reordering, validation)            │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  NavPreferencesRepository                   │
│         (DataStore: enabled_bottom_tabs + tab_order)        │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                     FixedCalendarApp                        │
│          (Renders enabled tabs in user's custom order,       │
│           redirects selectedTab to first visible tab)       │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. UI Specification

### 4.1 Entry in Profile Screen
- Located under the **`APPEARANCE`** section of `ProfileScreen`.
- Title: `"Customize Navigation"`
- Subtitle: `"Choose which tabs appear in the bottom bar"`
- Leading icon: `Icons.Default.Tune`
- Trailing: Chevron `Icons.AutoMirrored.Filled.KeyboardArrowRight`

### 4.2 Customize Navigation Screen
- **Top Bar:** Back button (`←`), title `"Customize Navigation"`.
- **Instruction Header:** `"Drag and drop tabs using the handle to reorder them. Toggle switches to show or hide tabs (at least one tab must remain enabled)."`
- **Reorderable Tab Cards:**
  - Dedicated **Drag Handle** (`Icons.Default.DragHandle`) detecting vertical drag gestures with haptic feedback.
  - Active drag state: card elevates with subtle scaling (`1.03x`), `translationY` following user's drag, `zIndex = 2f`, and active primary border highlight.
  - Directional shortcut arrows (`↑` / `↓`) for instant single-step reordering.
  - 40dp rounded square container with vector icon.
  - Title and subtitle (`"Required — cannot be disabled"` when only 1 enabled tab remains).
  - Material 3 `Switch` (disabled when locked).
- **Live Preview Card:** Renders a floating preview dock reflecting visible tabs in the exact user-customized order with animated spark divider.
- **Reset to Defaults:** Full-width outlined button with `Icons.Default.Restore` restoring canonical order (`Month → Tasks → Habit → Notes`) and enabling all 4 tabs.
