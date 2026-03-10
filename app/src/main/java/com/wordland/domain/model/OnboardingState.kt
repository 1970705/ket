package com.wordland.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Onboarding 流程状态
 *
 * Alpha 范围: WELCOME → PET_SELECTION → TUTORIAL → FIRST_CHEST
 *
 * @property userId 单用户MVP:固定为"user_001"
 * @property currentPhase 当前阶段
 * @property selectedPet 选择的宠物
 * @property completedTutorialWords 教学关卡完成单词数
 * @property lastOpenedChest 上次开宝箱时间
 * @property totalStars 获得星星总数
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
@Entity(tableName = "onboarding_state")
data class OnboardingState(
    @PrimaryKey
    val userId: String,
    val currentPhase: OnboardingPhase,
    val selectedPet: PetType?,
    val completedTutorialWords: Int = 0,
    val lastOpenedChest: Long = 0L,
    val totalStars: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Onboarding 阶段枚举
 */
enum class OnboardingPhase {
    NOT_STARTED, // 未开始
    WELCOME, // 欢迎界面
    PET_SELECTION, // 宠物选择
    TUTORIAL, // 教学关卡
    FIRST_CHEST, // 首次开宝箱
    COMPLETED, // Alpha 完成
}

/**
 * 宠物类型枚举（Alpha 全部解锁）
 */
enum class PetType {
    DOLPHIN, // 海豚 🐬
    CAT, // 猫咪 🐱
    DOG, // 小狗 🐶
    FOX, // 狐狸 🦊
}

/**
 * 获取宠物显示名称
 */
fun PetType.getDisplayName(): String =
    when (this) {
        PetType.DOLPHIN -> "海豚"
        PetType.CAT -> "猫咪"
        PetType.DOG -> "小狗"
        PetType.FOX -> "狐狸"
    }

/**
 * 获取宠物 Emoji
 */
fun PetType.getEmoji(): String =
    when (this) {
        PetType.DOLPHIN -> "\uD83D\uDC2C" // 🐬
        PetType.CAT -> "\uD83D\uDC31" // 🐱
        PetType.DOG -> "\uD83D\uDC36" // 🐶
        PetType.FOX -> "\uD83E\uDD8A" // 🦊
    }
