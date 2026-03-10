package com.wordland.ui.visual

import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Visual QA check utilities for automated visual verification.
 *
 * Provides automated checks for common visual issues:
 * - Text truncation detection
 * - Touch target size validation (48dp minimum)
 * - Overflow detection
 * - Color contrast validation (WCAG AA)
 * - Alignment verification
 *
 * Created for Epic #12 Task 12.6 - Visual QA Automation
 */
object VisualQAChecks {

    /**
     * Validates text content is fully rendered without truncation.
     *
     * Checks if the semantic text content matches expected text exactly.
     * Truncation is detected by comparing rendered text with expected text.
     *
     * @param expectedText The full text that should be displayed
     * @param actualText The actual text rendered in the component
     * @return true if no truncation detected, false otherwise
     */
    fun assertNoTextTruncation(
        expectedText: String,
        actualText: String,
        componentName: String = "Component"
    ): VisualCheckResult {
        val isTruncated = expectedText != actualText && actualText.length < expectedText.length

        return if (isTruncated) {
            val truncatedCount = expectedText.length - actualText.length
            VisualCheckResult.Failed(
                check = "Text Truncation",
                component = componentName,
                message = "Text truncated: $actualText (expected: $expectedText, $truncatedCount chars missing)"
            )
        } else {
            VisualCheckResult.Passed(
                check = "Text Truncation",
                component = componentName
            )
        }
    }

    /**
     * Validates touch target size meets Material Design guidelines.
     *
     * Material Design recommends minimum 48dp x 48dp for touch targets.
     * This check ensures the component height is at least the minimum.
     *
     * @param height The actual height of the component in dp
     * @param width The actual width of the component in dp
     * @param minHeight Minimum required height (default 48dp per Material Design)
     * @param minWidth Minimum required width (default 48dp per Material Design)
     * @return VisualCheckResult indicating pass/fail
     */
    fun assertTouchTargetSize(
        height: Float,
        width: Float,
        minHeight: Float = 48f,
        minWidth: Float = 48f,
        componentName: String = "Touch Target"
    ): VisualCheckResult {
        val heightFail = height < minHeight
        val widthFail = width < minWidth

        return when {
            heightFail && widthFail -> VisualCheckResult.Failed(
                check = "Touch Target Size",
                component = componentName,
                message = "Size too small: ${width}x${height}dp (minimum: ${minWidth}x${minHeight}dp)"
            )
            heightFail -> VisualCheckResult.Failed(
                check = "Touch Target Size",
                component = componentName,
                message = "Height too small: ${height}dp (minimum: ${minHeight}dp)"
            )
            widthFail -> VisualCheckResult.Failed(
                check = "Touch Target Size",
                component = componentName,
                message = "Width too small: ${width}dp (minimum: ${minWidth}dp)"
            )
            else -> VisualCheckResult.Passed(
                check = "Touch Target Size",
                component = componentName
            )
        }
    }

    /**
     * Detects if child content overflows parent container bounds.
     *
     * @param childBounds The bounds of the child content (left, top, right, bottom)
     * @param parentBounds The bounds of the parent container (left, top, right, bottom)
     * @return VisualCheckResult indicating if overflow detected
     */
    fun assertNoOverflow(
        childBounds: Bounds,
        parentBounds: Bounds,
        componentName: String = "Container"
    ): VisualCheckResult {
        val overflowLeft = childBounds.left < parentBounds.left
        val overflowTop = childBounds.top < parentBounds.top
        val overflowRight = childBounds.right > parentBounds.right
        val overflowBottom = childBounds.bottom > parentBounds.bottom

        return when {
            overflowLeft || overflowTop || overflowRight || overflowBottom -> {
                val overflowAreas = mutableListOf<String>()
                if (overflowLeft) overflowAreas.add("left")
                if (overflowTop) overflowAreas.add("top")
                if (overflowRight) overflowAreas.add("right")
                if (overflowBottom) overflowAreas.add("bottom")

                VisualCheckResult.Failed(
                    check = "Overflow Detection",
                    component = componentName,
                    message = "Content overflows on: ${overflowAreas.joinToString(", ")}\n" +
                            "Child bounds: [$childBounds]\n" +
                            "Parent bounds: [$parentBounds]"
                )
            }
            else -> VisualCheckResult.Passed(
                check = "Overflow Detection",
                component = componentName
            )
        }
    }

