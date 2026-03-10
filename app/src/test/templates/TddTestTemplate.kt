/**
 * TDD Test Template
 *
 * This template provides a standard structure for test-driven development.
 * Use this template to ensure consistent test coverage across all features.
 *
 * @author Claude Code (Autonomous TDD Skill)
 * @created 2026-02-28
 */

package com.wordland.templates

import org.junit.Test
import org.junit.Before
import org.junit.After
import assertk.assertThat
import assertk.assertions.*

/**
 * Template class for TDD tests.
 *
 * Copy this class and rename it to: {FeatureName}Test
 *
 * Sections:
 * 1. Test Setup (Before/After)
 * 2. Happy Path Tests (normal scenarios)
 * 3. Edge Case Tests (boundary conditions)
 * 4. Error Scenario Tests (exception handling)
 * 5. Performance Tests (if applicable)
 */
abstract class TddTestTemplate {

    /**
     * Setup method - runs before each test
     *
     * Use this to:
     * - Initialize test objects
     * - Set up test data
     * - Configure mocks
     */
    @Before
    open fun setup() {
        // TODO: Initialize test objects
        // Example:
        // systemUnderTest = FeatureUnderTest()
        // testDataProvider = TestDataProvider()
    }

    /**
     * Cleanup method - runs after each test
     *
     * Use this to:
     * - Clean up resources
     * - Reset state
     * - Close connections
     */
    @After
    open fun tearDown() {
        // TODO: Clean up resources
    }

    // ========================================================================
    // Happy Path Tests (Normal Scenarios)
    // ========================================================================
    // These tests cover the main, expected use cases
    // They should verify that the feature works as intended

    /**
     * Happy Path Test Template
     *
     * Naming convention: `should {expected outcome} when {condition}`
     */
    @Test
    fun `should return expected result when given valid input`() {
        // Given (Arrange)
        // TODO: Set up test data
        val input = "valid input"
        val expected = "expected output"

        // When (Act)
        // TODO: Execute the function being tested
        val result = systemUnderTest.process(input)

        // Then (Assert)
        // TODO: Verify the result
        assertThat(result).isEqualTo(expected)
    }

    // ========================================================================
    // Edge Case Tests (Boundary Conditions)
    // ========================================================================
    // These tests cover edge cases and boundary conditions
    // Examples: empty strings, zero values, maximum values, etc.

    /**
     * Edge Case Test Template 1: Empty/Null Values
     */
    @Test
    fun `should handle empty input gracefully`() {
        // Given
        val input = ""

        // When
        val result = systemUnderTest.process(input)

        // Then
        assertThat(result).isNotNull() // Or specific behavior
    }

    /**
     * Edge Case Test Template 2: Boundary Values
     */
    @Test
    fun `should handle minimum boundary value correctly`() {
        // Given
        val input = Int.MIN_VALUE // or other boundary

        // When
        val result = systemUnderTest.process(input)

        // Then
        assertThat(result).isNotNull()
    }

    /**
     * Edge Case Test Template 3: Maximum Values
     */
    @Test
    fun `should handle maximum boundary value correctly`() {
        // Given
        val input = Int.MAX_VALUE // or other boundary

        // When
        val result = systemUnderTest.process(input)

        // Then
        assertThat(result).isNotNull()
    }

    // ========================================================================
    // Error Scenario Tests (Exception Handling)
    // ========================================================================
    // These tests verify that the feature handles errors correctly
    // They should throw expected exceptions with clear messages

    /**
     * Error Scenario Test Template
     */
    @Test
    fun `should throw exception when given invalid input`() {
        // Given
        val invalidInput = "invalid input"

        // When & Then
        assertThrows<IllegalArgumentException> {
            systemUnderTest.process(invalidInput)
        }.also { exception ->
            // Optionally verify exception message
            assertThat(exception.message).contains("Expected error message")
        }
    }

    /**
     * Null Safety Test (for Kotlin code)
     */
    @Test
    fun `should handle null input gracefully`() {
        // Given
        val input: String? = null

        // When
        val result = systemUnderTest.process(input)

        // Then
        assertThat(result).isNotNull()
    }

    // ========================================================================
    // Performance Tests (Optional)
    // ========================================================================
    // These tests verify performance characteristics
    // Use only for performance-critical features

