package com.wordland.ui.viewmodel.helper

import androidx.lifecycle.SavedStateHandle
import com.wordland.domain.model.*
import com.wordland.domain.usecase.usecases.ExploreRegionResult
import com.wordland.domain.usecase.usecases.HintStats
import com.wordland.domain.usecase.usecases.UseHintUseCaseEnhanced
import io.mockk.every

/**
 * Test helper class providing shared test data and utilities for LearningViewModel tests.
 * Reduces code duplication across split test files.
 */
object LearningViewModelTestHelper {
    // Test data constants
    const val TEST_USER_ID = "user_001"
    const val TEST_LEVEL_ID = "look_level_01"
    const val TEST_ISLAND_ID = "look_island"

    // Test words
    val testWords =
        listOf(
            Word(
                id = "word1",
                word = "apple",
                translation = "苹果",
                pronunciation = "/ˈæpl/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 1,
                frequency = 100,
                theme = "fruit",
                islandId = TEST_ISLAND_ID,
                levelId = TEST_LEVEL_ID,
                order = 1,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            ),
            Word(
                id = "word2",
                word = "banana",
                translation = "香蕉",
                pronunciation = "/bəˈnɑːnə/",
                audioPath = null,
                partOfSpeech = "noun",
                difficulty = 2,
                frequency = 80,
                theme = "fruit",
                islandId = TEST_ISLAND_ID,
                levelId = TEST_LEVEL_ID,
                order = 2,
                ketLevel = true,
                petLevel = false,
                exampleSentences = null,
                relatedWords = null,
                root = null,
                prefix = null,
                suffix = null,
            ),
        )

    /**
     * Creates a SavedStateHandle with default navigation parameters.
     */
    fun createSavedStateHandle(
        levelId: String = TEST_LEVEL_ID,
        islandId: String = TEST_ISLAND_ID,
    ): SavedStateHandle {
        return SavedStateHandle().apply {
            set("levelId", levelId)
            set("islandId", islandId)
        }
    }

    /**
     * Creates a SavedStateHandle without levelId (for testing empty state).
     */
    fun createEmptySavedStateHandle(): SavedStateHandle {
        return SavedStateHandle()
    }

    /**
     * Creates a SubmitAnswerResult for correct answer.
     */
    fun createCorrectAnswerResult(
        word: Word = testWords[0],
        combo: Int = 1,
    ) = SubmitAnswerResult(
        word = word,
        isCorrect = true,
        newMemoryStrength = 60,
        isGuessing = false,
        timeTaken = 3000L,
        hintUsed = false,
        starsEarned = 3,
        message = "太棒了！",
        comboState = ComboState(consecutiveCorrect = combo),
    )

    /**
     * Creates a SubmitAnswerResult for wrong answer.
     */
    fun createWrongAnswerResult(word: Word = testWords[0]) =
        SubmitAnswerResult(
            word = word,
            isCorrect = false,
            newMemoryStrength = 20,
            isGuessing = false,
            timeTaken = 2000L,
            hintUsed = false,
            starsEarned = 0,
            message = "不对，继续练习！",
            comboState = ComboState(consecutiveCorrect = 0),
        )

    /**
     * Creates a HintResult.
     */
    fun createHintResult(
        level: Int = 1,
        text: String = "首字母: A",
    ) = com.wordland.domain.usecase.usecases.HintResult(
        hintText = text,
        hintLevel = level,
        hintsRemaining = 3 - level,
        hintsUsed = level,
        totalHints = 3,
        word = "apple",
        shouldApplyPenalty = true,
    )

    /**
     * Creates a default HintStats for mocking.
     */
    fun createDefaultHintStats() =
        HintStats(
            wordId = "word1",
            currentLevel = 0,
            hintsUsed = 0,
            hintsRemaining = 3,
            totalHints = 3,
            dependencyScore = 0f,
            isOverusing = false,
        )

    /**
     * Sets up default hint mocks for UseHintUseCaseEnhanced.
     */
    fun setupDefaultHintMocks(useHint: UseHintUseCaseEnhanced) {
        every { useHint.resetHints(any()) } returns Unit
        every { useHint.getHintStats(any()) } returns createDefaultHintStats()
    }

    /**
     * Creates an ExploreRegionResult for default region discovery.
     */
    fun createExploreRegionResult(regionId: String = "look_peninsula") =
        ExploreRegionResult(
            regionId = regionId,
            isNewDiscovery = true,
            totalDiscoveries = 1,
        )

    /**
     * Creates an IslandMastery for testing.
     */
    fun createIslandMastery(
        islandId: String = TEST_ISLAND_ID,
        userId: String = TEST_USER_ID,
    ) = IslandMastery(
        islandId = islandId,
        userId = userId,
        totalWords = 6,
        masteredWords = 1,
        totalLevels = 5,
        completedLevels = 1,
        crossSceneScore = 1.0,
        unlockedAt = System.currentTimeMillis(),
        masteredAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
    )

    /**
     * Creates a UserWordProgress for testing.
     */
    fun createUserWordProgress(
        wordId: String = "look_001",
        userId: String = TEST_USER_ID,
    ) = UserWordProgress(
        wordId = wordId,
        userId = userId,
        status = LearningStatus.MASTERED,
        memoryStrength = 100,
        totalAttempts = 3,
        correctAttempts = 3,
        incorrectAttempts = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        lastReviewTime = System.currentTimeMillis(),
    )
}
