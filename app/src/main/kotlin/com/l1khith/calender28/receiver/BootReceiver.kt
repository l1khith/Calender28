package com.l1khith.calender28.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.utils.AlarmScheduler
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val action = intent.action ?: return@launch

                val isMidnightRollover = action == "com.l1khith.calender28.ACTION_MIDNIGHT_ROLLOVER"
                if (isMidnightRollover && intent.`package` != context.packageName) {
                    return@launch
                }

                val isBootOrTimeChange = action == Intent.ACTION_BOOT_COMPLETED ||
                    action == Intent.ACTION_TIME_CHANGED ||
                    action == Intent.ACTION_TIMEZONE_CHANGED

                if (isBootOrTimeChange || isMidnightRollover) {
                    val db = TaskDatabase(context)
                    val currentDateStr = FixedCalendarHelper.fromTimestamp(System.currentTimeMillis()).toString()
                    db.catchUpRollover(currentDateStr)

                    val scheduler = AlarmScheduler(context)

                    if (isMidnightRollover || action == Intent.ACTION_BOOT_COMPLETED) {
                        scheduler.scheduleMidnightRollover()
                    }

                    if (isBootOrTimeChange) {
                        val tasks = db.getAllTasks()
                        for (task in tasks) {
                            if (task.reminder && !task.completed && task.utcTimestamp != null && task.utcTimestamp > System.currentTimeMillis()) {
                                scheduler.scheduleTaskAlarm(task)
                            }
                        }
                    }

                    WidgetUpdater.updateWidget(context)
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error during boot/rollover", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

