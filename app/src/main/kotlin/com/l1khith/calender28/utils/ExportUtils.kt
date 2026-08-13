package com.l1khith.calender28.utils

object ExportUtils {
    fun shareMonthViewImage(
        monthName: String,
        year: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Failed to export month view image")
        }
    }
}