    /**
     * Performance Test Template
     *
     * Note: This requires JUnit 4 or a timing library
     */
    @Test
    fun `should complete within acceptable time limit`() {
        // Given
        val input = generateLargeInput()

        // When
        val startTime = System.currentTimeMillis()
        val result = systemUnderTest.process(input)
        val duration = System.currentTimeMillis() - startTime

        // Then
        assertThat(result).isNotNull()
        assertThat(duration).isLessThan(1000) // 1 second max
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================
    // Add helper methods here to reduce code duplication

    /**
     * Helper: Generate test data
     */
    protected fun generateTestData(): String {
        return "test data"
    }

    /**
     * Helper: Generate large input for performance tests
     */
    protected fun generateLargeInput(): String {
        return "x".repeat(10000)
    }

    /**
     * Helper: Verify common invariants
     */
    protected fun <T : Any> verifyNotNull(value: T?, message: String = "Value should not be null") {
        assertThat(value).isNotNull() // Override message if needed
    }

    // ========================================================================
    // Example Usage
    // ========================================================================
    // Below is an example of how to use this template for a real feature

    /**
     * Example: StarRatingCalculator Test
     *
     * This shows how to extend the template for a specific feature
     */
    /*
    @RunWith(JUnit4::class)
    class StarRatingCalculatorTest : TddTestTemplate() {

        private lateinit var calculator: StarRatingCalculator

        @Before
        override fun setup() {
            super.setup()
            calculator = StarRatingCalculator()
        }

        @Test
        fun `should return 3 stars when accuracy is 100% and no hints used`() {
            // Given
            val correctAnswers = 6
            val totalQuestions = 6
            val hintsUsed = 0

            // When
            val stars = calculator.calculateStars(
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                hintsUsed = hintsUsed
            )

            // Then
            assertThat(stars).isEqualTo(3)
        }

        @Test
        fun `should return 2 stars when accuracy is 80%`() {
            // Given
            val correctAnswers = 4
            val totalQuestions = 5
            val hintsUsed = 0

            // When
            val stars = calculator.calculateStars(
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                hintsUsed = hintsUsed
            )

            // Then
            assertThat(stars).isEqualTo(2)
        }

        @Test
        fun `should deduct 1 star for each hint used`() {
            // Given
            val correctAnswers = 6
            val totalQuestions = 6
            val hintsUsed = 1

            // When
            val stars = calculator.calculateStars(
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                hintsUsed = hintsUsed
            )

            // Then
            assertThat(stars).isEqualTo(2) // 3 stars - 1 hint
        }

        @Test
        fun `should throw exception when correct answers exceed total questions`() {
            // Given
            val correctAnswers = 7
            val totalQuestions = 6
            val hintsUsed = 0

            // When & Then
            assertThrows<IllegalArgumentException> {
                calculator.calculateStars(
                    correctAnswers = correctAnswers,
                    totalQuestions = totalQuestions,
                    hintsUsed = hintsUsed
                )
            }
        }

        @Test
        fun `should handle zero correct answers`() {
            // Given
            val correctAnswers = 0
            val totalQuestions = 6
            val hintsUsed = 0

            // When
            val stars = calculator.calculateStars(
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                hintsUsed = hintsUsed
            )

            // Then
            assertThat(stars).isEqualTo(0)
        }
    }
    */
}

/**
 * Companion object for test constants and utilities
 */
object TddTestConstants {
    // Common test values
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val MAX_ITERATIONS = 10

    // Test data sizes
    const val SMALL_DATA_SIZE = 10
    const val MEDIUM_DATA_SIZE = 100
    const val LARGE_DATA_SIZE = 1000
}

/**
 * Test data provider helper
 */
class TestDataProvider {
    /**
     * Provide valid test data
     */
    fun provideValidData(): List<String> {
        return listOf(
            "test data 1",
            "test data 2",
            "test data 3"
        )
    }

    /**
     * Provide invalid test data
     */
    fun provideInvalidData(): List<String> {
        return listOf(
            "",
            " ",
            "\t",
            null.toString()
        )
    }

    /**
     * Provide edge case data
     */
    fun provideEdgeCaseData(): List<Any> {
        return listOf(
            Int.MIN_VALUE,
            Int.MAX_VALUE,
            Long.MIN_VALUE,
            Long.MAX_VALUE,
            Float.MIN_VALUE,
            Float.MAX_VALUE
        )
    }
}

/**
 * Performance measurement helper
 */
object PerformanceHelper {
    /**
     * Measure execution time of a block of code
     */
    inline fun <T> measureTime(block: () -> T): Pair<T, Long> {
        val startTime = System.nanoTime()
        val result = block()
        val duration = (System.nanoTime() - startTime) / 1_000_000 // Convert to ms
        return Pair(result, duration)
    }

    /**
     * Assert that execution time is within acceptable limit
     */
    inline fun <T> assertExecutesWithin(
        maxMs: Long,
        block: () -> T
    ): T {
        val (result, duration) = measureTime(block)
        assertThat(duration)
            .named("execution time")
            .isLessThan(maxMs)
        return result
    }
}
