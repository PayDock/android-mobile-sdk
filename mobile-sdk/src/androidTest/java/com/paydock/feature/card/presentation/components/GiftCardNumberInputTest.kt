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
import com.paydock.MobileSDK
import com.paydock.core.BaseUITest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class GiftCardNumberInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule =
        createComposeRule() // compose rule is required to get access to the composable component

    @Test
    fun testValidCardNumber() {
        val sdk = MobileSDK.getInstance()
        assertNotNull(sdk)

        var cardNumber by mutableStateOf("")

        // Start composable with valid card number
        composeTestRule.setContent {
            GiftCardNumberInput(
                value = cardNumber,
                onValueChange = {
                    cardNumber = it
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("cardIcon", true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performClick() // Focus the input field
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("62734010001104878")

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Assert the content of the TextField
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText("6273 4010 0011 0487 8 "))
        composeTestRule.onNodeWithTag("successIcon", true).assertIsDisplayed()

    }

    @Test
    fun testCardNumberInputDisplaysError() {
        // Invalid card number length > 25
        var cardNumber by mutableStateOf("411111111111111111111111111111111")

        // Start composable with valid card number
        composeTestRule.setContent {
            CreditCardNumberInput(
                value = cardNumber,
                onValueChange = {
                    cardNumber = it
                }
            )
        }

        // Assert that an error message is displayed
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals("Enter a valid card number")
    }
}