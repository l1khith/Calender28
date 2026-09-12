package com.l1khith.calender28.utils

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.util.Log
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object CalendarContentObserver {
    private var observer: ContentObserver? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    fun register(context: Context) {
        if (observer != null) return
        val appContext = context.applicationContext
        try {
            val handler = Handler(Looper.getMainLooper())
            observer = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    Log.d("CalendarObserver", "System calendar update detected: $uri")
                    syncJob?.cancel()
                    syncJob = scope.launch {
                        delay(1500L) // debounce sync bursts
                        try {
                            val systemEvents = importSystemCalendarEvents(appContext)
                            if (systemEvents.isNotEmpty()) {
                                val db = TaskDatabase(appContext)
                                for (task in systemEvents) {
                                    db.insertTask(task)
                                }
                                WidgetUpdater.updateWidget(appContext)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            appContext.contentResolver.registerContentObserver(
                CalendarContract.Events.CONTENT_URI,
                true,
                observer!!
            )
            Log.d("CalendarObserver", "Registered automatic CalendarContentObserver")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unregister(context: Context) {
        try {
            if (observer != null) {
                context.contentResolver.unregisterContentObserver(observer!!)
                observer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
