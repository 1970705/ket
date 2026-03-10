package com.wordland.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark for gameplay performance.
 *
 * Measures:
 * - Frame rate during gameplay (target: stable 60fps)
 * - Frame time consistency
 * - Jank percentage
 */
@RunWith(AndroidJUnit4::class)
class GameplayBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Benchmark virtual keyboard typing performance
     */
    @Test
    fun benchmarkVirtualKeyboardTyping() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric()
            ),
            iterations = 5,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            // Navigate to a level
            device.findObject(By.textContains("Look Island")).click()
            device.waitForIdle()
            device.findObject(By.textContains("Level 1")).click()
            device.waitForIdle()

            // Simulate typing on virtual keyboard
            repeat(10) {
                val keys = listOf("Q", "W", "E", "R", "T", "Y")
                keys.forEach { key ->
                    device.findObject(By.text(key)).click()
                    android.os.SystemClock.sleep(100) // Natural typing delay
                }

                // Press backspace
                device.findObject(By.text("⌫")).click()
                android.os.SystemClock.sleep(100)
            }
        }
    }

    /**
     * Benchmark level navigation and scrolling
     */
    @Test
    fun benchmarkLevelNavigation() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric()
            ),
            iterations = 5,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Navigate through islands
            device.findObject(By.textContains("Look Island")).click()
            device.waitForIdle()

            // Simulate scrolling through levels
            val scrollable = device.findObject(By.scrollable(true))
            scrollable.setGestureMargin(device.displayWidth / 5)

            // Scroll down
            scrollable.drag(
                android.graphics.Point(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4
                ),
                android.graphics.Point(
                    device.displayWidth / 2,
                    device.displayHeight / 4
                ),
                400 // Duration in ms
            )

            device.waitForIdle()

            // Scroll up
            scrollable.drag(
                android.graphics.Point(
                    device.displayWidth / 2,
                    device.displayHeight / 4
                ),
                android.graphics.Point(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4
                ),
                400
            )

            device.waitForIdle()
        }
    }

    /**
     * Benchmark answer validation and feedback animation
     */
    @Test
    fun benchmarkAnswerFeedback() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                FrameTimingMetric()
            ),
            iterations = 5,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            // Navigate to level
            device.findObject(By.textContains("Look Island")).click()
            device.waitForIdle()
            device.findObject(By.textContains("Level 1")).click()
            device.waitForIdle()

            // Type correct answer
            val word = "look"
            word.forEach { char ->
                device.findObject(By.text(char.toString().uppercase())).click()
                android.os.SystemClock.sleep(100)
            }

            // Submit answer
            device.findObject(By.text("提交")).click()
            device.waitForIdle()

            // Wait for feedback animation
            android.os.SystemClock.sleep(1000)

            // Continue to next word
            device.findObject(By.text("继续")).click()
            device.waitForIdle()
        }
    }
}
