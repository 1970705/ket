package com.wordland.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.MemoryMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark for memory leak detection and memory usage monitoring.
 *
 * Tests for potential memory leaks during:
 * - Screen navigation
 * - Animation playback
 * - View mode transitions
 * - Extended gameplay sessions
 *
 * Performance Targets:
 * - Memory growth rate: < 10MB/hour
 * - Peak memory: < 200MB
 * - No memory leaks detected
 *
 * Run with: ./gradlew :benchmark:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.wordland.benchmark.MemoryLeakBenchmark
 *
 * @author android-performance-expert
 * @since Sprint 1
 */
@RunWith(AndroidJUnit4::class)
class MemoryLeakBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Memory leak test: Navigation between screens.
     *
     * Tests for memory leaks when navigating between:
     * - Home Screen
     * - Island Map Screen
     * - Level Select Screen
     * - Learning Screen
     * - World Map Screen
     *
     * Target: No memory growth after multiple navigation cycles
     */
    @Test
    fun benchmarkNavigationMemoryLeaks() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                MemoryMetric(MemoryMetric.Mode.Avg),
                MemoryMetric(MemoryMetric.Mode.Max)
            ),
            iterations = 1,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Perform multiple navigation cycles
            repeat(5) {
                // Navigate to island map
                val islandButton = device.findObject(By.textContains("Look"))
                if (islandButton.exists()) {
                    islandButton.click()
                }
                device.waitForIdle()

                // Navigate back
                device.pressBack()
                device.waitForIdle()

                // Navigate to world map
                val worldButton = device.findObject(By.textContains("World"))
                if (worldButton.exists()) {
                    worldButton.click()
                }
                device.waitForIdle()

                // Navigate back
                device.pressBack()
                device.waitForIdle()

                // Navigate to progress screen
                val progressButton = device.findObject(By.textContains("Progress"))
                if (progressButton.exists()) {
                    progressButton.click()
                }
                device.waitForIdle()

                // Navigate back
                device.pressBack()
                device.waitForIdle()
            }
        }
    }

    /**
     * Memory leak test: Animation playback.
     *
     * Tests for memory leaks when playing animations repeatedly:
     * - Answer feedback animations
     * - Celebration animations
     * - Combo indicator animations
     * - Progress bar animations
     *
     * Target: No accumulation of animation resources
     */
    @Test
    fun benchmarkAnimationMemoryLeaks() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                MemoryMetric(MemoryMetric.Mode.Avg),
                MemoryMetric(MemoryMetric.Mode.Max)
            ),
            iterations = 1,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Navigate to level
            val islandButton = device.findObject(By.textContains("Look"))
            if (islandButton.exists()) {
                islandButton.click()
            }
            device.waitForIdle()

            val levelButton = device.findObject(By.text("Level 1"))
            if (levelButton.exists()) {
                levelButton.click()
            }
            device.waitForIdle()

            // Simulate multiple answer submissions (triggers animations)
            repeat(10) {
                // Type and submit answer
                typeAnswer("look")
                submitAnswer()

                Thread.sleep(500)

                // Click next
                val nextButton = device.findObject(By.text("Next"))
                if (nextButton.exists()) {
                    nextButton.click()
                }

                device.waitForIdle()
            }
        }
    }

    /**
     * Memory leak test: View mode transitions.
     *
     * Tests Story #2.1: World View Switching
     * Specifically tests for memory leaks during:
     * - Dual-view rendering during transition
     * - AnimatedVisibility enter/exit
     * - graphicsLayer transformations
     *
     * Target: No accumulation of transition resources
     */
    @Test
    fun benchmarkViewTransitionMemoryLeaks() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                MemoryMetric(MemoryMetric.Mode.Avg),
                MemoryMetric(MemoryMetric.Mode.Max)
            ),
            iterations = 1,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Navigate to world map
            val worldButton = device.findObject(By.textContains("World"))
            if (worldButton.exists()) {
                worldButton.click()
            }
            device.waitForIdle()

            // Rapid view switching
            val toggleButton = device.findObject(By.descContains("toggle view"))
            repeat(20) {
                if (toggleButton.exists()) {
                    toggleButton.click()
                    Thread.sleep(100)
                }
            }

            device.waitForIdle()
        }
    }

    /**
     * Memory stress test: Extended gameplay session.
     *
     * Simulates a 30-minute gameplay session compressed in time.
     * Tests for gradual memory accumulation.
     *
     * Target: < 10MB growth per hour equivalent
     */
    @Test
    fun benchmarkExtendedSessionMemory() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                MemoryMetric(MemoryMetric.Mode.Avg),
                MemoryMetric(MemoryMetric.Mode.Max)
            ),
            iterations = 1,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Extended navigation and interaction
            repeat(10) {
                // Navigate to island
                val islandButton = device.findObject(By.textContains("Look"))
                if (islandButton.exists()) {
                    islandButton.click()
                }
                device.waitForIdle()

                // Play through questions
                val levelButton = device.findObject(By.text("Level 1"))
                if (levelButton.exists()) {
                    levelButton.click()
                }
                device.waitForIdle()

                // Answer 5 questions
                repeat(5) {
                    typeAnswer("look")
                    submitAnswer()
                    Thread.sleep(300)

                    val nextButton = device.findObject(By.text("Next"))
                    if (nextButton.exists()) {
                        nextButton.click()
                    }
                    device.waitForIdle()
                }

                // Return to home
                device.pressBack()
                device.pressBack()
                device.waitForIdle()

                // Check progress
                val progressButton = device.findObject(By.textContains("Progress"))
                if (progressButton.exists()) {
                    progressButton.click()
                }
                device.waitForIdle()

                device.pressBack()
                device.waitForIdle()
            }
        }
    }

    /**
     * Memory leak test: Particle effects.
     *
     * Tests Story #1.3: Combo Visual Effects
     * Specifically tests confetti and particle cleanup.
     *
     * Target: All particle resources properly disposed
     */
    @Test
    fun benchmarkParticleEffectMemoryLeaks() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                MemoryMetric(MemoryMetric.Mode.Avg),
                MemoryMetric(MemoryMetric.Mode.Max)
            ),
            iterations = 1,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Navigate to level
            val islandButton = device.findObject(By.textContains("Look"))
            if (islandButton.exists()) {
                islandButton.click()
            }
            device.waitForIdle()

            val levelButton = device.findObject(By.text("Level 1"))
            if (levelButton.exists()) {
                levelButton.click()
            }
            device.waitForIdle()

            // Trigger multiple combo animations (particles)
            repeat(15) {
                typeAnswer("look")
                submitAnswer()

                // Wait for particle animation
                Thread.sleep(800)

                val nextButton = device.findObject(By.text("Next"))
                if (nextButton.exists()) {
                    nextButton.click()
                }
                device.waitForIdle()
            }
        }
    }

    /**
     * Memory leak test: Fog overlay rendering.
     *
     * Tests Story #2.2: Fog System Enhancement
     * Specifically tests fog texture and shader cleanup.
     *
     * Target: No accumulation of fog rendering resources
     */
    @Test
    fun benchmarkFogRenderingMemoryLeaks() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                MemoryMetric(MemoryMetric.Mode.Avg),
                MemoryMetric(MemoryMetric.Mode.Max)
            ),
            iterations = 1,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Navigate to world map
            val worldButton = device.findObject(By.textContains("World"))
            if (worldButton.exists()) {
                worldButton.click()
            }
            device.waitForIdle()

            // Pan extensively through fog
            val mapContainer = device.findObject(By.res("com.wordland:id/world_map_container"))
            if (mapContainer.exists()) {
                // Pan in all directions to encounter different fog levels
                repeat(30) {
                    when (it % 4) {
                        0 -> mapContainer.drag(androidx.test.uiautomator.Direction.DOWN, 200f)
                        1 -> mapContainer.drag(androidx.test.uiautomator.Direction.UP, 200f)
                        2 -> mapContainer.drag(androidx.test.uiautomator.Direction.LEFT, 200f)
                        3 -> mapContainer.drag(androidx.test.uiautomator.Direction.RIGHT, 200f)
                    }
                    Thread.sleep(50)
                }
            }

            device.waitForIdle()

            // Toggle view modes
            val toggleButton = device.findObject(By.descContains("toggle view"))
            repeat(10) {
                if (toggleButton.exists()) {
                    toggleButton.click()
                    Thread.sleep(100)
                }
            }

            device.waitForIdle()
        }
    }

    // Helper functions

    private fun typeAnswer(answer: String) {
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
