package com.wordland.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wordland.domain.model.achievement.Achievement
import com.wordland.domain.model.achievement.AchievementCategory
import com.wordland.domain.model.achievement.AchievementRequirement
import com.wordland.domain.model.achievement.AchievementReward
import com.wordland.domain.model.achievement.AchievementTier
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room entity for achievement definitions
 * Stored in database and seeded from AchievementSeeder
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    // Display info
    val name: String,
    val description: String,
    val icon: String,
    // Classification
    val category: String, // AchievementCategory.name
    val tier: String, // AchievementTier.name
    // Requirement (stored as JSON)
    val requirementType: String, // AchievementRequirement subclass name
    val requirementData: String, // JSON: specific requirement fields
    // Reward (stored as JSON)
    val rewardType: String, // AchievementReward subclass name
    val rewardData: String, // JSON: specific reward fields
    // Flags
    val isHidden: Boolean = false,
    val parentId: String? = null,
)

/**
 * Achievement requirement serializable data for JSON storage
 */
@Serializable
data class RequirementData(
    val type: String,
    val count: Int? = null,
    val minStars: Int? = null,
    val memoryStrengthThreshold: Int? = null,
    val comboCount: Int? = null,
    val days: Int? = null,
    val minWordsPerDay: Int? = null,
    val wordCount: Int? = null,
    val strength: Int? = null,
    val islandId: String? = null,
    val levelCount: Int? = null,
)

/**
 * Achievement reward serializable data for JSON storage
 */
@Serializable
data class RewardData(
    val type: String,
    val amount: Int? = null, // For Stars
    val titleId: String? = null, // For Title
    val displayName: String? = null, // For Title, Badge
    val badgeId: String? = null, // For Badge
    val iconName: String? = null, // For Badge
    val petId: String? = null, // For PetUnlock
    val petName: String? = null, // For PetUnlock
    val petIcon: String? = null, // For PetUnlock
    val rewards: List<String>? = null, // For Multiple (JSON array strings)
)

/**
 * Convert AchievementEntity to domain model
 */
fun AchievementEntity.toDomainModel(): Achievement {
    val requirement = parseRequirement(requirementType, requirementData)
    val reward = parseReward(rewardType, rewardData)

    return Achievement(
        id = id,
        name = name,
        description = description,
        icon = icon,
        category = AchievementCategory.valueOf(category),
        tier = AchievementTier.valueOf(tier),
        requirement = requirement,
        reward = reward,
        isHidden = isHidden,
        parentId = parentId,
    )
}

/**
 * Parse requirement from JSON
 */
fun parseRequirement(
    type: String,
    data: String,
): AchievementRequirement {
    return when (type) {
        "CompleteLevels" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RequirementData>(data)
            AchievementRequirement.CompleteLevels(
                count = parsed.count ?: 1,
                minStars = parsed.minStars ?: 1,
            )
        }
        "MasterWords" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RequirementData>(data)
            AchievementRequirement.MasterWords(
                count = parsed.count ?: 1,
                memoryStrengthThreshold = parsed.memoryStrengthThreshold ?: 80,
            )
        }
        "ComboMilestone" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RequirementData>(data)
            AchievementRequirement.ComboMilestone(
                comboCount = parsed.comboCount ?: 5,
            )
        }
        "StreakDays" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RequirementData>(data)
            AchievementRequirement.StreakDays(
                days = parsed.days ?: 7,
                minWordsPerDay = parsed.minWordsPerDay ?: 1,
            )
        }
        "PerfectLevel" -> {
            AchievementRequirement.PerfectLevel(count = 1)
        }
        "NoHintsLevel" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RequirementData>(data)
            AchievementRequirement.NoHintsLevel(
                wordCount = parsed.wordCount ?: 6,
            )
        }
        "MaxMemoryStrength" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RequirementData>(data)
            AchievementRequirement.MaxMemoryStrength(
                count = parsed.count ?: 10,
                strength = parsed.strength ?: 100,
            )
        }
        "CompleteIsland" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RequirementData>(data)
            AchievementRequirement.CompleteIsland(
                islandId = parsed.islandId ?: "",
                levelCount = parsed.levelCount ?: 5,
            )
        }
        "UnlockIslands" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RequirementData>(data)
            AchievementRequirement.UnlockIslands(
                count = parsed.count ?: 3,
            )
        }
        else -> throw IllegalArgumentException("Unknown requirement type: $type")
    }
}

/**
 * Parse reward from JSON
 */
