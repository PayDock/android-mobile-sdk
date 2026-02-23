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
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.R
import com.paydock.core.BaseUITest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CardHolderNameInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule =
        createComposeRule() // compose rule is required to get access to the composable component

    @Test
    fun testValidCardHolderName() {
        var cardHolderName by mutableStateOf("")

        // Start composable with valid cardholder name
        composeTestRule.setContent {
            CardHolderNameInput(
                value = cardHolderName,
                onValueChange = {
                    cardHolderName = it
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performClick() // Focus the input field
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("John Doe")

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Assert the content of the TextField
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText("John Doe"))

        composeTestRule.onNodeWithTag("successIcon", true).assertIsDisplayed()

    }

    @Test
    fun testValidCardHolderNamesValidation() {
        var cardHolderName by mutableStateOf("")

        // Start composable with valid cardholder name
        composeTestRule.setContent {
            CardHolderNameInput(
                value = cardHolderName,
                onValueChange = {
                    cardHolderName = it
                }
            )
        }

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performClick().apply {
            performTextInput("Mr Test Name")
            assert(hasText("Mr Test Name")).performTextClearance()

            performTextInput("Mr. Test Name Name-Name")
            assert(hasText("Mr. Test Name Name-Name")).performTextClearance()

            performClick().performTextInput("John O'Neil")
            assert(hasText("John O'Neil")).performTextClearance()

            performClick().performTextInput("^aA!@#\$&()-`.+,'_<>;:*=?[ ]/")
            assert(hasText("^aA!@#\$&()-`.+,'_<>;:*=?[ ]/")).performTextClearance()

            performClick().performTextInput("Test")
            assert(hasText("Test")).performTextClearance()

            performClick().performTextInput("Test The 2nd")
            assert(hasText("Test The 2nd")).performTextClearance()

            performClick().performTextInput("Test III")
            assert(hasText("Test III")).performTextClearance()
        }
    }

    @Test
    fun testCardHolderNameInputDisplaysInvalidNameError() {
        var cardHolderName by mutableStateOf("")

        composeTestRule.setContent {
            CardHolderNameInput(
                value = cardHolderName,
                onValueChange = { cardHolderName = it }
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("John123")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_card_holder_name))
    }

    @Test
    fun testCardHolderNameInputDisplaysLuhnErrorWhen12Digits() {
        var cardHolderName by mutableStateOf("")

        composeTestRule.setContent {
            CardHolderNameInput(
                value = cardHolderName,
                onValueChange = { cardHolderName = it }
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("222240000000")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_luhn_card_holder_name))
    }

    @Test
    fun testCardHolderNameInputClearsErrorOnRefocus() {
        var cardHolderName by mutableStateOf("")

        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize().testTag("root")) {
                CardHolderNameInput(
                    value = cardHolderName,
                    onValueChange = { cardHolderName = it }
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
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("John123")
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
    fun testCardHolderNameInputNoErrorWhenEmptyOnDefocus() {
        var cardHolderName by mutableStateOf("")

        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize().testTag("root")) {
                CardHolderNameInput(
                    value = cardHolderName,
                    onValueChange = { cardHolderName = it }
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