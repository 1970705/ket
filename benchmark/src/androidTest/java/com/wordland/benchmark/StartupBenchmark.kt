package com.wordland.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark for application startup performance.
 *
 * Measures:
 * - Cold start time (target: < 3s)
 * - Warm start time (target: < 1s)
 * - First frame rendering
 * - Time to interactive
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Cold startup benchmark
     * Simulates first launch after device reboot
     */
    @Test
    fun benchmarkColdStart() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                androidx.benchmark.macro.StartupTimingMetric(),
                androidx.benchmark.macro.FrameTimingMetric()
            ),
            iterations = 10,
            startupMode = StartupMode.COLD,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            // Wait for UI to settle
            device.waitForIdle()

            // Report first frame timing
            // (automatically captured by StartupTimingMetric)
        }
    }

    /**
     * Warm startup benchmark
     * Simulates relaunching app from background
     */
    @Test
    fun benchmarkWarmStart() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                androidx.benchmark.macro.StartupTimingMetric(),
                androidx.benchmark.macro.FrameTimingMetric()
            ),
            iterations = 10,
            startupMode = StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()
        }
    }

    /**
     * Hot startup benchmark
     * Simulates returning to app from recent apps
     */
    @Test
    fun benchmarkHotStart() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                androidx.benchmark.macro.StartupTimingMetric()
            ),
            iterations = 10,
            startupMode = StartupMode.HOT,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()
        }
    }

    /**
     * Time to first level benchmark
     * Measures time from launch to starting first level
     */
    @Test
    fun benchmarkTimeToFirstLevel() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                androidx.benchmark.macro.StartupTimingMetric(),
                androidx.benchmark.macro.FrameTimingMetric()
            ),
            iterations = 5,
            startupMode = StartupMode.COLD
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Navigate to island
            val islandButton = device.findObject(By.textContains("Look Island"))
            islandButton.click()

            device.waitForIdle()

            // Navigate to level
            val levelButton = device.findObject(By.textContains("Level 1"))
            levelButton.click()

            device.waitForIdle()

            // Wait for level screen to load
            device.wait(
                android.system.Clock.elapsedRealtime() + 5000
            )
        }
    }
}

/**
 * Helper extension for UiDevice
 */
private fun UiDevice.wait(condition: Long) {
    // Wait helper implementation
    android.os.SystemClock.sleep(condition - android.system.Clock.elapsedRealtime())
}
