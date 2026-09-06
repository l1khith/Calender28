package com.l1khith.calender28.repository

import com.l1khith.calender28.data.CoinTransactionEntity
import kotlinx.coroutines.flow.Flow

data class CoinRewardResult(
    val coinsAwarded: Int,
    val reasonName: String,
    val newBalance: Int,
    val message: String
)

interface CoinRepository {
    val coinBalance: Flow<Int>
    val recentTransactions: Flow<List<CoinTransactionEntity>>

    suspend fun getBalance(): Int
    suspend fun rewardDailyLogin(datePrefix: String): CoinRewardResult?
    suspend fun rewardHabitCycleComplete(habitId: String, cycleIndex: Long, habitName: String): CoinRewardResult?
    suspend fun rewardPartialHabitProgress(habitId: String, cycleIndex: Long, dayInCycle: Int, habitName: String): CoinRewardResult?
    suspend fun rewardTaskCompletion(taskId: String, dateStr: String, isRecurring: Boolean, taskTitle: String): CoinRewardResult?
    suspend fun rewardFocusSessionComplete(taskTitle: String, durationMinutes: Int): CoinRewardResult?
    suspend fun rewardStreakMilestone(streakCount: Int): CoinRewardResult?
    suspend fun redeemPromoCode(code: String): Result<CoinRewardResult>
    suspend fun buyPremiumWithCoins(): Result<Unit>
    suspend fun addCustomCoins(amount: Int, reason: String, note: String? = null): CoinRewardResult
}
