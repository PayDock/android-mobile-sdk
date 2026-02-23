package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.R
import com.paydock.core.BaseUITest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CardExpiryInputTest : BaseUITest() {

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
        var cardExpiry by mutableStateOf("")

        // Start composable with valid card security code
        composeTestRule.setContent {
            CardExpiryInput(
                value = cardExpiry,
                onValueChange = {
                    cardExpiry = it
                }
            )
        }
        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("0521")

        // Assert that an error message is displayed (Expired maps to error_expiry_date)
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_expiry_date))
    }

    @Test
    fun testCardExpiryInputDisplaysInvalidMonthError() {
        var cardExpiry by mutableStateOf("")

        composeTestRule.setContent {
            CardExpiryInput(
                value = cardExpiry,
                onValueChange = { cardExpiry = it }
            )
        }
        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("13")

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_expiry_month))
    }

    @Test
    fun testCardExpiryInputDisplaysInvalidError() {
        var cardExpiry by mutableStateOf("")

        composeTestRule.setContent {
            CardExpiryInput(
                value = cardExpiry,
                onValueChange = { cardExpiry = it }
            )
        }
        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("0012")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_expiry_date))
    }

    @Test
    fun testCardExpiryInputClearsErrorOnRefocus() {
        var cardExpiry by mutableStateOf("")

        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize().testTag("root")) {
                CardExpiryInput(
                    value = cardExpiry,
                    onValueChange = { cardExpiry = it }
                )
                Column(
                    modifier = Modifier
                        .testTag("clickToDefocus")
                        .fillMaxSize()
                        .clickable { }
                ) {}
            }
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("13")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()

        composeTestRule.onNodeWithTag("clickToDefocus").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertDoesNotExist()
    }

    @Test
    fun testCardExpiryInputNoErrorWhenEmptyOnDefocus() {
        var cardExpiry by mutableStateOf("")

        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize().testTag("root")) {
                CardExpiryInput(
                    value = cardExpiry,
                    onValueChange = { cardExpiry = it }
                )
                Column(
                    modifier = Modifier
                        .testTag("clickToDefocus")
                        .fillMaxSize()
                        .clickable { }
                ) {}
            }
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("clickToDefocus").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertDoesNotExist()
    }
}