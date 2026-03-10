package com.wordland.performance

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow

/**
 * Compose performance monitoring utilities.
 *
 * Helps track:
 * - Recomposition counts
 * - Recomposition causes
 * - Skipped compositions
 * - Composition durations
 */
object ComposePerformanceHelper {
    private const val TAG = "ComposePerf"

    /**
     * Track recompositions for a composable
     * @param key Unique identifier for tracking
     */
    @Composable
    fun TrackRecomposition(key: String) {
        val counter = remember { RecompositionCounter(key) }

        LaunchedEffect(key) {
            snapshotFlow { }
                .collect {
                    counter.increment()
                    Log.v(TAG, "Recomposition: $key (count: ${counter.count})")
                }
        }
    }

    /**
     * Log when composition starts
     */
    fun logCompositionStart(key: String) {
        Log.d(TAG, "Composition started: $key")
    }

    /**
     * Log when composition ends
     */
    fun logCompositionEnd(
        key: String,
        durationMs: Long,
    ) {
        Log.d(TAG, "Composition ended: $key (${durationMs}ms)")
    }

    /**
     * Measure composition duration
     */
    @Composable
    fun MeasureComposition(
        key: String,
        content: @Composable () -> Unit,
    ) {
        val startTime = remember { System.nanoTime() }

        LaunchedEffect(key) {
            val duration = (System.nanoTime() - startTime) / 1_000_000
            logCompositionEnd(key, duration)
        }

        logCompositionStart(key)
        content()
    }

    /**
     * Warning: Expensive composable detected
     * Use this to mark composables that need optimization
     */
    @Composable
    fun MarkExpensive(
        key: String,
        thresholdMs: Long = 16,
    ) {
        LaunchedEffect(key) {
            val startTime = System.nanoTime()
            snapshotFlow { }.collect {
                val duration = (System.nanoTime() - startTime) / 1_000_000
                if (duration > thresholdMs) {
                    Log.w(TAG, "⚠️ Expensive composition: $key took ${duration}ms")
                }
            }
        }
    }
}

/**
 * Recomposition counter
 */
class RecompositionCounter(private val key: String) : RememberObserver {
    var count: Int = 0
        private set

    fun increment() {
        count++
    }

    override fun onRemembered() {
        Log.v("ComposePerf", "Remembered: $key")
    }

    override fun onForgotten() {
        Log.v("ComposePerf", "Forgotten: $key (total recompositions: $count)")
    }

    override fun onAbandoned() {
        Log.v("ComposePerf", "Abandoned: $key")
    }
}

/**
 * Compose stability hints for compiler
 *
 * Use these annotations to help the Compose compiler optimize:
 *
 * 1. @Immutable - For classes that never change
 * 2. @Stable - For classes that change but notify Compose
 *
 * Examples:
 * - Immutable: data class with val properties
 * - Stable: class with mutable state but proper equals/hashCode
 */

/**
 * Performance-optimized remember with key
 * Prevents unnecessary recompositions when key doesn't change
 */
@Composable
fun <T> rememberWithKey(
    key: Any?,
    calculation: () -> T,
): T {
    return remember(key, calculation)
}

/**
 * Performance-optimized derivedStateOf
 * Only recomputes when dependencies actually change
 */
@Composable
inline fun <T> optimizedDerivedStateOf(crossinline calculation: () -> T): androidx.compose.runtime.State<T> {
    return androidx.compose.runtime.derivedStateOf { calculation() }
}

/**
 * Track and log expensive operations in Compose
 */
inline fun <T> traceComposeOperation(
    name: String,
    block: () -> T,
): T {
    android.os.Trace.beginSection(name)
    return try {
        val result = block()
        android.os.Trace.endSection()
        result
    } catch (e: Exception) {
        android.os.Trace.endSection()
        throw e
    }
}
