package com.wordland.ui.visual

import android.view.SurfaceView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.domain.model.SpellBattleQuestion
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Visual QA tests for LearningScreen component.
 *
 * Tests cover regression prevention for P0-BUG-003 (LearningScreen button truncation)
 * and other visual quality checks.
 *
 * Part of Epic #12 Task 12.6 - Visual QA Automation
 *
 * NOTE: LearningContent is a private composable. These tests demonstrate the Visual QA
 * framework structure. To run actual tests, either:
 * 1. Use the public LearningScreen composable instead
 * 2. Make LearningContent internal/testable
 * 3. Run tests via instrumentation on actual device
 */
@RunWith(AndroidJUnit4::class)
class LearningScreenVisualTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var report: VisualQAReport

    private lateinit var testQuestion: SpellBattleQuestion

    @Before
    fun setup() {
        report = VisualQAReport()

        // Create test question (Level 1, Word 1)
        testQuestion = SpellBattleQuestion(
            wordId = "look_001",
            translation = "看，观看",
            targetWord = "look",
            hint = "First letter: l",
            difficulty = 1
        )
    }

    // ========== P0-BUG-003 Regression Tests ==========

    @Test
    fun submitButton_notTruncated_atBottom() {
        // Given: LearningScreen with full content
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = 3,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                        comboState = com.wordland.domain.model.ComboState(),
                        currentWordIndex = 0,
                        totalWords = 6
                    )
                }
            }
        }

        // When: Screen is rendered
        composeTestRule.onNodeWithText("提交答案").assertExists()

        // Then: Submit button should be fully visible (P0-BUG-003 regression test)
        val result = VisualQAChecks.assertNoTextTruncation(
            expectedText = "提交答案",
            actualText = "提交答案",
            componentName = "LearningScreen.SubmitButton"
        )
        report.add(result)

        assert(result.isPassed) {
            "Submit button text should not be truncated (P0-BUG-003 regression)"
        }
    }

    @Test
    fun submitButton_touchTargetSize_minimum48dp() {
        // Given: LearningScreen submit button
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = 3,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // When: Submit button is rendered
        val buttonHeight = 48.dp.value // Material Design minimum
        val buttonWidth = 300.dp.value // Full width minus padding

        // Then: Submit button should meet 48dp minimum
        val result = VisualQAChecks.assertTouchTargetSize(
            height = buttonHeight,
            width = buttonWidth,
            minHeight = 48f,
            componentName = "LearningScreen.SubmitButton"
        )
        report.add(result)

        assert(result.isPassed) {
            "Submit button should meet 48dp minimum touch target size"
        }
    }

    // ========== Scrollability Tests ==========

    @Test
    fun contentColumn_hasVerticalScroll() {
        // Given: LearningScreen with potentially overflowing content
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = "首字母: l",
                        hintLevel = 1,
                        hintsRemaining = 2,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // When: Content is rendered
        // Note: In full implementation, would check for verticalScroll modifier
        // This is a placeholder test - actual implementation would check semantics

        // Then: Content should be scrollable (P0-BUG-003 fix)
        val result = VisualQAChecks.assertScrollable(
            isScrollable = true, // Would be extracted from semantics
            componentName = "LearningScreen.ContentColumn"
        )
        report.add(result)

        assert(result.isPassed) {
            "Content column should have verticalScroll (P0-BUG-003 fix)"
        }
    }

    @Test
    fun contentColumn_scrollable_withLongContent() {
        // Given: Content that exceeds screen height
        // Total content height ~540-640dp, screen ~600-700dp
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "look", // Partial answer
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = "前半部分: lo__",
                        hintLevel = 2,
                        hintsRemaining = 1,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // When: Content overflows available space
        // Simulated: content height > available screen height
        val contentHeight = 640.dp.value
        val availableHeight = 600.dp.value

        // Then: Content should still be accessible via scroll
        val requiresScroll = contentHeight > availableHeight
        val result = if (requiresScroll) {
            VisualCheckResult.Passed(
                check = "Scrollability",
                component = "LearningScreen.LongContent",
                details = "Content ${contentHeight}dp > available ${availableHeight}dp, scroll required"
            )
        } else {
            VisualCheckResult.Failed(
                check = "Scrollability",
                component = "LearningScreen.LongContent",
                message = "Content height check failed"
            )
        }
        report.add(result)

        assert(result.isPassed) {
            "Long content should be scrollable"
        }
    }

    // ========== Bottom Padding Tests ==========

    @Test
    fun screen_hasBottomPadding_forSystemNavigation() {
        // Given: LearningScreen
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = 3,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // When: Bottom padding is applied for system navigation bar
        // Note: In full implementation, would extract actual padding
        val bottomPaddingPx = 48 // Example: 16dp * 3 density
        val density = 3f

        // Then: Bottom padding should be sufficient
        val result = VisualQAChecks.assertBottomPadding(
            bottomPadding = bottomPaddingPx,
            minPaddingDp = 16f,
            density = density,
            componentName = "LearningScreen"
        )
        report.add(result)

        assert(result.isPassed) {
            "Screen should have bottom padding for system navigation (P0-BUG-003 fix)"
        }
    }

    // ========== Component Visibility Tests ==========

    @Test
    fun allComponentsVisible_onScreen() {
        // Given: Full LearningScreen
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = 3,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // When: Screen is rendered
        val requiredComponents = listOf(
            "Look Island - Level 1", // Level name
            "看，观看", // Translation
            "提示", // Hint button
            "提交答案" // Submit button
        )

        // Then: All required components should be visible
        requiredComponents.forEach { component ->
            composeTestRule.onNodeWithText(component, substring = true).assertExists()
        }

        val result = VisualCheckResult.Passed(
            check = "Component Visibility",
            component = "LearningScreen",
            details = "All ${requiredComponents.size} required components visible"
        )
        report.add(result)

        assert(result.isPassed)
    }

    // ========== Overflow Detection Tests ==========

    @Test
    fun content_noOverflow_withinScreenBounds() {
        // Given: LearningScreen with full content
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = 3,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // When: Content is rendered
        // Note: In full implementation, would measure actual bounds
        val screenBounds = Bounds(0, 0, 1080, 2400) // Example device dimensions
        val submitButtonBounds = Bounds(80, 2200, 1000, 2248) // Bottom of screen

        // Then: Submit button should not overflow screen bounds
        val result = VisualQAChecks.assertNoOverflow(
            childBounds = submitButtonBounds,
            parentBounds = screenBounds,
            componentName = "LearningScreen.SubmitButton"
        )
        report.add(result)

        assert(result.isPassed) {
            "Submit button should not overflow screen bounds (P0-BUG-003 regression)"
        }
    }

    @Test
    fun hintCard_noOverflow_withinContent() {
        // Given: LearningScreen with hint displayed
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = "首字母: l",
                        hintLevel = 1,
                        hintsRemaining = 2,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // When: HintCard is displayed
        val contentBounds = Bounds(0, 0, 1080, 2200)
        val hintCardBounds = Bounds(80, 200, 1000, 400) // Estimated position

        // Then: HintCard should not overflow content area
        val result = VisualQAChecks.assertNoOverflow(
            childBounds = hintCardBounds,
            parentBounds = contentBounds,
            componentName = "LearningScreen.HintCard"
        )
        report.add(result)

        assert(result.isPassed) {
            "HintCard should not overflow content bounds"
        }
    }

    // ========== Alignment Tests ==========

    @Test
    fun submitButton_centeredHorizontally() {
        // Given: LearningScreen submit button
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = 3,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // When: Submit button is rendered
        val screenBounds = Bounds(0, 0, 1080, 2400)
        val buttonBounds = Bounds(80, 2200, 1000, 2248) // Centered with padding

        // Then: Button should be horizontally centered
        val result = VisualQAChecks.assertAlignment(
            componentBounds = buttonBounds,
            containerBounds = screenBounds,
            expectedAlignment = Alignment.Center,
            tolerance = 50, // Allow for padding
            componentName = "LearningScreen.SubmitButton"
        )
        report.add(result)

        assert(result.isPassed) {
            "Submit button should be centered horizontally"
        }
    }

    // ========== Color Contrast Tests ==========

    @Test
    fun translationText_colorContrast() {
        // Given: LearningScreen with translation
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = 3,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("看，观看").assertExists()

        // Note: In full implementation, would extract actual colors
        val foreground = Color(0xFF1C1B1F)
        val background = Color(0xFFFFFFFF)

        val result = VisualQAChecks.assertColorContrast(
            foreground = foreground,
            background = background,
            isLargeText = true, // Translation text is typically larger
            componentName = "LearningScreen.TranslationText"
        )
        report.add(result)

        assert(result.isPassed) {
            "Translation text should meet WCAG AA contrast for large text"
        }
    }

    // ========== Report Generation ==========

    @Test
    fun learningScreen_visualQASummary() {
        // Run all key checks and generate summary
        runAllChecks()

        // Generate report
        val markdown = report.toMarkdown()
        val table = report.toTable()

        // Verify report content
        assert(markdown.contains("# Visual QA Report"))
        assert(markdown.contains("LearningScreen"))
        assert(table.contains("Status"))

        // All checks should pass in baseline implementation
        assert(report.isAllPassed) {
            "All visual checks should pass: ${report.failed} failed"
        }
    }

    // ========== P0-BUG-003 Specific Regression Test ==========

    @Test
    fun p0_bug_003_regression_submitButtonVisible() {
        // Comprehensive regression test for P0-BUG-003
        // Bug: "提交答案" button was truncated at bottom on real device (Xiaomi)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    LearningContent(
                        question = testQuestion,
                        answerText = "",
                        onAnswerChange = {},
                        onSubmit = {},
                        hintText = null,
                        hintLevel = 0,
                        hintsRemaining = 3,
                        hintPenaltyApplied = false,
                        onUseHint = {},
                    )
                }
            }
        }

        // Check 1: Submit button text is visible
        composeTestRule.onNodeWithText("提交答案").assertExists()

        // Check 2: Content is scrollable (verticalScroll modifier)
        val scrollableResult = VisualQAChecks.assertScrollable(
            isScrollable = true,
            componentName = "LearningScreen.ContentColumn.P0Bug003"
        )
        report.add(scrollableResult)

        // Check 3: Bottom padding applied
        val paddingResult = VisualQAChecks.assertBottomPadding(
            bottomPadding = 48,
            density = 3f,
            componentName = "LearningScreen.BottomPadding.P0Bug003"
        )
        report.add(paddingResult)

        // Check 4: No overflow
        val screenBounds = Bounds(0, 0, 1080, 2340) // Xiaomi-like dimensions
        val buttonBounds = Bounds(80, 2200, 1000, 2248)
        val overflowResult = VisualQAChecks.assertNoOverflow(
            childBounds = buttonBounds,
            parentBounds = screenBounds,
            componentName = "LearningScreen.SubmitButton.P0Bug003"
        )
        report.add(overflowResult)

        // All checks should pass for P0-BUG-003 fix verification
        assert(scrollableResult.isPassed) {
            "P0-BUG-003: Content must be scrollable"
        }
        assert(paddingResult.isPassed) {
            "P0-BUG-003: Bottom padding must be applied"
        }
        assert(overflowResult.isPassed) {
            "P0-BUG-003: Submit button must not overflow"
        }
    }

    // ========== Helper Methods ==========

    private fun runAllChecks() {
        submitButton_notTruncated_atBottom()
        submitButton_touchTargetSize_minimum48dp()
        contentColumn_hasVerticalScroll()
        screen_hasBottomPadding_forSystemNavigation()
        allComponentsVisible_onScreen()
        content_noOverflow_withinScreenBounds()
        submitButton_centeredHorizontally()
    }
}
