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

    // Idempotency queries
    override suspend fun countHabitDayReward(noteKey: String): Int {
        return transactions.count { it.reason == "PARTIAL_HABIT_PROGRESS" && it.note == noteKey }
    }

    override suspend fun countHabitCycleReward(noteKey: String): Int {
        return transactions.count { it.reason == "HABIT_CYCLE_COMPLETE" && it.note == noteKey }
    }

    override suspend fun countTaskCompletionReward(noteKey: String): Int {
        return transactions.count { (it.reason == "DAILY_TASK" || it.reason == "TASK_COMPLETE") && it.note == noteKey }
    }

    override suspend fun countFocusSessionReward(noteKey: String): Int {
        return transactions.count { it.reason == "FOCUS_SESSION" && it.note == noteKey }
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
        val result = repository.rewardHabitCycleComplete("habit1", 0L, "Morning Workout")
        assertNotNull(result)
        assertEquals(10, result?.coinsAwarded)
        assertEquals(10, repository.getBalance())

        // Complete 9 more cycles to hit 10 cycles milestone (+100 bonus)
        for (i in 2..10) {
            repository.rewardHabitCycleComplete("habit$i", i.toLong(), "Habit $i")
        }

        // 10 * 10 (base) + 100 (milestone) = 200
        assertEquals(200, repository.getBalance())
    }

    @Test
    fun `test recurring task completion awards 2 coins`() = runBlocking {
        val result = repository.rewardTaskCompletion(
            taskId = "task1",
            dateStr = "2026-08-28",
            isRecurring = true,
            taskTitle = "Daily Standup"
        )
        assertNotNull(result)
        assertEquals(2, result?.coinsAwarded)
        assertEquals(2, repository.getBalance())
    }

    @Test
    fun `test one-off task completion awards 1 coin`() = runBlocking {
        val result = repository.rewardTaskCompletion(
            taskId = "task1",
            dateStr = "2026-08-28",
            isRecurring = false,
            taskTitle = "Buy groceries"
        )
        assertNotNull(result)
        assertEquals(1, result?.coinsAwarded)
        assertEquals(1, repository.getBalance())
    }

    @Test
    fun `test habit day toggle exploit prevented - no double reward`() = runBlocking {
        val result1 = repository.rewardPartialHabitProgress("habit1", 0L, 5, "Morning Workout")
        assertNotNull(result1)
        assertEquals(1, result1?.coinsAwarded)
        assertEquals(1, repository.getBalance())

        // Toggle off and on again - same habit, cycle, day
        val result2 = repository.rewardPartialHabitProgress("habit1", 0L, 5, "Morning Workout")
        assertNull(result2)
        assertEquals(1, repository.getBalance()) // Still 1, not 2

        // Different day in same cycle - allowed
        val result3 = repository.rewardPartialHabitProgress("habit1", 0L, 6, "Morning Workout")
        assertNotNull(result3)
        assertEquals(2, repository.getBalance())
    }

    @Test
    fun `test habit cycle completion exploit prevented - no double reward`() = runBlocking {
        val result1 = repository.rewardHabitCycleComplete("habit1", 0L, "Morning Workout")
        assertNotNull(result1)
        assertEquals(10, result1?.coinsAwarded)
        assertEquals(10, repository.getBalance())

        // Re-completing same cycle - blocked
        val result2 = repository.rewardHabitCycleComplete("habit1", 0L, "Morning Workout")
        assertNull(result2)
        assertEquals(10, repository.getBalance()) // Still 10, not 20

        // Different cycle - allowed
        val result3 = repository.rewardHabitCycleComplete("habit1", 1L, "Morning Workout")
        assertNotNull(result3)
        assertEquals(20, repository.getBalance())
    }

    @Test
    fun `test task toggle exploit prevented - no double reward`() = runBlocking {
        val result1 = repository.rewardTaskCompletion(
            taskId = "task1",
            dateStr = "2026-08-28",
            isRecurring = true,
            taskTitle = "Daily Standup"
        )
        assertNotNull(result1)
        assertEquals(2, result1?.coinsAwarded)

        // Toggle off and on again - same task, same date
        val result2 = repository.rewardTaskCompletion(
            taskId = "task1",
            dateStr = "2026-08-28",
            isRecurring = true,
            taskTitle = "Daily Standup"
        )
        assertNull(result2)
        assertEquals(2, repository.getBalance()) // Still 2, not 4

        // Same task, different date - allowed
        val result3 = repository.rewardTaskCompletion(
            taskId = "task1",
            dateStr = "2026-08-29",
            isRecurring = true,
            taskTitle = "Daily Standup"
        )
        assertNotNull(result3)
        assertEquals(4, repository.getBalance())
    }

    @Test
    fun `test focus session rewards 5 coins`() = runBlocking {
        val result = repository.rewardFocusSessionComplete("Study Session", 25)
        assertNotNull(result)
        assertEquals(5, result?.coinsAwarded)
        assertEquals(5, repository.getBalance())
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
