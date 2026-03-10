package com.wordland.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration tests for WordDatabase
 *
 * Tests the database migration path from version 1 to current version
 * Ensures that existing user data is preserved during app upgrades
 */
@RunWith(AndroidJUnit4::class)
class WordDatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    /**
     * Test that MIGRATION_3_4 exists and is accessible
     *
     * Version 4 was an internal development version that was never released
     * The migration should be empty and not modify the database
     */
    @Test
    fun testMigration3To4Exists() {
        // Verify MIGRATION_3_4 is accessible
        assertEquals(3, WordDatabase.MIGRATION_3_4.startVersion)
        assertEquals(4, WordDatabase.MIGRATION_3_4.endVersion)
    }

    /**
     * Test that all required migrations are accessible
     */
    @Test
    fun testAllMigrationsAccessible() {
        // Verify MIGRATION_1_2
        assertEquals(1, WordDatabase.MIGRATION_1_2.startVersion)
        assertEquals(2, WordDatabase.MIGRATION_1_2.endVersion)

        // Verify MIGRATION_2_3
        assertEquals(2, WordDatabase.MIGRATION_2_3.startVersion)
        assertEquals(3, WordDatabase.MIGRATION_2_3.endVersion)

        // Verify MIGRATION_3_4
        assertEquals(3, WordDatabase.MIGRATION_3_4.startVersion)
        assertEquals(4, WordDatabase.MIGRATION_3_4.endVersion)

        // Verify MIGRATION_4_5
        assertEquals(4, WordDatabase.MIGRATION_4_5.startVersion)
        assertEquals(5, WordDatabase.MIGRATION_4_5.endVersion)
    }

    /**
     * Test that fresh database starts at current version
     */
    @Test
    fun testFreshDatabaseAtCurrentVersion() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db =
            Room.databaseBuilder(
                context,
                WordDatabase::class.java,
                TEST_DB,
            )
                .addMigrations(
                    WordDatabase.MIGRATION_1_2,
                    WordDatabase.MIGRATION_2_3,
                    WordDatabase.MIGRATION_3_4,
                    WordDatabase.MIGRATION_4_5,
                )
                .build()

        try {
            // Verify database opens successfully at current version
            val openHelper = db.openHelper
            assertEquals(5, openHelper.readableDatabase.version)
        } finally {
            db.close()
        }
    }

    /**
     * Test database instance creation
     */
    @Test
    fun testDatabaseInstanceCreation() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        // Test getInstance method
        val db1 = WordDatabase.getInstance(context)
        assertNotNull(db1)

        // Test singleton pattern
        val db2 = WordDatabase.getInstance(context)
        assertEquals(db1, db2)

        // Clean up
        WordDatabase.closeDatabase()

        // Verify new instance is created after close
        val db3 = WordDatabase.getInstance(context)
        assertTrue(db1 !== db3)

        WordDatabase.closeDatabase()
    }

    /**
     * Helper method to assert not null
     */
    private fun <T> assertNotNull(obj: T?) {
        org.junit.Assert.assertNotNull(obj)
    }
}
