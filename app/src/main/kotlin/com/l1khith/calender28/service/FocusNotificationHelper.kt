package com.l1khith.calender28.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.l1khith.calender28.MainActivity
import com.l1khith.calender28.R

object FocusNotificationHelper {

    const val CHANNEL_FOCUS = "focus_session_channel"
    const val NOTIFICATION_ID = 28001
    const val COMPLETION_NOTIFICATION_ID = 28002

    const val ACTION_PAUSE = "com.l1khith.calender28.FOCUS_PAUSE"
    const val ACTION_RESUME = "com.l1khith.calender28.FOCUS_RESUME"
    const val ACTION_STOP = "com.l1khith.calender28.FOCUS_STOP"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_FOCUS,
                "Focus Mode Session",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing timer notification for active Focus sessions"
                setShowBadge(false)
                enableVibration(false)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun buildFocusNotification(
        context: Context,
        taskTitle: String,
        timeFormatted: String,
        isTimerMode: Boolean,
        isPaused: Boolean,
        elapsedSeconds: Int = 0,
        remainingSeconds: Int = 0
    ): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "focus_mode")
        }
        val openAppPending = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleActionIntent = Intent(context, FocusActionReceiver::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val toggleActionPending = PendingIntent.getBroadcast(
            context,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopActionIntent = Intent(context, FocusActionReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopActionPending = PendingIntent.getBroadcast(
            context,
            2,
            stopActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPaused) "Focus Mode (Paused)" else "Focus Mode"
        val subtitle = if (isPaused) {
            if (isTimerMode) "$taskTitle • $timeFormatted remaining" else "$taskTitle • $timeFormatted elapsed"
        } else {
            taskTitle
        }

        val toggleActionTitle = if (isPaused) "Resume" else "Pause"

        val builder = NotificationCompat.Builder(context, CHANNEL_FOCUS)
            .setSmallIcon(R.drawable.ic_stopwatch)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(openAppPending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                toggleActionTitle,
                toggleActionPending
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopActionPending
            )

        if (!isPaused) {
            builder.setUsesChronometer(true)
            builder.setShowWhen(true)
            if (isTimerMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setChronometerCountDown(true)
                builder.setWhen(System.currentTimeMillis() + remainingSeconds * 1000L)
            } else {
                builder.setWhen(System.currentTimeMillis() - elapsedSeconds * 1000L)
            }
        } else {
            builder.setUsesChronometer(false)
            builder.setShowWhen(false)
        }

        return builder.build()
    }

    fun showCompletionNotification(context: Context, taskTitle: String, durationFormatted: String) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "focus_mode")
        }
        val openAppPending = PendingIntent.getActivity(
            context,
            3,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FOCUS)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setContentTitle("Focus Session Completed!")
            .setContentText("You focused on '$taskTitle' for $durationFormatted.")
            .setContentIntent(openAppPending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }
}