    /**
     * Validates color contrast ratio meets WCAG AA standards.
     *
     * WCAG AA requires:
     * - 4.5:1 for normal text (< 18pt or < 14pt bold)
     * - 3:1 for large text (≥ 18pt or ≥ 14pt bold)
     * - 3:1 for UI components and graphical objects
     *
     * @param foreground Foreground color (text)
     * @param background Background color
     * @param isLargeText Whether text is large (≥ 18pt or ≥ 14pt bold)
     * @return VisualCheckResult indicating pass/fail
     */
    fun assertColorContrast(
        foreground: androidx.compose.ui.graphics.Color,
        background: androidx.compose.ui.graphics.Color,
        isLargeText: Boolean = false,
        componentName: String = "Text"
    ): VisualCheckResult {
        val contrastRatio = calculateContrastRatio(foreground, background)
        val minRequiredRatio = if (isLargeText) 3.0 else 4.5

        return if (contrastRatio >= minRequiredRatio) {
            VisualCheckResult.Passed(
                check = "Color Contrast",
                component = componentName,
                details = "Contrast ratio: ${String.format("%.2f", contrastRatio)}:1"
            )
        } else {
            VisualCheckResult.Failed(
                check = "Color Contrast",
                component = componentName,
                message = "Contrast ratio ${String.format("%.2f", contrastRatio)}:1 below WCAG AA " +
                        "requirement of ${minRequiredRatio}:1 for ${if (isLargeText) "large" else "normal"} text"
            )
        }
    }

    /**
     * Calculates relative luminance of a color.
     * Used for contrast ratio calculation per WCAG 2.0.
     */
    private fun calculateLuminance(color: androidx.compose.ui.graphics.Color): Double {
        val r = color.red.toLinear()
        val g = color.green.toLinear()
        val b = color.blue.toLinear()

        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Converts sRGB component to linear RGB.
     */
    private fun Float.toLinear(): Double {
        return if (this <= 0.03928f) {
            this / 12.92
        } else {
            ((this + 0.055f) / 1.055f).let { pow(it.toDouble(), 2.4) }
        }
    }

    /**
     * Power function for luminance calculation.
     */
    private fun pow(base: Double, exp: Double): Double {
        return Math.pow(base, exp)
    }

    /**
     * Calculates contrast ratio between two colors per WCAG 2.0.
     * Formula: (L1 + 0.05) / (L2 + 0.05) where L1 is the lighter color.
     */
    private fun calculateContrastRatio(
        foreground: androidx.compose.ui.graphics.Color,
        background: androidx.compose.ui.graphics.Color
    ): Double {
        val l1 = calculateLuminance(foreground)
        val l2 = calculateLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)

        return (lighter + 0.05) / (darker + 0.05)
    }

