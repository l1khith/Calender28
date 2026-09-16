package com.l1khith.calender28.domain.model

sealed class NoteContext {
    object Standalone : NoteContext()
    data class FromTask(val taskId: String, val title: String) : NoteContext()
    data class FromHabit(val habitId: String, val name: String) : NoteContext()
    data class FromCycle(val cycleIndex: Long) : NoteContext()
}
