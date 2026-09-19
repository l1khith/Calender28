<p align="center">
  <img src="art/logo.svg" width="128" height="128" alt="Calender28 App Logo" />
</p>

<h1 align="center">Calender28 (Matrix28)</h1>

<p align="center">
  <strong>A high-performance, privacy-first productivity OS based on the 13-Month / 28-Day International Fixed Calendar.</strong><br>
  Built with 100% modern Jetpack Compose, offline-first SQLite persistence, biometric security, gamified habit economics, and minimalist Obsidian-style notes.
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.1.20-blue.svg?logo=kotlin" alt="Kotlin"></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Platform-Android%2014%2B%20%28API%2026%2B%29-green.svg?logo=android" alt="Platform"></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20BOM%202025.06-4285F4.svg?logo=jetpackcompose" alt="Compose"></a>
  <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Database-Room%202.7.1-orange.svg?logo=sqlite" alt="Database"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT"></a>
</p>

---

## 🌟 Overview

Gregorian calendar months are mathematically irregular—varying between 28, 29, 30, and 31 days, causing recurring days of the week to shift perpetually across months. 

**Calender28** solves this by adopting the **International Fixed Calendar**:
- **13 Months × Exactly 28 Days**: Every month begins on Sunday and ends on Saturday.
- **Perfect 4-Week Rhythm**: A calendar date always falls on the exact same weekday every single month.
- **Sol & Year Day**: Sol is intercalated between June and July; Year Day completes the solar year.
- **Unified Productivity Hub**: Eliminates scheduling fragmentation by marrying the 28-day calendar with daily task management, habit loops, deep work focus timers, an infinite markdown canvas, and a gamified reward economy.

---

## ✨ Core Features

### 📅 1. 28-Day Fixed Calendar Engine
- **Perpetual Symmetry**: Clean month views where dates and weekdays never shift.
- **Fast Day & Month Navigation**: Seamless swipeable month transitions and customized top-bar headers.
- **Gregorian Interop**: Native two-way conversion between standard Gregorian dates and 28-day Fixed dates.

### 📝 2. Obsidian-Style Frictionless Notes
- **Infinite Writing Canvas**: Zero artificial form boxes or title prompts. The first line naturally becomes the title.
- **Markdown Preview**: Instant toggle between raw Markdown and styled preview rendered with native Compose typography.
- **Bi-Directional Linking**: Cross-reference tasks, habits, and other notes with fast content search.
- **Export & Share**: Direct export to `.txt` and `.md` via Android system shares.

### ⚡ 3. Habits, Cycles & Confidence Contracts
- **28-Day Habit Cycles**: Track habit adherence visualised over exact 28-day monthly cycles.
- **Confidence Betting**: Stake CalCoins on daily completion targets with Tier A/B/C contracts.
- **Midnight Rollover Worker**: Automated, reliable rollover of incomplete recurring tasks without race conditions or data duplication.

### ⏱️ 4. Focus Session & Pomodoro Engine
- **Pomodoro & Free Timer**: Custom duration work intervals with progress arc visualization.
- **Foreground Service**: Persistent session notifications with real-time countdown updates.
- **Screen Pinning & Anti-Cheat**: Option to lock/pin the screen during focus blocks; navigating away cancels reward payouts.
- **Haptic Feedback**: Custom vibration cues for start, pause, and completion.

### 🪙 5. Gamified Virtual Economy & Sparky Companion
- **CalCoins**: Earn coins for staying consistent with habits, completing focus sessions, and winning confidence contracts.
- **Sparky Pet Companion**: Reactive digital companion animated via Lottie with progressive evolutionary stages based on lifetime streaks and achievements.
- **Dynamic Mood States**: Sparky reflects your productivity, streak momentum, and task velocity.

### 🔒 6. Biometric Security & Privacy
- **100% Offline-First**: All habits, tasks, notes, and coins reside strictly in your local device SQLite database.
- **App Lock**: Biometric fingerprint/face authentication or passcode protection with automatic background timeout.
- **Zero Account Friction**: No mandatory sign-in or external server tracking.

### 🎨 7. Matrix Cyber Aesthetics & Themes
- **Tailored Palettes**:
  - `Default Dark`: Sleek charcoal and deep blue accents.
  - `Matrix Terminal`: Retro cyberpunk emerald terminal phosphor.
  - `Cyber Sol`: Radiant amber and slate contrast.
  - `Nord Frost`: Arctic cyan and twilight blue.
  - `Monolith Monochrome`: Ultra-minimalist OLED pure black.
- **Customizable Top & Bottom Navigation**: Reorder tabs, customize quick-action slots, and personalize header widgets.

### 🆔 8. Persistent Anonymous Identity System
- **Deterministic Device Identity**: Automatically creates an immutable, collision-resistant UUID v4 on first launch.
- **Silent Display Tag**: Automatically pairs a deterministic tag (`User` + 4 random digits + 6 timestamp digits) stored in DataStore.
- **Leaderboard Ready**: Allows future cloud leaderboard and streak sync while keeping the local UI clean and unforced.

---

