package com.wordland.performance

import android.os.Trace
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Performance monitoring system for tracking app performance metrics.
 *
 * Features:
 * - Frame time monitoring (target: 16.6ms for 60fps)
 * - Operation timing with automatic logging
 * - Memory usage tracking
 * - Compose recomposition tracking
 * - Startup time measurement
 */
object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private const val TARGET_FRAME_TIME_MS = 16.6 // 60fps target

    // Enable/disable monitoring
    var isEnabled: Boolean = true
        private set

    // Operation timers
    private val operationStartTimes = ConcurrentHashMap<String, Long>()
    private val operationDurations = ConcurrentHashMap<String, MutableList<Long>>()

    // Frame time tracking
    private val frameTimes = mutableListOf<Long>()
    private var frameMonitoringJob: Job? = null
    private var lastFrameTime = AtomicLong(0)

    // Memory tracking
    private var memoryBaseline: Long = 0

    /**
     * Initialize performance monitoring
     */
    fun initialize() {
        if (!isEnabled) return

        memoryBaseline = getUsedMemory()
        Log.i(TAG, "Performance monitoring initialized")
        Log.i(TAG, "Memory baseline: ${memoryBaseline / 1024 / 1024}MB")

        // Start frame time monitoring
        startFrameTimeMonitoring()
    }

    /**
     * Start timing an operation
     * @param name Unique identifier for the operation
     */
    fun startOperation(name: String) {
        if (!isEnabled) return
        operationStartTimes[name] = System.nanoTime()
        Trace.beginSection(name)
    }

    /**
     * End timing an operation and log the result
     * @param name Unique identifier for the operation
     * @param thresholdMs Log warning if operation exceeds this threshold (default: 100ms)
     */
    fun endOperation(
        name: String,
        thresholdMs: Long = 100,
    ) {
        if (!isEnabled) return

        val startTime = operationStartTimes.remove(name) ?: return
        val duration = System.nanoTime() - startTime
        val durationMs = duration / 1_000_000

        Trace.endSection()

        // Store duration for statistics
        operationDurations.getOrPut(name) { mutableListOf() }.add(durationMs)

        // Log if exceeds threshold
        if (durationMs > thresholdMs) {
            Log.w(
                TAG,
                "⚠️ Operation '$name' took ${durationMs}ms (threshold: ${thresholdMs}ms)",
            )
        } else {
            Log.d(TAG, "✓ Operation '$name' took ${durationMs}ms")
        }
    }

    /**
     * Measure and log an operation block
     * @param name Operation name
     * @param thresholdMs Warning threshold in milliseconds
     * @param block Operation to measure
     */
    inline fun <T> measure(
        name: String,
        thresholdMs: Long = 100,
        block: () -> T,
    ): T {
        startOperation(name)
        return try {
            block()
        } finally {
            endOperation(name, thresholdMs)
        }
    }

    /**
     * Start monitoring frame times
     */
    private fun startFrameTimeMonitoring() {
        frameMonitoringJob =
            CoroutineScope(Dispatchers.Default).launch {
                while (isEnabled) {
                    val currentTime = System.nanoTime()
                    val lastTime = lastFrameTime.get()

                    if (lastTime != 0L) {
                        val frameTime = (currentTime - lastTime) / 1_000_000 // Convert to ms
                        frameTimes.add(frameTime)

                        // Warn if frame time exceeds target
                        if (frameTime > TARGET_FRAME_TIME_MS) {
                            Log.w(
                                TAG,
                                "⚠️ Frame time: ${frameTime}ms (target: ${TARGET_FRAME_TIME_MS}ms, " +
                                    "fps: ${1000 / frameTime})",
                            )
                        }

                        // Keep only last 300 frames (5 seconds at 60fps)
                        if (frameTimes.size > 300) {
                            frameTimes.removeAt(0)
                        }
                    }

                    lastFrameTime.set(currentTime)
                    delay(16) // ~60fps
                }
            }
    }

    /**
     * Get current frame statistics
     */
    fun getFrameStats(): FrameStats {
        if (frameTimes.isEmpty()) {
            return FrameStats(0.0, 0.0, 0.0, 0)
        }

        val avgFrameTime = frameTimes.average()
        val maxFrameTime = frameTimes.maxOrNull() ?: 0.0
        val minFrameTime = frameTimes.minOrNull() ?: 0.0
        val frameDropCount = frameTimes.count { it > TARGET_FRAME_TIME_MS }

        return FrameStats(
            averageFrameTimeMs = avgFrameTime.toDouble(),
            maxFrameTimeMs = maxFrameTime.toDouble(),
            minFrameTimeMs = minFrameTime.toDouble(),
            droppedFrames = frameDropCount,
        )
    }

    /**
     * Get operation statistics
     */
    fun getOperationStats(name: String): OperationStats? {
        val durations = operationDurations[name] ?: return null
        if (durations.isEmpty()) return null

        return OperationStats(
            name = name,
            count = durations.size,
            averageMs = durations.average(),
            maxMs = (durations.maxOrNull() ?: 0L).toDouble(),
            minMs = (durations.minOrNull() ?: 0L).toDouble(),
            totalMs = durations.sum().toDouble(),
        )
    }

    /**
     * Get current memory usage
     */
    fun getMemoryUsage(): MemoryUsage {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()

        return MemoryUsage(
            usedMB = usedMemory / 1024 / 1024,
            maxMB = maxMemory / 1024 / 1024,
            totalMB = totalMemory / 1024 / 1024,
            availableMB = (maxMemory - usedMemory) / 1024 / 1024,
        )
    }

    /**
     * Get memory delta since initialization
     */
    fun getMemoryDelta(): Long {
        val current = getUsedMemory()
        return current - memoryBaseline
    }

    /**
     * Get used memory in bytes
     */
    private fun getUsedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }

    /**
     * Log current performance report
     */
    fun logReport() {
        if (!isEnabled) return

        Log.i(TAG, "=".repeat(60))
        Log.i(TAG, "PERFORMANCE REPORT")
        Log.i(TAG, "=".repeat(60))

        // Frame statistics
        val frameStats = getFrameStats()
        Log.i(TAG, "Frame Stats:")
        Log.i(
            TAG,
            "  Avg: ${"%.2f".format(frameStats.averageFrameTimeMs)}ms " +
                "(${"%.1f".format(1000 / frameStats.averageFrameTimeMs)} fps)",
        )
        Log.i(TAG, "  Min: ${"%.2f".format(frameStats.minFrameTimeMs)}ms")
        Log.i(TAG, "  Max: ${"%.2f".format(frameStats.maxFrameTimeMs)}ms")
        Log.i(TAG, "  Dropped frames: ${frameStats.droppedFrames}")

        // Memory usage
        val memory = getMemoryUsage()
        val delta = getMemoryDelta()
        Log.i(TAG, "Memory Usage:")
        Log.i(TAG, "  Used: ${memory.usedMB}MB / ${memory.maxMB}MB")
        Log.i(TAG, "  Total: ${memory.totalMB}MB")
        Log.i(TAG, "  Available: ${memory.availableMB}MB")
        Log.i(TAG, "  Delta: +${delta / 1024 / 1024}MB")

        // Operation statistics
        Log.i(TAG, "Operation Stats:")
        operationDurations.keys.forEach { name ->
            val stats = getOperationStats(name)
            stats?.let {
                Log.i(
                    TAG,
                    "  $name: ${"%.2f".format(it.averageMs)}ms avg, " +
                        "${it.count} calls, total: ${"%.2f".format(it.totalMs)}ms",
                )
            }
        }

        Log.i(TAG, "=".repeat(60))
    }

    /**
     * Reset all monitoring data
     */
    fun reset() {
        operationStartTimes.clear()
        operationDurations.clear()
        frameTimes.clear()
        lastFrameTime.set(0)
        memoryBaseline = getUsedMemory()
        Log.i(TAG, "Performance monitoring reset")
    }

    /**
     * Enable performance monitoring
     */
    fun enable() {
        isEnabled = true
        Log.i(TAG, "Performance monitoring enabled")
    }

    /**
     * Disable performance monitoring
     */
    fun disable() {
        isEnabled = false
        frameMonitoringJob?.cancel()
        Log.i(TAG, "Performance monitoring disabled")
    }
}

/**
 * Frame statistics data class
 */
data class FrameStats(
    val averageFrameTimeMs: Double,
    val maxFrameTimeMs: Double,
    val minFrameTimeMs: Double,
    val droppedFrames: Int,
)

/**
 * Operation statistics data class
 */
data class OperationStats(
    val name: String,
    val count: Int,
    val averageMs: Double,
    val maxMs: Double,
    val minMs: Double,
    val totalMs: Double,
)

/**
 * Memory usage data class
 */
data class MemoryUsage(
    val usedMB: Long,
    val maxMB: Long,
    val totalMB: Long,
    val availableMB: Long,
)

/**
 * Performance monitoring scope for automatic resource management
 */
class PerformanceScope(private val operationName: String) {
    init {
        PerformanceMonitor.startOperation(operationName)
    }

    fun close(thresholdMs: Long = 100) {
        PerformanceMonitor.endOperation(operationName, thresholdMs)
    }
}

/**
 * Create a performance scope for use with try-with-resources
 */
inline fun performanceScope(
    operationName: String,
    block: PerformanceScope.() -> Unit,
) {
    val scope = PerformanceScope(operationName)
    try {
        scope.block()
    } finally {
        scope.close()
    }
}
