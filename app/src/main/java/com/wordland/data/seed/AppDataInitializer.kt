package com.wordland.data.seed

import com.wordland.data.dao.WordDao
import com.wordland.data.repository.IslandMasteryRepository
import com.wordland.data.repository.ProgressRepository
import com.wordland.domain.model.IslandMastery
import com.wordland.domain.model.LevelProgress
import com.wordland.domain.model.LevelStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for initializing app data on first launch
 * Coordinates seeding for all islands
 */
@Singleton
class AppDataInitializer
    @Inject
    constructor(
        private val lookIslandSeeder: LevelDataSeeder,
        private val makeLakeSeeder: MakeLakeSeeder,
        private val islandMasteryRepository: IslandMasteryRepository,
        private val progressRepository: ProgressRepository,
        private val wordDao: WordDao,
    ) {
        private val scope = CoroutineScope(Dispatchers.IO)

        /**
         * Initialize all app data
         * Should be called on first app launch
         * Uses idempotent operations - safe to call multiple times
         */
        fun initializeAllData() {
            scope.launch {
                try {
                    // Seed Look Island data
                    lookIslandSeeder.seedLookIsland()

                    // Seed Make Lake data (with validation)
                    ensureMakeLakeDataIntegrity()

                    // Create initial island mastery records
                    initializeIslandMastery()

                    // Unlock first level of each island
                    unlockFirstLevels()
                } catch (e: Exception) {
                    // Log error but don't crash
                    android.util.Log.e("AppDataInitializer", "Failed to initialize data", e)
                }
            }
        }

        /**
         * Ensure Make Lake data integrity
         * Validates and repairs Make Lake data if needed
         * This handles edge cases from database migrations
         */
        private suspend fun ensureMakeLakeDataIntegrity() {
            try {
                val userId = com.wordland.WordlandApplication.USER_ID

                // Check if Make Lake has expected word count
                val currentWordCount = wordDao.getWordCount()
                val makeLakeWords = wordDao.getWordsByIsland("make_lake")

                // If Make Lake has no words or wrong count, re-seed
                if (makeLakeWords.size != makeLakeSeeder.getTotalWordCount()) {
                    android.util.Log.w(
                        "AppDataInitializer",
                        "Make Lake word count mismatch. Expected: ${makeLakeSeeder.getTotalWordCount()}, Found: ${makeLakeWords.size}. Re-seeding...",
                    )

                    // Delete any existing Make Lake words to avoid conflicts
                    wordDao.deleteWordsByIsland("make_lake")

                    // Re-seed Make Lake
                    makeLakeSeeder.seedMakeLake(userId)
                } else {
                    // Just ensure levels exist (idempotent)
                    makeLakeSeeder.seedMakeLake(userId)
                }
            } catch (e: Exception) {
                android.util.Log.e("AppDataInitializer", "Error ensuring Make Lake data", e)
                // Try to seed anyway as fallback
                makeLakeSeeder.seedMakeLake()
            }
        }

        /**
         * Initialize island mastery records for all islands
         */
        private suspend fun initializeIslandMastery() {
            val userId = com.wordland.WordlandApplication.USER_ID
            val now = System.currentTimeMillis()

            // Look Island
            val lookMastery =
                IslandMastery(
                    islandId = "look_island",
                    userId = userId,
                    totalWords = lookIslandSeeder.getTotalWordCount(),
                    masteredWords = 0,
                    totalLevels = 10,
                    completedLevels = 0,
                    crossSceneScore = 0.0,
                    masteryPercentage = 0.0,
                    isNextIslandUnlocked = false,
                    unlockedAt = now,
                    masteredAt = null,
                    createdAt = now,
                    updatedAt = now,
                )
            islandMasteryRepository.insertIslandMastery(lookMastery)

            // Make Lake
            val makeMastery =
                IslandMastery(
                    islandId = "make_lake",
                    userId = userId,
                    totalWords = makeLakeSeeder.getTotalWordCount(),
                    masteredWords = 0,
                    totalLevels = 10,
                    completedLevels = 0,
                    crossSceneScore = 0.0,
                    masteryPercentage = 0.0,
                    isNextIslandUnlocked = false,
                    unlockedAt = now,
                    masteredAt = null,
                    createdAt = now,
                    updatedAt = now,
                )
            islandMasteryRepository.insertIslandMastery(makeMastery)
        }

        /**
         * Unlock first level of each island
         * Other levels remain locked
         */
        private suspend fun unlockFirstLevels() {
            val userId = com.wordland.WordlandApplication.USER_ID
            val now = System.currentTimeMillis()

            // Look Island Level 1 (should be UNLOCKED for first level)
            val lookLevel1 =
                LevelProgress(
                    levelId = "look_island_level_01",
                    userId = userId,
                    islandId = "look_island",
                    status = LevelStatus.UNLOCKED,
                    difficulty = "normal",
                    createdAt = now,
                    updatedAt = now,
                )
            progressRepository.insertLevelProgress(lookLevel1)

            // Make Lake Level 1 (should be UNLOCKED for first level)
            val makeLevel1 =
                LevelProgress(
                    levelId = "make_lake_level_01",
                    userId = userId,
                    islandId = "make_lake",
                    status = LevelStatus.UNLOCKED,
                    difficulty = "normal",
                    createdAt = now,
                    updatedAt = now,
                )
            progressRepository.insertLevelProgress(makeLevel1)
        }

        /**
         * Check if initialization is needed
         * Returns true if no islands exist in database
         */
        fun needsInitialization(): Boolean {
            // This check should be done on main thread before launching coroutine
            // For now, return true (always initialize on MVP)
            return true
        }

        /**
         * Get total content statistics
         */
        fun getContentStatistics(): ContentStatistics {
            return ContentStatistics(
                totalIslands = 7,
                totalLevels = 70,
                totalWords = lookIslandSeeder.getTotalWordCount() + makeLakeSeeder.getTotalWordCount(),
                ketWords = lookIslandSeeder.getKETWordCount() + makeLakeSeeder.getKETWordCount(),
                petWords = lookIslandSeeder.getPETWordCount() + makeLakeSeeder.getPETWordCount(),
            )
        }
    }

/**
 * Content statistics for app
 */
data class ContentStatistics(
    val totalIslands: Int,
    val totalLevels: Int,
    val totalWords: Int,
    val ketWords: Int,
    val petWords: Int,
)
