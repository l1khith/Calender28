package com.l1khith.calender28.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.FocusSession
import com.l1khith.calender28.repository.FocusRepositoryImpl
import com.l1khith.calender28.repository.TaskRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import androidx.compose.runtime.Immutable

private const val TAG = "FocusSessionManager"

@Immutable
sealed class FocusState {
    object Idle : FocusState()
    data class Setup(val task: AppTask) : FocusState()
    data class Active(
        val task: AppTask,
        val mode: String, // "timer" or "stopwatch"
        val targetDurationSeconds: Int,
        val elapsedSeconds: Int,
        val isPaused: Boolean,
        val startedAtMs: Long,
        val isScreenPinned: Boolean = false
    ) : FocusState() {
        val remainingSeconds: Int
            get() = (targetDurationSeconds - elapsedSeconds).coerceAtLeast(0)

        val displaySeconds: Int
            get() = if (mode == "timer") remainingSeconds else elapsedSeconds

        val formattedTime: String
            get() {
                val totalSec = displaySeconds
                val mins = totalSec / 60
                val secs = totalSec % 60
                return String.format("%02d:%02d", mins, secs)
            }

        val progressFraction: Float
            get() = if (mode == "timer" && targetDurationSeconds > 0) {
                (elapsedSeconds.toFloat() / targetDurationSeconds.toFloat()).coerceIn(0f, 1f)
            } else 0f
    }

    data class Completed(
        val task: AppTask,
        val mode: String,
        val durationSeconds: Int,
        val completedAtMs: Long = System.currentTimeMillis()
    ) : FocusState() {
        val formattedDuration: String
            get() {
                val mins = durationSeconds / 60
                val secs = durationSeconds % 60
                return if (mins > 0) {
                    "${mins}m ${secs}s"
                } else {
                    "${secs}s"
                }
            }
    }
}

object FocusSessionManager {

    private val _focusState = MutableStateFlow<FocusState>(FocusState.Idle)
    val focusState: StateFlow<FocusState> = _focusState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null

    // Wall-clock tracking variables to avoid sleep drift
    private var sessionStartTimeMs: Long = 0L
    private var accumulatedElapsedSeconds: Int = 0
    private var lastResumeTimestampMs: Long = 0L

    fun openSetup(task: AppTask) {
        if (_focusState.value is FocusState.Active) return
        if (task.completed) return
        _focusState.value = FocusState.Setup(task)
    }

    fun closeSetup() {
        if (_focusState.value is FocusState.Setup) {
            _focusState.value = FocusState.Idle
        }
    }

    fun startFocus(context: Context, task: AppTask, mode: String, durationMinutes: Int = 25, pinScreen: Boolean = false) {
        if (task.completed) return
        val targetSeconds = if (mode == "timer") durationMinutes * 60 else 0
        sessionStartTimeMs = System.currentTimeMillis()
        lastResumeTimestampMs = sessionStartTimeMs
        accumulatedElapsedSeconds = 0

        _focusState.value = FocusState.Active(
            task = task,
            mode = mode,
            targetDurationSeconds = targetSeconds,
            elapsedSeconds = 0,
            isPaused = false,
            startedAtMs = sessionStartTimeMs,
            isScreenPinned = pinScreen
        )

        vibrate(context, 100)
        startForegroundService(context)
        startTicker(context)
    }

    fun togglePause(context: Context) {
        val current = _focusState.value as? FocusState.Active ?: return
        if (current.isPaused) {
            // Resume
            lastResumeTimestampMs = System.currentTimeMillis()
            _focusState.value = current.copy(isPaused = false)
            vibrate(context, 50)
            startTicker(context)
        } else {
            // Pause
            val now = System.currentTimeMillis()
            val segmentSec = ((now - lastResumeTimestampMs) / 1000).toInt()
            accumulatedElapsedSeconds += segmentSec
            tickerJob?.cancel()
            _focusState.value = current.copy(isPaused = true, elapsedSeconds = accumulatedElapsedSeconds)
            vibrate(context, 50)
        }
        updateServiceNotification(context)
    }

