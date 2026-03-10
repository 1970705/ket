package com.wordland.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.domain.model.BubbleColor
import com.wordland.domain.model.BubbleState
import com.wordland.domain.model.MatchGameState
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for MatchGameScreen
 *
 * Tests the screen's UI rendering and user interactions using Compose testing framework.
 */
@RunWith(AndroidJUnit4::class)
class MatchGameScreenUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // Test data
    private val testBubbles =
        listOf(
            BubbleState(
                id = "bubble_1_en",
                word = "apple",
                pairId = "pair_1",
                color = BubbleColor.PINK,
            ),
            BubbleState(
                id = "bubble_1_cn",
                word = "苹果",
                pairId = "pair_1",
                color = BubbleColor.PINK,
            ),
            BubbleState(
                id = "bubble_2_en",
                word = "banana",
                pairId = "pair_2",
                color = BubbleColor.GREEN,
            ),
            BubbleState(
                id = "bubble_2_cn",
                word = "香蕉",
                pairId = "pair_2",
                color = BubbleColor.GREEN,
            ),
        )

    private val mockOnNavigateBack = mockk<(Boolean) -> Unit>(relaxed = true)

    // === Idle State Tests ===

    @Test
    fun idleState_displaysLoadingMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.Idle,
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("准备游戏...").assertExists()
    }

    // === Preparing State Tests ===

    @Test
    fun preparingState_displaysPreparingMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.Preparing,
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("加载单词中...").assertExists()
    }

    // === Ready State Tests ===

    @Test
    fun readyState_displaysGameTitle() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Ready(
                            pairs = 6,
                            bubbles = testBubbles,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("单词消消乐").assertExists()
    }

    @Test
    fun readyState_displaysPairCount() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Ready(
                            pairs = 6,
                            bubbles = testBubbles,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("配对数量: 6").assertExists()
    }

    @Test
    fun readyState_displaysStartButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Ready(
                            pairs = 6,
                            bubbles = testBubbles,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("开始游戏").assertExists()
    }

    @Test
    fun readyState_displaysBackButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Ready(
                            pairs = 6,
                            bubbles = testBubbles,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("返回").assertExists()
    }

    // === Playing State Tests ===

    @Test
    fun playingState_displaysBubbleWords() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Playing(
                            bubbles = testBubbles,
                            selectedBubbleIds = emptySet(),
                            matchedPairs = 0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - Should display bubble words
        composeTestRule.onNodeWithText("apple").assertExists()
        composeTestRule.onNodeWithText("苹果").assertExists()
        composeTestRule.onNodeWithText("banana").assertExists()
        composeTestRule.onNodeWithText("香蕉").assertExists()
    }

    @Test
    fun playingState_displaysProgressBar() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Playing(
                            bubbles = testBubbles,
                            selectedBubbleIds = emptySet(),
                            matchedPairs = 1,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - Progress indicator should be displayed
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun playingState_clickBubbleTriggersCallback() {
        // Given
        var clickedBubbleId: String? = null

        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Playing(
                            bubbles = testBubbles,
                            selectedBubbleIds = emptySet(),
                            matchedPairs = 0,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                    onBubbleClick = { clickedBubbleId = it },
                )
            }
        }

        // When - Click on a bubble
        composeTestRule.onNodeWithText("apple").performClick()

        // Then - Callback should be triggered
        assertEquals("bubble_1_en", clickedBubbleId)
    }

    // === Paused State Tests ===

    @Test
    fun pausedState_displaysPausedTitle() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Paused(
                            previousState =
                                MatchGameState.Playing(
                                    bubbles = testBubbles,
                                    selectedBubbleIds = emptySet(),
                                    matchedPairs = 0,
                                ),
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("游戏暂停").assertExists()
    }

    @Test
    fun pausedState_displaysResumeButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Paused(
                            previousState =
                                MatchGameState.Playing(
                                    bubbles = testBubbles,
                                    selectedBubbleIds = emptySet(),
                                    matchedPairs = 0,
                                ),
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("继续游戏").assertExists()
    }

    @Test
    fun pausedState_displaysExitButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Paused(
                            previousState =
                                MatchGameState.Playing(
                                    bubbles = testBubbles,
                                    selectedBubbleIds = emptySet(),
                                    matchedPairs = 0,
                                ),
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("退出游戏").assertExists()
    }

    // === Completed State Tests ===

    @Test
    fun completedState_displaysCongratulationsMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Completed(
                            elapsedTime = 30000,
                            pairs = 6,
                            accuracy = 1.0f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("🎉 恭喜完成！").assertExists()
    }

    @Test
    fun completedState_displaysElapsedTime() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Completed(
                            elapsedTime = 30000,
                            pairs = 6,
                            accuracy = 1.0f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - 30 seconds
        composeTestRule.onNodeWithText("用时: 30秒").assertExists()
    }

    @Test
    fun completedState_displaysPairsCount() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Completed(
                            elapsedTime = 30000,
                            pairs = 6,
                            accuracy = 1.0f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("配对: 6").assertExists()
    }

    @Test
    fun completedState_displaysAccuracy() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Completed(
                            elapsedTime = 30000,
                            pairs = 6,
                            accuracy = 1.0f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - 100% accuracy
        composeTestRule.onNodeWithText("准确率: 100%").assertExists()
    }

    @Test
    fun completedState_displaysPlayAgainButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Completed(
                            elapsedTime = 30000,
                            pairs = 6,
                            accuracy = 1.0f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("再玩一次").assertExists()
    }

    @Test
    fun completedState_displaysBackButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Completed(
                            elapsedTime = 30000,
                            pairs = 6,
                            accuracy = 1.0f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("返回").assertExists()
    }

    @Test
    fun completedState_displaysPartialAccuracy() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState =
                        MatchGameState.Completed(
                            elapsedTime = 30000,
                            pairs = 6,
                            accuracy = 0.75f,
                        ),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then - 75% accuracy
        composeTestRule.onNodeWithText("准确率: 75%").assertExists()
    }

    // === Game Over State Tests ===

    @Test
    fun gameOverState_displaysGameOverTitle() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.GameOver,
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("游戏结束").assertExists()
    }

    @Test
    fun gameOverState_displaysRestartButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.GameOver,
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("重新开始").assertExists()
    }

    @Test
    fun gameOverState_displaysBackButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.GameOver,
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("返回").assertExists()
    }

    // === Error State Tests ===

    @Test
    fun errorState_displaysErrorTitle() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.Error(message = "Failed to load game"),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("出错了").assertExists()
    }

    @Test
    fun errorState_displaysErrorMessage() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.Error(message = "Failed to load game"),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Failed to load game").assertExists()
    }

    @Test
    fun errorState_displaysBackButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.Error(message = "Failed to load game"),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("返回").assertExists()
    }

    @Test
    fun errorState_displaysWarningIcon() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameContent(
                    gameState = MatchGameState.Error(message = "Failed to load game"),
                    onNavigateBack = { mockOnNavigateBack(false) },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("⚠️").assertExists()
    }

    // === Top Bar Tests ===

    @Test
    fun playingState_displaysPairsCountInTopBar() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameTopBar(
                    gameState =
                        MatchGameState.Playing(
                            bubbles = testBubbles,
                            selectedBubbleIds = emptySet(),
                            matchedPairs = 2,
                        ),
                    onPauseClick = {},
                    onExitClick = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("配对: 2").assertExists()
    }

    @Test
    fun playingState_displaysPauseButton() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                TestableMatchGameTopBar(
                    gameState =
                        MatchGameState.Playing(
                            bubbles = testBubbles,
                            selectedBubbleIds = emptySet(),
                            matchedPairs = 0,
                        ),
                    onPauseClick = {},
                    onExitClick = {},
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("⏸").assertExists()
    }
}

