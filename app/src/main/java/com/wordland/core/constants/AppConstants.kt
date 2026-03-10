package com.wordland.core.constants

/**
 * Application-wide infrastructure constants
 * Used by Data layer for technical configuration
 *
 * Note: Business logic constants have been moved to DomainConstants
 * @see com.wordland.domain.constants.DomainConstants
 */
object AppConstants {
    // Database
    const val DATABASE_NAME = "wordland_database"
    const val DATABASE_VERSION = 1

    // Shared Preferences
    const val PREFS_NAME = "wordland_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_PARENT_MODE = "parent_mode"
    const val KEY_FIRST_LAUNCH = "first_launch"
}
