package com.l1khith.calender28.domain.usecase.bet

import com.l1khith.calender28.data.TransactionReason
import com.l1khith.calender28.domain.model.ConfidenceTier
import com.l1khith.calender28.repository.CoinRepository
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class BetEvaluationResult(
    val dateStr: String,
    val won: Boolean,
    val tier: ConfidenceTier,
    val coinsAwarded: Int,
    val streak: Int,
    val isForfeitDueToDeletion: Boolean,
    val bonusCoins: Int = 0,
    val braveBadgeUnlocked: Boolean = false
)

class EvaluateBetUseCase(
    private val taskRepository: TaskRepository,
    private val coinRepository: CoinRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(dateStr: String): Result<BetEvaluationResult> = withContext(Dispatchers.IO) {
        val lastPlayedDate = userPreferencesRepository.betLastPlayedDate.first()
        val activeTierStr = userPreferencesRepository.betActiveTier.first()

        if (lastPlayedDate != dateStr || activeTierStr == null) {
            return@withContext Result.failure(IllegalStateException("No active bet to evaluate for $dateStr."))
        }

        val tier = ConfidenceTier.fromId(activeTierStr)
        val snapshotCount = userPreferencesRepository.betSnapshotTaskCount.first()
        val snapshotIds = userPreferencesRepository.betSnapshotTaskIds.first()
        val currentTasks = taskRepository.getTasksSpanningDate(dateStr)

        val remainingOriginalCount = currentTasks.count { snapshotIds.contains(it.id) }
        val deletedCount = maxOf(0, snapshotCount - remainingOriginalCount)
        val deletionFraction = if (snapshotCount > 0) deletedCount.toDouble() / snapshotCount else 0.0

        // Rule E1.13: >30% task deletion auto-forfeits as LOSS
        val isForfeit = deletionFraction > 0.30

        val completedCount = currentTasks.count { it.completed && snapshotIds.contains(it.id) }
        val requiredToWin = tier.requiredTasksToWin(snapshotCount)

        val isWin = !isForfeit && (completedCount >= requiredToWin)
        val currentStreak = userPreferencesRepository.betStreak.first()
        val currentLossStreak = userPreferencesRepository.betLossStreak.first()

        var bonusCoins = 0
        var braveBadgeUnlocked = false
        val newStreak: Int

        if (isWin) {
            // Reward base tier coins
            coinRepository.addCustomCoins(
                amount = tier.rewardCoins,
                reason = TransactionReason.BET_WIN.name,
                note = "Confidence bet won on $dateStr"
            )

            newStreak = currentStreak + 1
            userPreferencesRepository.updateBetStreak(newStreak)
            userPreferencesRepository.updateBetLossStreak(0)

            // Streak milestone bonuses (Rule E1.8, E1.9)
            if (newStreak == 3) {
                bonusCoins = 25
                coinRepository.addCustomCoins(25, TransactionReason.BET_STREAK_BONUS.name, "3-Day Confidence Streak Milestone")
            } else if (newStreak == 5) {
                bonusCoins = 75
                coinRepository.addCustomCoins(75, TransactionReason.BET_STREAK_BONUS.name, "5-Day Confidence Streak Milestone")
            } else if (newStreak == 7) {
                bonusCoins = 200
                braveBadgeUnlocked = true
                coinRepository.addCustomCoins(200, TransactionReason.BET_STREAK_BONUS.name, "7-Day Confidence Streak Milestone")
            }
        } else {
            // Deduct base penalty coins (floor at 0 enforced by CoinRepository)
            coinRepository.addCustomCoins(
                amount = -tier.penaltyCoins,
                reason = TransactionReason.BET_LOSS.name,
                note = if (isForfeit) "Forfeited due to >30% task deletions on $dateStr" else "Confidence bet lost on $dateStr"
            )

            // Rule E1.10: Grace drop by 1
            newStreak = maxOf(0, currentStreak - 1)
            userPreferencesRepository.updateBetStreak(newStreak)
            userPreferencesRepository.updateBetLossStreak(currentLossStreak + 1)
        }

        userPreferencesRepository.clearActiveBet()

        Result.success(
            BetEvaluationResult(
                dateStr = dateStr,
                won = isWin,
                tier = tier,
                coinsAwarded = if (isWin) tier.rewardCoins + bonusCoins else -tier.penaltyCoins,
                streak = newStreak,
                isForfeitDueToDeletion = isForfeit,
                bonusCoins = bonusCoins,
                braveBadgeUnlocked = braveBadgeUnlocked
            )
        )
    }
}
