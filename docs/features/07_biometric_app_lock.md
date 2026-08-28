# Feature Spec 07: Biometric Authentication & Security Lock

## 1. Overview
The **Biometric App Lock & Security** feature protects user privacy by requiring biometric authentication (Fingerprint, Face Unlock, or Device PIN/Pattern) to open or access Calender28.

---

## 2. Core Components & Architecture

### Source Files
* **Manager**: [`AppLockManager.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/security/AppLockManager.kt)
* **UI**: [`AppLockScreens.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/security/AppLockScreens.kt)
* **Preferences**: [`UserPreferencesRepository.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/repository/UserPreferencesRepository.kt)
* **Lifecycle Integration**: [`MainActivity.kt`](file:///c:/Users/ailik/AndroidStudioProjects/Calender28/app/src/main/kotlin/com/l1khith/calender28/MainActivity.kt)

---

## 3. Technical Implementation

### A. AndroidX BiometricPrompt Integration
* Utilizes `androidx.biometric.BiometricPrompt` and `BiometricManager.Authenticators.BIOMETRIC_STRONG or DEVICE_CREDENTIAL`.
* Handles hardware readiness states:
  * `BIOMETRIC_SUCCESS`: Hardware available and enrolled.
  * `BIOMETRIC_ERROR_NONE_ENROLLED`: Prompts user to set up screen lock.
  * `BIOMETRIC_ERROR_NO_HARDWARE`: Disables biometric toggle gracefully.

### B. Lifecycle-Aware Auto-Lock Timeout
* Tracks app backgrounding using Android `ProcessLifecycleOwner` or `onStop()` / `onStart()` in `MainActivity`.
* Configurable lock timeout settings:
  * **Immediately** upon backgrounding.
  * **After 1 Minute**.
  * **After 5 Minutes**.
* If elapsed background time exceeds the configured threshold, the app state transitions to `isLocked = true`, rendering the full-screen `AppLockScreen` overlay before any sensitive calendar content is drawn.

### C. Custom In-App PIN Option
* Supports setting an independent 4-digit or 6-digit numeric PIN stored securely using Jetpack DataStore with SHA-256 / PBKDF2 hashing.
* Rate-limiting logic with exponential backoff delays after 5 failed attempts to prevent brute-force attacks.

---

## 4. Privacy & Compliance
* Biometric data never leaves the hardware Trusted Execution Environment (TEE) / Secure Element (SE).
* No biometric hashes or credentials are stored or accessible by the application.
