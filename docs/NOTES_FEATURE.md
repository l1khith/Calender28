# Notes Module Specification

## Calender28 — Minimalist Obsidian-Style Notes Engine

**Version:** 2.0  
**Date:** September 10, 2026  
**Status:** Approved for Implementation  
**Design Philosophy:** Zero Friction, Infinite Canvas, Zero Form-Fields, Pure Theme Tokens, Vector-Only  

---

## 1. Overview

The Notes Module provides an Obsidian/Apple Notes style writing experience in Calender28. Notes are full-screen, unconstrained canvases without artificial form boxes, titles, or category tags. The note's content is the sole source of truth.

### 1.1 Core Principles

| Principle | Specification |
|---|---|
| **No Form Boxes** | The writing surface is an infinite scrollable canvas (`BasicTextField`), not an `OutlinedTextField` or card. |
| **No Title Inputs** | The note has no separate title field. The first non-blank line naturally serves as the title/headline. |
| **No Category Chips** | Categories (Habits, Tasks, Cycles, General) are removed. Notes are unified and searched by content. |
| **Format Flexibility** | Defaults to Plain Text (`.txt`). Three-dot menu allows one-tap "Save as MD" or "Save as TXT". |
| **Markdown Preview** | Available directly when a note is in Markdown format (`.md`), accessible via top bar icon and menu. |
| **Zero Emojis in UI** | No emoji characters or hardcoded symbols in UI strings. Vector `Icon()` components are used exclusively. |
| **Strict Theme Tokens** | All colors derive directly from `MaterialTheme.colorScheme` and `MaterialTheme.typography`. |
| **Native Share & Delete** | Standard Android `Intent.ACTION_SEND` and native `AlertDialog` confirmation. |

---

## 2. Architecture & System Context

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Calender28 App                                │
│                                                                         │
│  ┌────────────────────┐  ┌────────────────────┐  ┌──────────────────┐ │
│  │  UI Layer          │  │  ViewModel Layer   │  │  Data Layer     │ │
│  │                    │  │                    │  │                 │ │
│  │  NotesListScreen   │◀─▶│  NotesViewModel    │◀─▶│  NoteRepository │ │
│  │  NoteEditorScreen  │  │  (Flows & Draft)   │  │  NoteDao        │ │
│  │  NotePreviewScreen │  │                    │  │  Room Database  │ │
│  │  NoteCard          │  │                    │  │                 │ │
│  └────────────────────┘  └────────────────────┘  └──────────────────┘ │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    Android Platform Layer                        │  │
│  │  - Intent.ACTION_SEND (share via system chooser)                │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Database Schema

**Note Entity** (`notes` table)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | TEXT | PRIMARY KEY | UUID |
| `title` | TEXT | NOT NULL | First line of content (auto-derived) |
| `content` | TEXT | NOT NULL | Full note body content |
| `format` | TEXT | NOT NULL | `"TXT"` or `"MD"` (default `"TXT"`) |
| `isPinned` | INTEGER | NOT NULL | Pinned priority flag (default 0) |
| `createdAt` | INTEGER | NOT NULL | Unix epoch timestamp (ms) |
| `updatedAt` | INTEGER | NOT NULL | Unix epoch timestamp (ms) |

---

## 4. UI Specifications

### 4.1 Notes List Screen

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  Profile        Matrix 28                           Streak   Coins   Sync   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Search notes...                                                           │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  [Search Icon] Search notes...                          [Clear Icon] │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  5 notes                                                                    │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  Morning Reflection                                            [MD]  │  │
│  │  I completed my 7-day streak today. Feeling motivated.               │  │
│  │  Updated Sep 10, 2026                                            [⋮] │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │  Groceries                                                    [TXT]  │  │
│  │  Oat milk, bananas, almond butter, sourdough bread                   │  │
│  │  Updated Sep 9, 2026                                             [⋮] │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│                                                                     (+) FAB │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Note Editor Screen (Full-Screen Canvas)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  [← Back]       New Note                                  [👁 Preview] [⋮] │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Start writing...                                                           │
│                                                                             │
│  (Infinite scrolling text surface — no box, no border, no card container)   │
│                                                                             │
│                                                                             │
│                                                                             │
│                                                                             │
│                                                                             │
│                                                                             │
│                                                                             │
│                                                                             │
│                                                                             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Editor Three-Dot Menu:**
- `Save as MD` (or `Save as TXT` if currently MD)
- `Preview` (visible when format is MD)
- `Share`
- `Delete`

### 4.3 Note Preview Screen

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  [← Back]       Preview                                                 [⋮] │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Morning Reflection                                                         │
│                                                                             │
│  I completed my 7-day streak today.                                         │
│                                                                             │
│  Focus Points: 25                                                           │
│  Next Goal: 14-day streak                                                   │
│                                                                             │
│  - Exercise: 30/30 min                                                      │
│  - Meditation: 5/10 min                                                     │
│                                                                             │
│  Notes                                                                      │
│  Feeling motivated. Tomorrow I will add 5 more minutes to meditation.       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Icon Standards

| UI Element | Vector Icon |
|---|---|
| Back arrow | `Icons.AutoMirrored.Filled.ArrowBack` |
| Three-dot menu | `Icons.Default.MoreVert` |
| Search | `Icons.Default.Search` |
| Close / clear | `Icons.Default.Close` |
| Add (FAB) | `Icons.Default.Add` |
| Delete | `Icons.Default.Delete` |
| Share | `Icons.Default.Share` |
| Preview | `Icons.Default.Visibility` |
| Pin | `Icons.Filled.PushPin` / `Icons.Outlined.PushPin` |

---

## 6. Theme Tokens Mapping

| Property | Source Token |
|---|---|
| Screen Background | `MaterialTheme.colorScheme.surface` |
| Card Background | `MaterialTheme.colorScheme.surfaceVariant` / `MatrixColors.SurfaceContainerLow` |
| Primary Text | `MaterialTheme.colorScheme.onSurface` |
| Secondary Text | `MaterialTheme.colorScheme.onSurfaceVariant` |
| Accent / Highlight | `MaterialTheme.colorScheme.primary` |
| Borders & Dividers | `MaterialTheme.colorScheme.outlineVariant` |
| Danger / Destructive | `MaterialTheme.colorScheme.error` |
| Shapes | `MaterialTheme.shapes.*` |
| Typography | `MaterialTheme.typography.*` |
