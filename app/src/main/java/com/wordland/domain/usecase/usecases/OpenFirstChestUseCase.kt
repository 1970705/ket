package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.ChestReward
import com.wordland.domain.model.PetType
import com.wordland.domain.model.RewardRarity
import com.wordland.domain.repository.OnboardingRepository
import kotlin.random.Random

/**
 * 开启首个宝箱 UseCase
 *
 * 负责生成随机奖励并标记 Onboarding 完成
 *
 * @property repository OnboardingRepository
 */
class OpenFirstChestUseCase(
    private val repository: OnboardingRepository,
) {
    /**
     * 开启宝箱并获取奖励
     *
     * @return ChestReward 生成的奖励
     * @throws IllegalStateException 如果 Onboarding 未启动或未选择宠物
     */
    suspend operator fun invoke(): ChestReward {
        val state =
            repository.getOnboardingState()
                ?: throw IllegalStateException("Onboarding not started")

        val selectedPet =
            state.selectedPet
                ?: throw IllegalStateException("No pet selected")

        // Alpha: 100% 掉落奖励
        val reward = generateReward(selectedPet)

        // 不要立即更新 phase 为 COMPLETED
        // phase 将在用户点击"太酷了！"按钮后才更新为 COMPLETED
        // 这里只记录宝箱开启时间
        val updated =
            state.copy(
                // 保持 currentPhase 为 FIRST_CHEST
                lastOpenedChest = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        repository.saveOnboardingState(updated)

        return reward
    }

    /**
     * 生成随机奖励
     *
     * @param pet 选择的宠物类型
     * @return ChestReward 生成的奖励
     */
    private fun generateReward(pet: PetType): ChestReward {
        // Alpha 简化版概率分配
        val chance = Random.nextInt(100)

        return when {
            // 50% 普通宠物表情
            chance < 50 ->
                ChestReward.PetEmoji(
                    petType = pet,
                    emoji = getRandomEmoji(RewardRarity.COMMON),
                    description = "你获得了一个可爱的表情！",
                )
            // 30% 庆祝特效
            chance < 80 ->
                ChestReward.CelebrationEffect(
                    effectName = getRandomEffect(),
                    description = "庆祝特效解锁！",
                )
            // 20% 稀有宠物造型
            else ->
                ChestReward.RarePetStyle(
                    petType = pet,
                    styleName = getRareStyleName(),
                    emoji = getRandomEmoji(RewardRarity.EPIC),
                    description = "稀有造型解锁！",
                )
        }
    }

    /**
     * 获取随机 Emoji
     */
    private fun getRandomEmoji(rarity: RewardRarity): String {
        val commonEmojis = listOf("😊", "🎉", "⭐", "👏", "💫")
        val rareEmojis = listOf("✨", "🌟", "💖", "🎊", "🔮")
        val epicEmojis = listOf("👑", "🦸", "🎨", "🌈", "💎")

        return when (rarity) {
            RewardRarity.COMMON -> commonEmojis.random()
            RewardRarity.RARE -> rareEmojis.random()
            RewardRarity.EPIC -> epicEmojis.random()
        }
    }

    /**
     * 获取随机特效名称
     */
    private fun getRandomEffect(): String {
        return listOf("彩带", "烟花", "星星雨", "光效", "波纹").random()
    }

    /**
     * 获取稀有造型名称
     */
    private fun getRareStyleName(): String {
        return listOf("超级英雄", "太空探险", "皇家骑士", "魔法师", "摇滚明星").random()
    }
}
