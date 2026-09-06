package com.l1khith.calender28.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "FocusActionReceiver"

class FocusActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        try {
            val action = intent?.action ?: return
            Log.d(TAG, "onReceive: Action=$action")

            when (action) {
                FocusNotificationHelper.ACTION_PAUSE,
                FocusNotificationHelper.ACTION_RESUME -> {
                    FocusSessionManager.togglePause(context)
                }
                FocusNotificationHelper.ACTION_STOP -> {
                    FocusSessionManager.finishSessionAsComplete(context)
                }
            }
        } finally {
            pendingResult.finish()
        }
    }
}
