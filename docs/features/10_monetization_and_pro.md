# Feature Spec 10: Monetization, RevenueCat Pro Paywall & AdMob

## 1. Overview
The **Monetization & Pro Subscriptions** system powers the application's business model. It integrates RevenueCat SDK v10.x (Google Play Billing Library v9.x) for in-app subscriptions and lifetime purchases, Google AdMob for banner ads in the Free tier, and a developer testing bypass mode.

---

## 2. Core Components & Architecture

### Source Files
* **Manager**: [`SubscriptionManager.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/billing/SubscriptionManager.kt)
* **UI**:
  * [`SubscriptionPaywallDialog.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/SubscriptionPaywallDialog.kt)
  * [`CustomerCenterDialog.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/CustomerCenterDialog.kt)
  * [`BannerAd.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/BannerAd.kt)
* **Preferences**: [`UserPreferencesRepository.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/repository/UserPreferencesRepository.kt)

---

## 3. Tier Comparison & Entitlements

| Feature | Free Tier | Pro Tier |
|---|---|---|
| 28-Day Fixed Calendar | Yes | Yes |
| Eisenhower Matrix Tasks | Yes | Yes |
| 28-Day Habit Tracker | Up to 3 Habits | **Unlimited Habits** |
| Deep Focus Pomodoro Timer | Yes | Yes |
| Themes | Default Dark, Matrix Terminal | **All 5 Custom Themes** |
| AdMob Banner Ads | Visible at bottom | **100% Ad-Free** |
| Advanced Data Exports (ICS/CSV) | Basic | **Full Export & Auto-Sync** |

---

## 4. Technical Implementation

### A. RevenueCat SDK 10.x & Play Billing 9.x
* Configured in `MatrixApplication.kt` via `SubscriptionManager.configure(context, apiKey)`.
* Entitlement check: listens to customer info streams or queries `customerInfo.entitlements["pro"]?.isActive`.
* Purchases and restores handled seamlessly through `Purchases.sharedInstance.purchaseWith(...)` and `restorePurchasesWith(...)`.

### B. Google Mobile Ads (AdMob) Integration
* Implemented in [`BannerAd.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/ui/BannerAd.kt) using Jetpack Compose `AndroidView`.
* If `isProActive == true`, the AdView is completely removed from the composition hierarchy and does not issue any network ad requests.

### C. Developer Testing Mode
* 5 consecutive rapid taps on the version / profile header activates developer mode (`onDevModeTap()`).
* Allows testing Pro features and unlocking premium themes during internal QA without initiating live Google Play transactions.
