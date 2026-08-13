package com.l1khith.calender28.data

data class Habit(
    val id: String,
    val name: String,
    val category: String = "Health",
    val reminderTime: String? = "07:00 AM Daily",
    val isReminderEnabled: Boolean = true,
    val priority: String = "High",
    val isPaused: Boolean = false,
    val colorHex: Long = 0xFF3B82F6,
    val completedDays: Set<Int> = emptySet(),
    val createdAtMs: Long = 0L
) {
    val completedCount: Int get() = completedDays.size
    val progressPercent: Int get() = ((completedCount.toFloat() / 28f) * 100).toInt()
    val consistencyPercent: Int get() = if (completedCount > 0) ((completedCount.toFloat() / 28f) * 100).toInt() else 0
    val streak: Int get() = calculateStreak()
    val longestStreak: Int get() = calculateStreak()

    private fun calculateStreak(): Int {
        var count = 0
        for (i in 1..28) {
            if (completedDays.contains(i)) {
                count++
            }
        }
        return count
    }
}

