package com.l1khith.calender28.data

import androidx.compose.runtime.Immutable
import java.util.UUID

enum class RecurrenceType {
    DAILY, WEEKDAYS, WEEKENDS, WEEKLY, MONTHLY, YEARLY
}

@Immutable
data class RecurringTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val recurrenceType: RecurrenceType,
    val recurrenceDays: List<Int> = emptyList(), // 0 = Sun, 6 = Sat
    val recurrenceInterval: Int = 1,
    val priority: Int = 1,
    val isActive: Boolean = true,
    val createdAt: Long,
    val endDate: String? = null,
    val reminderTime: String? = null
)

