package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.R
import com.paydock.core.BaseUITest
import com.paydock.core.MobileSDKConstants
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CreditCardNumberInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule =
        createComposeRule() // compose rule is required to get access to the composable component

    @Test
    fun testValidCardNumber() {
        var cardNumber by mutableStateOf("")

        // Start composable with valid card number
        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                CreditCardNumberInput(
                    schemeConfig = SupportedSchemeConfig(supportedSchemes = CardType.entries.toSet(), enableValidation = true),
                    value = cardNumber,
                    cardScheme = CardScheme(
                        type = CardType.VISA,
                        code = CardCode(
                            CodeType.CVV,
                            MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
                        )
                    ),
                    onValueChange = {
                        cardNumber = it
                    }
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.testTag("clickToDefocus"),
                    placeholder = { }
                )
            }
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("cardIcon", true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("4111111111111111")

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Defocus so valid icon appears (icons hidden when focused)
        composeTestRule.onNodeWithTag("clickToDefocus").performClick()
        composeTestRule.waitForIdle()

        // Assert the content of the TextField
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText("4111 1111 1111 1111 "))
        composeTestRule.onNodeWithTag("successIcon", true).assertIsDisplayed()

    }

    @Test
    fun testCardNumberInputDisplaysError() {
        // Invalid Luhn checksum - pass cardScheme so we reach Luhn validation (not UnsupportedCardScheme)
        var cardNumber by mutableStateOf("")

        composeTestRule.setContent {
            CreditCardNumberInput(
                schemeConfig = SupportedSchemeConfig(supportedSchemes = CardType.entries.toSet(), enableValidation = true),
                value = cardNumber,
                cardScheme = CardScheme(type = CardType.VISA, code = CardCode(CodeType.CVV, MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)),
                onValueChange = {
                    cardNumber = it
                }
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("4111111111111112")
        composeTestRule.waitForIdle()

        // Assert that an error message is displayed
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_card_number))
    }

    @Test
    fun testLuhnErrorVisibilityDuringTyping() {
        var cardNumber by mutableStateOf("")

        composeTestRule.setContent {
            CreditCardNumberInput(
                schemeConfig = SupportedSchemeConfig(
                    supportedSchemes = CardType.entries.toSet(),
                    enableValidation = true
                ),
                value = cardNumber,
                cardScheme = CardScheme(type = CardType.AMEX, code = CardCode(CodeType.CID, MobileSDKConstants.CardDetailsConfig.CID_LENGTH)),
                onValueChange = { cardNumber = it }
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("3400")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertDoesNotExist()

        composeTestRule.onNodeWithTag("sdkInput").performTextReplacement("340000099900052")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_card_number))
    }

    @Test
    fun testCardNumberErrorClearsOnClearInput() {
        var cardNumber by mutableStateOf("")

        composeTestRule.setContent {
            CreditCardNumberInput(
                schemeConfig = SupportedSchemeConfig(
                    supportedSchemes = CardType.entries.toSet(),
                    enableValidation = true
                ),
                value = cardNumber,
                cardScheme = CardScheme(type = CardType.AMEX, code = CardCode(CodeType.CID, MobileSDKConstants.CardDetailsConfig.CID_LENGTH)),
                onValueChange = { cardNumber = it }
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("340000099900052")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()

        composeTestRule.onNodeWithTag("sdkInput").performTextReplacement("")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertDoesNotExist()
    }

    @Test
    fun testErrorRecoveryOnRefocus() {
        var cardNumber by mutableStateOf("")

        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                CreditCardNumberInput(
                    schemeConfig = SupportedSchemeConfig(
                        supportedSchemes = CardType.entries.toSet(),
                        enableValidation = false
                    ),
                    value = cardNumber,
                    onValueChange = { cardNumber = it }
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.testTag("clickToDefocus"),
                    placeholder = { }
                )
            }
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("340000099900052")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()

        composeTestRule.onNodeWithTag("clickToDefocus").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()

        composeTestRule.onNodeWithTag("sdkInput").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sdkInput").performTextReplacement("4111111111111111")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("errorLabel").assertDoesNotExist()
        // Defocus so valid icon appears (icons hidden when focused)
        composeTestRule.onNodeWithTag("clickToDefocus").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("successIcon", true).assertIsDisplayed()
    }
}