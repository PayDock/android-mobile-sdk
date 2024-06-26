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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.core.BaseUITest
import com.paydock.feature.card.presentation.model.CardIssuerType
import com.paydock.feature.card.presentation.model.SecurityCodeType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardSecurityCodeInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule =
        createComposeRule() // compose rule is required to get access to the composable component

    @Test
    fun testValidCVVSecurityCodeState() {
        var securityCode by mutableStateOf("")

        // Start composable with valid card security code
        composeTestRule.setContent {
            CardSecurityCodeInput(
                value = securityCode,
                cardIssuer = CardIssuerType.VISA, // CVV
                onValueChange = {
                    securityCode = it
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithText(SecurityCodeType.CVV.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        composeTestRule.onNodeWithTag("sdkInput").performClick() // Focus the input field

        composeTestRule.onNodeWithText(
            buildString {
                repeat(SecurityCodeType.CVV.requiredDigits) { append("X") }
            }
        ).assertIsDisplayed()
    }

    @Test
    fun testValidCVCSecurityCodeState() {
        var securityCode by mutableStateOf("")

        // Start composable with valid card security code
        composeTestRule.setContent {
            CardSecurityCodeInput(
                value = securityCode,
                cardIssuer = CardIssuerType.MASTERCARD, // CVC
                onValueChange = {
                    securityCode = it
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithText(SecurityCodeType.CVC.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        composeTestRule.onNodeWithTag("sdkInput").performClick() // Focus the input field

        composeTestRule.onNodeWithText(
            buildString {
                repeat(SecurityCodeType.CVC.requiredDigits) { append("X") }
            }
        ).assertIsDisplayed()
    }

    @Test
    fun testValidCSCSecurityCodeState() {
        var securityCode by mutableStateOf("")

        // Start composable with valid card security code
        composeTestRule.setContent {
            CardSecurityCodeInput(
                value = securityCode,
                cardIssuer = CardIssuerType.AMERICAN_EXPRESS, // CSC
                onValueChange = {
                    securityCode = it
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithText(SecurityCodeType.CSC.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        composeTestRule.onNodeWithTag("sdkInput").performClick() // Focus the input field

        composeTestRule.onNodeWithText(
            buildString {
                repeat(SecurityCodeType.CSC.requiredDigits) { append("X") }
            }
        ).assertIsDisplayed()
    }

    @Test
    fun testValidSecurityCode() {
        var securityCode by mutableStateOf("")

        // Start composable with valid card security code
        composeTestRule.setContent {
            CardSecurityCodeInput(
                value = securityCode,
                onValueChange = {
                    securityCode = it
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithText(SecurityCodeType.CVV.name).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performClick() // Focus the input field
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("123")

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Assert the content of the TextField
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText("123"))
        composeTestRule.onNodeWithTag("successIcon", true).assertIsDisplayed()

    }

    @Test
    fun testCardSecurityCodeInputDisplaysError() {
        // Invalid security code exceeds expected expected digits (CVC = 4)
        var securityCode by mutableStateOf("123")

        // Start composable with valid card security code
        composeTestRule.setContent {
            CardSecurityCodeInput(
                value = securityCode,
                cardIssuer = CardIssuerType.AMERICAN_EXPRESS,
                onValueChange = {
                    securityCode = it
                }
            )
        }

        // Assert that an error message is displayed
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals("Card failed")
    }
}