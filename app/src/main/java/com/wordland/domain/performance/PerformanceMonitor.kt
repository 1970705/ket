package com.wordland.domain.performance

/**
 * Real-time performance monitoring for the application
 *
 * Part of Task #20: Device Performance Detection and Performance Baseline
 *
 * This class provides:
 * - Frame time tracking
 * - FPS calculation
 * - Memory usage monitoring
 * - Performance violation detection
 *
 * Architecture Notes:
 * - Follows existing algorithm pattern (MemoryStrengthAlgorithm)
 * - Pure Kotlin with no Android framework dependencies
 * - Thread-safe implementation using synchronized blocks
 *
 * Usage:
 * ```
 * PerformanceMonitor.startFrame()
 * // ... rendering code ...
 * PerformanceMonitor.endFrame()
 *
 * val metrics = PerformanceMonitor.getMetrics()
 * if (metrics.averageFrameTimeMs > 16.6) {
 *     // Performance issue detected
 * }
 * ```
 */
object PerformanceMonitor {
    // Configuration
    private const val DEFAULT_FRAME_TIME_BUDGET_MS = 16.6f // 60 FPS
    private const val SAMPLE_WINDOW_SIZE = 60 // Number of frames to average

    // Frame tracking
    private var frameStartTime: Long = 0
    private val frameTimesMs = ArrayDeque<Long>(SAMPLE_WINDOW_SIZE)
    private val totalFramesRendered: java.util.concurrent.atomic.AtomicLong =
        java.util.concurrent.atomic.AtomicLong(0)
    private val droppedFrames: java.util.concurrent.atomic.AtomicInteger =
        java.util.concurrent.atomic.AtomicInteger(0)

    // Memory tracking (bytes)
    private var lastMemorySnapshot: MemorySnapshot = MemorySnapshot.empty()

    // Performance budget
    private var frameTimeBudgetMs: Float = DEFAULT_FRAME_TIME_BUDGET_MS

    // State
    private var isMonitoring: Boolean = false

    /**
     * Start monitoring a new frame
     * Call this at the beginning of each frame/rendering cycle
     */
    fun startFrame() {
        frameStartTime = System.currentTimeMillis()
        isMonitoring = true
    }

    /**
     * End monitoring the current frame
     * Call this after rendering is complete
     */
    fun endFrame() {
        if (!isMonitoring) {
            return
        }

        val frameTime = System.currentTimeMillis() - frameStartTime

        // Add to sample window
        synchronized(frameTimesMs) {
            if (frameTimesMs.size >= SAMPLE_WINDOW_SIZE) {
                frameTimesMs.removeFirst()
            }
            frameTimesMs.addLast(frameTime)
        }

        totalFramesRendered.incrementAndGet()

        // Track dropped frames (exceeded budget)
        if (frameTime > frameTimeBudgetMs) {
            droppedFrames.incrementAndGet()
        }

        isMonitoring = false
    }

    /**
     * Update memory snapshot
     * Call periodically (e.g., every second) to track memory usage
     *
     * @param usedMemoryBytes Current used memory in bytes
     * @param totalMemoryBytes Total available memory in bytes
     * @param maxMemoryBytes Maximum memory the JVM can use in bytes
     */
    fun updateMemorySnapshot(
        usedMemoryBytes: Long,
        totalMemoryBytes: Long,
        maxMemoryBytes: Long,
    ) {
        lastMemorySnapshot =
            MemorySnapshot(
                usedBytes = usedMemoryBytes,
                totalBytes = totalMemoryBytes,
                maxBytes = maxMemoryBytes,
                timestamp = System.currentTimeMillis(),
            )
    }

    /**
     * Set custom frame time budget
     *
     * @param fps Target FPS (e.g., 60 for HIGH tier, 30 for MEDIUM/LOW)
     */
    fun setFrameTimeBudget(fps: Int) {
        frameTimeBudgetMs = 1000f / fps
    }

    /**
     * Get current performance metrics
     *
     * @return Current performance metrics
     */
    fun getMetrics(): PerformanceMetrics {
        synchronized(frameTimesMs) {
            val avgFrameTime =
                if (frameTimesMs.isNotEmpty()) {
                    frameTimesMs.average().toFloat()
                } else {
                    0f
                }

            val minFrameTime =
                if (frameTimesMs.isNotEmpty()) {
                    frameTimesMs.minOrNull()?.toFloat() ?: 0f
                } else {
                    0f
                }

            val maxFrameTime =
                if (frameTimesMs.isNotEmpty()) {
                    frameTimesMs.maxOrNull()?.toFloat() ?: 0f
                } else {
                    0f
                }

            val currentFps =
                if (avgFrameTime > 0) {
                    (1000f / avgFrameTime).coerceAtMost(60f)
                } else {
                    0f
                }

            val dropRate =
                if (totalFramesRendered.get() > 0) {
                    droppedFrames.get().toFloat() / totalFramesRendered.get().toFloat()
                } else {
                    0f
                }

            return PerformanceMetrics(
                averageFrameTimeMs = avgFrameTime,
                minFrameTimeMs = minFrameTime,
                maxFrameTimeMs = maxFrameTime,
                currentFps = currentFps,
                targetFps = (1000f / frameTimeBudgetMs).toInt(),
                totalFramesRendered = totalFramesRendered.get(),
                droppedFrames = droppedFrames.get(),
                frameDropRate = dropRate,
                memoryUsedMB = lastMemorySnapshot.usedMB,
                memoryTotalMB = lastMemorySnapshot.totalMB,
                memoryMaxMB = lastMemorySnapshot.maxMB,
            )
        }
    }

    /**
     * Check if current performance is within budget
     *
     * @return true if average frame time is within budget
     */
    fun isWithinBudget(): Boolean {
        val metrics = getMetrics()
        return metrics.averageFrameTimeMs <= frameTimeBudgetMs
    }

