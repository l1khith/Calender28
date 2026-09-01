package com.l1khith.calender28

import com.l1khith.calender28.data.CoinBalanceEntity
import com.l1khith.calender28.data.CoinDao
import com.l1khith.calender28.data.CoinTransactionEntity
import com.l1khith.calender28.data.TransactionReason
import com.l1khith.calender28.repository.CoinRepositoryImpl
import com.l1khith.calender28.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class FakeCoinDao : CoinDao {
    private var balanceEntity = CoinBalanceEntity(1, 0)
    private val transactions = mutableListOf<CoinTransactionEntity>()
    private val balanceFlow = MutableStateFlow<CoinBalanceEntity?>(balanceEntity)

    override fun getCoinBalanceFlow(): Flow<CoinBalanceEntity?> = balanceFlow

    override suspend fun getCoinBalance(): CoinBalanceEntity = balanceEntity

    override suspend fun insertOrUpdateBalance(entity: CoinBalanceEntity) {
        balanceEntity = entity
        balanceFlow.value = entity
    }

    override suspend fun insertTransaction(tx: CoinTransactionEntity) {
        transactions.add(0, tx)
    }

    override fun getRecentTransactionsFlow(limit: Int): Flow<List<CoinTransactionEntity>> {
        return flowOf(transactions.take(limit))
    }

    override suspend fun getRecentTransactions(limit: Int): List<CoinTransactionEntity> {
        return transactions.take(limit)
    }

    override suspend fun countDailyLoginRewardForDate(datePrefix: String): Int {
        return transactions.count { it.reason == "DAILY_LOGIN" && (it.timestamp.startsWith(datePrefix) || it.note?.contains(datePrefix) == true) }
    }

    override suspend fun countPromoCodeUsed(promoCode: String): Int {
        return transactions.count { it.reason == "PROMO_CODE" && it.note == promoCode }
    }

    override suspend fun countHabitCycleCompletions(): Int {
        return transactions.count { it.reason == "HABIT_CYCLE_COMPLETE" }
    }

    override suspend fun countMilestoneGiven(reason: String): Int {
        return transactions.count { it.reason == reason }
    }

    override suspend fun countStreakRewardGiven(streakReason: String, streakKey: String): Int {
        return transactions.count { it.reason == streakReason && it.note == streakKey }
    }
}

class CoinSystemTest {

    private lateinit var fakeDao: FakeCoinDao
    private lateinit var repository: CoinRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeCoinDao()
        repository = CoinRepositoryImpl(coinDao = fakeDao, userPrefsRepo = null)
    }

    @Test
    fun `test initial balance is zero`() = runBlocking {
        assertEquals(0, repository.getBalance())
    }

    @Test
    fun `test daily login reward adds 1 coin on first launch and ignores second launch same day`() = runBlocking {
        val today = "2026-08-28"
        val result1 = repository.rewardDailyLogin(today)
        assertNotNull(result1)
        assertEquals(1, result1?.coinsAwarded)
        assertEquals(1, repository.getBalance())

        // Second launch same day
        val result2 = repository.rewardDailyLogin(today)
        assertNull(result2)
        assertEquals(1, repository.getBalance())

        // Next day launch
        val nextDay = "2026-08-29"
        val result3 = repository.rewardDailyLogin(nextDay)
        assertNotNull(result3)
        assertEquals(1, result3?.coinsAwarded)
        assertEquals(2, repository.getBalance())
    }

    @Test
    fun `test habit cycle completion awards 10 coins and milestone bonuses`() = runBlocking {
        val result = repository.rewardHabitCycleComplete("Morning Workout")
        assertEquals(10, result.coinsAwarded)
        assertEquals(10, repository.getBalance())

        // Complete 9 more cycles to hit 10 cycles milestone (+100 bonus)
        for (i in 2..10) {
            repository.rewardHabitCycleComplete("Habit $i")
        }

        // 10 * 10 (base) + 100 (milestone) = 200
        assertEquals(200, repository.getBalance())
    }

    @Test
    fun `test recurring task completion and 7-day streak bonus`() = runBlocking {
        val result = repository.rewardTaskCompletion(isRecurring = true, streakDays = 6, taskTitle = "Daily Standup")
        assertNotNull(result)
        assertEquals(2, result?.coinsAwarded)
        assertEquals(2, repository.getBalance())

        // Day 7 streak bonus (+50)
        val resultDay7 = repository.rewardTaskCompletion(isRecurring = true, streakDays = 7, taskTitle = "Daily Standup")
        assertNotNull(resultDay7)
        // 2 + 2 (base) + 50 (bonus) = 54
        assertEquals(54, repository.getBalance())
    }

    @Test
    fun `test promo code redemption and duplicate rejection`() = runBlocking {
        val result = repository.redeemPromoCode("SHIPATON2026")
        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.coinsAwarded)
        assertEquals(100, repository.getBalance())

        // Duplicate code redemption fails
        val duplicateResult = repository.redeemPromoCode("SHIPATON2026")
        assertTrue(duplicateResult.isFailure)
        assertEquals(100, repository.getBalance())

        // Invalid code fails
        val invalidResult = repository.redeemPromoCode("INVALID_XYZ")
        assertTrue(invalidResult.isFailure)
        assertEquals(100, repository.getBalance())
    }

    @Test
    fun `test premium purchase requires 1500 coins and deducts accurately`() = runBlocking {
        // Balance = 0 -> purchase fails
        val failResult = repository.buyPremiumWithCoins()
        assertTrue(failResult.isFailure)

        // Add 1500 coins via promo MATRIXPRO
        val promoResult = repository.redeemPromoCode("MATRIXPRO")
        assertTrue(promoResult.isSuccess)
        assertEquals(1500, repository.getBalance())

        // Now purchase succeeds
        val successResult = repository.buyPremiumWithCoins()
        assertTrue(successResult.isSuccess)
        assertEquals(0, repository.getBalance())
    }
}
