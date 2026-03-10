package com.wordland.domain.algorithm

/**
 * Debug helper to verify star rating calculations
 */
object StarRatingCalculatorDebug {
    @JvmStatic
    fun main(args: Array<String>) {
        val standardWords = 6
        val standardTimeMs = 30000L

        // Test Scenario 1: Perfect performance
        val data1 =
            StarRatingCalculator.PerformanceData(
                totalWords = standardWords,
                correctAnswers = 6,
                hintsUsed = 0,
                totalTimeMs = 24000L,
                wrongAnswers = 0,
            )
        println("=== Scenario 1: Perfect Performance ===")
        println(StarRatingCalculator.getScoringBreakdown(data1))
        println()

        // Test Scenario 2: Good with hints
        val data2 =
            StarRatingCalculator.PerformanceData(
                totalWords = standardWords,
                correctAnswers = 5,
                hintsUsed = 2,
                totalTimeMs = standardTimeMs,
                wrongAnswers = 1,
            )
        println("=== Scenario 2: Good with Hints ===")
        println(StarRatingCalculator.getScoringBreakdown(data2))
        println()

        // Test Scenario 3: Passing with errors
        val data3 =
            StarRatingCalculator.PerformanceData(
                totalWords = standardWords,
                correctAnswers = 3,
                hintsUsed = 3,
                totalTimeMs = standardTimeMs,
                wrongAnswers = 3,
            )
        println("=== Scenario 3: Passing with Errors ===")
        println(StarRatingCalculator.getScoringBreakdown(data3))
        println()

        // Test Scenario 4: Failing
        val data4 =
            StarRatingCalculator.PerformanceData(
                totalWords = standardWords,
                correctAnswers = 0,
                hintsUsed = 0,
                totalTimeMs = standardTimeMs,
                wrongAnswers = 6,
            )
        println("=== Scenario 4: Failing ===")
        println(StarRatingCalculator.getScoringBreakdown(data4))
        println()
    }
}
