package com.wordland.domain.usecase.usecases

import com.wordland.domain.model.TutorialWordConfig

/**
 * 获取教学关卡单词 UseCase
 *
 * Alpha 版本: 返回5个最简单的单词
 *
 * 注意: Alpha 版本使用硬编码的默认单词列表
 * 后续版本将从数据库获取最简单的单词
 */
class GetTutorialWordsUseCase {
    /**
     * 获取教学单词列表
     *
     * @return List<TutorialWordConfig> 教学单词配置列表
     */
    operator fun invoke(): List<TutorialWordConfig> {
        // Alpha 版本使用默认单词
        return TutorialWordConfig.createDefaultAlphaWords()
    }

    /**
     * 获取默认 Alpha 单词（备用方案）
     *
     * @return List<TutorialWordConfig> 默认教学单词
     */
    fun getDefaultWords(): List<TutorialWordConfig> {
        return TutorialWordConfig.createDefaultAlphaWords()
    }

    companion object {
        /**
         * Alpha 版本的教学单词ID列表
         */
        val ALPHA_WORD_IDS =
            listOf(
                "look_001", // look
                "look_004", // see
                "look_007", // eye
                "look_010", // find
                "look_013", // glass
            )
    }
}
