package com.wordland.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ChestReward model and RewardRarity enum
 * Tests reward types, rarities, and extension functions
 */
class ChestRewardTest {
    // === ChestReward Type Tests ===

    @Test
    fun `PetEmoji has correct default properties`() {
        val reward =
            ChestReward.PetEmoji(
                petType = PetType.DOLPHIN,
                emoji = "🎉",
                description = "Test reward",
            )

        assertEquals(PetType.DOLPHIN, reward.petType)
        assertEquals("🎉", reward.emoji)
        assertEquals("Test reward", reward.description)
    }

    @Test
    fun `PetEmoji has COMMON rarity`() {
        val reward =
            ChestReward.PetEmoji(
                petType = PetType.CAT,
                emoji = "⭐",
                description = "Test",
            )

        assertEquals(RewardRarity.COMMON, reward.rarity)
    }

    @Test
    fun `PetEmoji has correct name`() {
        val reward =
            ChestReward.PetEmoji(
                petType = PetType.DOG,
                emoji = "👏",
                description = "Test",
            )

        assertEquals("宠物表情", reward.name)
    }

    @Test
    fun `CelebrationEffect has correct default properties`() {
        val reward =
            ChestReward.CelebrationEffect(
                effectName = "彩带",
                description = "庆祝特效解锁！",
            )

        assertEquals("彩带", reward.effectName)
        assertEquals("庆祝特效解锁！", reward.description)
    }

    @Test
    fun `CelebrationEffect has RARE rarity`() {
        val reward =
            ChestReward.CelebrationEffect(
                effectName = "烟花",
                description = "Test",
            )

        assertEquals(RewardRarity.RARE, reward.rarity)
    }

    @Test
    fun `CelebrationEffect has correct name`() {
        val reward =
            ChestReward.CelebrationEffect(
                effectName = "星星雨",
                description = "Test",
            )

        assertEquals("庆祝特效", reward.name)
    }

    @Test
    fun `RarePetStyle has correct default properties`() {
        val reward =
            ChestReward.RarePetStyle(
                petType = PetType.FOX,
                styleName = "超级英雄",
                emoji = "✨",
                description = "稀有造型解锁！",
            )

        assertEquals(PetType.FOX, reward.petType)
        assertEquals("超级英雄", reward.styleName)
        assertEquals("✨", reward.emoji)
        assertEquals("稀有造型解锁！", reward.description)
    }

    @Test
    fun `RarePetStyle has EPIC rarity`() {
        val reward =
            ChestReward.RarePetStyle(
                petType = PetType.DOLPHIN,
                styleName = "太空探险",
                emoji = "🌟",
                description = "Test",
            )

        assertEquals(RewardRarity.EPIC, reward.rarity)
    }

    @Test
    fun `RarePetStyle has correct name`() {
        val reward =
            ChestReward.RarePetStyle(
                petType = PetType.CAT,
                styleName = "皇家骑士",
                emoji = "👑",
                description = "Test",
            )

        assertEquals("稀有造型", reward.name)
    }

    // === RewardRarity Enum Tests ===

    @Test
    fun `RewardRarity has exactly 3 values`() {
        assertEquals(3, RewardRarity.entries.size)
    }

    @Test
    fun `RewardRarity contains all required values`() {
        val entries = RewardRarity.entries

        assertTrue(RewardRarity.COMMON in entries)
        assertTrue(RewardRarity.RARE in entries)
        assertTrue(RewardRarity.EPIC in entries)
    }

    @Test
    fun `RewardRarity enum constants are ordered`() {
        val entries = RewardRarity.entries

        assertEquals(RewardRarity.COMMON, entries[0])
        assertEquals(RewardRarity.RARE, entries[1])
        assertEquals(RewardRarity.EPIC, entries[2])
    }

    // === RewardRarity Extension Function Tests ===

    @Test
    fun `getColorName returns 蓝色 for COMMON rarity`() {
        assertEquals("蓝色", RewardRarity.COMMON.getColorName())
    }

    @Test
    fun `getColorName returns 紫色 for RARE rarity`() {
        assertEquals("紫色", RewardRarity.RARE.getColorName())
    }

    @Test
    fun `getColorName returns 橙色 for EPIC rarity`() {
        assertEquals("橙色", RewardRarity.EPIC.getColorName())
    }

    @Test
    fun `getEmoji returns correct emoji for COMMON rarity`() {
        assertEquals("\uD83D\uDD35", RewardRarity.COMMON.getEmoji()) // 🔵
    }

