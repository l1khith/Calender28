package com.l1khith.calender28.domain.usecase.bet

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.domain.model.BetStatus
import com.l1khith.calender28.domain.model.BetUnavailableReason
import com.l1khith.calender28.domain.model.ConfidenceTier
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

class GetBetStatusUseCase(
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(
        dateStr: String,
        calendarOverride: Calendar? = null
    ): BetStatus = withContext(Dispatchers.IO) {
        val lastPlayedDate = userPreferencesRepository.betLastPlayedDate.first()
        val activeTierStr = userPreferencesRepository.betActiveTier.first()
        val currentStreak = userPreferencesRepository.betStreak.first()
        val lossStreak = userPreferencesRepository.betLossStreak.first()
        val defaultTierStr = userPreferencesRepository.betTier.first()
        val defaultTier = ConfidenceTier.fromId(defaultTierStr)

        if (lastPlayedDate == dateStr && activeTierStr != null) {
            val tier = ConfidenceTier.fromId(activeTierStr)
            val snapshotCount = userPreferencesRepository.betSnapshotTaskCount.first()
            val snapshotIds = userPreferencesRepository.betSnapshotTaskIds.first()
            val tasks = taskRepository.getTasksSpanningDate(dateStr)
            val completedCount = tasks.count { it.completed && snapshotIds.contains(it.id) }
            val required = tier.requiredTasksToWin(snapshotCount)

            return@withContext BetStatus.Active(
                tier = tier,
                snapshotTaskCount = snapshotCount,
                currentCompletedCount = completedCount,
                requiredToWin = required,
                reward = tier.rewardCoins,
                penalty = tier.penaltyCoins,
                currentStreak = currentStreak
            )
        }

        if (lossStreak >= 3 && lastPlayedDate != dateStr) {
            return@withContext BetStatus.Unavailable(
                reason = BetUnavailableReason.RECOVERY_DAY,
                message = "Rest today. Recovery day after 3 consecutive losses."
            )
        }

        val tasks = taskRepository.getTasksSpanningDate(dateStr)

        if (tasks.isEmpty()) {
            return@withContext BetStatus.Unavailable(
                reason = BetUnavailableReason.NO_TASKS,
                message = "No tasks scheduled for today."
            )
        }
        if (tasks.size < 3) {
            return@withContext BetStatus.Unavailable(
                reason = BetUnavailableReason.TOO_FEW_TASKS,
                message = "Need at least 3 tasks to place a confidence bet."
            )
        }

        if (tasks.any { it.completed }) {
            return@withContext BetStatus.Unavailable(
                reason = BetUnavailableReason.TASK_ALREADY_COMPLETED,
                message = "Bet window closed: a task was already completed today."
            )
        }

        val calendar = calendarOverride ?: Calendar.getInstance()
        if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
            return@withContext BetStatus.Unavailable(
                reason = BetUnavailableReason.PAST_NOON,
                message = "Bet window closed at 12:00 PM."
            )
        }

        val requiredToWin = defaultTier.requiredTasksToWin(tasks.size)
        BetStatus.ReadyToBet(
            tier = defaultTier,
            taskCount = tasks.size,
            requiredToWin = requiredToWin,
            reward = defaultTier.rewardCoins,
            penalty = defaultTier.penaltyCoins,
            currentStreak = currentStreak
        )
    }

    fun observe(
        dateStr: String,
        calendarOverride: Calendar? = null
    ): Flow<BetStatus> {
        val prefsFlow = combine(
            userPreferencesRepository.betActiveTier,
            userPreferencesRepository.betLastPlayedDate,
            userPreferencesRepository.betSnapshotTaskCount,
            userPreferencesRepository.betSnapshotTaskIds,
            userPreferencesRepository.betStreak
        ) { activeTier, lastDate, snapshotCount, snapshotIds, streak ->
            ActiveBetData(activeTier, lastDate, snapshotCount, snapshotIds, streak)
        }

        val statusMetaFlow = combine(
            prefsFlow,
            userPreferencesRepository.betLossStreak,
            userPreferencesRepository.betTier
        ) { betData, lossStreak, defaultTierStr ->
            BetStateMeta(betData, lossStreak, defaultTierStr)
        }

        return combine(
            taskRepository.getTasksSpanningDateFlow(dateStr),
            statusMetaFlow
        ) { tasks, meta ->
            val lastPlayedDate = meta.betData.lastPlayedDate
            val activeTierStr = meta.betData.activeTier
            val currentStreak = meta.betData.streak

            if (lastPlayedDate == dateStr && activeTierStr != null) {
                val tier = ConfidenceTier.fromId(activeTierStr)
                val completedCount = tasks.count { it.completed && meta.betData.snapshotIds.contains(it.id) }
                val required = tier.requiredTasksToWin(meta.betData.snapshotCount)

                return@combine BetStatus.Active(
                    tier = tier,
                    snapshotTaskCount = meta.betData.snapshotCount,
                    currentCompletedCount = completedCount,
                    requiredToWin = required,
                    reward = tier.rewardCoins,
                    penalty = tier.penaltyCoins,
                    currentStreak = currentStreak
                )
            }

            if (meta.lossStreak >= 3 && lastPlayedDate != dateStr) {
                return@combine BetStatus.Unavailable(
                    reason = BetUnavailableReason.RECOVERY_DAY,
                    message = "Rest today. Recovery day after 3 consecutive losses."
                )
            }

            if (tasks.isEmpty()) {
                return@combine BetStatus.Unavailable(
                    reason = BetUnavailableReason.NO_TASKS,
                    message = "No tasks scheduled for today."
                )
            }

            if (tasks.size < 3) {
                return@combine BetStatus.Unavailable(
                    reason = BetUnavailableReason.TOO_FEW_TASKS,
                    message = "Need at least 3 tasks to place a confidence bet."
                )
            }

            if (tasks.any { it.completed }) {
                return@combine BetStatus.Unavailable(
                    reason = BetUnavailableReason.TASK_ALREADY_COMPLETED,
                    message = "Bet window closed: a task was already completed today."
                )
            }

            val calendar = calendarOverride ?: Calendar.getInstance()
            if (calendar.get(Calendar.HOUR_OF_DAY) >= 12) {
                return@combine BetStatus.Unavailable(
                    reason = BetUnavailableReason.PAST_NOON,
                    message = "Bet window closed at 12:00 PM."
                )
            }

            val defaultTier = ConfidenceTier.fromId(meta.defaultTierStr)
            val requiredToWin = defaultTier.requiredTasksToWin(tasks.size)
            BetStatus.ReadyToBet(
                tier = defaultTier,
                taskCount = tasks.size,
                requiredToWin = requiredToWin,
                reward = defaultTier.rewardCoins,
                penalty = defaultTier.penaltyCoins,
                currentStreak = currentStreak
            )
        }
    }

    private data class ActiveBetData(
        val activeTier: String?,
        val lastPlayedDate: String?,
        val snapshotCount: Int,
        val snapshotIds: Set<String>,
        val streak: Int
    )

    private data class BetStateMeta(
        val betData: ActiveBetData,
        val lossStreak: Int,
        val defaultTierStr: String
    )
}