    /**
     * Validates alignment of a component within its container.
     *
     * @param componentBounds The bounds of the component to check
     * @param containerBounds The bounds of the container
     * @param expectedAlignment The expected alignment (horizontal, vertical)
     * @param tolerance Allowed deviation in pixels (default 2px for rounding errors)
     * @return VisualCheckResult indicating pass/fail
     */
    fun assertAlignment(
        componentBounds: Bounds,
        containerBounds: Bounds,
        expectedAlignment: Alignment,
        tolerance: Int = 2,
        componentName: String = "Component"
    ): VisualCheckResult {
        val issues = mutableListOf<String>()

        when (expectedAlignment.horizontal) {
            Alignment.Horizontal.LEFT -> {
                val isLeftAligned = abs(componentBounds.left - containerBounds.left) <= tolerance
                if (!isLeftAligned) {
                    issues.add("not left aligned (gap: ${abs(componentBounds.left - containerBounds.left)}px)")
                }
            }
            Alignment.Horizontal.CENTER -> {
                val componentCenter = (componentBounds.left + componentBounds.right) / 2
                val containerCenter = (containerBounds.left + containerBounds.right) / 2
                val isCentered = abs(componentCenter - containerCenter) <= tolerance
                if (!isCentered) {
                    issues.add("not horizontally centered (offset: ${abs(componentCenter - containerCenter)}px)")
                }
            }
            Alignment.Horizontal.RIGHT -> {
                val isRightAligned = abs(componentBounds.right - containerBounds.right) <= tolerance
                if (!isRightAligned) {
                    issues.add("not right aligned (gap: ${abs(componentBounds.right - containerBounds.right)}px)")
                }
            }
        }

        when (expectedAlignment.vertical) {
            Alignment.Vertical.TOP -> {
                val isTopAligned = abs(componentBounds.top - containerBounds.top) <= tolerance
                if (!isTopAligned) {
                    issues.add("not top aligned (gap: ${abs(componentBounds.top - containerBounds.top)}px)")
                }
            }
            Alignment.Vertical.CENTER -> {
                val componentCenter = (componentBounds.top + componentBounds.bottom) / 2
                val containerCenter = (containerBounds.top + containerBounds.bottom) / 2
                val isCentered = abs(componentCenter - containerCenter) <= tolerance
                if (!isCentered) {
                    issues.add("not vertically centered (offset: ${abs(componentCenter - containerCenter)}px)")
                }
            }
            Alignment.Vertical.BOTTOM -> {
                val isBottomAligned = abs(componentBounds.bottom - containerBounds.bottom) <= tolerance
                if (!isBottomAligned) {
                    issues.add("not bottom aligned (gap: ${abs(componentBounds.bottom - containerBounds.bottom)}px)")
                }
            }
        }

        return if (issues.isEmpty()) {
            VisualCheckResult.Passed(
                check = "Alignment",
                component = componentName
            )
        } else {
            VisualCheckResult.Failed(
                check = "Alignment",
                component = componentName,
                message = "Alignment issues: ${issues.joinToString(", ")}"
            )
        }
    }

    /**
     * Validates that a scrollable container has scrolling enabled.
     *
     * @param isScrollable Whether the container reports as scrollable
     * @param componentName Name of the component being checked
     * @return VisualCheckResult
     */
    fun assertScrollable(
        isScrollable: Boolean,
        componentName: String = "Scroll Container"
    ): VisualCheckResult {
        return if (isScrollable) {
            VisualCheckResult.Passed(
                check = "Scrollability",
                component = componentName
            )
        } else {
            VisualCheckResult.Failed(
                check = "Scrollability",
                component = componentName,
                message = "Container is not scrollable - may cause overflow on small screens"
            )
        }
    }

    /**
     * Validates that bottom padding is applied for system navigation bar.
     *
     * @param bottomPadding Bottom padding value in pixels
     * @param minPadding Minimum required padding (default 16dp converted to px)
     * @param density Screen density to convert dp to px
     * @return VisualCheckResult
     */
    fun assertBottomPadding(
        bottomPadding: Int,
        minPaddingDp: Float = 16f,
        density: Float,
        componentName: String = "Screen"
    ): VisualCheckResult {
        val minPaddingPx = minPaddingDp * density

        return if (bottomPadding >= minPaddingPx) {
            VisualCheckResult.Passed(
                check = "Bottom Padding",
                component = componentName
            )
        } else {
            VisualCheckResult.Failed(
                check = "Bottom Padding",
                component = componentName,
                message = "Bottom padding ${bottomPadding}px insufficient " +
                        "(minimum: ${minPaddingPx.toInt()}px for system navigation bar)"
            )
        }
    }
}

