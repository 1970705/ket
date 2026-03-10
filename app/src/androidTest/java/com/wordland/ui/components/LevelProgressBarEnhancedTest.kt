package com.wordland.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.domain.model.ComboState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for LevelProgressBarEnhanced component
 *
 * Tests:
 * - Progress counter display
 * - Progress bar rendering
 * - Motivational messages for different states
 * - Combo indicator display
 * - Edge cases (zero words, last word, first word)
 *
 * Part of Epic #7: Test Coverage Improvement
 */
@RunWith(AndroidJUnit4::class)
class LevelProgressBarEnhancedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysProgressCounter_correctFormat() {
        // Given: 3 out of 6 words completed
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 3,
                    totalWords = 6,
                    comboState = ComboState(),
                )
            }
        }

        // Then: Progress counter should show "3 / 6"
        composeTestRule.onNodeWithText("3 / 6").assertExists()
    }

    @Test
    fun displaysProgressCounter_atStart() {
        // Given: 0 out of 6 words completed
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 0,
                    totalWords = 6,
                    comboState = ComboState(),
                )
            }
        }

        // Then: Should show "0 / 6"
        composeTestRule.onNodeWithText("0 / 6").assertExists()
    }

    @Test
    fun displaysProgressCounter_atCompletion() {
        // Given: All 6 words completed
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 6,
                    totalWords = 6,
                    comboState = ComboState(),
                )
            }
        }

        // Then: Should show "6 / 6"
        composeTestRule.onNodeWithText("6 / 6").assertExists()
    }

    @Test
    fun showsStartMessage_forFirstWord() {
        // Given: First word (currentWord = 0)
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 0,
                    totalWords = 6,
                    comboState = ComboState(),
                )
            }
        }

        // Then: Should show "开始吧！"
        composeTestRule.onNodeWithText("开始吧！").assertExists()
    }

    @Test
    fun showsLastWordMessage_whenOnLastWord() {
        // Given: On last word (5th out of 6)
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 5,
                    totalWords = 6,
                    comboState = ComboState(),
                )
            }
        }

        // Then: Should show "最后一个词！"
        composeTestRule.onNodeWithText("最后一个词！").assertExists()
    }

    @Test
    fun showsHotStreakMessage_whenHighCombo() {
        // Given: High combo streak (5+ consecutive correct)
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 3,
                    totalWords = 6,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 5,
                            totalCorrect = 5,
                            totalAttempts = 5,
                        ),
                )
            }
        }

        // Then: Should show fire emoji and hot streak message
        composeTestRule.onNodeWithText("🔥 你在燃烧！").assertExists()
    }

    @Test
    fun showsComboIndicator_withComboCount() {
        // Given: 3 consecutive correct answers
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 3,
                    totalWords = 6,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 3,
                            totalCorrect = 3,
                            totalAttempts = 3,
                        ),
                )
            }
        }

        // Then: Should show combo count
        composeTestRule.onNodeWithText("x3").assertExists()
    }

    @Test
    fun doesNotShowComboIndicator_whenNoCombo() {
        // Given: No consecutive correct answers
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 2,
                    totalWords = 6,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 0,
                            totalCorrect = 1,
                            totalAttempts = 3,
                        ),
                )
            }
        }

        // Then: Combo indicator should not be visible
        composeTestRule.onNodeWithText("x1").assertDoesNotExist()
    }

    @Test
    fun showsGreatJobMessage_forModerateCombo() {
        // Given: Combo threshold 3 (ComboState.COMBO_THRESHOLD_3X = 3)
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 3,
                    totalWords = 6,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 3,
                            totalCorrect = 3,
                            totalAttempts = 3,
                        ),
                )
            }
        }

        // Then: Should show "太棒了！"
        composeTestRule.onNodeWithText("太棒了！").assertExists()
    }

    @Test
    fun showsKeepGoingMessage_forSmallCombo() {
        // Given: Small combo streak (1-2 consecutive)
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 2,
                    totalWords = 6,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 1,
                            totalCorrect = 2,
                            totalAttempts = 4,
                        ),
                )
            }
        }

        // Then: Should show "继续前进！"
        composeTestRule.onNodeWithText("继续前进！").assertExists()
    }

    @Test
    fun showsHotStreakIcon_whenComboIsHigh() {
        // Given: High combo (5+)
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 5,
                    totalWords = 10,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 6,
                            totalCorrect = 6,
                            totalAttempts = 6,
                        ),
                )
            }
        }

        // Then: Fire emoji should be visible
        composeTestRule.onNodeWithText("🔥").assertExists()
    }

    @Test
    fun handlesZeroTotalWords_gracefully() {
        // Given: Edge case with zero total words
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 0,
                    totalWords = 0,
                    comboState = ComboState(),
                )
            }
        }

        // Then: Should still render without crashing
        composeTestRule.onNodeWithText("0 / 0").assertExists()
    }

    @Test
    fun handlesSingleWordLevel() {
        // Given: Level with only 1 word
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 0,
                    totalWords = 1,
                    comboState = ComboState(),
                )
            }
        }

        // Then: Should show first word message
        composeTestRule.onNodeWithText("开始吧！").assertExists()
        composeTestRule.onNodeWithText("0 / 1").assertExists()
    }

    @Test
    fun respectsShowComboParameter_false() {
        // Given: Combo exists but showCombo is false
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 3,
                    totalWords = 6,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 3,
                            totalCorrect = 3,
                            totalAttempts = 3,
                        ),
                    showCombo = false,
                )
            }
        }

        // Then: Combo indicator should not be visible
        composeTestRule.onNodeWithText("x3").assertDoesNotExist()
    }

    @Test
    fun respectsShowComboParameter_true() {
        // Given: Combo exists and showCombo is true (default)
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 3,
                    totalWords = 6,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 3,
                            totalCorrect = 3,
                            totalAttempts = 3,
                        ),
                    showCombo = true,
                )
            }
        }

        // Then: Combo indicator should be visible
        composeTestRule.onNodeWithText("x3").assertExists()
    }

    @Test
    fun displaysLastWordMessage_takesPriorityOverHotStreak() {
        // Given: On last word with hot streak
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 5,
                    totalWords = 6,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 5,
                            totalCorrect = 5,
                            totalAttempts = 5,
                        ),
                )
            }
        }

        // Then: "最后一个词！" should be shown (priority over hot streak)
        composeTestRule.onNodeWithText("最后一个词！").assertExists()
    }

    @Test
    fun handlesLargeWordCounts() {
        // Given: Level with 20 words
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 15,
                    totalWords = 20,
                    comboState = ComboState(),
                )
            }
        }

        // Then: Should display correctly
        composeTestRule.onNodeWithText("15 / 20").assertExists()
    }

    @Test
    fun compactVariant_displaysProgressAndBar() {
        // Given: Using compact variant
        composeTestRule.setContent {
            MaterialTheme {
                CompactLevelProgressBar(
                    currentWord = 3,
                    totalWords = 6,
                )
            }
        }

        // Then: Should show progress counter
        composeTestRule.onNodeWithText("3 / 6").assertExists()
    }

    @Test
    fun compactVariant_showsCorrectProgress() {
        // Given: Compact variant at different progress points
        composeTestRule.setContent {
            MaterialTheme {
                CompactLevelProgressBar(
                    currentWord = 5,
                    totalWords = 10,
                )
            }
        }

        // Then: Should show 5/10 (50% progress)
        composeTestRule.onNodeWithText("5 / 10").assertExists()
    }

    @Test
    fun compactVariant_handlesZeroProgress() {
        // Given: Compact variant at start
        composeTestRule.setContent {
            MaterialTheme {
                CompactLevelProgressBar(
                    currentWord = 0,
                    totalWords = 10,
                )
            }
        }

        // Then: Should show 0/10
        composeTestRule.onNodeWithText("0 / 10").assertExists()
    }

    @Test
    fun compactVariant_handlesFullProgress() {
        // Given: Compact variant at completion
        composeTestRule.setContent {
            MaterialTheme {
                CompactLevelProgressBar(
                    currentWord = 10,
                    totalWords = 10,
                )
            }
        }

        // Then: Should show 10/10
        composeTestRule.onNodeWithText("10 / 10").assertExists()
    }

    @Test
    fun hotStreakMakesProgressBold() {
        // Note: This test verifies the component renders; bold is visual
        // Given: Hot streak active
        composeTestRule.setContent {
            MaterialTheme {
                LevelProgressBarEnhanced(
                    currentWord = 5,
                    totalWords = 10,
                    comboState =
                        ComboState(
                            consecutiveCorrect = 6,
                            totalCorrect = 6,
                            totalAttempts = 6,
                        ),
                )
            }
        }

        // Then: Progress counter should still be visible (bold is visual style)
        composeTestRule.onNodeWithText("5 / 10").assertExists()
    }
}
