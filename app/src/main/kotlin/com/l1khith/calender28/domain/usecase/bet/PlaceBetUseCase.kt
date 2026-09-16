package com.l1khith.calender28.domain.usecase.bet

import com.l1khith.calender28.domain.model.ConfidenceTier
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

class PlaceBetUseCase(
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(
        dateStr: String,
        tierOverride: ConfidenceTier? = null,
        calendarOverride: Calendar? = null
    ): Result<ConfidenceTier> = withContext(Dispatchers.IO) {
        val tasks = taskRepository.getTasksSpanningDate(dateStr)

        // Rule E1.1 & E1.2: Minimum 3 tasks required
        if (tasks.size < 3) {
            val msg = if (tasks.isEmpty()) "No tasks to bet on." else "Need at least 3 tasks to bet."
            return@withContext Result.failure(IllegalStateException(msg))
        }

        // Rule E1.7: Recovery day check (3 consecutive losses)
        val lossStreak = userPreferencesRepository.betLossStreak.first()
        val lastPlayedDate = userPreferencesRepository.betLastPlayedDate.first()
        if (lossStreak >= 3 && lastPlayedDate != dateStr) {
            return@withContext Result.failure(IllegalStateException("Rest today. Recovery day after 3 consecutive losses."))
        }

        // Rule E1.11 & E1.12: Opt-in window closes at first task completion
        if (tasks.any { it.completed }) {
            return@withContext Result.failure(IllegalStateException("Bet window closed: a task was already completed today."))
        }

        // Rule: Opt-in window closes at 12:00 PM local
        val calendar = calendarOverride ?: Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 12) {
            return@withContext Result.failure(IllegalStateException("Bet window closed at 12:00 PM."))
        }

        val tier = tierOverride ?: ConfidenceTier.fromId(userPreferencesRepository.betTier.first())
        val taskIds = tasks.map { it.id }.toSet()

        userPreferencesRepository.recordBetSnapshot(
            dateStr = dateStr,
            tier = tier.id,
            taskCount = tasks.size,
            taskIds = taskIds
        )

        Result.success(tier)
    }
}
