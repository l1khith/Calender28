package com.l1khith.calender28.security

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.l1khith.calender28.repository.UserPreferencesRepository
import com.l1khith.calender28.repository.createDataStore
import com.l1khith.calender28.utils.PlatformUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AppLockManager"

sealed interface LockState {
    object Initializing : LockState
    object Unlocked : LockState
    object Locked : LockState
    object Authenticating : LockState
    data class Backgrounded(val timestampMs: Long) : LockState
}

object AppLockManager {

    private val _lockState = MutableStateFlow<LockState>(LockState.Unlocked)
    val lockState: StateFlow<LockState> = _lockState.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(false)
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var prefsRepo: UserPreferencesRepository? = null
    private var isInitialized = false

    // Timeout: 0 means lock immediately when app goes to background
    var lockTimeoutMillis: Long = 0L

    private fun transitionTo(newState: LockState) {
        _lockState.value = newState
        _isLocked.value = when (newState) {
            is LockState.Locked, is LockState.Authenticating -> true
            is LockState.Unlocked, is LockState.Initializing -> false
            is LockState.Backgrounded -> _isLocked.value
        }
    }

    fun init(context: Context, scope: CoroutineScope) {
        if (isInitialized) return
        isInitialized = true
        transitionTo(LockState.Initializing)

        val repo = UserPreferencesRepository.getInstance(context.applicationContext)
        prefsRepo = repo

        scope.launch(Dispatchers.Default) {
            repo.isAppLockEnabled.collect { enabled ->
                Log.d(TAG, "isAppLockEnabled observed from DataStore: $enabled")
                _isAppLockEnabled.value = enabled
                if (enabled && _lockState.value is LockState.Initializing) {
                    transitionTo(LockState.Locked)
                } else if (!enabled) {
                    transitionTo(LockState.Unlocked)
                }
            }
        }
    }

    fun onActivityResumed() {
        if (!_isAppLockEnabled.value) {
            transitionTo(LockState.Unlocked)
            return
        }

        val currentState = _lockState.value
        if (currentState is LockState.Backgrounded) {
            val elapsed = System.currentTimeMillis() - currentState.timestampMs
            if (elapsed >= lockTimeoutMillis) {
                Log.d(TAG, "App was in background for ${elapsed}ms (timeout=${lockTimeoutMillis}ms), locking app")
                transitionTo(LockState.Locked)
            } else {
                transitionTo(LockState.Unlocked)
            }
        }
    }

    fun onActivityPaused() {
        if (_isAppLockEnabled.value && _lockState.value !is LockState.Locked) {
            transitionTo(LockState.Backgrounded(System.currentTimeMillis()))
        }
    }

    fun unlock() {
        transitionTo(LockState.Unlocked)
    }

    fun lockNow() {
        if (_isAppLockEnabled.value) {
            transitionTo(LockState.Locked)
        }
    }

    /**
     * Determines whether biometric or device credential (PIN/pattern/password) can be used on this device.
     */
    fun canAuthenticate(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    }

    /**
     * Authenticates the user with Biometrics or Device PIN/Pattern/Password.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Calender28",
        subtitle: String = "Verify your identity",
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricManager = BiometricManager.from(activity)

        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuth = biometricManager.canAuthenticate(authenticators)

        if (canAuth == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            // No biometric hardware, but device credential (PIN/Pattern) might still be checked
            Log.w(TAG, "No biometric hardware available on device")
        } else if (canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            PlatformUtils.showToast(activity, "Please set up a screen lock or fingerprint in Settings first")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            authenticators
                        )
                    }
                    activity.startActivity(enrollIntent)
                } else {
                    activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
            } catch (_: Exception) {
                activity.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
            onError("No screen lock or biometric enrolled")
            return
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)

        val promptInfo = promptInfoBuilder.build()

        transitionTo(LockState.Authenticating)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.d(TAG, "Biometric authentication succeeded")
                    transitionTo(LockState.Unlocked)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.e(TAG, "Biometric authentication error $errorCode: $errString")
                    if (_isAppLockEnabled.value) {
                        transitionTo(LockState.Locked)
                    }
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        PlatformUtils.showToast(activity, "$errString")
                    }
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    Log.w(TAG, "Biometric authentication failed")
                    if (_isAppLockEnabled.value) {
                        transitionTo(LockState.Locked)
                    }
                    onError("Authentication failed")
                }
            }
        )

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch BiometricPrompt", e)
            onError(e.message ?: "Authentication initialization failed")
        }
    }

    /**
     * Toggles App Lock on/off with security verification.
     */
    fun toggleAppLock(
        activity: FragmentActivity,
        enable: Boolean,
        scope: CoroutineScope,
        onComplete: (Boolean) -> Unit
    ) {
        val actionTitle = if (enable) "Enable App Lock" else "Disable App Lock"
        val actionSubtitle = if (enable) "Verify your identity to secure Calender28" else "Verify your identity to remove app lock"

        authenticate(
            activity = activity,
            title = actionTitle,
            subtitle = actionSubtitle,
            onSuccess = {
                val repo = prefsRepo ?: UserPreferencesRepository.getInstance(activity.applicationContext)
                prefsRepo = repo

                scope.launch(Dispatchers.IO) {
                    repo.updateIsAppLockEnabled(enable)
                    _isAppLockEnabled.value = enable
                    if (!enable) {
                        _isLocked.value = false
                    }
                    launch(Dispatchers.Main) {
                        PlatformUtils.showToast(
                            activity,
                            if (enable) "App Lock Enabled" else "App Lock Disabled"
                        )
                        onComplete(true)
                    }
                }
            },
            onError = { err ->
                onComplete(false)
            }
        )
    }
}
