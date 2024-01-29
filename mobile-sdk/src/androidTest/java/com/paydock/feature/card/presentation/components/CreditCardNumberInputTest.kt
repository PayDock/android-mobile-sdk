/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
class CreditCardNumberInputTest : BaseUITest() {

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
                }
            )
        }

        // Asset default empty state
        composeTestRule.onNodeWithTag("cardIcon", true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("successIcon", true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))

        // Mimic the user inputting the text
        composeTestRule.onNodeWithTag("sdkInput").performClick() // Focus the input field
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
        var cardNumber by mutableStateOf("41111111111111111111")

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
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed().assertTextEquals("Enter a valid card number")
    }
}