package com.wordland.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Macrobenchmark for Epic #2: Map System Reconstruction.
 *
 * Measures map rendering, view transitions, and interaction performance.
 *
 * Performance Targets:
 * - View transition animation: 60 FPS (500ms duration)
 * - Map zoom/pan operations: ≥55 FPS
 * - Fog rendering: < 50ms per frame
 * - Player ship animation: 60 FPS
 * - Memory growth during navigation: < 10MB
 *
 * Run with: ./gradlew :benchmark:connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.wordland.benchmark.MapSystemBenchmark
 *
 * @author android-performance-expert
 * @since Sprint 1
 */
@RunWith(AndroidJUnit4::class)
class MapSystemBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Benchmark world view to island view transition.
     *
     * Tests Story #2.1: World View Switching
     * - AnimatedVisibility with fade + slide
     * - 500ms transition duration
     * - FastOutSlowInEasing
     *
     * Target: 60 FPS during transition
     */
    @Test
    fun benchmarkWorldToIslandViewTransition() {
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

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
                device.waitForIdle()
            }

            // Find and click view toggle button
            val toggleButton = device.findObject(By.descContains("toggle view"))
            if (toggleButton.exists()) {
                toggleButton.click()

                // Wait for transition animation
                // Target: 500ms with 60 FPS
                Thread.sleep(600)
            }

            device.waitForIdle()
        }
    }

    /**
     * Benchmark island view to world view transition.
     *
     * Target: 60 FPS during transition
     */
    @Test
    fun benchmarkIslandToWorldViewTransition() {
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

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
            }

            device.waitForIdle()

            // Toggle back to world view
            val toggleButton = device.findObject(By.descContains("toggle view"))
            if (toggleButton.exists()) {
                toggleButton.click()
                Thread.sleep(600)
            }

            device.waitForIdle()
        }
    }

    /**
     * Benchmark rapid view switching.
     *
     * Tests performance when user rapidly toggles between views.
     * This stress tests the dual-view rendering during transition.
     *
     * Target: Maintain ≥55 FPS even during rapid switching
     */
    @Test
    fun benchmarkRapidViewSwitching() {
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

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
            }

            device.waitForIdle()

            // Rapid toggle 5 times
            val toggleButton = device.findObject(By.descContains("toggle view"))
            repeat(5) {
                if (toggleButton.exists()) {
                    toggleButton.click()
                    Thread.sleep(300) // Less than transition duration
                }
            }

            device.waitForIdle()
        }
    }

    /**
     * Benchmark map zoom operation.
     *
     * Tests Story #2.2: Fog System Enhancement
     * - Pinch-to-zoom performance
     * - Fog rendering at different zoom levels
     *
     * Target: ≥55 FPS during zoom
     */
    @Test
    fun benchmarkMapZoom() {
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

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
            }

            device.waitForIdle()

            // Perform pinch zoom
            val mapContainer = device.findObject(By.res("com.wordland:id/world_map_container"))
            if (mapContainer.exists()) {
                // Pinch out to zoom in
                mapContainer.pinchOut(
                    percent = 50f,
                    speed = 500
                )
                Thread.sleep(500)

                // Pinch in to zoom out
                mapContainer.pinchIn(
                    percent = 50f,
                    speed = 500
                )
            }

            device.waitForIdle()
        }
    }

    /**
     * Benchmark map pan operation.
     *
     * Tests scrolling/panning performance with fog overlay.
     *
     * Target: ≥55 FPS during pan
     */
    @Test
    fun benchmarkMapPan() {
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

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
            }

            device.waitForIdle()

            // Perform pan gestures in 4 directions
            val mapContainer = device.findObject(By.res("com.wordland:id/world_map_container"))
            if (mapContainer.exists()) {
                // Pan down
                mapContainer.drag(Direction.DOWN, 500f)
                Thread.sleep(200)

                // Pan up
                mapContainer.drag(Direction.UP, 500f)
                Thread.sleep(200)

                // Pan right
                mapContainer.drag(Direction.RIGHT, 500f)
                Thread.sleep(200)

                // Pan left
                mapContainer.drag(Direction.LEFT, 500f)
            }

            device.waitForIdle()
        }
    }

    /**
     * Benchmark fog rendering at different visibility levels.
     *
     * Tests Story #2.2: Fog Overlay Performance
     * - VISIBLE: No fog
     * - PARTIAL: Semi-transparent fog
     * - HIDDEN: Heavy fog
     * - LOCKED: Dark fog with lock icon
     *
     * Target: < 50ms per frame for fog rendering
     */
    @Test
    fun benchmarkFogRendering() {
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

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
            }

            device.waitForIdle()

            // Pan through map to encounter different fog levels
            val mapContainer = device.findObject(By.res("com.wordland:id/world_map_container"))
            if (mapContainer.exists()) {
                // Pan across the map
                repeat(3) {
                    mapContainer.drag(Direction.DOWN, 300f)
                    Thread.sleep(100)
                    mapContainer.drag(Direction.RIGHT, 300f)
                    Thread.sleep(100)
                }
            }

            device.waitForIdle()
        }
    }

    /**
     * Benchmark player ship animation.
     *
     * Tests Story #2.3: Player Ship Display
     * - Ship movement animation
     * - Position update during navigation
     *
     * Target: 60 FPS for ship animation
     */
    @Test
    fun benchmarkPlayerShipAnimation() {
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

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
            }

            device.waitForIdle()

            // Tap on a region to trigger ship movement
            val region = device.findObject(By.textContains("Look"))
            if (region.exists()) {
                region.click()

                // Wait for ship animation
                // Target: animateFloatAsState with spring spec
                Thread.sleep(600)
            }

            device.waitForIdle()
        }
    }

    /**
     * Benchmark region unlock animation.
     *
     * Tests Story #2.4: Region Unlock Logic
     * - Unlock notification animation
     * - Fog fade-out animation
     *
     * Target: 60 FPS during unlock animation
     */
    @Test
    fun benchmarkRegionUnlockAnimation() {
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

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
            }

            device.waitForIdle()

            // Note: In real test, we would pre-populate database
            // with progress that triggers unlock
            // For now, test the general map navigation performance

            // Pan to newly unlocked region
            val mapContainer = device.findObject(By.res("com.wordland:id/world_map_container"))
            if (mapContainer.exists()) {
                mapContainer.drag(Direction.DOWN, 500f)
                Thread.sleep(500)
            }

            device.waitForIdle()
        }
    }

    /**
     * Memory stress test: Extended map navigation.
     *
     * Tests memory growth during extended map usage.
     * Monitors for potential memory leaks in map rendering.
     *
     * Target: < 10MB memory growth
     */
    @Test
    fun benchmarkMapMemoryStress() {
        benchmarkRule.measureRepeated(
            packageName = "com.wordland",
            metrics = listOf(
                androidx.benchmark.macro.TraceSectionMetric("MemoryStressTest")
            ),
            iterations = 1,
            startupMode = androidx.benchmark.macro.StartupMode.WARM,
            compilationMode = CompilationMode.Full()
        ) {
            pressHome()
            startActivityAndWait()

            device.waitForIdle()

            // Navigate to world map
            val worldMapButton = device.findObject(By.textContains("World"))
            if (worldMapButton.exists()) {
                worldMapButton.click()
            }

            device.waitForIdle()

            // Perform extensive navigation
            val mapContainer = device.findObject(By.res("com.wordland:id/world_map_container"))
            val toggleButton = device.findObject(By.descContains("toggle view"))

            if (mapContainer.exists()) {
                // 20 iterations of various operations
                repeat(20) {
                    when (it % 4) {
                        0 -> mapContainer.drag(Direction.DOWN, 200f)
                        1 -> mapContainer.drag(Direction.UP, 200f)
                        2 -> mapContainer.drag(Direction.LEFT, 200f)
                        3 -> {
                            if (toggleButton.exists()) {
                                toggleButton.click()
                            }
                        }
                    }
                    Thread.sleep(100)
                }
            }

            device.waitForIdle()
        }
    }
}
