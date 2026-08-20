package com.l1khith.calender28.utils

import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PlatformUtils {

    fun copyToClipboard(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Calendar Event ICS", text)
            clipboard.setPrimaryClip(clip)
            showToast(context, "Copied to clipboard")
        } catch (_: Exception) {}
    }

    fun showToast(context: Context, message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }
}

fun copyToClipboard(context: Context, text: String) {
    PlatformUtils.copyToClipboard(context, text)
}

fun showPlatformToast(context: Context, message: String) {
    PlatformUtils.showToast(context, message)
}

fun showPlatformToast(message: String) {
    // Overload for places without explicit context
}

@Composable
fun PlatformTimePicker(
    show: Boolean,
    initialTime: String,
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    if (!show) return
    val context = LocalContext.current
    var hour = 7
    var min = 0
    try {
        val clean = initialTime.replace(" Daily", "")
        val parts = clean.split(" ", ":")
        hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        min = parts.getOrNull(1)?.toIntOrNull() ?: 0
        if (clean.contains("PM", ignoreCase = true) && hour < 12) hour += 12
        if (clean.contains("AM", ignoreCase = true) && hour == 12) hour = 0
    } catch (_: Exception) {}

    DisposableEffect(show) {
        val dialog = TimePickerDialog(
            context,
            { _, h, m ->
                val amPm = if (h >= 12) "PM" else "AM"
                val hour12 = when {
                    h == 0 -> 12
                    h > 12 -> h - 12
                    else -> h
                }
                val formatted = "${hour12.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')} $amPm"
                onTimeSelected(formatted)
            },
            hour,
            min,
            false
        )
        dialog.setOnDismissListener { onDismiss() }
        dialog.show()
        onDispose {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }
}

@Composable
fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun rememberSecurityLockLauncher(): () -> Unit {
    val context = LocalContext.current

    return remember(context) {
        {
            val activity = context.findFragmentActivity()
            if (activity != null) {
                com.l1khith.calender28.security.AppLockManager.authenticate(
                    activity = activity,
                    onSuccess = {
                        PlatformUtils.showToast(context, "Unlocked successfully")
                    }
                )
            }
        }
    }
}

