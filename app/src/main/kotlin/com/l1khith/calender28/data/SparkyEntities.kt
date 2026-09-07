package com.l1khith.calender28.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.runtime.Immutable

/**
 * The 5 life stages of Sparky based on completed habits.
 */
enum class EvolutionStage(
    val displayName: String,
    val minHabits: Int,
    val maxHabits: Int
) {
    EGG("Egg", 0, 4),
    BABY("Baby", 5, 24),
    TEEN("Teen", 25, 99),
    ADULT("Adult", 100, 499),
    LEGEND("Legend", 500, Int.MAX_VALUE);

    fun nextStage(): EvolutionStage? = when (this) {
        EGG -> BABY
        BABY -> TEEN
        TEEN -> ADULT
        ADULT -> LEGEND
        LEGEND -> null
    }

    companion object {
        fun fromHabits(habits: Int): EvolutionStage = when {
            habits >= 500 -> LEGEND
            habits >= 100 -> ADULT
            habits >= 25 -> TEEN
            habits >= 5 -> BABY
            else -> EGG
        }
    }
}

/**
 * Real-time mood of Sparky, driving animations and conversational dialogue.
 */
enum class SparkyMood(val label: String) {
    IDLE("Resting"),
    HAPPY("Happy"),
    CELEBRATING("Celebrating!"),
    PROUD("Proud"),
    CALM("Calm & Meditating"),
    ENERGETIC("Full of Energy"),
    TIRED("Sleepy"),
    CURIOUS("Curious"),
    SAD("A Bit Down"),
    EVOLVING("Evolving!")
}

/**
 * 6 personality traits that evolve based on user activity.
 */
enum class PersonalityTrait(val displayName: String, val subtitle: String) {
    CALM("Calm", "Inner peace via focus sessions"),
    ENERGETIC("Energetic", "Driven by daily momentum"),
    FRIENDLY("Friendly", "Warm daily companion"),
    CURIOUS("Curious", "Always exploring new tasks"),
    RESILIENT("Resilient", "Bounces back after tough days"),
    WISE("Wise", "Builds long-term consistency")
}

/**
 * Room Database entity storing persistent state for Sparky (single-row table).
 */
@Entity(tableName = "sparky")
data class SparkyEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Sparky",
    val evolutionStage: String = EvolutionStage.EGG.name,
    val xp: Int = 0,
    val level: Int = 1,
    val personalityCalm: Int = 50,
    val personalityEnergetic: Int = 50,
    val personalityFriendly: Int = 60,
    val personalityCurious: Int = 40,
    val personalityResilient: Int = 45,
    val personalityWise: Int = 30,
    val unlockedHats: String = "",
    val equippedHat: String? = null,
    val unlockedSkins: String = "",
    val equippedSkin: String? = null,
    val unlockedBoosters: String = "",
    val totalHabitsCompleted: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalFocusSessions: Int = 0,
    val streakRecord: Int = 0,
    val lastPersonalityShift: Long = 0L,
    val claimedAchievements: String = ""
)

/**
 * Immutable UI State representation of Sparky.
 */
@Immutable
data class SparkyState(
    val name: String = "Sparky",
    val stage: EvolutionStage = EvolutionStage.EGG,
    val xp: Int = 0,
    val level: Int = 1,
    val currentLevelXp: Int = 0,
    val requiredLevelXp: Int = 100,
    val progressToNextLevel: Float = 0f,
    val traits: Map<PersonalityTrait, Int> = emptyMap(),
    val dominantTraits: List<PersonalityTrait> = emptyList(),
    val unlockedHats: Set<String> = emptySet(),
    val equippedHat: String? = null,
    val unlockedSkins: Set<String> = emptySet(),
    val equippedSkin: String? = null,
    val unlockedBoosters: Set<String> = emptySet(),
    val totalHabitsCompleted: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalFocusSessions: Int = 0,
    val streakRecord: Int = 0,
    val claimedAchievements: Set<String> = emptySet()
)

