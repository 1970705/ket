package com.wordland.domain.constants

/**
 * Domain-layer constants for business logic
 * Used by domain models and use cases
 */
object DomainConstants {
    // Difficulty Settings
    const val DIFFICULTY_EASY = 1
    const val DIFFICULTY_NORMAL = 2
    const val DIFFICULTY_HARD = 3

    // Mastery Thresholds (Domain business rules)
    const val MASTERY_PERCENTAGE_TO_UNLOCK_NEXT = 60.0
    const val MASTERY_PERCENTAGE_TO_UNLOCK_NEXT_ISLAND = 60.0
    const val MASTERY_PERCENTAGE_COMPLETE = 100.0

    // Memory Strength (Domain algorithm parameters)
    const val MEMORY_STRENGTH_INITIAL = 10
    const val MEMORY_STRENGTH_MAX = 100
    const val CORRECT_BONUS = 10
    const val INCORRECT_PENALTY = 15

    // Guessing Detection (Domain business rules)
    // Child-friendly: 0.5 seconds per letter minimum for non-guessing
    const val MILLIS_PER_LETTER_THRESHOLD = 500L // 0.5 seconds per letter
    const val GUESSING_THRESHOLD_FAST = 2000 // 2 seconds (deprecated, use MILLIS_PER_LETTER_THRESHOLD)
    const val GUESSING_THRESHOLD_VERY_FAST = 1500 // 1.5 seconds (deprecated)
    const val MIN_RESPONSE_TIME_MS = 1000L // Minimum 1 second for any answer (child-friendly base)

    // Cross Scene Validation (Domain business rules)
    const val CROSS_SCENE_CORRECT_RATE_THRESHOLD = 0.7 // 70%

    // Energy System (Domain business rules)
    const val ENERGY_MAX = 100
    const val ENERGY_RECOVERY_RATE_MINUTES = 1.5
    const val ENERGY_PER_QUESTION = 5

    // Time Limits (Domain business rules)
    const val SESSION_DURATION_THRESHOLD_GUESSING = 2000 // 2 seconds

    // Progress (Domain business rules)
    const val LEVEL_STARS_TO_UNLOCK_NEXT = 3

    // Content (Domain business rules)
    const val WORDS_PER_ISLAND_MVP = 60
    const val LEVELS_PER_ISLAND_MVP = 8
}