    @Test
    fun `getEmoji returns correct emoji for RARE rarity`() {
        assertEquals("\uD83D\uDD36", RewardRarity.RARE.getEmoji()) // 🟣
    }

    @Test
    fun `getEmoji returns correct emoji for EPIC rarity`() {
        assertEquals("\uD83D\uDFB2", RewardRarity.EPIC.getEmoji()) // 🎲
    }

    // === Polymorphism Tests ===

    @Test
    fun `ChestReward PetEmoji is instance of ChestReward`() {
        val reward: ChestReward =
            ChestReward.PetEmoji(
                petType = PetType.DOLPHIN,
                emoji = "😊",
                description = "Test",
            )

        assertTrue(reward is ChestReward)
    }

    @Test
    fun `ChestReward CelebrationEffect is instance of ChestReward`() {
        val reward: ChestReward =
            ChestReward.CelebrationEffect(
                effectName = "Test",
                description = "Test",
            )

        assertTrue(reward is ChestReward)
    }

    @Test
    fun `ChestReward RarePetStyle is instance of ChestReward`() {
        val reward: ChestReward =
            ChestReward.RarePetStyle(
                petType = PetType.CAT,
                styleName = "Test",
                emoji = "✨",
                description = "Test",
            )

        assertTrue(reward is ChestReward)
    }

    @Test
    fun `ChestReward when expression can distinguish types`() {
        val petEmoji: ChestReward =
            ChestReward.PetEmoji(
                petType = PetType.DOG,
                emoji = "🎉",
                description = "Test",
            )

        val name =
            when (petEmoji) {
                is ChestReward.PetEmoji -> "宠物表情"
                is ChestReward.CelebrationEffect -> "庆祝特效"
                is ChestReward.RarePetStyle -> "稀有造型"
            }

        assertEquals("宠物表情", name)
    }

    // === All Pet Types with Rewards ===

    @Test
    fun `PetEmoji can be created with all pet types`() {
        PetType.entries.forEach { petType ->
            val reward =
                ChestReward.PetEmoji(
                    petType = petType,
                    emoji = "⭐",
                    description = "Reward for $petType",
                )

            assertEquals(petType, reward.petType)
        }
    }

    @Test
    fun `RarePetStyle can be created with all pet types`() {
        PetType.entries.forEach { petType ->
            val reward =
                ChestReward.RarePetStyle(
                    petType = petType,
                    styleName = "Test Style",
                    emoji = "✨",
                    description = "Rare style for $petType",
                )

            assertEquals(petType, reward.petType)
        }
    }

    // === Abstract Property Tests ===

    @Test
    fun `All ChestReward types have rarity property`() {
        val petEmoji =
            ChestReward.PetEmoji(
                petType = PetType.DOLPHIN,
                emoji = "😊",
                description = "Test",
            )
        val celebrationEffect =
            ChestReward.CelebrationEffect(
                effectName = "Test",
                description = "Test",
            )
        val rarePetStyle =
            ChestReward.RarePetStyle(
                petType = PetType.CAT,
                styleName = "Test",
                emoji = "✨",
                description = "Test",
            )

        assertNotNull(petEmoji.rarity)
        assertNotNull(celebrationEffect.rarity)
        assertNotNull(rarePetStyle.rarity)
    }

    @Test
    fun `All ChestReward types have name property`() {
        val petEmoji =
            ChestReward.PetEmoji(
                petType = PetType.DOG,
                emoji = "🎉",
                description = "Test",
            )
        val celebrationEffect =
            ChestReward.CelebrationEffect(
                effectName = "Test",
                description = "Test",
            )
        val rarePetStyle =
            ChestReward.RarePetStyle(
                petType = PetType.FOX,
                styleName = "Test",
                emoji = "✨",
                description = "Test",
            )

        assertNotNull(petEmoji.name)
        assertNotNull(celebrationEffect.name)
        assertNotNull(rarePetStyle.name)
    }

    @Test
    fun `All ChestReward types have description property`() {
        val petEmoji =
            ChestReward.PetEmoji(
                petType = PetType.CAT,
                emoji = "😊",
                description = "Pet emoji description",
            )
        val celebrationEffect =
            ChestReward.CelebrationEffect(
                effectName = "Test",
                description = "Celebration effect description",
            )
        val rarePetStyle =
            ChestReward.RarePetStyle(
                petType = PetType.DOLPHIN,
                styleName = "Test",
                emoji = "✨",
                description = "Rare style description",
            )

        assertEquals("Pet emoji description", petEmoji.description)
        assertEquals("Celebration effect description", celebrationEffect.description)
        assertEquals("Rare style description", rarePetStyle.description)
    }
}
