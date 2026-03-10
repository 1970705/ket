package com.wordland.ui.components

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Animation preference settings
 *
 * @param reduceMotion Whether animations should be reduced
 * @param animationDurationScale System animation duration scale (0.0 = off, 1.0 = normal)
 */
@Immutable
data class AnimationPreference(
    val reduceMotion: Boolean = false,
    val animationDurationScale: Float = 1.0f,
) {
    /**
     * Whether animations should be played at all
     */
    val animationsEnabled: Boolean
        get() = animationDurationScale > 0f

    /**
     * Whether to use reduced duration (0.5x) for better performance
     */
    val useReducedDuration: Boolean
        get() = animationDurationScale < 1.0f && animationDurationScale > 0f

    companion object {
        /**
         * Default preference (animations enabled, no reduction)
         */
        val Default =
            AnimationPreference(
                reduceMotion = false,
                animationDurationScale = 1.0f,
            )

        /**
         * No animations preference
         */
        val NoAnimations =
            AnimationPreference(
                reduceMotion = true,
                animationDurationScale = 0.0f,
            )
    }
}

/**
 * Helper for respecting user animation preferences
 *
 * Features:
 * - Detects system "Reduce Motion" setting
 * - Detects system animation duration scale
 * - Provides reactive state flow for preference changes
 * - Caches result for performance
 *
 * Usage:
 * ```kotlin
 * val animationPreference = LocalAnimationPreference.current
 * if (animationPreference.reduceMotion) {
 *     // Show static content
 * } else {
 *     // Show animated content
 * }
 * ```
 *
 * @since 1.6 (Epic #8.3)
 */
object ReduceMotionHelper {
    private val cachedPreference = MutableStateFlow(AnimationPreference.Default)

    /**
     * Check if reduce motion is enabled in system settings
     *
     * @param context Android context
     * @return True if reduce motion is enabled
     */
    fun isReduceMotionEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: Use dedicated API
            try {
                Settings.System.getFloat(
                    context.contentResolver,
                    "reduced_motion", // Use string literal for API 33+ constant
                ) == 1f
            } catch (e: Settings.SettingNotFoundException) {
                false
            }
        } else {
            // Android 12 and below: Use animator duration scale
            getAnimationDurationScale(context) == 0f
        }
    }

    /**
     * Get the system animation duration scale
     *
     * @param context Android context
     * @return Animation scale (0.0 = off, 0.5 = reduced, 1.0 = normal)
     */
    fun getAnimationDurationScale(context: Context): Float {
        return try {
            val scale =
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.WINDOW_ANIMATION_SCALE,
                    1.0f,
                )
            // Also check transition animation scale
            val transitionScale =
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.TRANSITION_ANIMATION_SCALE,
                    1.0f,
                )
            // Use the minimum of both (most restrictive)
            minOf(scale, transitionScale)
        } catch (e: Settings.SettingNotFoundException) {
            1.0f
        }
    }

    /**
     * Get animation preference for the given context
     *
     * @param context Android context
     * @return Current animation preference
     */
    fun getPreference(context: Context): AnimationPreference {
        val reduceMotion = isReduceMotionEnabled(context)
        val durationScale = getAnimationDurationScale(context)

        return AnimationPreference(
            reduceMotion = reduceMotion,
            animationDurationScale = durationScale,
        )
    }

    /**
     * Update cached preference
     *
     * Call this when settings may have changed
     *
     * @param context Android context
     */
    fun updatePreference(context: Context) {
        cachedPreference.value = getPreference(context)
    }

    /**
     * Get cached preference as StateFlow
     *
     * @return StateFlow of animation preference
     */
    fun getPreferenceFlow(): StateFlow<AnimationPreference> = cachedPreference.asStateFlow()

    /**
     * Calculate adjusted animation duration based on preference
     *
     * @param baseDuration Base duration in milliseconds
     * @param preference Animation preference
     * @return Adjusted duration (or 0 if animations disabled)
     */
    fun adjustDuration(
        baseDuration: Int,
        preference: AnimationPreference,
    ): Int {
        if (!preference.animationsEnabled) return 0
        if (preference.reduceMotion) return (baseDuration * 0.3f).toInt()
        if (preference.useReducedDuration) return (baseDuration * 0.5f).toInt()
        return baseDuration
    }

    /**
     * Calculate adjusted animation duration based on preference
     *
     * @param baseDuration Base duration in milliseconds
     * @return Adjusted duration (or 0 if animations disabled)
     */
    fun adjustDuration(baseDuration: Int): Int {
        return adjustDuration(baseDuration, cachedPreference.value)
    }
}

/**
 * Composition local for animation preference
 */
val LocalAnimationPreference: ProvidableCompositionLocal<AnimationPreference> =
    staticCompositionLocalOf { AnimationPreference.Default }

/**
 * Observe animation preference changes
 *
 * Composable that provides animation preference and updates
 * when system settings change
 *
 * @param context Android context
 * @return Current animation preference
 *
 * Usage:
 * ```kotlin
 * val animationPreference = observeAnimationPreference()
 * AnimatedWordSwitch(
 *     wordKey = currentWord,
 *     reduceMotion = animationPreference.reduceMotion,
 * ) { ... }
 * ```
 */
@Composable
fun observeAnimationPreference(context: Context = LocalContext.current): AnimationPreference {
    val preference = ReduceMotionHelper.getPreference(context)
    val lifecycleOwner = LocalLifecycleOwner.current

    // Update preference when app resumes (settings may have changed)
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    ReduceMotionHelper.updatePreference(context)
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return preference
}

/**
 * Helper composable for conditional animation rendering
 *
 * @param preference Animation preference
 * @param animatedContent Content to show when animations are enabled
 * @param staticContent Content to show when animations are reduced (optional)
 */
@Composable
fun AnimatedContent(
    preference: AnimationPreference = LocalAnimationPreference.current,
    animatedContent: @Composable () -> Unit,
    staticContent: @Composable () -> Unit = animatedContent,
) {
    if (preference.reduceMotion) {
        staticContent()
    } else {
        animatedContent()
    }
}

/**
 * Quality setting based on animation preference
 *
 * Maps animation preference to AnimationQuality
 *
 * @param preference Animation preference
 * @return Corresponding animation quality
 */
fun animationQualityFromPreference(preference: AnimationPreference): com.wordland.domain.model.AnimationQuality {
    return when {
        preference.reduceMotion -> com.wordland.domain.model.AnimationQuality.Low
        preference.useReducedDuration -> com.wordland.domain.model.AnimationQuality.Medium
        else -> com.wordland.domain.model.AnimationQuality.High
    }
}

/**
 * Extension property to check if animations should be reduced
 */
val AnimationPreference.isReduced: Boolean
    get() = reduceMotion || !animationsEnabled

/**
 * Extension property to get animation multiplier
 */
val AnimationPreference.durationMultiplier: Float
    get() =
        when {
            !animationsEnabled -> 0f
            reduceMotion -> 0.3f
            useReducedDuration -> 0.5f
            else -> 1f
        }
