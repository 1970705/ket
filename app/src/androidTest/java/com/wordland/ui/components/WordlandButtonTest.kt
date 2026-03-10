package com.wordland.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for WordlandButton component
 */
@RunWith(AndroidJUnit4::class)
class WordlandButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun buttonDisplaysTextCorrectly() {
        // Given
        val buttonText = "Start Learning"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { },
                    text = buttonText,
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(buttonText)
            .assertExists()
    }

    @Test
    fun buttonIsEnabledByDefault() {
        // Given
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { },
                    text = "Click Me",
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Click Me")
            .assertIsEnabled()
    }

    @Test
    fun buttonRespectsEnabledState() {
        // Given
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { },
                    text = "Disabled Button",
                    enabled = false,
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Disabled Button")
            .assertIsNotEnabled()
    }

    @Test
    fun buttonClickTriggersOnClick() {
        // Given
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { clicked = true },
                    text = "Click Me",
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Click Me")
            .performClick()

        // Then
        assert(clicked)
    }

    @Test
    fun buttonDoesNotTriggerOnClickWhenDisabled() {
        // Given
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { clicked = true },
                    text = "Disabled Button",
                    enabled = false,
                )
            }
        }

        // When
        composeTestRule
            .onNodeWithText("Disabled Button")
            .performClick()

        // Then
        assert(!clicked)
    }

    @Test
    fun buttonDisplaysDifferentSizes() {
        // Test SMALL size
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { },
                    text = "Small",
                    size = ButtonSize.SMALL,
                )
            }
        }
        composeTestRule.onNodeWithText("Small").assertExists()

        // Test MEDIUM size
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { },
                    text = "Medium",
                    size = ButtonSize.MEDIUM,
                )
            }
        }
        composeTestRule.onNodeWithText("Medium").assertExists()

        // Test LARGE size
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { },
                    text = "Large",
                    size = ButtonSize.LARGE,
                )
            }
        }
        composeTestRule.onNodeWithText("Large").assertExists()
    }

    @Test
    fun buttonWithIconDisplaysCorrectly() {
        // Given
        composeTestRule.setContent {
            MaterialTheme {
                WordlandButton(
                    onClick = { },
                    text = "With Icon",
                    icon = Icons.Default.Star,
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("With Icon")
            .assertExists()
    }
}
