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
import kotlin.concurrent.thread

object CalendarContentObserver {
    private var observer: ContentObserver? = null

    fun register(context: Context) {
        if (observer != null) return
        try {
            val handler = Handler(Looper.getMainLooper())
            observer = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    Log.d("CalendarObserver", "System calendar update detected: $uri")
                    thread {
                        try {
                            val systemEvents = importSystemCalendarEvents(context)
                            if (systemEvents.isNotEmpty()) {
                                val db = TaskDatabase(context)
                                for (task in systemEvents) {
                                    db.insertTask(task)
                                }
                                WidgetUpdater.updateWidget(context)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            context.contentResolver.registerContentObserver(
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
