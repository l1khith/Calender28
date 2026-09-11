package com.l1khith.calender28.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log

private const val TAG = "FocusService"

class FocusService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        FocusNotificationHelper.createNotificationChannel(this)
        Log.d(TAG, "onCreate: FocusService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Starting FocusService in foreground")

        val state = FocusSessionManager.focusState.value
        val taskTitle = if (state is FocusState.Active) state.task.title else "Task Focus"
        val timeFormatted = if (state is FocusState.Active) state.formattedTime else "00:00"
        val isTimer = if (state is FocusState.Active) state.mode == "timer" else true
        val isPaused = if (state is FocusState.Active) state.isPaused else false

        val notification = FocusNotificationHelper.buildFocusNotification(
            context = this,
            taskTitle = taskTitle,
            timeFormatted = timeFormatted,
            isTimerMode = isTimer,
            isPaused = isPaused
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                FocusNotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(FocusNotificationHelper.NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: FocusService destroyed")
    }
}
