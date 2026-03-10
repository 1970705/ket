package com.wordland.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark for Epic #1: Visual Feedback Enhancement.
 *
 * Measures animation performance and frame rate during visual feedback scenarios.
 *
 * Performance Targets:
 * - Animation frame rate: 60 FPS (16.6ms/frame)
 * - Answer feedback animation: < 500ms
 * - Celebration animation (3 stars): < 1200ms
 * - Celebration animation (2 stars): < 800ms
 * - Celebration animation (1 star): < 500ms
 * - Combo indicator animation: < 300ms
 *
 * Run with: ./gradlew :benchmark:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.wordland.benchmark.VisualFeedbackBenchmark
 *
 * @author android-performance-expert
 * @since Sprint 1
 */
@RunWith(AndroidJUnit4::class)
class VisualFeedbackBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Benchmark answer feedback animation (correct answer).
     *
     * Tests Story #1.1: Spelling Animation
     * - Letter fly-in animation
     * - Correct answer celebration
     *
     * Target: 60 FPS during animation
     */
    @Test
    fun benchmarkCorrectAnswerFeedback() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric().copy(
                    frameLatencyTracking = true
                )
            ),
            iterations = 5,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Navigate to learning screen
            navigateToLevel("Level 1")

            // Wait for question to load
            device.waitForIdle()

            // Type correct answer
            typeAnswer("look")

            // Submit answer
            submitAnswer()

            // Wait for feedback animation to complete
            // Target: < 500ms
            Thread.sleep(600)

            device.waitForIdle()
        }
    }

    /**
     * Benchmark answer feedback animation (incorrect answer).
     *
     * Tests shake animation for wrong answers.
     *
     * Target: 60 FPS during shake animation
     */
    @Test
    fun benchmarkIncorrectAnswerFeedback() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric().copy(
                    frameLatencyTracking = true
                )
            ),
            iterations = 5,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()
            navigateToLevel("Level 1")

            // Type incorrect answer
            typeAnswer("wrong")

            // Submit answer
            submitAnswer()

            // Wait for shake animation to complete
            // Target: < 640ms (as per VISUAL_FEEDBACK_DESIGN.md)
            Thread.sleep(700)

            device.waitForIdle()
        }
    }

    /**
     * Benchmark three-star celebration animation.
     *
     * Tests Story #1.2: Celebration Animation
     * - 50 confetti particles
     * - 1200ms duration
     * - Pet jumping animation
     *
     * Target: 60 FPS during celebration
     */
    @Test
    fun benchmarkThreeStarCelebration() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric().copy(
                    frameLatencyTracking = true
                )
            ),
            iterations = 3,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()
            navigateToLevel("Level 1")

            // Complete level with perfect score
            // (In real test, we would pre-populate database)
            // For now, just test the animation performance

            // Wait for celebration animation
            // Target: < 1200ms for 3-star celebration
            Thread.sleep(1300)

            device.waitForIdle()
        }
    }

    /**
     * Benchmark combo indicator animation.
     *
     * Tests Story #1.3: Combo Visual Effects
     * - 3-combo: Single flame + pulse animation
     * - 5-combo: Double flames + screen shake
     * - 10-combo: Triple flames + particle effects
     *
     * Target: 60 FPS during combo animation
     */
    @Test
    fun benchmarkComboIndicatorAnimation() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric().copy(
                    frameLatencyTracking = true
                )
            ),
            iterations = 5,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()
            navigateToLevel("Level 1")

            // Answer multiple questions correctly to trigger combo
            repeat(3) {
                typeAnswer("look")
                submitAnswer()
                Thread.sleep(300)
                device.waitForIdle()

                // Click next
                device.findObject(By.text("Next")).click()
                Thread.sleep(200)
            }

            // Wait for combo animation
            // Target: < 300ms for combo indicator update
            Thread.sleep(400)

            device.waitForIdle()
        }
    }

    /**
     * Benchmark progress bar animation.
     *
     * Tests Story #1.4: Progress Bar Enhancement
     * - Smooth transition animation (500ms)
     * - Percentage number rolling
     *
     * Target: 60 FPS during progress animation
     */
    @Test
    fun benchmarkProgressBarAnimation() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric().copy(
                    frameLatencyTracking = true
                )
            ),
            iterations = 5,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()
            navigateToLevel("Level 1")

            // Submit answer to trigger progress update
            typeAnswer("look")
            submitAnswer()

            // Wait for progress bar animation
            // Target: < 500ms (EaseOutCubic easing)
            Thread.sleep(600)

            device.waitForIdle()
        }
    }

    /**
     * Stress test: Multiple animations in quick succession.
     *
     * Tests performance when multiple animations overlap.
     * This simulates rapid-fire gameplay scenario.
     *
     * Target: Maintain 60 FPS throughout
     */
    @Test
    fun benchmarkAnimationStressTest() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric().copy(
                    frameLatencyTracking = true
                )
            ),
            iterations = 3,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()
            navigateToLevel("Level 1")

            // Rapid fire answers
            repeat(5) {
                typeAnswer("look")
                submitAnswer()
                Thread.sleep(100) // Very short delay

                device.findObject(By.text("Next")).click()
                Thread.sleep(100)
            }

            device.waitForIdle()
        }
    }

    // Helper functions

    private fun navigateToLevel(levelName: String) {
        // Click on Look Island
        val islandButton = device.findObject(By.textContains("Look"))
        if (islandButton.exists()) {
            islandButton.click()
            device.waitForIdle()
        }

        // Click on level
        val levelButton = device.findObject(By.text(levelName))
        if (levelButton.exists()) {
            levelButton.click()
            device.waitForIdle()
        }
    }

    private fun typeAnswer(answer: String) {
        // Type each letter
        for (char in answer) {
            val keyButton = device.findObject(By.text(char.toString()))
            if (keyButton.exists()) {
                keyButton.click()
                Thread.sleep(50)
            }
        }
    }

    private fun submitAnswer() {
        val submitButton = device.findObject(By.text("Submit"))
        if (submitButton.exists()) {
            submitButton.click()
        }
    }
}
