package com.wordland.domain.model

/**
 * 宝箱奖励模型（Alpha 简化版）
 *
 * @property T 奖励类型
 */
sealed class ChestReward {
    abstract val rarity: RewardRarity
    abstract val name: String
    abstract val description: String

    /**
     * 宠物表情奖励
     */
    data class PetEmoji(
        val petType: PetType,
        val emoji: String,
        override val description: String,
    ) : ChestReward() {
        override val rarity = RewardRarity.COMMON
        override val name = "宠物表情"
    }

    /**
     * 庆祝特效奖励
     */
    data class CelebrationEffect(
        val effectName: String,
        override val description: String,
    ) : ChestReward() {
        override val rarity = RewardRarity.RARE
        override val name = "庆祝特效"
    }

    /**
     * 稀有宠物造型奖励
     */
    data class RarePetStyle(
        val petType: PetType,
        val styleName: String,
        val emoji: String,
        override val description: String,
    ) : ChestReward() {
        override val rarity = RewardRarity.EPIC
        override val name = "稀有造型"
    }
}

/**
 * 奖励稀有度枚举
 */
enum class RewardRarity {
    COMMON, // 普通（50%）
    RARE, // 稀有（30%）
    EPIC, // 史诗（20%）
}

/**
 * 获取稀有度显示颜色名称
 */
fun RewardRarity.getColorName(): String =
    when (this) {
        RewardRarity.COMMON -> "蓝色"
        RewardRarity.RARE -> "紫色"
        RewardRarity.EPIC -> "橙色"
    }

/**
 * 获取稀有度 Emoji
 */
fun RewardRarity.getEmoji(): String =
    when (this) {
        RewardRarity.COMMON -> "\uD83D\uDD35" // 🔵
        RewardRarity.RARE -> "\uD83D\uDD36" // 🟣
        RewardRarity.EPIC -> "\uD83D\uDFB2" // 🎲
    }
