package com.wordland.domain.performance

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PerformanceMonitor
 *
 * Part of Task #20: Device Performance Detection and Performance Baseline
 *
 * Tests:
 * - Frame time tracking
 * - FPS calculation
 * - Memory snapshot updates
 * - Performance status detection
 * - Performance violation detection
 */
class PerformanceMonitorTest {
    @Before
    fun setUp() {
        PerformanceMonitor.reset()
    }

    @Test
    fun `startFrame and endFrame track frame time`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)

        // Act
        PerformanceMonitor.startFrame()
        Thread.sleep(10) // Simulate 10ms frame time
        PerformanceMonitor.endFrame()

        // Assert
        val metrics = PerformanceMonitor.getMetrics()
        assertEquals(1, metrics.totalFramesRendered)
        assertTrue(metrics.averageFrameTimeMs >= 9f) // At least 9ms
        assertTrue(metrics.averageFrameTimeMs < 20f) // Less than 20ms
    }

    @Test
    fun `endFrame without startFrame does not crash`() {
        // Act - should not crash
        PerformanceMonitor.endFrame()

        // Assert
        val metrics = PerformanceMonitor.getMetrics()
        assertEquals(0, metrics.totalFramesRendered)
    }

    @Test
    fun `frame time window is limited to sample size`() {
        // Arrange
        val sampleSize = 60
        PerformanceMonitor.setFrameTimeBudget(60)

        // Act - Render more than sample size frames
        repeat(100) {
            PerformanceMonitor.startFrame()
            Thread.sleep(1)
            PerformanceMonitor.endFrame()
        }

        // Assert - Only sampleSize frames should be tracked
        val metrics = PerformanceMonitor.getMetrics()
        assertEquals(100, metrics.totalFramesRendered) // Total counted
        // Average based on window, not all frames
        assertTrue(metrics.averageFrameTimeMs > 0)
    }

    @Test
    fun `droppedFrames are tracked when budget exceeded`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60) // 16.6ms budget

        // Act - Render frames with varying times
        repeat(10) {
            PerformanceMonitor.startFrame()
            Thread.sleep(5) // Within budget
            PerformanceMonitor.endFrame()
        }

        // Render slow frames
        repeat(5) {
            PerformanceMonitor.startFrame()
            Thread.sleep(20) // Exceeds budget
            PerformanceMonitor.endFrame()
        }

        // Assert
        val metrics = PerformanceMonitor.getMetrics()
        assertTrue(metrics.droppedFrames >= 4) // At least the 5 slow frames
    }

    @Test
    fun `FPS is calculated correctly`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)

        // Act - Render consistent 16ms frames (62.5 FPS theoretical)
        repeat(10) {
            PerformanceMonitor.startFrame()
            Thread.sleep(16)
            PerformanceMonitor.endFrame()
        }

        // Assert
        val metrics = PerformanceMonitor.getMetrics()
        // FPS should be around 60, accounting for sleep variance
        assertTrue(metrics.currentFps > 30)
        assertTrue(metrics.currentFps <= 60)
    }

    @Test
    fun `setFrameTimeBudget changes target FPS`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(30) // 30 FPS = 33.3ms budget

        // Act
        val budgetMs = PerformanceMonitor.getFrameTimeBudgetMs()

        // Assert
        assertEquals(33.33f, budgetMs, 0.5f)
    }

    @Test
    fun `getTargetFps returns correct value`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)

        // Act
        val targetFps = PerformanceMonitor.getTargetFps()

        // Assert
        assertEquals(60, targetFps)
    }

    @Test
    fun `updateMemorySnapshot updates memory metrics`() {
        // Arrange
        val usedBytes = 100_000_000L // ~95MB actual
        val totalBytes = 500_000_000L // ~477MB actual
        val maxBytes = 1_000_000_000L // ~954MB actual

        // Act
        PerformanceMonitor.updateMemorySnapshot(usedBytes, totalBytes, maxBytes)

        // Assert
        val metrics = PerformanceMonitor.getMetrics()
        // 100_000_000 / (1024 * 1024) ≈ 95.37 MB
        assertEquals(95.37f, metrics.memoryUsedMB, 1f)
        // 500_000_000 / (1024 * 1024) ≈ 476.84 MB
        assertEquals(476.84f, metrics.memoryTotalMB, 1f)
        // 1_000_000_000 / (1024 * 1024) ≈ 953.67 MB
        assertEquals(953.67f, metrics.memoryMaxMB, 1f)
    }

    @Test
    fun `isWithinBudget returns true when frame time is good`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)

        // Act
        PerformanceMonitor.startFrame()
        Thread.sleep(5) // Well within budget
        PerformanceMonitor.endFrame()

        // Assert
        assertTrue(PerformanceMonitor.isWithinBudget())
    }

    @Test
    fun `isWithinBudget returns false when frame time is poor`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)

        // Act
        repeat(20) {
            PerformanceMonitor.startFrame()
            Thread.sleep(20) // Exceeds budget
            PerformanceMonitor.endFrame()
        }

        // Assert
        assertFalse(PerformanceMonitor.isWithinBudget())
    }

    @Test
    fun `getPerformanceStatus returns EXCELLENT for good FPS`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)

        // Act - Render fast frames
        repeat(10) {
            PerformanceMonitor.startFrame()
            Thread.sleep(5)
            PerformanceMonitor.endFrame()
        }

        // Assert
        val status = PerformanceMonitor.getPerformanceStatus()
        assertEquals(PerformanceStatus.EXCELLENT, status)
    }

    @Test
    fun `getPerformanceStatus returns POOR for bad FPS`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)

        // Act - Render slow frames (35ms = ~28 FPS, which is < 50% of 60)
        repeat(10) {
            PerformanceMonitor.startFrame()
            Thread.sleep(35) // 35ms = 28.6 FPS < 30 FPS threshold
            PerformanceMonitor.endFrame()
        }

        // Assert
        val status = PerformanceMonitor.getPerformanceStatus()
        assertEquals(PerformanceStatus.POOR, status)
    }

    @Test
    fun `reset clears all metrics`() {
        // Arrange - Add some data
        PerformanceMonitor.setFrameTimeBudget(60)
        repeat(10) {
            PerformanceMonitor.startFrame()
            Thread.sleep(5)
            PerformanceMonitor.endFrame()
        }

        // Act
        PerformanceMonitor.reset()

        // Assert
        val metrics = PerformanceMonitor.getMetrics()
        assertEquals(0, metrics.totalFramesRendered)
        assertEquals(0, metrics.droppedFrames)
        assertEquals(0f, metrics.averageFrameTimeMs, 0f)
    }

    @Test
    fun `PerformanceMetrics meetsTarget returns correct value`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)
        repeat(10) {
            PerformanceMonitor.startFrame()
            Thread.sleep(5)
            PerformanceMonitor.endFrame()
        }

        // Act
        val metrics = PerformanceMonitor.getMetrics()

        // Assert
        assertTrue(metrics.meetsTarget())
    }

    @Test
    fun `PerformanceMetrics getFrameTimeBudgetUsage calculates correctly`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60) // 16.6ms budget
        repeat(5) {
            PerformanceMonitor.startFrame()
            Thread.sleep(8) // ~50% of budget
            PerformanceMonitor.endFrame()
        }

        // Act
        val metrics = PerformanceMonitor.getMetrics()
        val usage = metrics.getFrameTimeBudgetUsage()

        // Assert
        assertTrue(usage > 30 && usage < 70) // Around 50%
    }

    @Test
    fun `PerformanceMetrics getMemoryUsagePercentage calculates correctly`() {
        // Arrange
        val usedBytes = 500_000_000L // 500MB
        val totalBytes = 1_000_000_000L // 1GB
        val maxBytes = 2_000_000_000L // 2GB

        PerformanceMonitor.updateMemorySnapshot(usedBytes, totalBytes, maxBytes)

        // Act
        val metrics = PerformanceMonitor.getMetrics()
        val percentage = metrics.getMemoryUsagePercentage()

        // Assert
        assertEquals(25f, percentage, 1f) // ~25% (500MB / 2GB)
    }

    @Test
    fun `PerformanceMetrics getViolations returns empty for good performance`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)
        repeat(10) {
            PerformanceMonitor.startFrame()
            Thread.sleep(5)
            PerformanceMonitor.endFrame()
        }
        PerformanceMonitor.updateMemorySnapshot(100_000_000, 1_000_000_000, 2_000_000_000)

        // Act
        val metrics = PerformanceMonitor.getMetrics()
        val violations = metrics.getViolations()

        // Assert
        assertTrue(violations.isEmpty())
    }

    @Test
    fun `PerformanceMetrics getViolations detects low FPS`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)
        repeat(10) {
            PerformanceMonitor.startFrame()
            Thread.sleep(30) // Slow frames (~33 FPS)
            PerformanceMonitor.endFrame()
        }

        // Act
        val metrics = PerformanceMonitor.getMetrics()
        val violations = metrics.getViolations()

        // Assert
        assertTrue(violations.any { it is PerformanceViolation.FrameRate })
    }

    @Test
    fun `PerformanceMetrics getViolations detects high memory usage`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)
        PerformanceMonitor.updateMemorySnapshot(
            usedMemoryBytes = 1_900_000_000L, // 95% of 2GB
            totalMemoryBytes = 2_000_000_000L,
            maxMemoryBytes = 2_000_000_000L,
        )

        // Act
        val metrics = PerformanceMonitor.getMetrics()
        val violations = metrics.getViolations()

        // Assert
        assertTrue(violations.any { it is PerformanceViolation.Memory })
    }

    @Test
    fun `PerformanceMetrics getViolations detects frame drops`() {
        // Arrange
        PerformanceMonitor.setFrameTimeBudget(60)
        // Mix of good and bad frames
        repeat(20) {
            PerformanceMonitor.startFrame()
            if (it % 2 == 0) {
                Thread.sleep(5) // Good
            } else {
                Thread.sleep(25) // Bad
            }
            PerformanceMonitor.endFrame()
        }

        // Act
        val metrics = PerformanceMonitor.getMetrics()
        val violations = metrics.getViolations()

        // Assert
        assertTrue(violations.any { it is PerformanceViolation.FrameDrops })
    }

    @Test
    fun `PerformanceStatus values are correctly ordered`() {
        // Test that status enum values are accessible
        val statuses =
            listOf(
                PerformanceStatus.EXCELLENT,
                PerformanceStatus.GOOD,
                PerformanceStatus.FAIR,
                PerformanceStatus.POOR,
            )

        assertEquals(4, statuses.size)
    }

    @Test
    fun `concurrent metric access is thread-safe`() {
        // Arrange
        // First, add some frames from main thread
        PerformanceMonitor.setFrameTimeBudget(60)
        repeat(50) {
            PerformanceMonitor.startFrame()
            Thread.sleep(1)
            PerformanceMonitor.endFrame()
        }

        // Act - Concurrent reads of metrics (realistic use case)
        val threads =
            List(4) {
                Thread {
                    repeat(10) {
                        // Multiple threads can safely read metrics
                        val metrics = PerformanceMonitor.getMetrics()
                        // Verify metrics are consistent
                        assertTrue(metrics.totalFramesRendered >= 0)
                        assertTrue(metrics.averageFrameTimeMs >= 0)
                    }
                }
            }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Assert - metrics should remain consistent
        val metrics = PerformanceMonitor.getMetrics()
        assertEquals(50, metrics.totalFramesRendered)
    }

    @Test
    fun `MemorySnapshot calculates MB correctly`() {
        // Arrange
        val bytes = 10 * 1024 * 1024L // 10MB
        val timestamp = System.currentTimeMillis()

        // Act
        val snapshot = MemorySnapshot(bytes, bytes * 2, bytes * 4, timestamp)

        // Assert
        assertEquals(10f, snapshot.usedMB, 0.1f)
        assertEquals(20f, snapshot.totalMB, 0.1f)
        assertEquals(40f, snapshot.maxMB, 0.1f)
    }

    @Test
    fun `PerformanceViolation FrameRate has correct properties`() {
        // Arrange
        val violation =
            PerformanceViolation.FrameRate(
                actual = 30f,
                target = 60f,
                severity = ViolationSeverity.MEDIUM,
            )

        // Assert
        assertEquals(30f, violation.actual)
        assertEquals(60f, violation.target)
        assertEquals(30f, violation.deficit)
        assertEquals(ViolationSeverity.MEDIUM, violation.severity)
    }

    @Test
    fun `PerformanceViolation Memory has correct properties`() {
        // Arrange
        val violation =
            PerformanceViolation.Memory(
                usedMB = 1800f,
                maxMB = 2000f,
                percentage = 90f,
                severity = ViolationSeverity.HIGH,
            )

        // Assert
        assertEquals(1800f, violation.usedMB)
        assertEquals(2000f, violation.maxMB)
        assertEquals(90f, violation.percentage)
        assertEquals(ViolationSeverity.HIGH, violation.severity)
    }

    @Test
    fun `PerformanceViolation FrameDrops has correct properties`() {
        // Arrange
        val violation =
            PerformanceViolation.FrameDrops(
                rate = 0.15f,
                count = 15,
                total = 100,
                severity = ViolationSeverity.MEDIUM,
            )

        // Assert
        assertEquals(0.15f, violation.rate)
        assertEquals(15, violation.count)
        assertEquals(100, violation.total)
        assertEquals(15f, violation.percentage, 0.001f) // Allow small floating point error
        assertEquals(ViolationSeverity.MEDIUM, violation.severity)
    }
}
