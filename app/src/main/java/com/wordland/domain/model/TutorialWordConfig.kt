package com.wordland.domain.model

/**
 * 教学关卡单词配置
 *
 * Alpha 版本: 35%预填，最多3次提示
 *
 * @property word 单词
 * @property translation 中文翻译
 * @property preFillRatio 预填比例（默认35%）
 * @property minPreFillLetters 至少预填字母数
 * @property hintsAllowed 最多提示次数
 * @property showFirstLetter 是否显示首字母
 * @property timeLimit 时间限制（null表示无限制）
 */
data class TutorialWordConfig(
    val word: String,
    val translation: String,
    val preFillRatio: Float = 0.35f,
    val minPreFillLetters: Int = 1,
    val hintsAllowed: Int = 3,
    val showFirstLetter: Boolean = true,
    val timeLimit: Int? = null,
) {
    /**
     * 计算预填字母数量
     */
    fun calculatePreFillCount(): Int {
        val count = (word.length * preFillRatio).toInt()
        return maxOf(count, minPreFillLetters)
    }

    /**
     * 生成预填字母索引
     * 优先预填首字母（如果 showFirstLetter 为 true）
     */
    fun generatePreFilledIndices(): Set<Int> {
        val count = calculatePreFillCount()
        val indices =
            if (showFirstLetter && word.isNotEmpty()) {
                // 首字母必须预填，然后随机选择剩余的
                val remaining = (count - 1).coerceAtLeast(0)
                setOf(0) +
                    (1 until word.length)
                        .shuffled()
                        .take(remaining)
                        .toSet()
            } else {
                (0 until word.length)
                    .shuffled()
                    .take(count)
                    .toSet()
            }
        return indices
    }

    /**
     * 检查答案是否正确
     */
    fun isCorrect(answer: String): Boolean {
        return answer.equals(word, ignoreCase = true)
    }

    /**
     * 获取预填后的单词显示
     * 例如 "c_t" 表示 "cat" 预填了首字母
     */
    fun getPreFilledDisplay(): String {
        val preFilledIndices = generatePreFilledIndices()
        return buildString {
            for (i in word.indices) {
                if (i in preFilledIndices) {
                    append(word[i])
                } else {
                    append('_')
                }
            }
        }
    }

    companion object {
        /**
         * 创建 Alpha 版本的默认教学单词列表
         * 选择最简单的5个单词
         */
        fun createDefaultAlphaWords(): List<TutorialWordConfig> {
            return listOf(
                TutorialWordConfig(word = "cat", translation = "猫"),
                TutorialWordConfig(word = "dog", translation = "狗"),
                TutorialWordConfig(word = "sun", translation = "太阳"),
                TutorialWordConfig(word = "eye", translation = "眼睛"),
                TutorialWordConfig(word = "red", translation = "红色"),
            )
        }
    }
}
