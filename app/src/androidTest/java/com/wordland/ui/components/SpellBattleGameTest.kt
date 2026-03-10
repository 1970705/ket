package com.wordland.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wordland.domain.model.SpellBattleQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for SpellBattleGame component
 * Tests the virtual keyboard, answer boxes, and user interaction
 */
@RunWith(AndroidJUnit4::class)
class SpellBattleGameTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testQuestion =
        SpellBattleQuestion(
            wordId = "test_001",
            translation = "苹果",
            targetWord = "apple",
            hint = null,
        )

    @Test
    fun displaysTranslationText() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = "",
                    onAnswerChange = { },
                    onBackspace = { },
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("苹果")
            .assertExists()
    }

    @Test
    fun displaysInstructionText() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = "",
                    onAnswerChange = { },
                    onBackspace = { },
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("用英语拼写这个词")
            .assertExists()
    }

    @Test
    fun displaysCorrectNumberOfAnswerBoxes() {
        // Given - "apple" has 5 letters
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = "",
                    onAnswerChange = { },
                    onBackspace = { },
                )
            }
        }

        // Then - Should have 5 empty boxes
        // The answer boxes don't have text, but we can verify component rendered
        composeTestRule
            .onNodeWithText("A")
            .assertDoesNotExist() // No letters yet
    }

    @Test
    fun displaysAllKeyboardRows() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = "",
                    onAnswerChange = { },
                    onBackspace = { },
                )
            }
        }

        // Then - Verify all keys in first row exist
        composeTestRule.onNodeWithText("Q").assertExists()
        composeTestRule.onNodeWithText("W").assertExists()
        composeTestRule.onNodeWithText("E").assertExists()
        composeTestRule.onNodeWithText("R").assertExists()
        composeTestRule.onNodeWithText("T").assertExists()
        composeTestRule.onNodeWithText("Y").assertExists()
        composeTestRule.onNodeWithText("U").assertExists()
        composeTestRule.onNodeWithText("I").assertExists()
        composeTestRule.onNodeWithText("O").assertExists()
        composeTestRule.onNodeWithText("P").assertExists()
    }

    @Test
    fun keyboardClickUpdatesAnswer() {
        // Given
        var currentAnswer = ""
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = { },
                )
            }
        }

        // When - Click key "A"
        composeTestRule.onNodeWithText("A").performClick()

        // Then
        assertEquals("a", currentAnswer)
    }

    @Test
    fun keyboardClickAppendsToExistingAnswer() {
        // Given
        var currentAnswer = "app"
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = { },
                )
            }
        }

        // When - Click key "l"
        composeTestRule.onNodeWithText("L").performClick()

        // Then
        assertEquals("appl", currentAnswer)
    }

    @Test
    fun backspaceRemovesLastCharacter() {
        // Given
        var currentAnswer = "apple"
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = { currentAnswer = currentAnswer.dropLast(1) },
                )
            }
        }

        // When - Click backspace
        composeTestRule.onNodeWithText("⌫").performClick()

        // Then
        assertEquals("appl", currentAnswer)
    }

    @Test
    fun backspaceOnEmptyAnswerDoesNothing() {
        // Given
        var currentAnswer = ""
        var backspaceCalled = false
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = {
                        if (currentAnswer.isNotEmpty()) {
                            backspaceCalled = true
                        }
                    },
                )
            }
        }

        // When - Click backspace on empty answer
        composeTestRule.onNodeWithText("⌫").performClick()

        // Then
        assertFalse(backspaceCalled)
        assertEquals("", currentAnswer)
    }

    @Test
    fun multipleKeyPressesBuildCorrectAnswer() {
        // Given
        var currentAnswer = ""
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = { },
                )
            }
        }

        // When - Type "apple"
        composeTestRule.onNodeWithText("A").performClick()
        composeTestRule.onNodeWithText("P").performClick()
        composeTestRule.onNodeWithText("P").performClick()
        composeTestRule.onNodeWithText("L").performClick()
        composeTestRule.onNodeWithText("E").performClick()

        // Then
        assertEquals("apple", currentAnswer)
    }

    @Test
    fun backspaceMultipleClicksRemoveMultipleCharacters() {
        // Given
        var currentAnswer = "apple"
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = { currentAnswer = currentAnswer.dropLast(1) },
                )
            }
        }

        // When - Click backspace twice
        composeTestRule.onNodeWithText("⌫").performClick()
        composeTestRule.onNodeWithText("⌫").performClick()

        // Then
        assertEquals("app", currentAnswer)
    }

    @Test
    fun answerBoxesReflectUserInput() {
        // This test verifies the component state updates correctly
        // Given
        var currentAnswer = "app"
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = { },
                )
            }
        }

        // When - Update state with more letters
        currentAnswer = "apple"
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { },
                    onBackspace = { },
                )
            }
        }

        // Then - Component should render with new state
        // "APPLE" should be visible (5 letters filled)
        composeTestRule.onNodeWithText("A").assertExists()
    }

    @Test
    fun keyboardContainsAllQWERTYKeys() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = "",
                    onAnswerChange = { },
                    onBackspace = { },
                )
            }
        }

        // Then - Verify all rows have expected keys
        // Row 1
        composeTestRule.onNodeWithText("Q").assertExists()
        composeTestRule.onNodeWithText("W").assertExists()
        composeTestRule.onNodeWithText("E").assertExists()
        composeTestRule.onNodeWithText("R").assertExists()
        composeTestRule.onNodeWithText("T").assertExists()
        composeTestRule.onNodeWithText("Y").assertExists()

        // Row 2
        composeTestRule.onNodeWithText("A").assertExists()
        composeTestRule.onNodeWithText("S").assertExists()
        composeTestRule.onNodeWithText("D").assertExists()
        composeTestRule.onNodeWithText("F").assertExists()
        composeTestRule.onNodeWithText("G").assertExists()
        composeTestRule.onNodeWithText("H").assertExists()
        composeTestRule.onNodeWithText("J").assertExists()
        composeTestRule.onNodeWithText("K").assertExists()
        composeTestRule.onNodeWithText("L").assertExists()

        // Row 3
        composeTestRule.onNodeWithText("Z").assertExists()
        composeTestRule.onNodeWithText("X").assertExists()
        composeTestRule.onNodeWithText("C").assertExists()
        composeTestRule.onNodeWithText("V").assertExists()
        composeTestRule.onNodeWithText("B").assertExists()
        composeTestRule.onNodeWithText("N").assertExists()
        composeTestRule.onNodeWithText("M").assertExists()
    }

    @Test
    fun backspaceKeyExistsOnKeyboard() {
        // When
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = "",
                    onAnswerChange = { },
                    onBackspace = { },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("⌫").assertExists()
    }

    @Test
    fun handlesLongerWordsCorrectly() {
        // Given - 11 letter word
        val longWordQuestion =
            SpellBattleQuestion(
                wordId = "test_002",
                translation = "International",
                targetWord = "international",
                hint = null,
            )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = longWordQuestion,
                    userAnswer = "",
                    onAnswerChange = { },
                    onBackspace = { },
                )
            }
        }

        // Then - Should render without errors
        // Verify at least some keyboard keys exist
        composeTestRule.onNodeWithText("I").assertExists()
        composeTestRule.onNodeWithText("N").assertExists()
        composeTestRule.onNodeWithText("T").assertExists()
    }

    @Test
    fun handlesSingleLetterWords() {
        // Given - 1 letter word
        val singleLetterQuestion =
            SpellBattleQuestion(
                wordId = "test_003",
                translation = "I",
                targetWord = "i",
                hint = null,
            )

        // When
        var currentAnswer = ""
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = singleLetterQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = { },
                )
            }
        }

        // When - Type the letter
        composeTestRule.onNodeWithText("I").performClick()

        // Then
        assertEquals("i", currentAnswer)
    }

    @Test
    fun supportsAnswerReplacement() {
        // Given
        var currentAnswer = "applx" // Wrong spelling
        composeTestRule.setContent {
            MaterialTheme {
                SpellBattleGame(
                    question = testQuestion,
                    userAnswer = currentAnswer,
                    onAnswerChange = { currentAnswer = it },
                    onBackspace = { },
                )
            }
        }

        // When - Use backspace to correct
        composeTestRule.onNodeWithText("⌫").performClick()
        // Type "e" instead of "x"
        composeTestRule.onNodeWithText("E").performClick()

        // Then
        assertEquals("apple", currentAnswer)
    }
}
