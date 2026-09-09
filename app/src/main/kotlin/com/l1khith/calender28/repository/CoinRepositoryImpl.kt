package com.l1khith.calender28.repository

import android.content.Context
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.data.CoinBalanceEntity
import com.l1khith.calender28.data.CoinDao
import com.l1khith.calender28.data.CoinTransactionEntity
import com.l1khith.calender28.data.RoomTaskDatabase
import com.l1khith.calender28.data.TransactionReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeoutException

class CoinRepositoryImpl(
    private val coinDao: CoinDao,
    private val userPrefsRepo: UserPreferencesRepository? = null
) : CoinRepository {

    constructor(context: Context) : this(
        coinDao = RoomTaskDatabase.getInstance(context.applicationContext).coinDao(),
        userPrefsRepo = UserPreferencesRepository.getInstance(context.applicationContext)
    )

    private val mutex = Mutex()

    override val coinBalance: Flow<Int> = coinDao.getCoinBalanceFlow().map { it?.balance ?: 0 }.flowOn(Dispatchers.IO)
    override val recentTransactions: Flow<List<CoinTransactionEntity>> = coinDao.getRecentTransactionsFlow(50).flowOn(Dispatchers.IO)

    private fun getCurrentIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        return sdf.format(Date())
    }

    override suspend fun getBalance(): Int = withContext(Dispatchers.IO) {
        coinDao.getCoinBalance()?.balance ?: 0
    }

    private suspend fun executeTransaction(
        amount: Int,
        reason: TransactionReason,
        note: String? = null
    ): CoinRewardResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
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
                    "+$amount CalCoins: ${reason.displayName}"
                } else {
                    "$amount CalCoins: ${reason.displayName}"
                }

                CoinRewardResult(
                    coinsAwarded = amount,
                    reasonName = reason.name,
                    newBalance = newBalance,
                    message = message
                )
            }
        } ?: throw TimeoutException("Coin transaction timed out")
        result
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

    override suspend fun rewardHabitCycleComplete(habitId: String, cycleIndex: Long, habitName: String): CoinRewardResult? {
        val noteKey = "cycle:$habitId:$cycleIndex"
        if (coinDao.countHabitCycleReward(noteKey) > 0) return null

        val baseResult = executeTransaction(
            amount = TransactionReason.HABIT_CYCLE_COMPLETE.defaultAmount,
            reason = TransactionReason.HABIT_CYCLE_COMPLETE,
            note = noteKey
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

    override suspend fun rewardPartialHabitProgress(habitId: String, cycleIndex: Long, dayInCycle: Int, habitName: String): CoinRewardResult? {
        val noteKey = "habit_day:$habitId:$cycleIndex:$dayInCycle"
        if (coinDao.countHabitDayReward(noteKey) > 0) return null

        return executeTransaction(
            amount = TransactionReason.PARTIAL_HABIT_PROGRESS.defaultAmount,
            reason = TransactionReason.PARTIAL_HABIT_PROGRESS,
            note = noteKey
        )
    }

    override suspend fun rewardTaskCompletion(
        taskId: String,
        dateStr: String,
        isRecurring: Boolean,
        taskTitle: String
    ): CoinRewardResult? {
        val noteKey = "task_done:$taskId:$dateStr"
        if (coinDao.countTaskCompletionReward(noteKey) > 0) return null

        val reason = if (isRecurring) TransactionReason.DAILY_TASK else TransactionReason.TASK_COMPLETE
        return executeTransaction(
            amount = reason.defaultAmount,
            reason = reason,
            note = noteKey
        )
    }

    override suspend fun rewardStreakMilestone(streakCount: Int): CoinRewardResult? {
        if (streakCount >= 1000) {
            val noteKey = "streak_milestone_1000"
            if (coinDao.countStreakRewardGiven(TransactionReason.STREAK_1000_DAY.name, noteKey) == 0) {
                return executeTransaction(
                    amount = TransactionReason.STREAK_1000_DAY.defaultAmount,
                    reason = TransactionReason.STREAK_1000_DAY,
                    note = noteKey
                )
            }
        }
        return null
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

    override suspend fun buyPremiumWithCoins(): Result<Unit> = withContext(Dispatchers.IO) {
        if (SubscriptionManager.isProActive.value) {
            return@withContext Result.failure(IllegalStateException("Pro is already active on this device."))
        }

        val requiredCoins = com.l1khith.calender28.utils.Constants.PREMIUM_UNLOCK_COIN_COST

        // Check balance AND deduct inside the same mutex lock with timeout to prevent race conditions or deadlocks
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val currentBalance = coinDao.getCoinBalance()?.balance ?: 0
                if (currentBalance < requiredCoins) {
                    return@withLock Result.failure(IllegalStateException("Insufficient CalCoins. You need $requiredCoins CalCoins to unlock Premium."))
                }

                val newBalance = (currentBalance - requiredCoins).coerceAtLeast(0)
                coinDao.insertOrUpdateBalance(CoinBalanceEntity(id = 1, balance = newBalance))

                val tx = CoinTransactionEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    amount = -requiredCoins,
                    reason = TransactionReason.PREMIUM_PURCHASE.name,
                    timestamp = getCurrentIsoTimestamp(),
                    note = "Unlocked Premium with $requiredCoins CalCoins"
                )
                coinDao.insertTransaction(tx)

                userPrefsRepo?.updateIsProUser(true)
                SubscriptionManager.setProActive(true)

                Result.success(Unit)
            }
        }
        result ?: Result.failure(TimeoutException("Purchase transaction timed out"))
    }

    override suspend fun rewardFocusSessionComplete(taskTitle: String, durationMinutes: Int): CoinRewardResult? {
        val noteKey = "focus:${taskTitle}:${getCurrentIsoTimestamp().take(10)}"
        if (coinDao.countFocusSessionReward(noteKey) > 0) return null

        return executeTransaction(
            amount = TransactionReason.FOCUS_SESSION.defaultAmount,
            reason = TransactionReason.FOCUS_SESSION,
            note = noteKey
        )
    }

    override suspend fun addCustomCoins(amount: Int, reason: String, note: String?): CoinRewardResult {
        val parsedReason = TransactionReason.fromString(reason)
        return executeTransaction(amount, parsedReason, note)
    }
}
