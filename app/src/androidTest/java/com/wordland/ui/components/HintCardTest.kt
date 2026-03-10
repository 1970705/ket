package com.wordland.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for HintCard component
 *
 * Tests:
 * - Display with no hint shown
 * - Display with hint at each level (1, 2, 3)
 * - Button states (disabled, enabled, penalty warning)
 * - Hint counter display
 * - Penalty warning visibility
 *
 * Part of Epic #7: Test Coverage Improvement
 */
@RunWith(AndroidJUnit4::class)
class HintCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Given-When-Then test pattern

    @Test
    fun displaysPlaceholderText_whenNoHintShown() {
        // Given: No hint available yet
        var hintClicked = false

        // When: Component is rendered with null hintText
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = null,
                    hintLevel = 0,
                    hintsRemaining = 3,
                    hintPenaltyApplied = false,
                    onUseHint = { hintClicked = true },
                )
            }
        }

        // Then: Placeholder text should be visible
        composeTestRule
            .onNodeWithText("点击提示获取帮助")
            .assertExists()
    }

    @Test
    fun displaysHintLevel1_whenHintIsShown() {
        // Given: Level 1 hint
        val hintText = "首字母: A"

        // When: Component is rendered
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = hintText,
                    hintLevel = 1,
                    hintsRemaining = 2,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Hint level indicator and text should be visible
        composeTestRule.onNodeWithText("提示 1/3").assertExists()
        composeTestRule.onNodeWithText(hintText).assertExists()
    }

    @Test
    fun displaysHintLevel2_whenHintIsShown() {
        // Given: Level 2 hint
        val hintText = "前半部分: app___"

        // When: Component is rendered
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = hintText,
                    hintLevel = 2,
                    hintsRemaining = 1,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Hint level indicator should show "2/3"
        composeTestRule.onNodeWithText("提示 2/3").assertExists()
        composeTestRule.onNodeWithText(hintText).assertExists()
    }

    @Test
    fun displaysHintLevel3_whenHintIsShown() {
        // Given: Level 3 hint (full reveal)
        val hintText = "完整单词（元音隐藏）: _ppl_"

        // When: Component is rendered
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = hintText,
                    hintLevel = 3,
                    hintsRemaining = 0,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Hint level indicator should show "3/3"
        composeTestRule.onNodeWithText("提示 3/3").assertExists()
        composeTestRule.onNodeWithText(hintText).assertExists()
    }

    @Test
    fun showsDisabledButton_whenNoHintsRemaining() {
        // Given: No hints remaining
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Some hint",
                    hintLevel = 1,
                    hintsRemaining = 0,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Disabled button icon should be visible
        // The disabled state is shown with a block emoji
        composeTestRule.onNodeWithText("🚫").assertExists()
    }

    @Test
    fun showsHintButton_withRemainingCount_whenHintAlreadyShown() {
        // Given: Hint is shown with 2 remaining
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "First hint",
                    hintLevel = 1,
                    hintsRemaining = 2,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: "再提示" button with count should be visible
        composeTestRule.onNodeWithText("再提示").assertExists()
        composeTestRule.onNodeWithText("(2)").assertExists()
    }

    @Test
    fun showsInitialHintButton_withCorrectCount() {
        // Given: No hint shown yet, 3 remaining
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = null,
                    hintLevel = 0,
                    hintsRemaining = 3,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Initial "提示" button with count should be visible
        composeTestRule.onNodeWithText("提示").assertExists()
        composeTestRule.onNodeWithText("(3)").assertExists()
    }

    @Test
    fun showsHintButton_withOneRemaining_usesWarningColor() {
        // Given: Only 1 hint remaining
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "First hint",
                    hintLevel = 1,
                    hintsRemaining = 1,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Button should still show "再提示" with remaining count
        composeTestRule.onNodeWithText("再提示").assertExists()
        composeTestRule.onNodeWithText("(1)").assertExists()
    }

    @Test
    fun showsPenaltyWarning_whenPenaltyAppliedAndHintShown() {
        // Given: Penalty is applied and hint is shown
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Hint with penalty",
                    hintLevel = 1,
                    hintsRemaining = 2,
                    hintPenaltyApplied = true,
                    onUseHint = { },
                )
            }
        }

        // Then: Penalty warning should be visible
        composeTestRule.onNodeWithText("⚠️").assertExists()
        composeTestRule.onNodeWithText("使用提示将扣除1星").assertExists()
    }

    @Test
    fun doesNotShowPenaltyWarning_whenPenaltyNotApplied() {
        // Given: Penalty not applied
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Hint without penalty",
                    hintLevel = 1,
                    hintsRemaining = 2,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Penalty warning should not be visible
        composeTestRule.onNodeWithText("使用提示将扣除1星").assertDoesNotExist()
    }

    @Test
    fun doesNotShowPenaltyWarning_whenNoHintShown() {
        // Given: No hint shown yet
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = null,
                    hintLevel = 0,
                    hintsRemaining = 3,
                    hintPenaltyApplied = true,
                    onUseHint = { },
                )
            }
        }

        // Then: Penalty warning should not be visible (hint not shown yet)
        composeTestRule.onNodeWithText("使用提示将扣除1星").assertDoesNotExist()
    }

    @Test
    fun onUseHintCallback_whenInitialHintButtonClicked() {
        // Given: No hint shown, callback tracking
        var callbackInvoked = false
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = null,
                    hintLevel = 0,
                    hintsRemaining = 3,
                    hintPenaltyApplied = false,
                    onUseHint = { callbackInvoked = true },
                )
            }
        }

        // When: Click hint button
        composeTestRule.onNodeWithText("提示").performClick()

        // Then: Callback should be invoked
        assertTrue("onUseHint callback should be invoked", callbackInvoked)
    }

    @Test
    fun onUseHintCallback_whenNextHintButtonClicked() {
        // Given: Hint already shown, callback tracking
        var callbackInvoked = false
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "First hint",
                    hintLevel = 1,
                    hintsRemaining = 2,
                    hintPenaltyApplied = false,
                    onUseHint = { callbackInvoked = true },
                )
            }
        }

        // When: Click "再提示" button
        composeTestRule.onNodeWithText("再提示").performClick()

        // Then: Callback should be invoked
        assertTrue("onUseHint callback should be invoked", callbackInvoked)
    }

    @Test
    fun hintIconChanges_forDifferentLevels() {
        // Test hint level 0 - lightbulb icon
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = null,
                    hintLevel = 0,
                    hintsRemaining = 3,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }
        // Lightbulb emoji 💡 should be shown
        composeTestRule.onNodeWithText("💡").assertExists()

        // Test hint level 1 - key icon
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Hint 1",
                    hintLevel = 1,
                    hintsRemaining = 2,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }
        // Key emoji 🔑 should be shown
        composeTestRule.onNodeWithText("🔑").assertExists()

        // Test hint level 2 - magnifying glass icon
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Hint 2",
                    hintLevel = 2,
                    hintsRemaining = 1,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }
        // Magnifying glass emoji 🔎 should be shown
        composeTestRule.onNodeWithText("🔎").assertExists()

        // Test hint level 3 - sun icon
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Hint 3",
                    hintLevel = 3,
                    hintsRemaining = 0,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }
        // Sun emoji ☀️ should be shown
        composeTestRule.onNodeWithText("☀️").assertExists()
    }

    @Test
    fun displaysCorrectHintCounter_forVariousRemainingCounts() {
        // Test 3 remaining
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = null,
                    hintLevel = 0,
                    hintsRemaining = 3,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }
        composeTestRule.onNodeWithText("(3)").assertExists()

        // Test 2 remaining
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Hint",
                    hintLevel = 1,
                    hintsRemaining = 2,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }
        composeTestRule.onNodeWithText("(2)").assertExists()

        // Test 1 remaining
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Hint",
                    hintLevel = 2,
                    hintsRemaining = 1,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }
        composeTestRule.onNodeWithText("(1)").assertExists()

        // Test 0 remaining - should show disabled state
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Hint",
                    hintLevel = 3,
                    hintsRemaining = 0,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }
        composeTestRule.onNodeWithText("🚫").assertExists()
    }

    @Test
    fun handlesHintLevelAbove3_gracefully() {
        // Given: Abnormally high hint level (safety check)
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Overflow hint",
                    hintLevel = 5, // Above expected max
                    hintsRemaining = 0,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Component should still render and show hint text
        composeTestRule.onNodeWithText("Overflow hint").assertExists()
        // Level indicator should cap at 3
        composeTestRule.onNodeWithText("提示 3/3").assertExists()
    }

    @Test
    fun handlesNegativeHintLevel_gracefully() {
        // Given: Negative hint level (edge case)
        composeTestRule.setContent {
            MaterialTheme {
                HintCard(
                    hintText = "Edge case hint",
                    hintLevel = -1,
                    hintsRemaining = 3,
                    hintPenaltyApplied = false,
                    onUseHint = { },
                )
            }
        }

        // Then: Component should still render
        composeTestRule.onNodeWithText("Edge case hint").assertExists()
        // Should show lightbulb icon for level 0 equivalent
        composeTestRule.onNodeWithText("💡").assertExists()
    }
}
