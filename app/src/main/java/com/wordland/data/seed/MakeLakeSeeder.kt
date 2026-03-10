package com.wordland.data.seed

import com.wordland.data.dao.IslandMasteryDao
import com.wordland.data.dao.ProgressDao
import com.wordland.data.dao.TrackingDao
import com.wordland.data.dao.WordDao
import com.wordland.domain.model.IslandMastery
import com.wordland.domain.model.LearningStatus
import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import com.wordland.domain.model.UserWordProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Database seeder for Make Lake (creation theme)
 * 60 words, 10 levels, 1 island mastery
 */
@Singleton
class MakeLakeSeeder
    @Inject
    constructor(
        private val wordDao: WordDao,
        private val progressDao: ProgressDao,
        private val islandMasteryDao: IslandMasteryDao,
        private val trackingDao: TrackingDao,
    ) {
        private val islandId = "make_lake"
        private val islandName = "Make Lake"

        /**
         * Seed Make Lake data on first app launch
         */
        suspend fun seedMakeLake(userId: String = "user_001") =
            withContext(Dispatchers.IO) {
                try {
                    // 1. Insert all 60 words
                    val words = MakeLakeWords.getAllWords()
                    wordDao.insertWords(words)

                    // 2. Create 10 levels for Make Lake
                    val levels =
                        (1..10).map { levelIndex ->
                            LevelProgress(
                                levelId = "make_lake_level_${levelIndex.toString().padStart(2, '0')}",
                                userId = userId,
                                islandId = islandId,
                                status = if (levelIndex == 1) LevelStatus.UNLOCKED else LevelStatus.LOCKED,
                                difficulty = "easy",
                                stars = 0,
                                score = 0,
                            )
                        }
                    progressDao.insertLevelProgressList(levels)

                    // 3. Create UserWordProgress for all 60 words
                    val wordProgress =
                        words.map { word ->
                            UserWordProgress(
                                wordId = word.id,
                                userId = userId,
                                status = LearningStatus.NEW,
                                memoryStrength = 20, // Initial memory strength
                                totalAttempts = 0,
                                correctAttempts = 0,
                                incorrectAttempts = 0,
                                averageResponseTime = 0L,
                            )
                        }
                    progressDao.insertWordProgressList(wordProgress)

                    // 4. Create IslandMastery record for Make Lake
                    val islandMastery =
                        IslandMastery(
                            islandId = islandId,
                            userId = "user_001",
                            totalWords = 60,
                            masteredWords = 0,
                            totalLevels = 10,
                            completedLevels = 0,
                            masteryPercentage = 0.0,
                            isNextIslandUnlocked = false,
                            crossSceneScore = 0.0,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    islandMasteryDao.insertIslandMastery(islandMastery)
                } catch (e: Exception) {
                    throw Exception("Failed to seed Make Lake: ${e.message}", e)
                }
            }

        /**
         * Get total word count for Make Lake
         */
        fun getTotalWordCount(): Int = 60

        /**
         * Get KET word count for Make Lake
         */
        fun getKETWordCount(): Int = 52

        /**
         * Get PET word count for Make Lake
         */
        fun getPETWordCount(): Int = 8
    }
