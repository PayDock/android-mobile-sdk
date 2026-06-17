package com.paydock.feature.card.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paydock.R
import com.paydock.core.BaseUITest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [GiftCardNumberInput] is built on [com.paydock.designsystems.components.input.SdkTextField], which
 * curates a single accessibility readout via `clearAndSetSemantics`. These tests drive the field as
 * a controlled component (value via prop, `forceShowErrors` to surface validation without simulated
 * keystrokes) and assert on the field's `contentDescription`.
 */
@RunWith(AndroidJUnit4::class)
internal class GiftCardNumberInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setField(number: String, forceShowErrors: Boolean = false) {
        composeTestRule.setContent {
            var cardNumber by remember { mutableStateOf(number) }
            GiftCardNumberInput(
                value = cardNumber,
                forceShowErrors = forceShowErrors,
                onValueChange = { cardNumber = it }
            )
        }
    }

    @Test
    fun testValidCardNumber_AnnouncesValid() {
        // A complete, valid gift card number announces the "Valid" state.
        setField(number = "62734010001104878")

        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains(getStringRes(R.string.content_desc_valid_icon), substring = true)
    }

    @Test
    fun testInvalidCardNumber_AnnouncesError() {
        // Too short for a gift card (min 14 digits) surfaced via forceShowErrors -> error announced.
        setField(number = "1234", forceShowErrors = true)

        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains("Error: ${getStringRes(R.string.error_card_number)}", substring = true)
    }

    @Test
    fun testEmptyField_AnnouncesLabel() {
        setField(number = "")

        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains(getStringRes(R.string.label_card_number), substring = true)
            .assertContentDescriptionContains("Edit box", substring = true)
    }
}
