package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

/**
 * [CreditCardNumberInput] is built on [com.paydock.designsystems.components.input.SdkTextField],
 * which curates a single accessibility readout via `clearAndSetSemantics`. These tests therefore
 * drive the field as a controlled component (value + scheme via props, and `forceShowErrors` to
 * surface validation without simulated keystrokes) and assert on the field's `contentDescription`.
 *
 * The digit-entry cap (e.g. blocking a 17th digit on a 16-digit Mastercard) is covered by the fast
 * unit test [ShouldAcceptCardNumberInputTest].
 */
@RunWith(AndroidJUnit4::class)
internal class CreditCardNumberInputTest : BaseUITest() {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setField(
        number: String,
        scheme: CardScheme?,
        forceShowErrors: Boolean = false
    ) {
        composeTestRule.setContent {
            var cardNumber by remember { mutableStateOf(number) }
            Column(modifier = Modifier.fillMaxSize()) {
                CreditCardNumberInput(
                    schemeConfig = SupportedSchemeConfig(
                        supportedSchemes = CardType.entries.toSet(),
                        enableValidation = true
                    ),
                    value = cardNumber,
                    cardScheme = scheme,
                    forceShowErrors = forceShowErrors,
                    onValueChange = { cardNumber = it }
                )
            }
        }
    }

    private fun visa() = CardScheme(
        type = CardType.VISA,
        code = CardCode(CodeType.CVV, MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH)
    )

    private fun amex() = CardScheme(
        type = CardType.AMEX,
        code = CardCode(CodeType.CID, MobileSDKConstants.CardDetailsConfig.CID_LENGTH)
    )

    @Test
    fun testValidCardNumber_AnnouncesValid() {
        // A complete, valid (unfocused) number announces the "Valid" state.
        setField(number = "4111111111111111", scheme = visa())

        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains(getStringRes(R.string.content_desc_valid_icon), substring = true)
    }

    @Test
    fun testInvalidCardNumber_AnnouncesError() {
        // Invalid Luhn checksum surfaced via forceShowErrors -> the error is announced.
        setField(number = "4111111111111112", scheme = visa(), forceShowErrors = true)

        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains("Error: ${getStringRes(R.string.error_card_number)}", substring = true)
    }

    @Test
    fun testInvalidAmexNumber_AnnouncesError() {
        // Scheme coverage: an invalid Amex number also surfaces the error.
        setField(number = "340000099900052", scheme = amex(), forceShowErrors = true)

        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains("Error: ${getStringRes(R.string.error_card_number)}", substring = true)
    }

    @Test
    fun testEmptyField_AnnouncesLabel() {
        // An empty field announces its label and edit-box role (no value, no error).
        setField(number = "", scheme = null)

        composeTestRule.onNodeWithTag("sdkInput")
            .assertContentDescriptionContains(getStringRes(R.string.label_card_number), substring = true)
            .assertContentDescriptionContains("Edit box", substring = true)
    }
}
