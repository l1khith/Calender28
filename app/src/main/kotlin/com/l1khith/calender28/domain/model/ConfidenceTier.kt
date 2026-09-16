package com.l1khith.calender28.domain.model

import kotlin.math.ceil

/**
 * Represents the three Confidence Contract commitment tiers.
 *
 * @property id Short string key ("A", "B", "C") persisted in DataStore.
 * @property displayName Human-readable tier label.
 * @property commitmentFraction Required fraction of tasks completed to win (0.50, 0.80, 1.00).
 * @property rewardCoins CalCoins awarded upon winning the bet.
 * @property penaltyCoins CalCoins deducted upon losing the bet (subject to balance floor 0).
 */
enum class ConfidenceTier(
    val id: String,
    val displayName: String,
    val commitmentFraction: Double,
    val rewardCoins: Int,
    val penaltyCoins: Int
) {
    A(
        id = "A",
        displayName = "Casual",
        commitmentFraction = 0.50,
        rewardCoins = 3,
        penaltyCoins = 2
    ),
    B(
        id = "B",
        displayName = "Focused",
        commitmentFraction = 0.80,
        rewardCoins = 7,
        penaltyCoins = 5
    ),
    C(
        id = "C",
        displayName = "All-In",
        commitmentFraction = 1.00,
        rewardCoins = 15,
        penaltyCoins = 12
    );

    /**
     * Calculates the minimum number of tasks required to satisfy this tier.
     * Uses ceiling so e.g. 5 tasks * 50% = 3 tasks.
     */
    fun requiredTasksToWin(totalTasks: Int): Int {
        if (totalTasks <= 0) return 0
        return ceil(totalTasks * commitmentFraction).toInt()
    }

    companion object {
        val DEFAULT = A

        fun fromId(id: String?): ConfidenceTier {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
