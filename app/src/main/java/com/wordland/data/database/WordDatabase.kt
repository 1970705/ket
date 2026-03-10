package com.wordland.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wordland.data.converter.Converters
import com.wordland.data.dao.*
import com.wordland.data.entity.*
import com.wordland.domain.model.*
import com.wordland.domain.model.Word

/**
 * Main Room database for Wordland app
 */
@TypeConverters(Converters::class)
@Database(
    entities = [
        Word::class,
        UserWordProgress::class,
        LevelProgress::class,
        BehaviorTracking::class,
        IslandMastery::class,
        WorldMapExplorationEntity::class,
        AchievementEntity::class,
        UserAchievementEntity::class,
        // Statistics entities (added in version 5)
        GameHistoryEntity::class,
        LevelStatisticsEntity::class,
        GlobalStatisticsEntity::class,
        OnboardingState::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class WordDatabase : RoomDatabase() {
    // DAOs
    abstract fun wordDao(): WordDao

    abstract fun progressDao(): ProgressDao

    abstract fun trackingDao(): TrackingDao

    abstract fun islandMasteryDao(): IslandMasteryDao

    abstract fun worldMapDao(): WorldMapDao

    abstract fun achievementDao(): AchievementDao

    // Statistics DAOs (added in version 5)
    abstract fun gameHistoryDao(): GameHistoryDao

    abstract fun statisticsDao(): StatisticsDao

    // Onboarding DAO (added in version 7)
    abstract fun onboardingStateDao(): OnboardingStateDao

    companion object {
        private const val DATABASE_NAME = "wordland.db"

        /**
         * Migration from version 1 to 2
         * Adds world_map_exploration table
         */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Create world_map_exploration table
                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS world_map_exploration (
                        userId TEXT PRIMARY KEY NOT NULL,
                        exploredRegions TEXT NOT NULL DEFAULT '[]',
                        totalDiscoveries INTEGER NOT NULL DEFAULT 0,
                        currentRegion TEXT,
                        explorationDays INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """,
                    )

                    // Create index for faster queries
                    database.execSQL(
                        """
                    CREATE INDEX IF NOT EXISTS index_world_map_exploration_userId
                    ON world_map_exploration(userId)
                """,
                    )
                }
            }

        /**
         * Migration from version 2 to 3
         * Adds achievement tables
         */
        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Create achievements table
                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS achievements (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        category TEXT NOT NULL,
                        tier TEXT NOT NULL,
                        requirementType TEXT NOT NULL,
                        requirementData TEXT NOT NULL,
                        rewardType TEXT NOT NULL,
                        rewardData TEXT NOT NULL,
                        isHidden INTEGER NOT NULL DEFAULT 0,
                        parentId TEXT
                    )
                """,
                    )

                    // Create user_achievements table
                    database.execSQL(
                        """
                    CREATE TABLE IF NOT EXISTS user_achievements (
                        userId TEXT NOT NULL,
                        achievementId TEXT NOT NULL,
                        isUnlocked INTEGER NOT NULL DEFAULT 0,
                        progress INTEGER NOT NULL DEFAULT 0,
                        target INTEGER NOT NULL DEFAULT 1,
                        unlockedAt INTEGER,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(userId, achievementId)
                    )
                """,
                    )

                    // Create indexes
                    database.execSQL(
                        """
                    CREATE INDEX IF NOT EXISTS index_achievements_category
                    ON achievements(category)
                """,
                    )
                    database.execSQL(
                        """
                    CREATE INDEX IF NOT EXISTS index_achievements_tier
                    ON achievements(tier)
                """,
                    )
                    database.execSQL(
                        """
                    CREATE INDEX IF NOT EXISTS index_user_achievements_userId
                    ON user_achievements(userId)
                """,
                    )
                    database.execSQL(
                        """
                    CREATE INDEX IF NOT EXISTS index_user_achievements_isUnlocked
                    ON user_achievements(isUnlocked)
                """,
                    )
                }
            }

        /**
         * Migration from version 3 to 4
         * Empty migration - version 4 was an internal version that didn't ship
         * This ensures smooth upgrade path for users with version 3 database
         */
        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Version 4 was an internal development version
                    // No schema changes, just version number increment
                    // This empty migration ensures users can upgrade from v3 to v5
                }
            }

        /**
         * Migration from version 4 to 5
         * Adds statistics tables (game_history, level_statistics, global_statistics)
         */
        val MIGRATION_4_5 =
            object : Migration(4, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Create game_history table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS game_history (
                            gameId TEXT PRIMARY KEY NOT NULL,
                            userId TEXT NOT NULL,
                            levelId TEXT NOT NULL,
                            islandId TEXT NOT NULL,
                            gameMode TEXT NOT NULL,
                            startTime INTEGER NOT NULL,
                            endTime INTEGER NOT NULL,
                            duration INTEGER NOT NULL,
                            score INTEGER NOT NULL,
                            stars INTEGER NOT NULL,
                            totalQuestions INTEGER NOT NULL,
                            correctAnswers INTEGER NOT NULL,
                            accuracy REAL NOT NULL,
                            maxCombo INTEGER NOT NULL,
                            hintsUsed INTEGER NOT NULL DEFAULT 0,
                            wrongAnswers INTEGER NOT NULL DEFAULT 0,
                            avgResponseTime INTEGER NOT NULL,
                            fastestAnswer INTEGER,
                            slowestAnswer INTEGER,
                            difficulty TEXT NOT NULL,
                            createdAt INTEGER NOT NULL
                        )
                    """,
                    )

                    // Create level_statistics table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS level_statistics (
                            userId TEXT NOT NULL,
                            levelId TEXT NOT NULL,
                            totalGames INTEGER NOT NULL DEFAULT 0,
                            completedGames INTEGER NOT NULL DEFAULT 0,
                            perfectGames INTEGER NOT NULL DEFAULT 0,
                            highestScore INTEGER NOT NULL DEFAULT 0,
                            lowestScore INTEGER NOT NULL DEFAULT 0,
                            averageScore REAL NOT NULL DEFAULT 0,
                            totalScore INTEGER NOT NULL DEFAULT 0,
                            bestTime INTEGER,
                            worstTime INTEGER,
                            averageTime INTEGER NOT NULL DEFAULT 0,
                            totalTime INTEGER NOT NULL DEFAULT 0,
                            totalCorrect INTEGER NOT NULL DEFAULT 0,
                            totalQuestions INTEGER NOT NULL DEFAULT 0,
                            overallAccuracy REAL NOT NULL DEFAULT 0,
                            bestCombo INTEGER NOT NULL DEFAULT 0,
                            firstPlayedAt INTEGER,
                            lastPlayedAt INTEGER,
                            lastUpdatedAt INTEGER NOT NULL,
                            PRIMARY KEY(userId, levelId)
                        )
                    """,
                    )

                    // Create global_statistics table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS global_statistics (
                            userId TEXT PRIMARY KEY NOT NULL,
                            totalGames INTEGER NOT NULL DEFAULT 0,
                            totalScore INTEGER NOT NULL DEFAULT 0,
                            totalPerfectGames INTEGER NOT NULL DEFAULT 0,
                            totalStudyTime INTEGER NOT NULL DEFAULT 0,
                            currentStreak INTEGER NOT NULL DEFAULT 0,
                            longestStreak INTEGER NOT NULL DEFAULT 0,
                            lastStudyDate INTEGER,
                            totalLevelsCompleted INTEGER NOT NULL DEFAULT 0,
                            totalLevelsPerfected INTEGER NOT NULL DEFAULT 0,
                            totalWordsMastered INTEGER NOT NULL DEFAULT 0,
                            totalCorrectAnswers INTEGER NOT NULL DEFAULT 0,
                            totalPracticeSessions INTEGER NOT NULL DEFAULT 0,
                            firstUsedAt INTEGER NOT NULL,
                            lastUpdatedAt INTEGER NOT NULL DEFAULT 0
                        )
                    """,
                    )

                    // Create indexes for game_history
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_game_history_userId
                        ON game_history(userId)
                    """,
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_game_history_levelId
                        ON game_history(levelId)
                    """,
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_game_history_gameMode
                        ON game_history(gameMode)
                    """,
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_game_history_startTime
                        ON game_history(startTime DESC)
                    """,
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_game_history_user_level_time
                        ON game_history(userId, levelId, startTime DESC)
                    """,
                    )

                    // Create indexes for level_statistics
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_level_statistics_userId
                        ON level_statistics(userId)
                    """,
                    )
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_level_statistics_levelId
                        ON level_statistics(levelId)
                    """,
                    )

                    // Create index for global_statistics
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_global_statistics_userId
                        ON global_statistics(userId)
                    """,
                    )
                }
            }

        /**
         * Migration from version 5 to 6
         * Fixes Make Lake data migration issue:
         * - Ensures Make Lake words have correct islandId (make_lake)
         * - Removes any stale make_atoll data if present
         * - No schema changes, only data cleanup
         */
        val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Fix any words that might have islandId = 'make_atoll'
                    // Update them to 'make_lake'
                    database.execSQL(
                        """
                        UPDATE words
                        SET islandId = 'make_lake'
                        WHERE islandId = 'make_atoll'
                        """,
                    )

                    // Fix any level_progress entries that might have islandId = 'make_atoll'
                    database.execSQL(
                        """
                        UPDATE level_progress
                        SET islandId = 'make_lake'
                        WHERE islandId = 'make_atoll'
                        """,
                    )

                    // Fix any island_mastery entries
                    database.execSQL(
                        """
                        UPDATE island_mastery
                        SET islandId = 'make_lake'
                        WHERE islandId = 'make_atoll'
                        """,
                    )

                    // Fix words levelId format if needed
                    // Ensure format is make_lake_level_XX
                    database.execSQL(
                        """
                        UPDATE words
                        SET levelId = 'make_lake_level_' || substr(levelId, -2)
                        WHERE islandId = 'make_lake'
                        AND levelId NOT LIKE 'make_lake_level_%'
                        """,
                    )

                    // Fix level_progress levelId format if needed
                    database.execSQL(
                        """
                        UPDATE level_progress
                        SET levelId = 'make_lake_level_' || substr(levelId, -2)
                        WHERE islandId = 'make_lake'
                        AND levelId NOT LIKE 'make_lake_level_%'
                        """,
                    )
                }
            }

        /**
         * Migration from version 6 to 7
         * Adds onboarding_state table for Onboarding Alpha feature
         */
        val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Create onboarding_state table
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS onboarding_state (
                            userId TEXT PRIMARY KEY NOT NULL,
                            currentPhase TEXT NOT NULL,
                            selectedPet TEXT,
                            completedTutorialWords INTEGER NOT NULL DEFAULT 0,
                            lastOpenedChest INTEGER NOT NULL DEFAULT 0,
                            totalStars INTEGER NOT NULL DEFAULT 0,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Create index for faster queries
                    database.execSQL(
                        """
                        CREATE INDEX IF NOT EXISTS index_onboarding_state_currentPhase
                        ON onboarding_state(currentPhase)
                        """.trimIndent(),
                    )
                }
            }

        @Volatile
        private var INSTANCE: WordDatabase? = null

        /**
         * Get database instance (singleton pattern)
         */
        fun getInstance(context: Context): WordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        WordDatabase::class.java,
                        DATABASE_NAME,
                    )
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4, // Empty migration for version 4
                            MIGRATION_4_5,
                            MIGRATION_5_6, // Make Lake data migration fix
                            MIGRATION_6_7, // Onboarding Alpha feature
                        )
                        .fallbackToDestructiveMigration() // Fallback for development
                        .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * For testing: create in-memory database
         */
        fun getTestInstance(context: Context): WordDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                WordDatabase::class.java,
            ).build()
        }

        /**
         * Close database connection
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
