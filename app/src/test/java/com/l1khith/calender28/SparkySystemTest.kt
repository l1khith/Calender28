package com.l1khith.calender28

import com.l1khith.calender28.data.*
import com.l1khith.calender28.repository.CoinRepository
import com.l1khith.calender28.repository.SparkyRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class FakeSparkyDao : SparkyDao {
    private var entity: SparkyEntity? = null
    private val flow = MutableStateFlow<SparkyEntity?>(null)

    override fun observeSparky(): Flow<SparkyEntity?> = flow

    override suspend fun getSparky(): SparkyEntity? = entity

    override fun getSparkySync(): SparkyEntity? = entity

    override suspend fun insertOrUpdateSparky(sparky: SparkyEntity): Long {
        entity = sparky
        flow.value = sparky
        return 1L
    }

    override suspend fun renameSparky(newName: String): Int {
        entity = entity?.copy(name = newName)
        flow.value = entity
        return 1
    }
}

class SparkySystemTest {

    private lateinit var fakeDao: FakeSparkyDao
    private lateinit var mockCoinRepo: CoinRepository
    private lateinit var sparkyRepo: SparkyRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeSparkyDao()
        mockCoinRepo = mock()
        sparkyRepo = SparkyRepositoryImpl(fakeDao, mockCoinRepo)
    }

    @Test
    fun testInitialSparkyState() {
        runBlocking {
            val sparky = sparkyRepo.getSparky()
            assertEquals("Sparky", sparky.name)
            assertEquals(EvolutionStage.EGG, sparky.stage)
            assertEquals(1, sparky.level)
            assertEquals(0, sparky.xp)
            assertEquals(0, sparky.totalHabitsCompleted)
            assertEquals(0, sparky.totalTasksCompleted)
            assertEquals(0, sparky.currentLevelXp)
            assertEquals(100, sparky.requiredLevelXp)
        }
    }

    @Test
    fun testHabitCompletionProgressionAndEvolution() {
        runBlocking {
            // First 4 habits should keep Sparky in EGG stage
            repeat(4) {
                val result = sparkyRepo.onHabitCompleted()
                assertEquals(EvolutionStage.EGG, result.newStage)
                assertFalse(result.didEvolve)
            }

            val stateAfter4 = sparkyRepo.getSparky()
            assertEquals(4, stateAfter4.totalHabitsCompleted)
            assertEquals(60, stateAfter4.xp)
            assertEquals(1, stateAfter4.level)

            // 5th habit should hatch the egg -> BABY stage!
            val result5 = sparkyRepo.onHabitCompleted()
            assertEquals(EvolutionStage.BABY, result5.newStage)
            assertEquals(EvolutionStage.EGG, result5.previousStage)
            assertTrue(result5.didEvolve)
            assertEquals(5, result5.newState.totalHabitsCompleted)
            assertEquals(75, result5.newState.xp)

            // Verify evolution bonus coins were awarded (100 coins)
            verify(mockCoinRepo).addCustomCoins(eq(100), eq("SPARKY_EVOLUTION"), any())
        }
    }

    @Test
    fun testLevelUpCalculation() {
        runBlocking {
            // Complete 10 tasks (10 * 10 XP = 100 XP) -> Level 2
            var lastResult = sparkyRepo.onTaskCompleted()
            repeat(9) {
                lastResult = sparkyRepo.onTaskCompleted()
            }

            assertEquals(100, lastResult.newState.xp)
            assertEquals(2, lastResult.newState.level)
            assertTrue(lastResult.didLevelUp)

            // Verify level up bonus coins were awarded (25 coins)
            verify(mockCoinRepo).addCustomCoins(eq(25), eq("SPARKY_LEVEL_UP"), any())
        }
    }

    @Test
    fun testEvolutionThresholds() {
        assertEquals(EvolutionStage.EGG, EvolutionStage.fromHabits(0))
        assertEquals(EvolutionStage.EGG, EvolutionStage.fromHabits(4))
        assertEquals(EvolutionStage.BABY, EvolutionStage.fromHabits(5))
        assertEquals(EvolutionStage.BABY, EvolutionStage.fromHabits(24))
        assertEquals(EvolutionStage.TEEN, EvolutionStage.fromHabits(25))
        assertEquals(EvolutionStage.TEEN, EvolutionStage.fromHabits(99))
        assertEquals(EvolutionStage.ADULT, EvolutionStage.fromHabits(100))
        assertEquals(EvolutionStage.ADULT, EvolutionStage.fromHabits(499))
        assertEquals(EvolutionStage.LEGEND, EvolutionStage.fromHabits(500))
        assertEquals(EvolutionStage.LEGEND, EvolutionStage.fromHabits(1000))
    }

    @Test
    fun testShopItemPurchaseAndEquip() {
        runBlocking {
            val crownItem = SparkyCatalog.items.first { it.id == "crown" }

            // 1. Fail purchase due to insufficient balance
            whenever(mockCoinRepo.getBalance()).thenReturn(50) // Crown costs 100
            val failResult = sparkyRepo.buyShopItem(crownItem)
            assertTrue(failResult.isFailure)

            // 2. Succeed purchase with sufficient balance
            whenever(mockCoinRepo.getBalance()).thenReturn(150)
            val successResult = sparkyRepo.buyShopItem(crownItem)
            assertTrue(successResult.isSuccess)

            val stateAfterBuy = sparkyRepo.getSparky()
            assertTrue(stateAfterBuy.unlockedHats.contains("crown"))
            // Auto-equipped upon purchase
            assertEquals("crown", stateAfterBuy.equippedHat)

            // 3. Unequip the crown
            val unequipResult = sparkyRepo.toggleEquip(crownItem)
            assertTrue(unequipResult.isSuccess)
            assertNull(sparkyRepo.getSparky().equippedHat)

            // 4. Re-equip the crown
            val reEquipResult = sparkyRepo.toggleEquip(crownItem)
            assertTrue(reEquipResult.isSuccess)
            assertEquals("crown", sparkyRepo.getSparky().equippedHat)
        }
    }

    @Test
    fun testRenameSparky() {
        runBlocking {
            val result = sparkyRepo.renameSparky("Phoenix")
            assertTrue(result.isSuccess)

            val sparky = sparkyRepo.getSparky()
            assertEquals("Phoenix", sparky.name)
        }
    }

    @Test
    fun testAchievementsUnlockAndClaim() {
        runBlocking {
            // Complete 1 habit
            sparkyRepo.onHabitCompleted()
            val state = sparkyRepo.getSparky()

            val achievements = sparkyRepo.getAchievements(state)
            val firstHabitAchievement = achievements.find { it.id == "ach_first_habit" }
            assertNotNull(firstHabitAchievement)
            assertTrue(firstHabitAchievement!!.isUnlocked)

            // Claim achievement
            val claimResult = sparkyRepo.claimAchievement("ach_first_habit")
            assertTrue(claimResult.isSuccess)
            assertEquals(firstHabitAchievement.rewardCoins, claimResult.getOrNull())

            // Verify coins awarded
            verify(mockCoinRepo).addCustomCoins(eq(firstHabitAchievement.rewardCoins), eq("SPARKY_ACHIEVEMENT"), any())

            // Trying to claim again should fail
            val duplicateClaim = sparkyRepo.claimAchievement("ach_first_habit")
            assertTrue(duplicateClaim.isFailure)
        }
    }
}
