package com.paydock.feature.card.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseUITest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardExpiryInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule =
        createComposeRule() // compose rule is required to get access to the composable component

    @Test
    fun testValidCardExpiry() {
        var expiry by mutableStateOf("")

        // Start composable with valid card expiry
        composeTestRule.setContent {
            CardExpiryInput(
                value = expiry,
                onValueChange = {
                    expiry = it
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performClick() // Focus the input field
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("0536")

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Assert the content of the TextField
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText("05/36"))
        composeTestRule.onNodeWithTag("successIcon", true).assertIsDisplayed()

    }

    @Test
    fun testCardExpiryInputDisplaysExpiredError() {
        // Invalid expiry expired
        var expiry by mutableStateOf("0521")

        // Start composable with valid card expiry
        composeTestRule.setContent {
            CardExpiryInput(
                value = expiry,
                onValueChange = {
                    expiry = it
                }
            )
        }

        // Assert that an error message is displayed
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals("Card expired")
    }

    @Test
    fun testCardExpiryInputDisplaysInvalidError() {
        // Invalid expiry entry (too short)
        var expiry by mutableStateOf("05")

        // Start composable with valid card expiry
        composeTestRule.setContent {
            CardExpiryInput(
                value = expiry,
                onValueChange = {
                    expiry = it
                }
            )
        }

        // Assert that an error message is displayed
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals("Invalid expiry date")
    }
}