fun parseReward(
    type: String,
    data: String,
): AchievementReward {
    return when (type) {
        "Stars" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RewardData>(data)
            AchievementReward.Stars(amount = parsed.amount ?: 0)
        }
        "Title" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RewardData>(data)
            AchievementReward.Title(
                titleId = parsed.titleId ?: "",
                displayName = parsed.displayName ?: "",
            )
        }
        "Badge" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RewardData>(data)
            AchievementReward.Badge(
                badgeId = parsed.badgeId ?: "",
                iconName = parsed.iconName ?: "",
                displayName = parsed.displayName ?: "",
            )
        }
        "PetUnlock" -> {
            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString<RewardData>(data)
            AchievementReward.PetUnlock(
                petId = parsed.petId ?: "",
                petName = parsed.petName ?: "",
                petIcon = parsed.petIcon ?: "",
            )
        }
        "Multiple" -> {
            // For multiple rewards, data contains JSON array of individual reward JSONs
            AchievementReward.Multiple(
                rewards = emptyList(), // Parse recursively in seeder
            )
        }
        else -> throw IllegalArgumentException("Unknown reward type: $type")
    }
}

/**
 * Convert domain Achievement to entity
 */
fun Achievement.toEntity(): AchievementEntity {
    val (requirementType, requirementData) = serializeRequirement(requirement)
    val (rewardType, rewardData) = serializeReward(reward)

    return AchievementEntity(
        id = id,
        name = name,
        description = description,
        icon = icon,
        category = category.name,
        tier = tier.name,
        requirementType = requirementType,
        requirementData = requirementData,
        rewardType = rewardType,
        rewardData = rewardData,
        isHidden = isHidden,
        parentId = parentId,
    )
}

/**
 * Serialize requirement to JSON
 */
fun serializeRequirement(requirement: AchievementRequirement): Pair<String, String> {
    val json = Json { encodeDefaults = true }
    val data =
        when (requirement) {
            is AchievementRequirement.CompleteLevels ->
                RequirementData(
                    type = "CompleteLevels",
                    count = requirement.count,
                    minStars = requirement.minStars,
                )
            is AchievementRequirement.MasterWords ->
                RequirementData(
                    type = "MasterWords",
                    count = requirement.count,
                    memoryStrengthThreshold = requirement.memoryStrengthThreshold,
                )
            is AchievementRequirement.ComboMilestone ->
                RequirementData(
                    type = "ComboMilestone",
                    comboCount = requirement.comboCount,
                )
            is AchievementRequirement.StreakDays ->
                RequirementData(
                    type = "StreakDays",
                    days = requirement.days,
                    minWordsPerDay = requirement.minWordsPerDay,
                )
            is AchievementRequirement.PerfectLevel ->
                RequirementData(
                    type = "PerfectLevel",
                    count = requirement.count,
                )
            is AchievementRequirement.NoHintsLevel ->
                RequirementData(
                    type = "NoHintsLevel",
                    wordCount = requirement.wordCount,
                )
            is AchievementRequirement.MaxMemoryStrength ->
                RequirementData(
                    type = "MaxMemoryStrength",
                    count = requirement.count,
                    strength = requirement.strength,
                )
            is AchievementRequirement.CompleteIsland ->
                RequirementData(
                    type = "CompleteIsland",
                    islandId = requirement.islandId,
                    levelCount = requirement.levelCount,
                )
            is AchievementRequirement.UnlockIslands ->
                RequirementData(
                    type = "UnlockIslands",
                    count = requirement.count,
                )
        }
    return (requirement::class.simpleName ?: "Unknown") to json.encodeToString(data)
}

/**
 * Serialize reward to JSON
 */
fun serializeReward(reward: AchievementReward): Pair<String, String> {
    val json = Json { encodeDefaults = true }
    val data =
        when (reward) {
            is AchievementReward.Stars ->
                RewardData(
                    type = "Stars",
                    amount = reward.amount,
                )
            is AchievementReward.Title ->
                RewardData(
                    type = "Title",
                    titleId = reward.titleId,
                    displayName = reward.displayName,
                )
            is AchievementReward.Badge ->
                RewardData(
                    type = "Badge",
                    badgeId = reward.badgeId,
                    iconName = reward.iconName,
                    displayName = reward.displayName,
                )
            is AchievementReward.PetUnlock ->
                RewardData(
                    type = "PetUnlock",
                    petId = reward.petId,
                    petName = reward.petName,
                    petIcon = reward.petIcon,
                )
            is AchievementReward.Multiple ->
                RewardData(
                    type = "Multiple",
                    rewards = reward.rewards.map { serializeReward(it).second },
                )
        }
    return (reward::class.simpleName ?: "Unknown") to json.encodeToString(data)
}
