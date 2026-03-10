package com.wordland.performance

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

/**
 * Application startup performance tracker.
 *
 * Tracks critical startup phases:
 * 1. Application.onCreate
 * 2. Activity.onCreate
 * 3. First frame rendering
 * 4. Data initialization
 * 5. ViewModel creation
 *
 * Target: Cold start < 3s
 */
object StartupPerformanceTracker {
    private const val TAG = "StartupPerf"

    private var applicationCreateTime: Long = 0
    private var activityCreateTime: Long = 0
    private var firstFrameTime: Long = 0
    private var dataInitTime: Long = 0
    private var totalTime: Long = 0

    private val phaseTimings = mutableMapOf<String, Long>()
    private var isTracking = false

    /**
     * Start tracking application creation
     */
    fun onApplicationCreate() {
        if (isTracking) return
        isTracking = true

        applicationCreateTime = System.currentTimeMillis()
        recordPhase("Application.onCreate_start")
        Log.i(TAG, "Application creation started at $applicationCreateTime")
    }

    /**
     * Record phase completion
     */
    fun recordPhase(phaseName: String) {
        if (!isTracking) return
        val currentTime = System.currentTimeMillis()
        phaseTimings[phaseName] = currentTime - applicationCreateTime
        Log.d(TAG, "Phase: $phaseName (${phaseTimings[phaseName]}ms from start)")
    }

    /**
     * Track activity creation
     */
    fun onActivityCreate(activity: ComponentActivity) {
        if (!isTracking) return

        activityCreateTime = System.currentTimeMillis()
        recordPhase("Activity.onCreate_start")

        Log.i(TAG, "Activity creation started at $activityCreateTime")
        Log.i(TAG, "Time from Application to Activity: ${activityCreateTime - applicationCreateTime}ms")
    }

    /**
     * Track first frame rendering
     */
    fun onFirstFrame() {
        if (!isTracking) return

        firstFrameTime = System.currentTimeMillis()
        recordPhase("First_frame")

        totalTime = firstFrameTime - applicationCreateTime

        Log.i(TAG, "=".repeat(60))
        Log.i(TAG, "STARTUP PERFORMANCE REPORT")
        Log.i(TAG, "=".repeat(60))
        Log.i(TAG, "Total startup time: ${totalTime}ms")
        Log.i(TAG, "  Application.onCreate: ${phaseTimings["Application.onCreate_start"] ?: 0}ms")
        Log.i(TAG, "  Activity.onCreate: ${activityCreateTime - applicationCreateTime}ms")
        Log.i(TAG, "  First frame: ${firstFrameTime - activityCreateTime}ms")
        Log.i(TAG, "=".repeat(60))

        // Warn if exceeds target
        if (totalTime > 3000) {
            Log.w(TAG, "⚠️ Startup time exceeds 3s target (${totalTime}ms)")
        } else {
            Log.i(TAG, "✓ Startup time within 3s target")
        }
    }

    /**
     * Track data initialization
     */
    fun onDataInitComplete() {
        if (!isTracking) return
        dataInitTime = System.currentTimeMillis()
        recordPhase("Data_init_complete")
        Log.i(TAG, "Data initialization completed at ${dataInitTime - applicationCreateTime}ms")
    }

    /**
     * Get startup report
     */
    fun getReport(): StartupReport {
        return StartupReport(
            totalStartupTimeMs = totalTime,
            applicationCreateTimeMs = phaseTimings["Application.onCreate_start"] ?: 0,
            activityCreateTimeMs = activityCreateTime - applicationCreateTime,
            firstFrameTimeMs = firstFrameTime - activityCreateTime,
            dataInitTimeMs = dataInitTime - applicationCreateTime,
            phaseTimings = phaseTimings.toMap(),
            isWithinTarget = totalTime <= 3000,
        )
    }

    /**
     * Reset tracking
     */
    fun reset() {
        isTracking = false
        applicationCreateTime = 0
        activityCreateTime = 0
        firstFrameTime = 0
        dataInitTime = 0
        totalTime = 0
        phaseTimings.clear()
    }
}

/**
 * Startup report data class
 */
data class StartupReport(
    val totalStartupTimeMs: Long,
    val applicationCreateTimeMs: Long,
    val activityCreateTimeMs: Long,
    val firstFrameTimeMs: Long,
    val dataInitTimeMs: Long,
    val phaseTimings: Map<String, Long>,
    val isWithinTarget: Boolean,
) {
    fun toReadableString(): String {
        return """
            |
            |${"=".repeat(60)}
            |STARTUP REPORT
            |${"=".repeat(60)}
            |Total Time: ${totalStartupTimeMs}ms ${if (isWithinTarget) "✓" else "⚠️"}
            |
            |Phase Breakdown:
            |  Application.onCreate: ${applicationCreateTimeMs}ms
            |  Activity.onCreate: ${activityCreateTimeMs}ms
            |  Data Init: ${dataInitTimeMs}ms
            |  First Frame: ${firstFrameTimeMs}ms
            |
            |Target: < 3000ms (${if (isWithinTarget) "PASS" else "FAIL"})
            |${"=".repeat(60)}
            |
            """.trimMargin()
    }
}

/**
 * Base activity class for automatic startup tracking
 */
abstract class TrackedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        StartupPerformanceTracker.onActivityCreate(this)
        super.onCreate(savedInstanceState)

        // Report first frame when window is focused
        window.decorView.postOnAnimation {
            StartupPerformanceTracker.onFirstFrame()
        }
    }
}

/**
 * Application class for automatic startup tracking
 */
abstract class TrackedApplication : Application() {
    override fun onCreate() {
        StartupPerformanceTracker.onApplicationCreate()
        super.onCreate()
    }
}
