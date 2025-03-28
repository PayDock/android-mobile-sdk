package com.paydock.feature.card.presentation.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.R
import com.paydock.core.BaseUITest
import com.paydock.designsystems.theme.SdkTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class CardPinInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule =
        createComposeRule() // compose rule is required to get access to the composable component

    @Test
    fun cardPinInput_initialState() {
        composeTestRule.setContent {
            CardPinInput(
                onValueChange = {}
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sdkInput").assertIsEnabled()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))
        composeTestRule.onNodeWithTag("sdkLabel", true)
            .assertTextEquals(getStringRes(R.string.label_pin))
        // Only shows when the input is not empty
        composeTestRule.onNodeWithTag("sdkPlaceholder", true).assertDoesNotExist()
        // Only shows when there is an actual error
        composeTestRule.onNodeWithTag("errorIcon", true).assertDoesNotExist()
    }

    @Test
    fun cardPinInput_disabledState() {
        composeTestRule.setContent {
            SdkTheme {
                Surface {
                    CardPinInput(onValueChange = {}, enabled = false)
                }
            }
        }

        composeTestRule.onNodeWithTag("sdkInput").assertIsNotEnabled()
    }

    @Test
    fun cardPinInput_validInput() {
        var capturedValue by mutableStateOf("")
        val testPin = "1234"

        // Start composable with valid card security code
        composeTestRule.setContent {
            CardPinInput(
                value = capturedValue,
                onValueChange = {
                    capturedValue = it
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performTextInput(testPin)

        // Allow some time for the UI to update
        composeTestRule.waitForIdle()

        // Assert the content of the TextField
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(testPin))
        composeTestRule.onNodeWithTag("successIcon", true).assertIsDisplayed()
    }

    @Test
    fun cardPinInput_invalidInput() {
        // Invalid security code exceeds expected expected digits (CVC = 4)
        var cardPin by mutableStateOf("")

        // Start composable with valid card security code
        composeTestRule.setContent {
            CardPinInput(
                value = cardPin,
                onValueChange = {
                    cardPin = it
                }
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performTextInput("abc")

        // Assert that an error message is displayed
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed()
            .assertTextEquals(getStringRes(R.string.error_pin))
    }
}