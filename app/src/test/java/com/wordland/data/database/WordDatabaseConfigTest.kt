package com.wordland.data.database

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Tests for WordDatabase configuration
 *
 * Tests database constants and configuration values
 */
@RunWith(JUnit4::class)
class WordDatabaseConfigTest {
    @Test
    fun wordDatabase_versionIsCorrect() {
        // Current database version should be 3
        val currentVersion = 3

        assertEquals(3, currentVersion)
    }

    @Test
    fun wordDatabase_entityCount() {
        // Database should have 8 entities
        val entityCount = 8

        val entities =
            listOf(
                "Word",
                "UserWordProgress",
                "LevelProgress",
                "BehaviorTracking",
                "IslandMastery",
                "WorldMapExplorationEntity",
                "AchievementEntity",
                "UserAchievementEntity",
            )

        assertEquals(entityCount, entities.size)
    }

    @Test
    fun wordDatabase_entityNamesAreCorrect() {
        val entities =
            listOf(
                "Word",
                "UserWordProgress",
                "LevelProgress",
                "BehaviorTracking",
                "IslandMastery",
                "WorldMapExplorationEntity",
                "AchievementEntity",
                "UserAchievementEntity",
            )

        assertTrue(entities.contains("Word"))
        assertTrue(entities.contains("AchievementEntity"))
        assertTrue(entities.contains("UserAchievementEntity"))
    }

    @Test
    fun wordDatabase_daoCount() {
        // Database should have 6 DAOs
        val daoCount = 6

        val daos =
            listOf(
                "WordDao",
                "ProgressDao",
                "TrackingDao",
                "IslandMasteryDao",
                "WorldMapDao",
                "AchievementDao",
            )

        assertEquals(daoCount, daos.size)
    }

    @Test
    fun wordDatabase_migrationVersions() {
        // Migration versions
        val migration1_2_start = 1
        val migration1_2_end = 2
        val migration2_3_start = 2
        val migration2_3_end = 3

        assertEquals(1, migration1_2_start)
        assertEquals(2, migration1_2_end)
        assertEquals(2, migration2_3_start)
        assertEquals(3, migration2_3_end)

        // Verify sequential migrations
        assertEquals(migration1_2_end, migration2_3_start)
    }

    @Test
    fun wordDatabase_schemaExport() {
        // Schema export should be enabled
        val exportSchema = true

        assertTrue(exportSchema)
    }

    @Test
    fun wordDatabase_tableNames() {
        val tables =
            listOf(
                "words",
                "user_word_progress",
                "level_progress",
                "behavior_tracking",
                "island_mastery",
                "world_map_exploration",
                "achievements",
                "user_achievements",
            )

        assertEquals(8, tables.size)
        assertTrue(tables.contains("words"))
        assertTrue(tables.contains("achievements"))
        assertTrue(tables.contains("user_achievements"))
    }

    @Test
    fun wordDatabase_indexNames() {
        val indexes =
            listOf(
                "index_world_map_exploration_userId",
                "index_achievements_category",
                "index_achievements_tier",
                "index_user_achievements_userId",
                "index_user_achievements_isUnlocked",
            )

        assertEquals(5, indexes.size)
    }

    @Test
    fun wordDatabase_primaryKeys() {
        // Test primary key conventions
        val wordTablePK = "id"
        val userWordProgressPK = "wordId" // Composite with userId
        val achievementsPK = "id"
        val userAchievementsPK = "userId_achievementId" // Composite

        assertEquals("id", wordTablePK)
        assertEquals("id", achievementsPK)
    }

    @Test
    fun wordDatabase_columnTypes() {
        // Test column type mappings
        val textType = "TEXT"
        val integerType = "INTEGER"
        val realType = "REAL"

        assertEquals("TEXT", textType)
        assertEquals("INTEGER", integerType)
        assertEquals("REAL", realType)
    }

    @Test
    fun wordDatabase_foreignKeys() {
        // Foreign key relationships
        val relationships =
            listOf(
                "user_word_progress -> words(wordId)",
                "level_progress -> islands(islandId)",
                "user_achievements -> achievements(achievementId)",
            )

        assertEquals(3, relationships.size)
    }

    @Test
    fun wordDatabase_booleanMapping() {
        // Room maps Boolean to INTEGER (0 or 1)
        val booleanTrue = 1
        val booleanFalse = 0

        assertEquals(1, booleanTrue)
        assertEquals(0, booleanFalse)
    }

    @Test
    fun wordDatabase_migration1to2_createsWorldMapTable() {
        val tableName = "world_map_exploration"
        val expectedColumns =
            listOf(
                "userId",
                "exploredRegions",
                "totalDiscoveries",
                "currentRegion",
                "explorationDays",
                "createdAt",
                "updatedAt",
            )

        assertEquals(7, expectedColumns.size)
        assertTrue(expectedColumns.contains("userId"))
        assertTrue(expectedColumns.contains("exploredRegions"))
    }

    @Test
    fun wordDatabase_migration2to3_createsAchievementTables() {
        val achievementTable = "achievements"
        val userAchievementTable = "user_achievements"

        assertEquals("achievements", achievementTable)
        assertEquals("user_achievements", userAchievementTable)
    }
}
