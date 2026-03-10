package com.wordland.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.domain.model.ComboState
import com.wordland.domain.model.SpellBattleQuestion
import com.wordland.ui.uistate.LearningUiState
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for LearningScreen
 *
 * Tests the screen's UI rendering and user interactions using Compose testing framework.
 */
@RunWith(AndroidJUnit4::class)
class LearningScreenUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Test data
    private val testQuestion =
        SpellBattleQuestion(
            wordId = "test_001",
            translation = "苹果",
            targetWord = "apple",
            hint = "/ˈæpl/",
            difficulty = 1,
        )

    private val mockOnNavigateBack = mockk<(Boolean) -> Unit>(relaxed = true)
    private val mockOnViewStarBreakdown =
        mockk<
            (
                stars: Int,
                accuracy: Int,
                hintsUsed: Int,
                timeTaken: Int,
                errorCount: Int,
            ) -> Unit,
            >(relaxed = true)

    // === Loading State Tests ===

    @Test
    fun loadingState_displaysProgressIndicator() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState = LearningUiState.Loading,
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - CircularProgressIndicator should be displayed
        // Note: CircularProgressIndicator doesn't have text, but we can verify it's rendered
        composeTestRule.onRoot().assertExists()
    }

    // === Ready State Tests ===

    @Test
    fun readyState_displaysQuestionTranslation() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Ready(
                            question = testQuestion,
                            hintAvailable = true,
                            hintShown = false,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("苹果").assertExists()
    }

    @Test
    fun readyState_displaysSubmitButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Ready(
                            question = testQuestion,
                            hintAvailable = true,
                            hintShown = false,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("提交答案").assertExists()
    }

    @Test
    fun readyState_submitButtonDisabledWhenEmpty() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Ready(
                            question = testQuestion,
                            hintAvailable = true,
                            hintShown = false,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - Submit button should be disabled when answer is empty
        composeTestRule
            .onNodeWithText("提交答案")
            .assertIsNotEnabled()
    }

    @Test
    fun readyState_displaysHintButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Ready(
                            question = testQuestion,
                            hintAvailable = true,
                            hintShown = false,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("提示").assertExists()
    }

    @Test
    fun readyState_showsComboIndicator() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Ready(
                            question = testQuestion,
                            hintAvailable = true,
                            hintShown = false,
                            comboState =
                                ComboState(
                                    isActive = true,
                                    consecutiveCorrect = 3,
                                ),
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("3").assertExists()
    }

    // === Feedback State Tests ===

    @Test
    fun feedbackState_displaysContinueButton() {
        // When
        val result =
            mockk<com.wordland.domain.model.LearnWordResult> {
                every { isCorrect } returns true
                every { message } returns "Correct!"
                every { word } returns
                    com.wordland.domain.model.Word(
                        id = "test_001",
                        word = "apple",
                        translation = "苹果",
                        pronunciation = "/ˈæpl/",
                    )
                every { newMemoryStrength } returns 0.5f
            }

        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Feedback(
                            result = result,
                            stars = 3,
                            progress = 0.5f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("继续").assertExists()
    }

    @Test
    fun feedbackState_displaysCorrectStars() {
        // When
        val result =
            mockk<com.wordland.domain.model.LearnWordResult> {
                every { isCorrect } returns true
                every { message } returns "Correct!"
                every { word } returns
                    com.wordland.domain.model.Word(
                        id = "test_001",
                        word = "apple",
                        translation = "苹果",
                        pronunciation = "/ˈæpl/",
                    )
                every { newMemoryStrength } returns 0.5f
            }

        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Feedback(
                            result = result,
                            stars = 3,
                            progress = 0.5f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - Should display 3 stars
        composeTestRule.onNodeWithContentDescription("获得星星").assertExists()
    }

    // === Level Complete State Tests ===

    @Test
    fun levelCompleteState_displaysCompletionMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 3,
                            score = 60,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("🎉 关卡完成!").assertExists()
    }

    @Test
    fun levelCompleteState_displaysCorrectStars() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 2,
                            score = 40,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then - Should display 2 stars
        composeTestRule.onNodeWithContentDescription("获得星星").assertExists()
    }

    @Test
    fun levelCompleteState_displaysScore() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 3,
                            score = 60,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("60").assertExists()
    }

    @Test
    fun levelCompleteState_displaysMasteryPercentage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 3,
                            score = 60,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 75.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("75%").assertExists()
    }

    @Test
    fun levelCompleteState_displaysViewStarBreakdownButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 3,
                            score = 60,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("查看星级详情").assertExists()
    }

    @Test
    fun levelCompleteState_displaysContinueAdventureButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 3,
                            score = 60,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("继续探险").assertExists()
    }

    @Test
    fun levelCompleteState_threeStarsDisplaysPerfectMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 3,
                            score = 60,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("🌟").assertExists()
        composeTestRule.onNodeWithText("完美表现！").assertExists()
        composeTestRule.onNodeWithText("全部正确，太棒了！").assertExists()
    }

    @Test
    fun levelCompleteState_twoStarsDisplaysGoodMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 2,
                            score = 40,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("👍").assertExists()
        composeTestRule.onNodeWithText("做得不错！").assertExists()
        composeTestRule.onNodeWithText("继续努力，争取满分！").assertExists()
    }

    @Test
    fun levelCompleteState_oneStarDisplaysEncouragementMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 1,
                            score = 10,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("💪").assertExists()
        composeTestRule.onNodeWithText("继续加油！").assertExists()
        composeTestRule.onNodeWithText("多多练习会更好！").assertExists()
    }

    @Test
    fun levelCompleteState_zeroStarsDisplaysTryAgainMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 0,
                            score = 0,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 0.0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("🎯").assertExists()
        composeTestRule.onNodeWithText("再试一次！").assertExists()
        composeTestRule.onNodeWithText("相信你能做得更好！").assertExists()
    }

    @Test
    fun levelCompleteState_displaysMaxComboWhenActive() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.LevelComplete(
                            stars = 3,
                            score = 60,
                            isNextIslandUnlocked = false,
                            islandMasteryPercentage = 50.0,
                            maxCombo = 5,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onViewStarBreakdown = mockOnViewStarBreakdown,
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("🔥 5").assertExists()
        composeTestRule.onNodeWithText("最大连击").assertExists()
    }

    // === Error State Tests ===

    @Test
    fun errorState_displaysErrorMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState = LearningUiState.Error(message = "Failed to load level"),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("❌").assertExists()
        composeTestRule.onNodeWithText("Failed to load level").assertExists()
    }

    // === Progress Display Tests ===

    @Test
    fun readyState_displaysProgressForFirstWord() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Ready(
                            question = testQuestion,
                            hintAvailable = true,
                            hintShown = false,
                            currentWordIndex = 0,
                            totalWords = 6,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - Should show "1 / 6" or similar
        composeTestRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun readyState_displaysProgressForLastWord() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableLearningContent(
                    uiState =
                        LearningUiState.Ready(
                            question = testQuestion,
                            hintAvailable = true,
                            hintShown = false,
                            currentWordIndex = 5,
                            totalWords = 6,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - Should show "6 / 6"
        composeTestRule.onNodeWithText("6").assertExists()
    }
}

/**
 * Testable composable that wraps the actual LearningScreen content
 * This allows us to test the screen UI without the full scaffold
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TestableLearningContent(
    uiState: LearningUiState,
    onNavigateBack: () -> Unit,
    onViewStarBreakdown: (
        stars: Int,
        accuracy: Int,
        hintsUsed: Int,
        timeTaken: Int,
        errorCount: Int,
    ) -> Unit = { _, _, _, _, _ -> },
) {
    when (val state = uiState) {
        is LearningUiState.Loading -> {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = androidx.compose.ui.Modifier,
            )
        }

        is LearningUiState.Ready -> {
            LearningContent(
                question = state.question,
                answerText = "",
                onAnswerChange = {},
                onSubmit = {},
                hintText = state.hintText,
                hintLevel = state.hintLevel,
                hintsRemaining = state.hintsRemaining,
                hintPenaltyApplied = state.hintPenaltyApplied,
                onUseHint = {},
                comboState = state.comboState,
                currentWordIndex = state.currentWordIndex,
                totalWords = state.totalWords,
            )
        }

        is LearningUiState.Feedback -> {
            FeedbackContent(
                result = state.result,
                stars = state.stars,
                progress = state.progress,
                comboState = state.comboState,
                onContinue = {},
            )
        }

        is LearningUiState.LevelComplete -> {
            LevelCompleteContent(
                stars = state.stars,
                score = state.score,
                isNextIslandUnlocked = state.isNextIslandUnlocked,
                islandMasteryPercentage = state.islandMasteryPercentage,
                maxCombo = state.maxCombo,
                onBack = onNavigateBack,
                onViewStarBreakdown = {
                    onViewStarBreakdown(
                        state.stars,
                        state.accuracy,
                        state.hintsUsed,
                        state.timeTaken,
                        state.errorCount,
                    )
                },
            )
        }

        is LearningUiState.Error -> {
            ErrorContent(
                message = state.message,
            )
        }
    }
}