    /**
     * Get performance status
     *
     * @return Current performance status
     */
    fun getPerformanceStatus(): PerformanceStatus {
        val metrics = getMetrics()

        return when {
            metrics.currentFps >= metrics.targetFps * 0.9 -> PerformanceStatus.EXCELLENT
            metrics.currentFps >= metrics.targetFps * 0.7 -> PerformanceStatus.GOOD
            metrics.currentFps >= metrics.targetFps * 0.5 -> PerformanceStatus.FAIR
            else -> PerformanceStatus.POOR
        }
    }

    /**
     * Reset all metrics
     */
    fun reset() {
        synchronized(frameTimesMs) {
            frameTimesMs.clear()
            totalFramesRendered.set(0)
            droppedFrames.set(0)
        }
        lastMemorySnapshot = MemorySnapshot.empty()
    }

    /**
     * Get frame time budget in milliseconds
     */
    fun getFrameTimeBudgetMs(): Float = frameTimeBudgetMs

    /**
     * Get target FPS
     */
    fun getTargetFps(): Int = (1000f / frameTimeBudgetMs).toInt()
}

/**
 * Performance metrics snapshot
 *
 * Represents a point-in-time snapshot of performance metrics
 */
data class PerformanceMetrics(
    val averageFrameTimeMs: Float,
    val minFrameTimeMs: Float,
    val maxFrameTimeMs: Float,
    val currentFps: Float,
    val targetFps: Int,
    val totalFramesRendered: Long,
    val droppedFrames: Int,
    val frameDropRate: Float,
    val memoryUsedMB: Float,
    val memoryTotalMB: Float,
    val memoryMaxMB: Float,
) {
    /**
     * Check if performance meets target FPS
     */
    fun meetsTarget(): Boolean = currentFps >= targetFps * 0.9f

    /**
     * Get frame time percentage of budget
     */
    fun getFrameTimeBudgetUsage(): Float {
        return if (targetFps > 0) {
            (averageFrameTimeMs / (1000f / targetFps)) * 100
        } else {
            0f
        }
    }

    /**
     * Get memory usage percentage
     */
    fun getMemoryUsagePercentage(): Float {
        return if (memoryMaxMB > 0) {
            (memoryUsedMB / memoryMaxMB) * 100
        } else {
            0f
        }
    }

    /**
     * Check for performance violations
     */
    fun getViolations(): List<PerformanceViolation> {
        val violations = mutableListOf<PerformanceViolation>()

        if (currentFps < targetFps * 0.5f) {
            violations.add(
                PerformanceViolation.FrameRate(
                    actual = currentFps,
                    target = targetFps.toFloat(),
                    severity = ViolationSeverity.HIGH,
                ),
            )
        } else if (currentFps < targetFps * 0.7f) {
            violations.add(
                PerformanceViolation.FrameRate(
                    actual = currentFps,
                    target = targetFps.toFloat(),
                    severity = ViolationSeverity.MEDIUM,
                ),
            )
        }

        if (getMemoryUsagePercentage() > 90f) {
            violations.add(
                PerformanceViolation.Memory(
                    usedMB = memoryUsedMB,
                    maxMB = memoryMaxMB,
                    percentage = getMemoryUsagePercentage(),
                    severity = ViolationSeverity.HIGH,
                ),
            )
        } else if (getMemoryUsagePercentage() > 75f) {
            violations.add(
                PerformanceViolation.Memory(
                    usedMB = memoryUsedMB,
                    maxMB = memoryMaxMB,
                    percentage = getMemoryUsagePercentage(),
                    severity = ViolationSeverity.MEDIUM,
                ),
            )
        }

        if (frameDropRate > 0.1f) {
            violations.add(
                PerformanceViolation.FrameDrops(
                    rate = frameDropRate,
                    count = droppedFrames,
                    total = totalFramesRendered.toInt(),
                    severity = if (frameDropRate > 0.2f) ViolationSeverity.HIGH else ViolationSeverity.MEDIUM,
                ),
            )
        }

        return violations
    }
}

/**
 * Memory snapshot
 */
data class MemorySnapshot(
    val usedBytes: Long,
    val totalBytes: Long,
    val maxBytes: Long,
    val timestamp: Long,
) {
    val usedMB: Float get() = usedBytes / (1024f * 1024f)
    val totalMB: Float get() = totalBytes / (1024f * 1024f)
    val maxMB: Float get() = maxBytes / (1024f * 1024f)

    companion object {
        fun empty() = MemorySnapshot(0, 0, 0, 0)
    }
}

/**
 * Performance status enum
 */
enum class PerformanceStatus {
    EXCELLENT, // >= 90% of target FPS
    GOOD, // >= 70% of target FPS
    FAIR, // >= 50% of target FPS
    POOR, // < 50% of target FPS
}

/**
 * Performance violation types
 */
sealed class PerformanceViolation {
    abstract val severity: ViolationSeverity

    data class FrameRate(
        val actual: Float,
        val target: Float,
        override val severity: ViolationSeverity,
    ) : PerformanceViolation() {
        val deficit: Float get() = target - actual
    }

    data class Memory(
        val usedMB: Float,
        val maxMB: Float,
        val percentage: Float,
        override val severity: ViolationSeverity,
    ) : PerformanceViolation()

    data class FrameDrops(
        val rate: Float,
        val count: Int,
        val total: Int,
        override val severity: ViolationSeverity,
    ) : PerformanceViolation() {
        val percentage: Float get() = rate * 100f
    }
}

enum class ViolationSeverity {
    LOW, // Warning only
    MEDIUM, // May affect user experience
    HIGH, // Significantly affects user experience
}
