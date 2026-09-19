package com.l1khith.calender28.repository

import com.l1khith.calender28.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeoutException

data class SparkyProgressionResult(
    val newState: SparkyState,
    val xpGained: Int,
    val didLevelUp: Boolean,
    val didEvolve: Boolean,
    val previousStage: EvolutionStage,
    val newStage: EvolutionStage
)

interface SparkyRepository {
    val sparkyState: Flow<SparkyState>
    suspend fun getSparky(): SparkyState
    suspend fun onHabitCompleted(): SparkyProgressionResult
    suspend fun onTaskCompleted(): SparkyProgressionResult
    suspend fun onFocusCompleted(durationMinutes: Int): SparkyProgressionResult
    suspend fun onStreakUpdated(streak: Int): SparkyProgressionResult
    suspend fun buyShopItem(item: SparkyShopItem): Result<Unit>
    suspend fun toggleEquip(item: SparkyShopItem): Result<Unit>
    suspend fun renameSparky(name: String): Result<Unit>
    suspend fun claimAchievement(achievementId: String): Result<Int>
    fun getAchievements(state: SparkyState): List<SparkyAchievement>
}

class SparkyRepositoryImpl(
    private val sparkyDao: SparkyDao,
    private val coinRepository: CoinRepository
) : SparkyRepository {

    private val mutex = Mutex()

    override val sparkyState: Flow<SparkyState> = sparkyDao.observeSparky().map { entity ->
        entity?.toSparkyState() ?: SparkyState()
    }.flowOn(Dispatchers.IO)

    private suspend fun getOrCreateEntity(): SparkyEntity {
        val existing = sparkyDao.getSparky()
        if (existing != null) return existing

        val initial = SparkyEntity(
            id = 1,
            name = "Sparky",
            evolutionStage = EvolutionStage.EGG.name,
            xp = 0,
            level = 1,
            personalityCalm = 50,
            personalityEnergetic = 50,
            personalityFriendly = 60,
            personalityCurious = 40,
            personalityResilient = 45,
            personalityWise = 30
        )
        sparkyDao.insertOrUpdateSparky(initial)
        return initial
    }

    override suspend fun getSparky(): SparkyState = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                getOrCreateEntity().toSparkyState()
            }
        }
        result ?: SparkyState()
    }

    override suspend fun onHabitCompleted(): SparkyProgressionResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val current = getOrCreateEntity()
                val newHabits = current.totalHabitsCompleted + 1
                val xpGain = 15
                val newXp = current.xp + xpGain

                val prevLevel = current.level
                val newLevel = (newXp / 100) + 1
                val didLevelUp = newLevel > prevLevel

                val prevStage = EvolutionStage.valueOf(current.evolutionStage)
                val calculatedStage = EvolutionStage.fromHabits(newHabits)
                val isPro = com.l1khith.calender28.billing.RevenueCatManager.isPremium.value || com.l1khith.calender28.billing.SubscriptionManager.isProActive.value
                val newStage = if (!isPro && calculatedStage.ordinal > EvolutionStage.BABY.ordinal) {
                    EvolutionStage.BABY
                } else {
                    calculatedStage
                }
                val didEvolve = newStage != prevStage

                val updated = current.copy(
                    totalHabitsCompleted = newHabits,
                    xp = newXp,
                    level = newLevel,
                    evolutionStage = newStage.name,
                    personalityEnergetic = (current.personalityEnergetic + 1).coerceAtMost(100),
                    personalityWise = (current.personalityWise + 1).coerceAtMost(100)
                )
                sparkyDao.insertOrUpdateSparky(updated)

                // Award level-up bonus coins
                if (didLevelUp) {
                    coinRepository.addCustomCoins(25, "SPARKY_LEVEL_UP", "Sparky reached Level $newLevel!")
                }
                if (didEvolve) {
                    coinRepository.addCustomCoins(100, "SPARKY_EVOLUTION", "Sparky evolved to ${newStage.displayName}!")
                }

                SparkyProgressionResult(
                    newState = updated.toSparkyState(),
                    xpGained = xpGain,
                    didLevelUp = didLevelUp,
                    didEvolve = didEvolve,
                    previousStage = prevStage,
                    newStage = newStage
                )
            }
        } ?: throw TimeoutException("Habit completion timed out")
        result
    }

    override suspend fun onTaskCompleted(): SparkyProgressionResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val current = getOrCreateEntity()
                val newTasks = current.totalTasksCompleted + 1
                val xpGain = 10
                val newXp = current.xp + xpGain

                val prevLevel = current.level
                val newLevel = (newXp / 100) + 1
                val didLevelUp = newLevel > prevLevel

                val stage = EvolutionStage.valueOf(current.evolutionStage)

                val updated = current.copy(
                    totalTasksCompleted = newTasks,
                    xp = newXp,
                    level = newLevel,
                    personalityCurious = (current.personalityCurious + 1).coerceAtMost(100),
                    personalityResilient = (current.personalityResilient + 1).coerceAtMost(100)
                )
                sparkyDao.insertOrUpdateSparky(updated)

                if (didLevelUp) {
                    coinRepository.addCustomCoins(25, "SPARKY_LEVEL_UP", "Sparky reached Level $newLevel!")
                }

                SparkyProgressionResult(
                    newState = updated.toSparkyState(),
                    xpGained = xpGain,
                    didLevelUp = didLevelUp,
                    didEvolve = false,
                    previousStage = stage,
                    newStage = stage
                )
            }
        } ?: throw TimeoutException("Task completion timed out")
        result
    }

    override suspend fun onFocusCompleted(durationMinutes: Int): SparkyProgressionResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val current = getOrCreateEntity()
                val newSessions = current.totalFocusSessions + 1
                val xpGain = (durationMinutes / 5).coerceAtLeast(1) * 5
                val newXp = current.xp + xpGain

                val prevLevel = current.level
                val newLevel = (newXp / 100) + 1
                val didLevelUp = newLevel > prevLevel

                val stage = EvolutionStage.valueOf(current.evolutionStage)

                val updated = current.copy(
                    totalFocusSessions = newSessions,
                    xp = newXp,
                    level = newLevel,
                    personalityCalm = (current.personalityCalm + 2).coerceAtMost(100)
                )
                sparkyDao.insertOrUpdateSparky(updated)

                if (didLevelUp) {
                    coinRepository.addCustomCoins(25, "SPARKY_LEVEL_UP", "Sparky reached Level $newLevel!")
                }

                SparkyProgressionResult(
                    newState = updated.toSparkyState(),
                    xpGained = xpGain,
                    didLevelUp = didLevelUp,
                    didEvolve = false,
                    previousStage = stage,
                    newStage = stage
                )
            }
        } ?: throw TimeoutException("Focus completion timed out")
        result
    }

    override suspend fun onStreakUpdated(streak: Int): SparkyProgressionResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val current = getOrCreateEntity()
                val record = current.streakRecord.coerceAtLeast(streak)
                val xpGain = 5
                val newXp = current.xp + xpGain

                val prevLevel = current.level
                val newLevel = (newXp / 100) + 1
                val didLevelUp = newLevel > prevLevel

                val stage = EvolutionStage.valueOf(current.evolutionStage)

                val updated = current.copy(
                    streakRecord = record,
                    xp = newXp,
                    level = newLevel,
                    personalityFriendly = (current.personalityFriendly + 1).coerceAtMost(100),
                    personalityResilient = (current.personalityResilient + 1).coerceAtMost(100)
                )
                sparkyDao.insertOrUpdateSparky(updated)

                if (didLevelUp) {
                    coinRepository.addCustomCoins(25, "SPARKY_LEVEL_UP", "Sparky reached Level $newLevel!")
                }

                SparkyProgressionResult(
                    newState = updated.toSparkyState(),
                    xpGained = xpGain,
                    didLevelUp = didLevelUp,
                    didEvolve = false,
                    previousStage = stage,
                    newStage = stage
                )
            }
        } ?: throw TimeoutException("Streak update timed out")
        result
    }

    override suspend fun buyShopItem(item: SparkyShopItem): Result<Unit> = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val current = getOrCreateEntity()
                val state = current.toSparkyState()

                val isAlreadyOwned = when (item.category) {
                    SparkyShopCategory.HATS -> state.unlockedHats.contains(item.id)
                    SparkyShopCategory.SKINS -> state.unlockedSkins.contains(item.id)
                    SparkyShopCategory.BOOSTERS -> state.unlockedBoosters.contains(item.id)
                }
                if (isAlreadyOwned) {
                    return@withLock Result.failure(IllegalStateException("Item '${item.name}' is already owned."))
                }

                val balance = coinRepository.getBalance()
                if (balance < item.price) {
                    return@withLock Result.failure(IllegalStateException("Insufficient CalCoins. You need ${item.price} coins."))
                }

                // Deduct coins
                coinRepository.addCustomCoins(-item.price, "SPARKY_SHOP_BUY", "Purchased ${item.name} for Sparky")

                // Add to inventory and equip
                val updated = when (item.category) {
                    SparkyShopCategory.HATS -> {
                        val newHats = (state.unlockedHats + item.id).joinToString(",")
                        current.copy(unlockedHats = newHats, equippedHat = item.id)
                    }
                    SparkyShopCategory.SKINS -> {
                        val newSkins = (state.unlockedSkins + item.id).joinToString(",")
                        current.copy(unlockedSkins = newSkins, equippedSkin = item.id)
                    }
                    SparkyShopCategory.BOOSTERS -> {
                        val newBoosters = (state.unlockedBoosters + item.id).joinToString(",")
                        current.copy(unlockedBoosters = newBoosters)
                    }
                }
                sparkyDao.insertOrUpdateSparky(updated)
                Result.success(Unit)
            }
        }
        result ?: Result.failure(TimeoutException("Shop purchase timed out"))
    }

    override suspend fun toggleEquip(item: SparkyShopItem): Result<Unit> = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val current = getOrCreateEntity()
                val state = current.toSparkyState()

                val isOwned = when (item.category) {
                    SparkyShopCategory.HATS -> state.unlockedHats.contains(item.id)
                    SparkyShopCategory.SKINS -> state.unlockedSkins.contains(item.id)
                    SparkyShopCategory.BOOSTERS -> return@withLock Result.failure(IllegalArgumentException("Boosters cannot be equipped."))
                }
                if (!isOwned) {
                    return@withLock Result.failure(IllegalStateException("You don't own '${item.name}' yet."))
                }

                val updated = when (item.category) {
                    SparkyShopCategory.HATS -> {
                        val newEquip = if (current.equippedHat == item.id) null else item.id
                        current.copy(equippedHat = newEquip)
                    }
                    SparkyShopCategory.SKINS -> {
                        val newEquip = if (current.equippedSkin == item.id) null else item.id
                        current.copy(equippedSkin = newEquip)
                    }
                    else -> current
                }
                sparkyDao.insertOrUpdateSparky(updated)
                Result.success(Unit)
            }
        }
        result ?: Result.failure(TimeoutException("Equip item timed out"))
    }

    override suspend fun renameSparky(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        val clean = name.trim()
        if (clean.isBlank()) return@withContext Result.failure(IllegalArgumentException("Name cannot be empty."))
        if (clean.length > 20) return@withContext Result.failure(IllegalArgumentException("Name too long (max 20 characters)."))
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val current = getOrCreateEntity()
                sparkyDao.insertOrUpdateSparky(current.copy(name = clean))
                Result.success(Unit)
            }
        }
        result ?: Result.failure(TimeoutException("Rename timed out"))
    }

    override suspend fun claimAchievement(achievementId: String): Result<Int> = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(5000L) {
            mutex.withLock {
                val current = getOrCreateEntity()
                val state = current.toSparkyState()

                if (state.claimedAchievements.contains(achievementId)) {
                    return@withLock Result.failure(IllegalStateException("Achievement already claimed."))
                }

                val achievements = getAchievements(state)
                val target = achievements.find { it.id == achievementId }
                    ?: return@withLock Result.failure(IllegalArgumentException("Unknown achievement."))

                if (!target.isUnlocked) {
                    return@withLock Result.failure(IllegalStateException("Achievement requirement not yet reached."))
                }

                // Award reward coins
                coinRepository.addCustomCoins(target.rewardCoins, "SPARKY_ACHIEVEMENT", "Achievement: ${target.title}")

                val newClaimed = (state.claimedAchievements + achievementId).joinToString(",")
                sparkyDao.insertOrUpdateSparky(current.copy(claimedAchievements = newClaimed))

                Result.success(target.rewardCoins)
            }
        }
        result ?: Result.failure(TimeoutException("Claim achievement timed out"))
    }

    override fun getAchievements(state: SparkyState): List<SparkyAchievement> {
        val claimed = state.claimedAchievements
        return listOf(
            SparkyAchievement(
                id = "ach_first_habit",
                title = "Baby Steps",
                description = "Complete your very first habit with Sparky.",
                rewardCoins = 10,
                isUnlocked = state.totalHabitsCompleted >= 1,
                progress = (state.totalHabitsCompleted / 1f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_egg_hatch",
                title = "Egg Hatch!",
                description = "Complete 5 habits to hatch Sparky into a Baby!",
                rewardCoins = 25,
                isUnlocked = state.totalHabitsCompleted >= 5,
                progress = (state.totalHabitsCompleted / 5f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_teen_evolve",
                title = "Feathered Rebel",
                description = "Complete 25 habits to evolve Sparky to Teen stage.",
                rewardCoins = 50,
                isUnlocked = state.totalHabitsCompleted >= 25,
                progress = (state.totalHabitsCompleted / 25f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_adult_evolve",
                title = "Majestic Flight",
                description = "Complete 100 habits to reach Adult stage.",
                rewardCoins = 100,
                isUnlocked = state.totalHabitsCompleted >= 100,
                progress = (state.totalHabitsCompleted / 100f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_legend_evolve",
                title = "Living Legend",
                description = "Complete 500 habits to reach the mythical Legend stage.",
                rewardCoins = 500,
                isUnlocked = state.totalHabitsCompleted >= 500,
                progress = (state.totalHabitsCompleted / 500f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_streak_3",
                title = "Spark Ignition",
                description = "Achieve a 3-day overall streak.",
                rewardCoins = 15,
                isUnlocked = state.streakRecord >= 3,
                progress = (state.streakRecord / 3f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_streak_7",
                title = "Week of Fire",
                description = "Maintain a full 7-day streak.",
                rewardCoins = 30,
                isUnlocked = state.streakRecord >= 7,
                progress = (state.streakRecord / 7f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_streak_28",
                title = "Full Matrix Cycle",
                description = "Complete an entire 28-day consecutive streak!",
                rewardCoins = 150,
                isUnlocked = state.streakRecord >= 28,
                progress = (state.streakRecord / 28f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_focus_1",
                title = "Mindful Moment",
                description = "Complete 1 focus session alongside Sparky.",
                rewardCoins = 15,
                isUnlocked = state.totalFocusSessions >= 1,
                progress = (state.totalFocusSessions / 1f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_focus_10",
                title = "Zen Master",
                description = "Complete 10 focused work sessions.",
                rewardCoins = 50,
                isUnlocked = state.totalFocusSessions >= 10,
                progress = (state.totalFocusSessions / 10f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_tasks_10",
                title = "Task Enthusiast",
                description = "Check off 10 calendar tasks.",
                rewardCoins = 20,
                isUnlocked = state.totalTasksCompleted >= 10,
                progress = (state.totalTasksCompleted / 10f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_tasks_50",
                title = "Productivity Titan",
                description = "Conquer 50 tasks across your calendar.",
                rewardCoins = 75,
                isUnlocked = state.totalTasksCompleted >= 50,
                progress = (state.totalTasksCompleted / 50f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_level_5",
                title = "Growing Stronger",
                description = "Reach Level 5 with Sparky.",
                rewardCoins = 30,
                isUnlocked = state.level >= 5,
                progress = (state.level / 5f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_level_10",
                title = "Seasoned Companion",
                description = "Reach Level 10 with Sparky.",
                rewardCoins = 60,
                isUnlocked = state.level >= 10,
                progress = (state.level / 10f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_shop_first",
                title = "Dressed to Impress",
                description = "Buy your very first item in Sparky Shop.",
                rewardCoins = 25,
                isUnlocked = (state.unlockedHats.isNotEmpty() || state.unlockedSkins.isNotEmpty()),
                progress = if (state.unlockedHats.isNotEmpty() || state.unlockedSkins.isNotEmpty()) 1f else 0f
            ),
            SparkyAchievement(
                id = "ach_shop_3_hats",
                title = "Hat Collector",
                description = "Unlock 3 different hats for Sparky.",
                rewardCoins = 50,
                isUnlocked = state.unlockedHats.size >= 3,
                progress = (state.unlockedHats.size / 3f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_calm_75",
                title = "Inner Sanctuary",
                description = "Grow Sparky's Calm personality trait to 75 or higher.",
                rewardCoins = 40,
                isUnlocked = (state.traits[PersonalityTrait.CALM] ?: 0) >= 75,
                progress = ((state.traits[PersonalityTrait.CALM] ?: 0) / 75f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_energy_75",
                title = "Electric Surge",
                description = "Grow Sparky's Energetic personality trait to 75 or higher.",
                rewardCoins = 40,
                isUnlocked = (state.traits[PersonalityTrait.ENERGETIC] ?: 0) >= 75,
                progress = ((state.traits[PersonalityTrait.ENERGETIC] ?: 0) / 75f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_friendly_75",
                title = "Heartwarming Bond",
                description = "Grow Sparky's Friendly personality trait to 75 or higher.",
                rewardCoins = 40,
                isUnlocked = (state.traits[PersonalityTrait.FRIENDLY] ?: 0) >= 75,
                progress = ((state.traits[PersonalityTrait.FRIENDLY] ?: 0) / 75f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_wise_75",
                title = "Sage Bird",
                description = "Grow Sparky's Wise personality trait to 75 or higher.",
                rewardCoins = 50,
                isUnlocked = (state.traits[PersonalityTrait.WISE] ?: 0) >= 75,
                progress = ((state.traits[PersonalityTrait.WISE] ?: 0) / 75f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_skin_rare",
                title = "Aura Shift",
                description = "Equip a custom cosmetic skin on Sparky.",
                rewardCoins = 35,
                isUnlocked = state.equippedSkin != null,
                progress = if (state.equippedSkin != null) 1f else 0f
            ),
            SparkyAchievement(
                id = "ach_focus_30",
                title = "Deep Focus Master",
                description = "Complete 30 focus sessions.",
                rewardCoins = 120,
                isUnlocked = state.totalFocusSessions >= 30,
                progress = (state.totalFocusSessions / 30f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_tasks_100",
                title = "Century of Action",
                description = "Complete 100 tasks on your calendar.",
                rewardCoins = 150,
                isUnlocked = state.totalTasksCompleted >= 100,
                progress = (state.totalTasksCompleted / 100f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_level_25",
                title = "Master Companion",
                description = "Ascend Sparky all the way to Level 25!",
                rewardCoins = 200,
                isUnlocked = state.level >= 25,
                progress = (state.level / 25f).coerceIn(0f, 1f)
            ),
            SparkyAchievement(
                id = "ach_matrix_bond",
                title = "Eternal Matrix Bond",
                description = "Hatch Sparky, unlock a skin, and complete a full 28-day cycle.",
                rewardCoins = 300,
                isUnlocked = state.stage != EvolutionStage.EGG && state.unlockedSkins.isNotEmpty() && state.streakRecord >= 28,
                progress = if (state.stage != EvolutionStage.EGG && state.unlockedSkins.isNotEmpty() && state.streakRecord >= 28) 1f else 0.5f
            )
        )
    }
}