## 🏛️ Architecture & Tech Stack

Calender28 uses **Clean Architecture with Unidirectional Data Flow (UDF)** optimized for zero ANRs, smooth 60fps animations, and deterministic state transitions:

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer                              │
│  Jetpack Compose Screens • ViewModels • StateFlow<UiState>  │
├─────────────────────────────────────────────────────────────┤
│                    Repository Layer                         │
│  TaskRepo • HabitRepo • NoteRepo • CoinRepo • FocusRepo    │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                             │
│  Room SQLite (DAOs, Indexed Tables) • DataStore Preferences │
├─────────────────────────────────────────────────────────────┤
│                   Platform & Services                       │
│  WorkManager • AlarmScheduler • Foreground FocusService     │
│  Glance Home Widget • BiometricPrompt • RevenueCat • AdMob  │
└─────────────────────────────────────────────────────────────┘
```

### Technology Highlights

| Component | Library / Framework | Purpose |
|---|---|---|
| **Language** | Kotlin 2.1.20 | Modern concise coroutines, serialization, and type-safety |
| **UI Framework** | Jetpack Compose (BOM 2025.06) | Declarative reactive UI with Material 3 design tokens |
| **Dependency Injection** | Manual `AppContainer` | Lightweight, reflection-free, instant startup DI |
| **Persistence** | Room 2.7.1 + SQLite | Indexed relational storage with Flow reactive queries |
| **Preferences** | DataStore Preferences 1.1.6 | Asynchronous non-blocking key-value storage |
| **Background Tasks** | AndroidX WorkManager 2.10.0 | Guarantees daily midnight rollover execution |
| **Home Screen** | AndroidX Glance 1.1.1 | Composable app widget displaying today's tasks |
| **Animations** | Lottie Compose 6.6.2 | Hardware-accelerated interactive Sparky companion animations |
| **Security** | AndroidX Biometric 1.1.0 | Device-level biometric authentication prompt |
| **Monetization** | Google Mobile Ads (AdMob) 24.x | Non-intrusive banner and interstitial ad integration |
| **Subscriptions** | RevenueCat Purchases SDK 10.x | Secure Google Play Billing and Pro entitlement verification |

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug (2024.2+) or newer
- **JDK**: Version 17 or Version 21
- **Android SDK**: Compile SDK 35 / Min SDK 26 (Android 8.0+)

### Building from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/l1khith/Calender28.git
   cd Calender28
   ```

2. **Configure local properties:**
   Create a `secrets.properties` or `local.properties` file in the project root if integrating custom AdMob or RevenueCat keys:
   ```properties
   REVENUECAT_API_KEY=your_revenuecat_api_key_here
   ```

3. **Build the debug APK:**
   ```bash
   # On macOS / Linux
   ./gradlew assembleDebug

   # On Windows (PowerShell)
   .\gradlew assembleDebug
   ```

4. **Install on connected device or emulator:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📁 Repository Structure

```
Calender28/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/l1khith/calender28/
│   │   │   ├── ads/              # AdMob Banner & Interstitial managers
│   │   │   ├── billing/          # RevenueCat & Subscription state handlers
│   │   │   ├── data/             # Room Database, Entities, TypeConverters & DAOs
│   │   │   │   └── user/         # UUID & DisplayName generation infrastructure
│   │   │   ├── di/               # Manual AppContainer & Dependency Injection
│   │   │   ├── repository/       # Data repositories (Task, Habit, Coin, Notes...)
│   │   │   ├── security/         # Biometric AppLockManager & Security overlays
│   │   │   ├── service/          # FocusService, MidnightRolloverWorker, AlarmScheduler
│   │   │   ├── ui/               # Compose Screens, Navigation, Themes & Widgets
│   │   │   │   ├── notes/        # Obsidian-style Note Editor & Markdown viewer
│   │   │   │   ├── sparky/       # Sparky Pet companion & Lottie animations
│   │   │   │   └── theme/        # Color tokens, Typography, Themes & Vectors
│   │   │   ├── utils/            # Fixed calendar math, sound effects & constants
│   │   │   ├── viewmodel/        # Architecture ViewModels & ViewModelFactory
│   │   │   └── widget/           # Jetpack Glance Home Screen Widget
│   │   └── res/                  # Drawable vectors, raw animations & values
│   └── build.gradle.kts
├── docs/                         # Technical specifications and feature deep dives
├── gradle/                       # Version catalogs (libs.versions.toml) & wrapper
├── LICENSE                       # MIT License
└── README.md                     # Project documentation
```

---

## 🛡️ Privacy & Security

- **No Remote Servers**: Calender28 does not transmit your personal calendar entries, habits, notes, or balances to private cloud servers.
- **Biometric Enclave**: Biometric data remains strictly within Android's hardware security module (KeyStore / TEE).
- **Data Portability**: Easily export your data into standard open formats (CSV/ICS/JSON).

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

```
MIT License
Copyright (c) 2026 Likhith (l1khith)
```

---

<p align="center">
  Crafted with precision for deep focus, daily rhythm, and lifelong consistency.
</p>
