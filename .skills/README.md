# Project Skills Directory (`.skills/`)

This directory contains the curated suite of Android engineering and optimization skills tailored for the **Calender28** project.

---

## 🛠️ Installed Skills Catalog

| Skill Name | Purpose & Workflow in Calender28 |
|---|---|
| [`adaptive`](adaptive/SKILL.md) | Guides layout adaptation across phones, foldables, and tablets using Compose adaptive components. |
| [`agp-9-upgrade`](agp-9-upgrade/SKILL.md) | Android Gradle Plugin (AGP) version upgrade and maintenance workflows. |
| [`android-cli`](android-cli/SKILL.md) | Provides instructions and command references for the official Google Android CLI. |
| [`android-intent-security`](android-intent-security/SKILL.md) | Intent security auditing for `AlarmReceiver`, `BootReceiver`, and `PendingIntent` declarations. |
| [`android-profiler`](android-profiler/SKILL.md) | Performance profiling, system traces, Room SQL query debugging, jank, and startup bottlenecks. |
| [`appfunctions`](appfunctions/SKILL.md) | Exposes key app workflows (e.g. creating a task, starting focus) to system AI agents and voice shortcuts. |
| [`edge-to-edge`](edge-to-edge/SKILL.md) | Jetpack Compose edge-to-edge migrations, WindowInsets handling, and system bar styling. |
| [`navigation-3`](navigation-3/SKILL.md) | Multi-backstack handling, deep link synthesis, type-safe navigation, and scene transitions. |
| [`play-billing-library-version-upgrade`](play-billing-library-version-upgrade/SKILL.md) | Google Play Billing Library migration and RevenueCat compatibility management. |
| [`play-policy-insights`](play-policy-insights/SKILL.md) | Google Play policy compliance auditor (permissions, data safety declarations, account hygiene). |
| [`r8-analyzer`](r8-analyzer/SKILL.md) | Analyzes ProGuard / R8 keep rules in `proguard-rules.pro` to eliminate redundancies and optimize APK size. |
| [`styles`](styles/SKILL.md) | Jetpack Compose Styles API, theme tokens, and custom styleable design components. |
| [`testing-setup`](testing-setup/SKILL.md) | Test harness infrastructure for Room DAOs, ViewModels, Compose UI tests, and screenshot testing. |

---

## 🚀 How Antigravity Uses These Skills
Antigravity automatically detects these skills to assist with:
* Auditing Android permissions and Play Store compliance.
* Optimizing ProGuard/R8 rules before production builds.
* Generating unit and instrumented tests for Room DAOs and ViewModels.
* Maintaining edge-to-edge UI layouts and system bar insets.