/**
 * Represents the bounds of a UI element.
 */
data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top

    override fun toString(): String {
        return "[$left, $top, $right, $bottom] (${width}x${height})"
    }
}

/**
 * Represents alignment direction.
 */
data class Alignment(
    val horizontal: Horizontal = Horizontal.CENTER,
    val vertical: Vertical = Vertical.CENTER
) {
    enum class Horizontal { LEFT, CENTER, RIGHT }
    enum class Vertical { TOP, CENTER, BOTTOM }

    companion object {
        val CenterLeft = Alignment(Horizontal.LEFT, Vertical.CENTER)
        val Center = Alignment(Horizontal.CENTER, Vertical.CENTER)
        val CenterRight = Alignment(Horizontal.RIGHT, Vertical.CENTER)
        val TopCenter = Alignment(Horizontal.CENTER, Vertical.TOP)
        val BottomCenter = Alignment(Horizontal.CENTER, Vertical.BOTTOM)
    }
}

/**
 * Result of a visual check.
 */
sealed class VisualCheckResult {
    abstract val check: String
    abstract val component: String
    abstract val details: String

    data class Passed(
        override val check: String,
        override val component: String,
        override val details: String = "Passed"
    ) : VisualCheckResult() {
        val isPassed: Boolean get() = true
    }

    data class Failed(
        override val check: String,
        override val component: String,
        override val details: String = "",
        val message: String = details
    ) : VisualCheckResult() {
        val isPassed: Boolean get() = false
    }
}

/**
 * Collection of visual check results with reporting capabilities.
 */
class VisualQAReport {
    private val _results = mutableListOf<VisualCheckResult>()
    val results: List<VisualCheckResult> get() = _results.toList()

    fun add(result: VisualCheckResult) {
        _results.add(result)
    }

    val passed: Int get() = _results.count { it is VisualCheckResult.Passed }
    val total: Int get() = _results.size
    val failed: Int get() = _results.count { it is VisualCheckResult.Failed }
    val isAllPassed: Boolean get() = failed == 0

    /**
     * Generates a markdown report string.
     */
    fun toMarkdown(): String {
        val sb = StringBuilder()
        sb.appendLine("# Visual QA Report")
        sb.appendLine()
        sb.appendLine("**Total Checks**: $total")
        sb.appendLine("**Passed**: $passed ✅")
        sb.appendLine("**Failed**: $failed ❌")
        sb.appendLine()

        if (failed > 0) {
            sb.appendLine("## ❌ Failed Checks")
            sb.appendLine()
            _results.filterIsInstance<VisualCheckResult.Failed>().forEach { result ->
                sb.appendLine("### ${result.component} - ${result.check}")
                sb.appendLine("**Message**: ${result.message}")
                sb.appendLine()
            }
        }

        sb.appendLine("## ✅ Passed Checks")
        sb.appendLine()
        _results.filterIsInstance<VisualCheckResult.Passed>().forEach { result ->
            sb.appendLine("- `${result.component}` - ${result.check}")
        }

        return sb.toString()
    }

    /**
     * Generates a summary table for console output.
     */
    fun toTable(): String {
        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("┌────────────────────┬──────────────┬─────────────────┐")
        sb.appendLine("│ Component          │ Check        │ Status          │")
        sb.appendLine("├────────────────────┼──────────────┼─────────────────┤")

        _results.forEach { result ->
            val component = result.component.take(18).padEnd(18)
            val check = result.check.take(12).padEnd(12)
            val status = if (result is VisualCheckResult.Passed) "✅ PASS" else "❌ FAIL"

            sb.appendLine("│ $component │ $check │ $status │")
        }

        sb.appendLine("└────────────────────┴──────────────┴─────────────────┘")
        sb.appendLine()
        sb.appendLine("Summary: $passed/$total passed")

        return sb.toString()
    }
}
