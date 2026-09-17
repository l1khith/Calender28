package com.l1khith.calender28.domain.usecase.conflicts

import com.l1khith.calender28.domain.model.FreeSlot
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.utils.ConflictResolver
import com.l1khith.calender28.utils.FixedCalendarHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UseCase to compute the nearest 3 conflict-free slots on candidate date or next day.
 */
class SuggestFreeSlotsUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        candidateDate: String,
        durationMinutes: Long,
        ignoreTaskId: String? = null,
        count: Int = 3
    ): List<FreeSlot> = withContext(Dispatchers.Default) {
        val fixedDate = FixedCalendarHelper.parseDateStr(candidateDate)
            ?: FixedCalendarHelper.currentFixedDate()
        val tomorrowFixed = FixedCalendarHelper.fromTimestamp(FixedCalendarHelper.toTimestamp(fixedDate) + 86400000L)

        val tasksToday = taskRepository.getTasksForDate(fixedDate.toString())
        val tasksTomorrow = taskRepository.getTasksForDate(tomorrowFixed.toString())
        val combined = tasksToday + tasksTomorrow

        ConflictResolver.findNearestFreeSlots(
            candidateDate = fixedDate.toString(),
            durationMinutes = durationMinutes,
            existingTasks = combined,
            ignoreTaskId = ignoreTaskId,
            count = count
        )
    }
}
