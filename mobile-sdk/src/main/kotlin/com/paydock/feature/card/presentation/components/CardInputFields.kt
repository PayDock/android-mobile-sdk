package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.presentation.utils.validators.CardSchemeValidator

/**
 * A composable function for rendering input fields for cardholder information and payment details.
 *
 * This function includes input fields for the cardholder's name (optional), card number, expiry date,
 * and security code. It supports customization for enabling/disabling inputs and handling focus transitions.
 *
 * @param shouldCollectCardholderName Whether the cardholder's name should be collected. If `true`, displays a name input field.
 * @param schemeConfig The configuration defining the supported card schemes and validation settings.
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
fun CardInputFields(
    shouldCollectCardholderName: Boolean,
    schemeConfig: SupportedSchemeConfig,
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
    onSecurityCodeChange: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    // Define threshold for large font scale
    val largeFontScaleThreshold = MobileSDKConstants.CardDetailsConfig.FONT_SCALE_THRESHOLD

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing)) {
        // Cardholder Name Input
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

        // Card Number Input
        CreditCardNumberInput(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusCardNumber)
                .testTag("cardNumberInput"),
            schemeConfig = schemeConfig,
            value = cardNumber,
            enabled = enabled,
            onValueChange = onCardNumberChange,
            nextFocus = focusExpiry
        )

        // Expiry and Security Code Inputs
        if (fontScale >= largeFontScaleThreshold) {
            ExpiryAndCodeColumn(
                expiry = expiry,
                code = code,
                focusExpiry = focusExpiry,
                focusCode = focusCode,
                enabled = enabled,
                cardNumber = cardNumber,
                onExpiryChange = onExpiryChange,
                onSecurityCodeChange = onSecurityCodeChange
            )
        } else {
            ExpiryAndCodeRow(
                expiry = expiry,
                code = code,
                focusExpiry = focusExpiry,
                focusCode = focusCode,
                enabled = enabled,
                cardNumber = cardNumber,
                onExpiryChange = onExpiryChange,
                onSecurityCodeChange = onSecurityCodeChange
            )
        }
    }
}

/**
 * Composable function to display the expiry and security code input fields in a column layout.
 *
 * @param expiry The expiry date value to be displayed in the input field.
 * @param code The security code value to be displayed in the input field.
 * @param focusExpiry The [FocusRequester] used to manage focus for the expiry date input.
 * @param focusCode The [FocusRequester] used to manage focus for the security code input.
 * @param enabled A flag indicating whether the input fields should be enabled or disabled.
 * @param cardNumber The card number used to detect the card scheme.
 * @param onExpiryChange Callback function to handle changes to the expiry date input value.
 * @param onSecurityCodeChange Callback function to handle changes to the security code input value.
 */
@Suppress("LongParameterList")
@Composable
private fun ExpiryAndCodeColumn(
    expiry: String,
    code: String,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    enabled: Boolean,
    cardNumber: String,
    onExpiryChange: (String) -> Unit,
    onSecurityCodeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing),
        horizontalAlignment = Alignment.Start
    ) {
        CardExpiryInput(
            modifier = Modifier
                .focusRequester(focusExpiry)
                .testTag("cardExpiryInput"),
            value = expiry,
            enabled = enabled,
            onValueChange = onExpiryChange,
            nextFocus = focusCode
        )
        CardSecurityCodeInput(
            modifier = Modifier
                .focusRequester(focusCode)
                .testTag("cardSecurityCodeInput"),
            value = code,
            enabled = enabled,
            cardScheme = CardSchemeValidator.detectCardScheme(cardNumber),
            onValueChange = onSecurityCodeChange
        )
    }
}

/**
 * Composable function to display the expiry and security code input fields in a row layout.
 *
 * @param expiry The expiry date value to be displayed in the input field.
 * @param code The security code value to be displayed in the input field.
 * @param focusExpiry The [FocusRequester] used to manage focus for the expiry date input.
 * @param focusCode The [FocusRequester] used to manage focus for the security code input.
 * @param enabled A flag indicating whether the input fields should be enabled or disabled.
 * @param cardNumber The card number used to detect the card scheme.
 * @param onExpiryChange Callback function to handle changes to the expiry date input value.
 * @param onSecurityCodeChange Callback function to handle changes to the security code input value.
 */
@Suppress("LongParameterList")
@Composable
private fun ExpiryAndCodeRow(
    expiry: String,
    code: String,
    focusExpiry: FocusRequester,
    focusCode: FocusRequester,
    enabled: Boolean,
    cardNumber: String,
    onExpiryChange: (String) -> Unit,
    onSecurityCodeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Theme.dimensions.spacing, Alignment.CenterHorizontally),
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