fun SparkyEntity.toSparkyState(): SparkyState {
    val currentStage = try {
        EvolutionStage.valueOf(evolutionStage)
    } catch (_: Exception) {
        EvolutionStage.EGG
    }
    val xpInLevel = xp % 100
    val derivedLevel = (xp / 100) + 1

    val traitMap = mapOf(
        PersonalityTrait.CALM to personalityCalm.coerceIn(0, 100),
        PersonalityTrait.ENERGETIC to personalityEnergetic.coerceIn(0, 100),
        PersonalityTrait.FRIENDLY to personalityFriendly.coerceIn(0, 100),
        PersonalityTrait.CURIOUS to personalityCurious.coerceIn(0, 100),
        PersonalityTrait.RESILIENT to personalityResilient.coerceIn(0, 100),
        PersonalityTrait.WISE to personalityWise.coerceIn(0, 100)
    )

    val topTraits = traitMap.entries
        .sortedByDescending { it.value }
        .take(2)
        .map { it.key }

    return SparkyState(
        name = name,
        stage = currentStage,
        xp = xp,
        level = derivedLevel,
        currentLevelXp = xpInLevel,
        requiredLevelXp = 100,
        progressToNextLevel = (xpInLevel.toFloat() / 100f).coerceIn(0f, 1f),
        traits = traitMap,
        dominantTraits = topTraits,
        unlockedHats = if (unlockedHats.isBlank()) emptySet() else unlockedHats.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
        equippedHat = equippedHat,
        unlockedSkins = if (unlockedSkins.isBlank()) emptySet() else unlockedSkins.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
        equippedSkin = equippedSkin,
        unlockedBoosters = if (unlockedBoosters.isBlank()) emptySet() else unlockedBoosters.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
        totalHabitsCompleted = totalHabitsCompleted,
        totalTasksCompleted = totalTasksCompleted,
        totalFocusSessions = totalFocusSessions,
        streakRecord = streakRecord,
        claimedAchievements = if (claimedAchievements.isBlank()) emptySet() else claimedAchievements.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    )
}

/**
 * Categories of items available in the Sparky Shop.
 */
enum class SparkyShopCategory(val displayName: String) {
    HATS("Hats"),
    SKINS("Skins"),
    BOOSTERS("Boosters")
}

/**
 * Cosmetic or functional item for Sparky.
 */
@Immutable
data class SparkyShopItem(
    val id: String,
    val name: String,
    val category: SparkyShopCategory,
    val price: Int,
    val description: String,
    val accentColor: Long
)

object SparkyCatalog {
    val items = listOf(
        // Hats
        SparkyShopItem("top_hat", "Top Hat", SparkyShopCategory.HATS, 50, "Classy gentleman styling for Sparky.", 0xFF1E293B),
        SparkyShopItem("crown", "Royal Crown", SparkyShopCategory.HATS, 100, "Golden crown fit for habit royalty.", 0xFFF59E0B),
        SparkyShopItem("cap", "Athletic Cap", SparkyShopCategory.HATS, 75, "Sporty visor for active daily streaks.", 0xFF3B82F6),
        SparkyShopItem("wizard_hat", "Wizard Hat", SparkyShopCategory.HATS, 120, "Mystical pointed hat radiating wisdom.", 0xFF8B5CF6),
        SparkyShopItem("jester", "Jester Cap", SparkyShopCategory.HATS, 150, "Bouncy, playful cap with joyful bells.", 0xFFEC4899),

        // Skins
        SparkyShopItem("cosmic", "Cosmic Glow", SparkyShopCategory.SKINS, 300, "Deep space nebula aura with starfield shimmer.", 0xFF6366F1),
        SparkyShopItem("fire", "Fire Blaze", SparkyShopCategory.SKINS, 250, "Blazing fire trails inspired by your active streak.", 0xFFEF4444),
        SparkyShopItem("dragon", "Jade Dragon", SparkyShopCategory.SKINS, 500, "Mythical green dragon wings & scale sheen.", 0xFF10B981),
        SparkyShopItem("cyber", "Matrix Cyber", SparkyShopCategory.SKINS, 400, "Glowing green phosphor matrix circuitry.", 0xFF14B8A6),

        // Boosters
        SparkyShopItem("xp_booster", "2x XP Doubler", SparkyShopCategory.BOOSTERS, 100, "Doubles Sparky XP gain from all habits for 24h.", 0xFFEAB308),
        SparkyShopItem("focus_shield", "Focus Shield", SparkyShopCategory.BOOSTERS, 150, "Bonus +5 CalCoins on the next 3 completed focus sessions.", 0xFF06B6D4),
        SparkyShopItem("streak_freeze", "Streak Charm", SparkyShopCategory.BOOSTERS, 200, "Protects your overall streak from resetting for one missed day.", 0xFF84CC16)
    )
}

/**
 * In-game achievement for Sparky milestones.
 */
@Immutable
data class SparkyAchievement(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val isUnlocked: Boolean,
    val progress: Float
)