    fun stopOrCancelFocus(context: Context, markAsCancelled: Boolean = true) {
        val current = _focusState.value as? FocusState.Active
        tickerJob?.cancel()
        stopForegroundService(context)

        if (current != null && current.elapsedSeconds > 10) {
            // Save session even if interrupted
            val now = System.currentTimeMillis()
            val focusRepo = FocusRepositoryImpl(context.applicationContext)
            scope.launch(Dispatchers.IO) {
                focusRepo.saveFocusSession(
                    FocusSession(
                        taskId = current.task.id,
                        taskTitle = current.task.title,
                        mode = current.mode,
                        durationSeconds = current.elapsedSeconds,
                        completed = !markAsCancelled,
                        startedAt = current.startedAtMs,
                        endedAt = now,
                        wasCancelled = markAsCancelled
                    )
                )
            }
        }

        _focusState.value = FocusState.Idle
    }

    fun finishSessionAsComplete(context: Context) {
        val current = _focusState.value as? FocusState.Active ?: return
        tickerJob?.cancel()
        stopForegroundService(context)

        val duration = if (current.mode == "timer") {
            current.targetDurationSeconds
        } else {
            current.elapsedSeconds
        }

        vibrate(context, 300)
        FocusNotificationHelper.showCompletionNotification(
            context,
            current.task.title,
            String.format("%02d:%02d", duration / 60, duration % 60)
        )

        _focusState.value = FocusState.Completed(
            task = current.task,
            mode = current.mode,
            durationSeconds = duration
        )
    }

    fun commitCompletedTask(context: Context, markTaskDone: Boolean = true) {
        val completedState = _focusState.value as? FocusState.Completed ?: return
        val taskRepo = TaskRepositoryImpl(context.applicationContext)
        val focusRepo = FocusRepositoryImpl(context.applicationContext)
        val coinRepo = com.l1khith.calender28.repository.CoinRepositoryImpl(context.applicationContext)

        scope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            focusRepo.saveFocusSession(
                FocusSession(
                    taskId = completedState.task.id,
                    taskTitle = completedState.task.title,
                    mode = completedState.mode,
                    durationSeconds = completedState.durationSeconds,
                    completed = true,
                    startedAt = completedState.completedAtMs - (completedState.durationSeconds * 1000L),
                    endedAt = now,
                    wasCancelled = false
                )
            )

            // Reward focus session coins
            val durationMinutes = (completedState.durationSeconds / 60).coerceAtLeast(1)
            coinRepo.rewardFocusSessionComplete(completedState.task.title, durationMinutes)

            if (markTaskDone && !completedState.task.completed) {
                val updatedTask = taskRepo.toggleTaskCompletion(completedState.task)
                val isRecurring = updatedTask.recurringParentId != null || updatedTask.isGenerated == 1
                coinRepo.rewardTaskCompletion(
                    taskId = updatedTask.id,
                    dateStr = updatedTask.associatedDate,
                    isRecurring = isRecurring,
                    taskTitle = updatedTask.title
                )
            }
        }

        _focusState.value = FocusState.Idle
    }

    fun restartSameTask(context: Context) {
        val completedState = _focusState.value as? FocusState.Completed ?: return
        if (completedState.task.completed) {
            _focusState.value = FocusState.Idle
            return
        }
        val durationMins = (completedState.durationSeconds / 60).coerceAtLeast(15)
        startFocus(context, completedState.task, completedState.mode, durationMins)
    }

    private fun startTicker(context: Context) {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                delay(1000L)
                val current = _focusState.value as? FocusState.Active ?: break
                if (current.isPaused) continue

                val now = System.currentTimeMillis()
                val currentSegmentSec = ((now - lastResumeTimestampMs) / 1000).toInt()
                val totalElapsed = accumulatedElapsedSeconds + currentSegmentSec

                if (current.mode == "timer" && totalElapsed >= current.targetDurationSeconds) {
                    // Timer finished!
                    finishSessionAsComplete(context)
                    break
                } else {
                    _focusState.value = current.copy(elapsedSeconds = totalElapsed)
                    updateServiceNotification(context)
                }
            }
        }
    }

    private fun startForegroundService(context: Context) {
        val serviceIntent = Intent(context, FocusService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun stopForegroundService(context: Context) {
        val serviceIntent = Intent(context, FocusService::class.java)
        context.stopService(serviceIntent)
        FocusNotificationHelper.cancelNotification(context)
    }

    private fun updateServiceNotification(context: Context) {
        val current = _focusState.value as? FocusState.Active ?: return
        val notif = FocusNotificationHelper.buildFocusNotification(
            context = context,
            taskTitle = current.task.title,
            timeFormatted = current.formattedTime,
            isTimerMode = current.mode == "timer",
            isPaused = current.isPaused
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(FocusNotificationHelper.NOTIFICATION_ID, notif)
    }

    private fun vibrate(context: Context, durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}
