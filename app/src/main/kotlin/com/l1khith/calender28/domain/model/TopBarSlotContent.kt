package com.l1khith.calender28.domain.model

enum class TopBarSlotContent(val id: String, val displayName: String) {
    MATRIX28("matrix28", "Matrix 28"),
    USER_NAME("user_name", "Your name"),
    TASK_COUNT("task_count", "Today's task count"),
    PENDING_TODOS("pending_todos", "Pending to-dos");

    companion object {
        val DEFAULT = MATRIX28

        fun fromId(id: String?): TopBarSlotContent {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
