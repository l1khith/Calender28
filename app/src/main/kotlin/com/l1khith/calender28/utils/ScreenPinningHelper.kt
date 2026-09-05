package com.l1khith.calender28.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log

object ScreenPinningHelper {
    private const val TAG = "ScreenPinningHelper"

    /**
     * Check if screen pinning / lock task mode is currently active
     */
    fun isPinned(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } else {
            @Suppress("DEPRECATION")
            am.isInLockTaskMode
        }
    }

    /**
     * Start screen pinning (app pinning) for the given Activity.
     * Pins Calender28 to the screen so user cannot switch apps without unpinning via device PIN/pattern.
     */
    fun startPinning(activity: Activity): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.startLockTask()
                Log.d(TAG, "startLockTask invoked successfully")
                true
            } else {
                Log.w(TAG, "Screen pinning not supported on API < 21")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start lock task (screen pinning)", e)
            false
        }
    }

    /**
     * Stop screen pinning
     */
    fun stopPinning(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (isPinned(activity)) {
                    activity.stopLockTask()
                    Log.d(TAG, "stopLockTask invoked successfully")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop lock task (screen pinning)", e)
        }
    }
}
