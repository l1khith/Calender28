# Feature Spec 08: Matrix Dynamic Multi-Theme Engine

## 1. Overview
The **Matrix Dynamic Multi-Theme Engine** provides custom dark-aesthetic design themes inspired by cyberpunk, terminal, nord, and monolith styles. Themes dynamically adjust the Material 3 `ColorScheme`, surface elevations, outline borders, and typography accents.

---

## 2. Core Components & Architecture

### Source Files
* **Theme Manager**: [`ThemeManager.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/theme/ThemeManager.kt)
* **Theme Provider**: [`MatrixTheme.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/theme/MatrixTheme.kt)
* **Color Tokens**: [`MatrixColors`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/theme/MatrixTheme.kt#L12), [`MatrixShapes`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/theme/MatrixTheme.kt#L59), [`AppIcons.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/theme/AppIcons.kt)

---

## 3. Available Themes & Palette Specifications

| Theme Key | Name | Tier | Primary Accent | Background / Surface |
|---|---|---|---|---|
| `DEFAULT` | **Default Dark** | Free | Matrix Blue (`#ADC6FF`) | Deep Onyx (`#131316` / `#1B1B1E`) |
| `TERMINAL`| **Matrix Terminal**| Free | Emerald Phosphor (`#10B981`) | Pure Terminal (`#09090B` / `#18181B`) |
| `CYBER_SOL`| **Cyber Sol** | Pro | Solar Amber (`#F59E0B`) | Midnight Slate (`#0F172A` / `#1E293B`) |
| `NORD_FROST`| **Nord Frost** | Pro | Arctic Ice (`#38BDF8`) | Deep Polar (`#0B132B` / `#1C2541`) |
| `MONOLITH` | **Monolith Mono** | Pro | Stark White (`#FFFFFF`) | Pitch Black (`#000000` / `#121212`) |

---

## 4. Technical Implementation

### Dynamic State Flow & CompositionLocal
`ThemeManager.currentTheme` is a `StateFlow<AppTheme>` collected at the root `MatrixTheme` composable wrapper:

```kotlin
@Composable
fun MatrixTheme(content: @Composable () -> Unit) {
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    val colors = ThemeManager.getColors(currentTheme)

    val colorScheme = darkColorScheme(
        primary = colors.primary,
        onPrimary = Color.Black,
        primaryContainer = colors.primaryContainer,
        secondary = colors.secondary,
        background = colors.surface,
        surface = colors.surfaceContainerLow,
        onSurface = colors.textHeader,
        surfaceVariant = colors.surfaceContainerHigh,
        onSurfaceVariant = colors.textSecondary,
        outlineVariant = colors.outlineVariant
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

### Pro Tier Gate
* Premium themes (`CYBER_SOL`, `NORD_FROST`, `MONOLITH`) check `SubscriptionManager.isProActive`.
* Selecting a locked theme automatically opens the `SubscriptionPaywallDialog`.
* Theme selection is persisted asynchronously in Jetpack DataStore across application sessions.
