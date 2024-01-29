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

package com.paydock.designsystems.components

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SdkTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSdkTextField_DefaultState() {
        composeTestRule.setContent {
            SdkTextField(
                value = "",
                onValueChange = { /* No-op */ },
                placeholder = "Enter your name",
                label = "Name"
            )
        }

        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter your name").assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))
        composeTestRule.onNodeWithTag("errorIcon").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorLabel").assertDoesNotExist()
    }

    @Test
    fun testSdkTextField_DefaultFocusedState() {
        composeTestRule.setContent {
            SdkTextField(
                value = "",
                onValueChange = { /* No-op */ },
                placeholder = "Enter your name",
                label = "Name"
            )
        }

        composeTestRule.onNodeWithTag("sdkInput").performClick()

        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter your name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText(""))
        composeTestRule.onNodeWithTag("errorIcon").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorLabel").assertDoesNotExist()
    }

    @Test
    fun testSdkTextField_TextState() {
        composeTestRule.setContent {
            SdkTextField(
                value = "John Doe",
                onValueChange = { /* No-op */ },
                placeholder = "Enter your name",
                label = "Name"
            )
        }

        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter your name").assertDoesNotExist()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText("John Doe"))
        composeTestRule.onNodeWithTag("errorIcon").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorLabel").assertDoesNotExist()
    }

    @Test
    fun testSdkTextField_ErrorState() {
        val error = "Invalid input"

        composeTestRule.setContent {
            SdkTextField(
                value = "John",
                onValueChange = { /* No-op */ },
                error = error,
                label = "Name"
            )
        }

        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sdkInput").assert(hasText("John"))
        composeTestRule.onNodeWithTag("errorIcon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("errorLabel").assertIsDisplayed().assertTextEquals(error)
    }

    @Test
    fun testSdkTextField_DisabledState() {
        composeTestRule.setContent {
            SdkTextField(
                value = "John Doe",
                onValueChange = { /* No-op */ },
                enabled = false,
                label = "Name"
            )
        }

        // Assert that the input field is disabled
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed().assertIsNotEnabled()
    }
}