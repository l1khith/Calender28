package com.l1khith.calender28.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {

    @Query("SELECT * FROM coin_balance WHERE id = 1 LIMIT 1")
    fun getCoinBalanceFlow(): Flow<CoinBalanceEntity?>

    @Query("SELECT * FROM coin_balance WHERE id = 1 LIMIT 1")
    suspend fun getCoinBalance(): CoinBalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBalance(entity: CoinBalanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: CoinTransactionEntity)

    @Query("SELECT * FROM coin_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int = 50): Flow<List<CoinTransactionEntity>>

    @Query("SELECT * FROM coin_transactions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTransactions(limit: Int = 50): List<CoinTransactionEntity>

    @Query("SELECT COUNT(*) FROM coin_transactions WHERE reason = 'DAILY_LOGIN' AND (timestamp LIKE :datePrefix || '%' OR note LIKE '%' || :datePrefix || '%')")
    suspend fun countDailyLoginRewardForDate(datePrefix: String): Int

    @Query("SELECT COUNT(*) FROM coin_transactions WHERE reason = 'PROMO_CODE' AND note = :promoCode")
    suspend fun countPromoCodeUsed(promoCode: String): Int

    @Query("SELECT COUNT(*) FROM coin_transactions WHERE reason = 'HABIT_CYCLE_COMPLETE'")
    suspend fun countHabitCycleCompletions(): Int

    @Query("SELECT COUNT(*) FROM coin_transactions WHERE reason = :reason")
    suspend fun countMilestoneGiven(reason: String): Int

    @Query("SELECT COUNT(*) FROM coin_transactions WHERE reason = :streakReason AND note = :streakKey")
    suspend fun countStreakRewardGiven(streakReason: String, streakKey: String): Int

    // Idempotency: prevent double-rewarding a specific habit day toggle
    @Query("SELECT COUNT(*) FROM coin_transactions WHERE reason = 'PARTIAL_HABIT_PROGRESS' AND note = :noteKey")
    suspend fun countHabitDayReward(noteKey: String): Int

    // Idempotency: prevent double-rewarding a specific habit cycle completion
    @Query("SELECT COUNT(*) FROM coin_transactions WHERE reason = 'HABIT_CYCLE_COMPLETE' AND note = :noteKey")
    suspend fun countHabitCycleReward(noteKey: String): Int

    // Idempotency: prevent double-rewarding a task completion on a specific date
    @Query("SELECT COUNT(*) FROM coin_transactions WHERE (reason = 'DAILY_TASK' OR reason = 'TASK_COMPLETE') AND note = :noteKey")
    suspend fun countTaskCompletionReward(noteKey: String): Int

    // Idempotency: prevent double-rewarding a focus session
    @Query("SELECT COUNT(*) FROM coin_transactions WHERE reason = 'FOCUS_SESSION' AND note = :noteKey")
    suspend fun countFocusSessionReward(noteKey: String): Int
}
