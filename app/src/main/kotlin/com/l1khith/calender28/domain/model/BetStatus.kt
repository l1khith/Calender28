package com.l1khith.calender28.domain.model

enum class BetUnavailableReason {
    NO_TASKS,
    TOO_FEW_TASKS,
    TASK_ALREADY_COMPLETED,
    PAST_NOON,
    RECOVERY_DAY,
    ALREADY_EVALUATED
}

sealed class BetStatus {

    data class Unavailable(
        val reason: BetUnavailableReason,
        val message: String
    ) : BetStatus()

    data class ReadyToBet(
        val tier: ConfidenceTier,
        val taskCount: Int,
        val requiredToWin: Int,
        val reward: Int,
        val penalty: Int,
        val currentStreak: Int
    ) : BetStatus()

    data class Active(
        val tier: ConfidenceTier,
        val snapshotTaskCount: Int,
        val currentCompletedCount: Int,
        val requiredToWin: Int,
        val reward: Int,
        val penalty: Int,
        val currentStreak: Int
    ) : BetStatus()

    data class Evaluated(
        val won: Boolean,
        val tier: ConfidenceTier,
        val coinDelta: Int,
        val streak: Int,
        val message: String
    ) : BetStatus()
}
