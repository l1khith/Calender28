package com.l1khith.calender28.repository

import android.content.Context
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.data.CoinBalanceEntity
import com.l1khith.calender28.data.CoinDao
import com.l1khith.calender28.data.CoinTransactionEntity
import com.l1khith.calender28.data.RoomTaskDatabase
import com.l1khith.calender28.data.TransactionReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CoinRepositoryImpl(
    private val context: Context? = null,
    private val coinDao: CoinDao = RoomTaskDatabase.getInstance(context!!).coinDao(),
    private val userPrefsRepo: UserPreferencesRepository? = context?.let { UserPreferencesRepository.getInstance(it) }
) : CoinRepository {

    private val mutex = Mutex()

    override val coinBalance: Flow<Int> = coinDao.getCoinBalanceFlow().map { it?.balance ?: 0 }
    override val recentTransactions: Flow<List<CoinTransactionEntity>> = coinDao.getRecentTransactionsFlow(50)

    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        return sdf.format(Date())
    }

    override suspend fun getBalance(): Int {
        return coinDao.getCoinBalance()?.balance ?: 0
    }

    private suspend fun executeTransaction(
        amount: Int,
        reason: TransactionReason,
        note: String? = null
    ): CoinRewardResult = mutex.withLock {
        val currentBalance = coinDao.getCoinBalance()?.balance ?: 0
        val newBalance = (currentBalance + amount).coerceAtLeast(0)

        coinDao.insertOrUpdateBalance(CoinBalanceEntity(id = 1, balance = newBalance))

        val tx = CoinTransactionEntity(
            id = UUID.randomUUID().toString(),
            amount = amount,
            reason = reason.name,
            timestamp = getCurrentIsoTimestamp(),
            note = note
        )
        coinDao.insertTransaction(tx)

        val message = if (amount >= 0) {
            "🪙 +$amount CalCoins: ${reason.displayName}"
        } else {
            "🪙 $amount CalCoins: ${reason.displayName}"
        }

        CoinRewardResult(
            coinsAwarded = amount,
            reasonName = reason.name,
            newBalance = newBalance,
            message = message
        )
    }

    override suspend fun rewardDailyLogin(datePrefix: String): CoinRewardResult? {
        val alreadyRewarded = coinDao.countDailyLoginRewardForDate(datePrefix) > 0
        if (alreadyRewarded) return null

        return executeTransaction(
            amount = TransactionReason.DAILY_LOGIN.defaultAmount,
            reason = TransactionReason.DAILY_LOGIN,
            note = "Daily login on $datePrefix"
        )
    }

    override suspend fun rewardHabitCycleComplete(habitName: String): CoinRewardResult {
        val baseResult = executeTransaction(
            amount = TransactionReason.HABIT_CYCLE_COMPLETE.defaultAmount,
            reason = TransactionReason.HABIT_CYCLE_COMPLETE,
            note = "Cycle completed for $habitName"
        )

        // Check milestones: 10, 50, 100
        val totalCompletions = coinDao.countHabitCycleCompletions()
        if (totalCompletions >= 100 && coinDao.countMilestoneGiven(TransactionReason.MILESTONE_100_CYCLES.name) == 0) {
            executeTransaction(
                amount = TransactionReason.MILESTONE_100_CYCLES.defaultAmount,
                reason = TransactionReason.MILESTONE_100_CYCLES,
                note = "100 cycles milestone reached!"
            )
        } else if (totalCompletions >= 50 && coinDao.countMilestoneGiven(TransactionReason.MILESTONE_50_CYCLES.name) == 0) {
            executeTransaction(
                amount = TransactionReason.MILESTONE_50_CYCLES.defaultAmount,
                reason = TransactionReason.MILESTONE_50_CYCLES,
                note = "50 cycles milestone reached!"
            )
        } else if (totalCompletions >= 10 && coinDao.countMilestoneGiven(TransactionReason.MILESTONE_10_CYCLES.name) == 0) {
            executeTransaction(
                amount = TransactionReason.MILESTONE_10_CYCLES.defaultAmount,
                reason = TransactionReason.MILESTONE_10_CYCLES,
                note = "10 cycles milestone reached!"
            )
        }

        return baseResult
    }

    override suspend fun rewardPartialHabitProgress(habitName: String): CoinRewardResult {
        return executeTransaction(
            amount = TransactionReason.PARTIAL_HABIT_PROGRESS.defaultAmount,
            reason = TransactionReason.PARTIAL_HABIT_PROGRESS,
            note = "Logged day for $habitName"
        )
    }

    override suspend fun rewardTaskCompletion(
        isRecurring: Boolean,
        streakDays: Int,
        taskTitle: String
    ): CoinRewardResult? {
        if (!isRecurring) return null

        val baseResult = executeTransaction(
            amount = TransactionReason.DAILY_TASK.defaultAmount,
            reason = TransactionReason.DAILY_TASK,
            note = "Completed recurring task: $taskTitle"
        )

        // Check streaks (7, 14, 30 days)
        if (streakDays >= 30) {
            val key = "${taskTitle}_streak_30"
            if (coinDao.countStreakRewardGiven(TransactionReason.STREAK_30_DAY.name, key) == 0) {
                executeTransaction(
                    amount = TransactionReason.STREAK_30_DAY.defaultAmount,
                    reason = TransactionReason.STREAK_30_DAY,
                    note = key
                )
            }
        } else if (streakDays >= 14) {
            val key = "${taskTitle}_streak_14"
            if (coinDao.countStreakRewardGiven(TransactionReason.STREAK_14_DAY.name, key) == 0) {
                executeTransaction(
                    amount = TransactionReason.STREAK_14_DAY.defaultAmount,
                    reason = TransactionReason.STREAK_14_DAY,
                    note = key
                )
            }
        } else if (streakDays >= 7) {
            val key = "${taskTitle}_streak_7"
            if (coinDao.countStreakRewardGiven(TransactionReason.STREAK_7_DAY.name, key) == 0) {
                executeTransaction(
                    amount = TransactionReason.STREAK_7_DAY.defaultAmount,
                    reason = TransactionReason.STREAK_7_DAY,
                    note = key
                )
            }
        }

        return baseResult
    }

    override suspend fun redeemPromoCode(code: String): Result<CoinRewardResult> {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a promo code."))
        }

        val alreadyUsed = coinDao.countPromoCodeUsed(cleanCode) > 0
        if (alreadyUsed) {
            return Result.failure(IllegalStateException("Promo code '$cleanCode' has already been redeemed."))
        }

        val promoReward = when (cleanCode) {
            com.l1khith.calender28.utils.Constants.PROMO_CODE_SHIPATON -> com.l1khith.calender28.utils.Constants.PROMO_REWARD_SHIPATON
            com.l1khith.calender28.utils.Constants.PROMO_CODE_WELCOME -> com.l1khith.calender28.utils.Constants.PROMO_REWARD_WELCOME
            com.l1khith.calender28.utils.Constants.PROMO_CODE_CALENDER28 -> com.l1khith.calender28.utils.Constants.PROMO_REWARD_CALENDER28
            com.l1khith.calender28.utils.Constants.PROMO_CODE_MATRIXPRO -> com.l1khith.calender28.utils.Constants.PROMO_REWARD_MATRIXPRO
            else -> return Result.failure(IllegalArgumentException("Invalid promo code '$cleanCode'."))
        }

        val result = executeTransaction(
            amount = promoReward,
            reason = TransactionReason.PROMO_CODE,
            note = cleanCode
        )
        return Result.success(result)
    }

    override suspend fun buyPremiumWithCoins(): Result<Unit> {
        val currentBalance = getBalance()
        val requiredCoins = com.l1khith.calender28.utils.Constants.PREMIUM_UNLOCK_COIN_COST
        if (currentBalance < requiredCoins) {
            return Result.failure(IllegalStateException("Insufficient CalCoins. You need $requiredCoins CalCoins to unlock Premium."))
        }

        executeTransaction(
            amount = -requiredCoins,
            reason = TransactionReason.PREMIUM_PURCHASE,
            note = "Unlocked Premium with $requiredCoins CalCoins"
        )

        userPrefsRepo?.updateIsProUser(true)
        SubscriptionManager.setProActive(true)

        return Result.success(Unit)
    }

    override suspend fun addCustomCoins(amount: Int, reason: String, note: String?): CoinRewardResult {
        val parsedReason = TransactionReason.fromString(reason)
        return executeTransaction(amount, parsedReason, note)
    }
}
