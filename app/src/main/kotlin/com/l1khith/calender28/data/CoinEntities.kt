package com.l1khith.calender28.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coin_balance")
data class CoinBalanceEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Int = 0
)

@Entity(tableName = "coin_transactions")
data class CoinTransactionEntity(
    @PrimaryKey val id: String,
    val amount: Int,
    val reason: String,
    val timestamp: String,
    val note: String? = null
)

enum class TransactionReason(val displayName: String, val defaultAmount: Int) {
    DAILY_LOGIN("Daily App Open", com.l1khith.calender28.utils.Constants.REWARD_DAILY_LOGIN),
    HABIT_CYCLE_COMPLETE("Habit Cycle Completed", com.l1khith.calender28.utils.Constants.REWARD_HABIT_CYCLE_COMPLETE),
    PARTIAL_HABIT_PROGRESS("Habit Day Tracked", com.l1khith.calender28.utils.Constants.REWARD_HABIT_DAY_LOG),
    DAILY_TASK("Recurring Task Completed", com.l1khith.calender28.utils.Constants.REWARD_RECURRING_TASK),
    STREAK_7_DAY("7-Day Streak Bonus", com.l1khith.calender28.utils.Constants.REWARD_STREAK_7_DAY),
    STREAK_14_DAY("14-Day Streak Bonus", com.l1khith.calender28.utils.Constants.REWARD_STREAK_14_DAY),
    STREAK_30_DAY("30-Day Streak Bonus", com.l1khith.calender28.utils.Constants.REWARD_STREAK_30_DAY),
    MILESTONE_10_CYCLES("10 Habit Cycles Milestone", com.l1khith.calender28.utils.Constants.REWARD_MILESTONE_10_CYCLES),
    MILESTONE_50_CYCLES("50 Habit Cycles Milestone", com.l1khith.calender28.utils.Constants.REWARD_MILESTONE_50_CYCLES),
    MILESTONE_100_CYCLES("100 Habit Cycles Milestone", com.l1khith.calender28.utils.Constants.REWARD_MILESTONE_100_CYCLES),
    PREMIUM_PURCHASE("Premium Unlock", -com.l1khith.calender28.utils.Constants.PREMIUM_UNLOCK_COIN_COST),
    PROMO_CODE("Promo Code Bonus", 0);

    companion object {
        fun fromString(value: String): TransactionReason {
            return entries.firstOrNull { it.name == value } ?: DAILY_LOGIN
        }
    }
}
