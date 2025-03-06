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
import androidx.compose.ui.test.performTextInput
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
            CreditCardNumberInput(
                value = cardNumber,
                onValueChange = {
                    cardNumber = it
                },
                schemeConfig = SupportedSchemeConfig(supportedSchemes = CardType.entries.toSet(), enableValidation = true),
                cardScheme = CardScheme(
                    type = CardType.VISA,
                    code = CardCode(
                        CodeType.CVV,
                        MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
                    )
                )
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("cardIcon", true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performTextInput("4111111111111111")

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Assert the content of the TextField
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText("4111 1111 1111 1111 "))
        composeTestRule.onNodeWithTag("successIcon", true).assertIsDisplayed()

    }

    @Test
    fun testCardNumberInputDisplaysError() {
        // Invalid card number length > 19
        var cardNumber by mutableStateOf("")

        // Start composable with valid card number
        composeTestRule.setContent {
            CreditCardNumberInput(
                value = cardNumber,
                onValueChange = {
                    cardNumber = it
                },
                schemeConfig = SupportedSchemeConfig(supportedSchemes = CardType.entries.toSet(), enableValidation = true)
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performTextInput("41111111111111111111")

        // Assert that an error message is displayed
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_card_number))
    }
}