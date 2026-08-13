package com.l1khith.calender28.widget

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object WidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var listener: ((Context) -> Unit)? = null

    fun updateWidget(context: Context) {
        scope.launch {
            try {
                val currentListener = listener
                if (currentListener != null) {
                    currentListener(context)
                } else {
                    TodayTaskWidget.updateWidget(context)
                }
            } catch (e: Exception) {
                Log.e("WidgetUpdater", "Failed to update widget", e)
            }
        }
    }

    fun updateWidget() {
        // Fallback overload
    }
}

