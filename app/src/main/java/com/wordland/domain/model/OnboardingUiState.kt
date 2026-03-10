package com.wordland.domain.model

/**
 * Onboarding UI 状态
 *
 * 使用 sealed class 表示不同的 UI 状态
 */
sealed class OnboardingUiState {
    /** 空闲状态 */
    object Idle : OnboardingUiState()

    /** 加载中 */
    object Loading : OnboardingUiState()

    /** 欢迎界面 */
    data class Welcome(
        val showStartButton: Boolean = true,
    ) : OnboardingUiState()

    /** 宠物选择 */
    data class PetSelection(
        val availablePets: List<PetType> = PetType.entries.toList(),
        val selectedPet: PetType? = null,
    ) : OnboardingUiState()

    /** 教学关卡 */
    data class Tutorial(
        val currentWordIndex: Int,
        val totalWords: Int = 5,
        val question: TutorialQuestion,
        val progress: Float,
    ) : OnboardingUiState()

    /** 开宝箱 */
    data class OpeningChest(
        val reward: ChestReward,
    ) : OnboardingUiState()

    /** Alpha 完成 */
    data class Completed(
        val pet: PetType,
        val wordsLearned: Int,
        val stars: Int,
    ) : OnboardingUiState()

    /** 错误 */
    data class Error(
        val message: String,
        val recoverable: Boolean = true,
    ) : OnboardingUiState()
}

/**
 * 教学关卡问题
 *
 * @property word 单词
 * @property translation 中文翻译
 * @property preFilledLetters 预填字母的索引集合
 * @property hintsRemaining 剩余提示次数
 * @property currentAnswer 用户当前输入的答案
 */
data class TutorialQuestion(
    val word: String,
    val translation: String,
    val preFilledLetters: Set<Int>,
    val hintsRemaining: Int,
    val currentAnswer: String = "",
) {
    /**
     * 检查答案是否完整
     */
    fun isAnswerComplete(): Boolean {
        return currentAnswer.length == word.length
    }

    /**
     * 获取显示的答案（预填字母 + 用户输入）
     */
    fun getDisplayAnswer(): String {
        return buildString {
            for (i in word.indices) {
                if (i in preFilledLetters) {
                    append(word[i])
                } else if (i < currentAnswer.length) {
                    append(currentAnswer[i])
                } else {
                    append('_')
                }
            }
        }
    }

    /**
     * 获取进度百分比
     */
    fun getProgressPercentage(): Float {
        return if (word.isEmpty()) {
            0f
        } else {
            currentAnswer.length.toFloat() / word.length
        }
    }
}

/**
 * 宠物选择项
 */
data class PetSelectionItem(
    val petType: PetType,
    val displayName: String,
    val emoji: String,
    val isSelected: Boolean = false,
) {
    companion object {
        /**
         * 从 PetType 列表创建选择项
         */
        fun fromList(
            pets: List<PetType>,
            selected: PetType? = null,
        ): List<PetSelectionItem> {
            return pets.map { pet ->
                PetSelectionItem(
                    petType = pet,
                    displayName = pet.getDisplayName(),
                    emoji = pet.getEmoji(),
                    isSelected = pet == selected,
                )
            }
        }
    }
}