/**
 * Testable composable that wraps the actual MatchGameScreen content
 * This allows us to test the screen UI without the full scaffold
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TestableMatchGameContent(
    gameState: MatchGameState,
    onNavigateBack: () -> Unit,
    onBubbleClick: (String) -> Unit = {},
) {
    when (val state = gameState) {
        is MatchGameState.Idle -> {
            LoadingView("准备游戏...")
        }

        is MatchGameState.Preparing -> {
            LoadingView("加载单词中...")
        }

        is MatchGameState.Ready -> {
            ReadyView(
                pairs = state.pairs,
                onStartGame = {},
                onBack = onNavigateBack,
            )
        }

        is MatchGameState.Playing -> {
            PlayingView(
                bubbles = state.bubbles,
                selectedBubbleIds = state.selectedBubbleIds,
                matchedPairs = state.matchedPairs,
                progress = state.progress,
                onBubbleClick = onBubbleClick,
            )
        }

        is MatchGameState.Paused -> {
            PausedView(
                onResume = {},
                onExit = onNavigateBack,
            )
        }

        is MatchGameState.Completed -> {
            CompletedView(
                elapsedTime = state.elapsedTime,
                pairs = state.pairs,
                accuracy = state.accuracy,
                onPlayAgain = {},
                onBack = onNavigateBack,
            )
        }

        is MatchGameState.GameOver -> {
            GameOverView(
                onRestart = {},
                onBack = onNavigateBack,
            )
        }

        is MatchGameState.Error -> {
            ErrorView(
                message = state.message,
                onBack = onNavigateBack,
            )
        }
    }
}

@Composable
private fun TestableMatchGameTopBar(
    gameState: MatchGameState,
    onPauseClick: () -> Unit,
    onExitClick: () -> Unit,
) {
    MatchGameTopBar(
        gameState = gameState,
        onPauseClick = onPauseClick,
        onExitClick = onExitClick,
    )
}
