package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.enums.CardScheme
import com.paydock.feature.card.presentation.utils.validators.CardSchemeValidator

/**
 * A composable function for rendering input fields for cardholder information and payment details.
 *
 * This function includes input fields for the cardholder's name (optional), card number, expiry date,
 * and security code. It supports customization for enabling/disabling inputs and handling focus transitions.
 *
 * @param shouldCollectCardholderName Whether the cardholder's name should be collected. If `true`, displays a name input field.
 * @param focusCardNumber The [FocusRequester] for the card number input field to handle focus changes.
 * @param focusExpiry The [FocusRequester] for the expiry date input field to handle focus changes.
 * @param focusCode The [FocusRequester] for the security code input field to handle focus changes.
 * @param enabled Determines whether the input fields are enabled or disabled.
 * @param cardHolderName The current value of the cardholder's name.
 * @param cardNumber The current value of the card number.
 * @param expiry The current value of the expiry date.
 * @param code The current value of the security code.
 * @param onCardHolderNameChange A callback invoked when the cardholder's name value changes.
 * @param onCardNumberChange A callback invoked when the card number value changes.
 * @param onExpiryChange A callback invoked when the expiry date value changes.
 * @param onSecurityCodeChange A callback invoked when the security code value changes.
 */
@Suppress("LongParameterList")
@Composable
internal fun CardInputFields(
    shouldCollectCardholderName: Boolean,
    supportedCardSchemes: List<CardScheme>?,
    focusCardNumber: FocusRequester,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    enabled: Boolean,
    cardHolderName: String,
    cardNumber: String,
    expiry: String,
    code: String,
    onCardHolderNameChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onSecurityCodeChange: (String) -> Unit,
) {
    // Input field for cardholder name
    if (shouldCollectCardholderName) {
        CardHolderNameInput(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cardHolderInput"),
            value = cardHolderName,
            enabled = enabled,
            nextFocus = focusCardNumber,
            onValueChange = onCardHolderNameChange
        )
    }

    // Input field for card number
    CreditCardNumberInput(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusCardNumber)
            .testTag("cardNumberInput"),
        supportedCardSchemes,
        value = cardNumber,
        enabled = enabled,
        onValueChange = onCardNumberChange,
        nextFocus = focusExpiry
    )

    // Row for expiry and security code input fields
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            Theme.dimensions.spacing,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.Top
    ) {
        CardExpiryInput(
            modifier = Modifier
                .weight(0.5f)
                .focusRequester(focusExpiry)
                .testTag("cardExpiryInput"),
            value = expiry,
            enabled = enabled,
            onValueChange = onExpiryChange,
            nextFocus = focusCode
        )

        CardSecurityCodeInput(
            modifier = Modifier
                .weight(0.5f)
                .focusRequester(focusCode)
                .testTag("cardSecurityCodeInput"),
            value = code,
            enabled = enabled,
            cardScheme = CardSchemeValidator.detectCardScheme(cardNumber),
            onValueChange = onSecurityCodeChange
        )
    }
}