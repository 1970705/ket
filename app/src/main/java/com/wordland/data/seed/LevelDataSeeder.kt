package com.wordland.data.seed

import com.wordland.data.repository.ProgressRepository
import com.wordland.data.repository.WordRepository
import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import com.wordland.domain.model.UserWordProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeder for initial level and progress data
 */
@Singleton
class LevelDataSeeder
    @Inject
    constructor(
        private val wordRepository: WordRepository,
        private val progressRepository: ProgressRepository,
    ) {
        private val scope = CoroutineScope(Dispatchers.IO)

        /**
         * Seed initial data for Look Island
         */
        fun seedLookIsland() {
            scope.launch {
                // Insert words
                val words = LookIslandWords.getAllWords()
                wordRepository.insertWords(words)

                // Initialize levels
                val userId = com.wordland.WordlandApplication.USER_ID

                for (levelIndex in 1..5) { // 5 levels for prototype
                    val levelId = "look_island_level_${levelIndex.toString().padStart(2, '0')}"

                    // Create level progress
                    val levelProgress =
                        LevelProgress(
                            levelId = levelId,
                            userId = userId,
                            islandId = "look_island",
                            status = if (levelIndex == 1) LevelStatus.UNLOCKED else LevelStatus.LOCKED,
                            difficulty = "normal",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )

                    progressRepository.insertLevelProgress(levelProgress)

                    // Get words for this level
                    val levelWords = LookIslandWords.getWordsForLevel(levelIndex - 1)

                    // Initialize word progress for each word
                    levelWords.forEach { word ->
                        val wordProgress =
                            UserWordProgress(
                                userId = userId,
                                wordId = word.id,
                                status = LearningStatus.NEW,
                                memoryStrength = 20, // Initial strength
                                totalAttempts = 0,
                                correctAttempts = 0,
                                incorrectAttempts = 0,
                                lastReviewTime = null,
                                nextReviewTime = null,
                                averageResponseTime = 0,
                                isGuessingDetected = false,
                                sceneExposure = null,
                                crossSceneCorrect = 0,
                                crossSceneTotal = 0,
                                firstLearnTime = null,
                                masteryTime = null,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                            )

                        progressRepository.insertWordProgress(wordProgress)
                    }
                }
            }
        }

        /**
         * Clear all seeded data
         */
        fun clearData() {
            scope.launch {
                // For now, just log
                // TODO: Implement clear functionality if needed
            }
        }

        /**
         * Get total word count for Look Island
         */
        fun getTotalWordCount(): Int {
            return LookIslandWords.getAllWords().size
        }

        /**
         * Get KET word count for Look Island
         */
        fun getKETWordCount(): Int {
            return LookIslandWords.getAllWords().count { it.ketLevel }
        }

        /**
         * Get PET word count for Look Island
         */
        fun getPETWordCount(): Int {
            return LookIslandWords.getAllWords().count { it.petLevel }
        }
    }
