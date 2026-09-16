package com.l1khith.calender28

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.domain.model.BetStatus
import com.l1khith.calender28.domain.model.BetUnavailableReason
import com.l1khith.calender28.domain.model.ConfidenceTier
import com.l1khith.calender28.domain.usecase.bet.EvaluateBetUseCase
import com.l1khith.calender28.domain.usecase.bet.GetBetStatusUseCase
import com.l1khith.calender28.domain.usecase.bet.PlaceBetUseCase
import com.l1khith.calender28.repository.CoinRepositoryImpl
import com.l1khith.calender28.repository.UserPreferencesRepository
import com.l1khith.calender28.test.FakeTaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Calendar

class ConfidenceContractTest {

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var coinRepository: CoinRepositoryImpl
    private lateinit var fakeCoinDao: FakeCoinDao
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private lateinit var placeBetUseCase: PlaceBetUseCase
    private lateinit var evaluateBetUseCase: EvaluateBetUseCase
    private lateinit var getBetStatusUseCase: GetBetStatusUseCase

    private val testDate = "2026-09-15"

    @Before
    fun setUp() {
        taskRepository = FakeTaskRepository()
        fakeCoinDao = FakeCoinDao()
        val tempFile = File.createTempFile("test_prefs_contract", ".preferences_pb")
        tempFile.deleteOnExit()
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + Job()),
            produceFile = { tempFile }
        )
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        coinRepository = CoinRepositoryImpl(coinDao = fakeCoinDao, userPrefsRepo = userPreferencesRepository)

        placeBetUseCase = PlaceBetUseCase(taskRepository, userPreferencesRepository)
        evaluateBetUseCase = EvaluateBetUseCase(taskRepository, coinRepository, userPreferencesRepository)
        getBetStatusUseCase = GetBetStatusUseCase(taskRepository, userPreferencesRepository)
    }

    private fun createTask(id: String, completed: Boolean = false): AppTask {
        return AppTask(
            id = id,
            title = "Task $id",
            description = null,
            associatedDate = testDate,
            isCompleted = if (completed) 1 else 0
        )
    }

    @Test
    fun `placeBet fails when fewer than 3 tasks exist`() = runBlocking {
        taskRepository.tasks.add(createTask("1"))
        taskRepository.tasks.add(createTask("2"))

        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9) }
        val result = placeBetUseCase(testDate, calendarOverride = calendar)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("at least 3 tasks") == true)
    }

    @Test
    fun `placeBet fails when any task is already completed`() = runBlocking {
        taskRepository.tasks.add(createTask("1", completed = true))
        taskRepository.tasks.add(createTask("2"))
        taskRepository.tasks.add(createTask("3"))

        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9) }
        val result = placeBetUseCase(testDate, calendarOverride = calendar)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already completed") == true)
    }

    @Test
    fun `placeBet fails after 12 00 PM`() = runBlocking {
        taskRepository.tasks.add(createTask("1"))
        taskRepository.tasks.add(createTask("2"))
        taskRepository.tasks.add(createTask("3"))

        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 12) }
        val result = placeBetUseCase(testDate, calendarOverride = calendar)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("12:00 PM") == true)
    }

    @Test
    fun `placeBet fails on recovery day after 3 consecutive losses`() = runBlocking {
        userPreferencesRepository.updateBetLossStreak(3)
        userPreferencesRepository.updateBetLastPlayedDate("2026-09-14")

        taskRepository.tasks.add(createTask("1"))
        taskRepository.tasks.add(createTask("2"))
        taskRepository.tasks.add(createTask("3"))

        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9) }
        val result = placeBetUseCase(testDate, calendarOverride = calendar)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Recovery day") == true)
    }

    @Test
    fun `placeBet succeeds before noon with 3 uncompleted tasks and snapshots them`() = runBlocking {
        taskRepository.tasks.add(createTask("t1"))
        taskRepository.tasks.add(createTask("t2"))
        taskRepository.tasks.add(createTask("t3"))

        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 10) }
        val result = placeBetUseCase(testDate, tierOverride = ConfidenceTier.B, calendarOverride = calendar)

        assertTrue(result.isSuccess)
        assertEquals(ConfidenceTier.B, result.getOrNull())

        assertEquals(testDate, userPreferencesRepository.betLastPlayedDate.first())
        assertEquals("B", userPreferencesRepository.betActiveTier.first())
        assertEquals(3, userPreferencesRepository.betSnapshotTaskCount.first())
        assertEquals(setOf("t1", "t2", "t3"), userPreferencesRepository.betSnapshotTaskIds.first())
    }

    @Test
    fun `evaluateBet wins and awards tier coins plus streak milestones`() = runBlocking {
        val t1 = createTask("t1", completed = false)
        val t2 = createTask("t2", completed = false)
        val t3 = createTask("t3", completed = false)
        taskRepository.tasks.addAll(listOf(t1, t2, t3))

        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9) }
        val placeResult = placeBetUseCase(testDate, tierOverride = ConfidenceTier.A, calendarOverride = calendar)
        assertTrue(placeResult.isSuccess)

        // Complete 2 tasks (satisfies Tier A: 50% of 3 = 2 required)
        taskRepository.tasks.clear()
        taskRepository.tasks.addAll(listOf(
            t1.copy(isCompleted = 1),
            t2.copy(isCompleted = 1),
            t3
        ))

        // Pre-set streak to 2, so this win becomes 3 (milestone: +25 bonus)
        userPreferencesRepository.updateBetStreak(2)
        userPreferencesRepository.updateBetLossStreak(1)

        val evalResult = evaluateBetUseCase(testDate)
        assertTrue(evalResult.isSuccess)

        val eval = evalResult.getOrNull()!!
        assertTrue(eval.won)
        assertEquals(3, eval.streak)
        assertEquals(25, eval.bonusCoins)
        // 3 base reward + 25 bonus = 28
        assertEquals(28, eval.coinsAwarded)
        assertEquals(0, userPreferencesRepository.betLossStreak.first())
        assertEquals(3, userPreferencesRepository.betStreak.first())
        // Active bet cleared
        assertNull(userPreferencesRepository.betActiveTier.first())
    }

    @Test
    fun `evaluateBet applies grace loss penalty and increments loss streak`() = runBlocking {
        taskRepository.tasks.add(createTask("t1", completed = false))
        taskRepository.tasks.add(createTask("t2", completed = false))
        taskRepository.tasks.add(createTask("t3", completed = false))

        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9) }
        placeBetUseCase(testDate, tierOverride = ConfidenceTier.C, calendarOverride = calendar)

        userPreferencesRepository.updateBetStreak(4)
        userPreferencesRepository.updateBetLossStreak(1)

        val evalResult = evaluateBetUseCase(testDate)
        assertTrue(evalResult.isSuccess)

        val eval = evalResult.getOrNull()!!
        assertFalse(eval.won)
        // Streak drops by 1 (4 -> 3), not reset to 0!
        assertEquals(3, eval.streak)
        assertEquals(2, userPreferencesRepository.betLossStreak.first())
        assertEquals(-12, eval.coinsAwarded)
        assertEquals(0, coinRepository.getBalance()) // Balance floor at 0
    }

    @Test
    fun `evaluateBet auto-forfeits when greater than 30 percent tasks are deleted`() = runBlocking {
        val t1 = createTask("t1", completed = false)
        val t2 = createTask("t2", completed = false)
        val t3 = createTask("t3", completed = false)
        val t4 = createTask("t4", completed = false)
        taskRepository.tasks.addAll(listOf(t1, t2, t3, t4))

        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9) }
        val placeResult = placeBetUseCase(testDate, tierOverride = ConfidenceTier.A, calendarOverride = calendar)
        assertTrue(placeResult.isSuccess)

        // Delete 2 out of 4 tasks (50% deleted > 30% threshold)
        taskRepository.tasks.remove(t3)
        taskRepository.tasks.remove(t4)

        val evalResult = evaluateBetUseCase(testDate)
        assertTrue(evalResult.isSuccess)

        val eval = evalResult.getOrNull()!!
        assertFalse(eval.won)
        assertTrue(eval.isForfeitDueToDeletion)
    }

    @Test
    fun `getBetStatus returns ReadyToBet and Active appropriately`() = runBlocking {
        taskRepository.tasks.add(createTask("t1"))
        taskRepository.tasks.add(createTask("t2"))
        taskRepository.tasks.add(createTask("t3"))

        val morningCal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9) }
        val status1 = getBetStatusUseCase(testDate, calendarOverride = morningCal)
        assertTrue(status1 is BetStatus.ReadyToBet)

        placeBetUseCase(testDate, tierOverride = ConfidenceTier.A, calendarOverride = morningCal)

        val status2 = getBetStatusUseCase(testDate, calendarOverride = morningCal)
        assertTrue(status2 is BetStatus.Active)
        assertEquals(3, (status2 as BetStatus.Active).snapshotTaskCount)
    }
}
