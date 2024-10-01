package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.InputValidIcon
import com.paydock.designsystems.components.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.model.CardIssuerType
import com.paydock.feature.card.presentation.model.SecurityCodeType
import com.paydock.feature.card.presentation.utils.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.CreditCardInputValidator

/**
 * A composable function that creates a card security code input field.
 *
 * @param modifier The modifier to apply to the input field.
 * @param value The current value of the input field.
 * @param nextFocus The focus requester for the next input field (optional).
 * @param cardIssuer The type of card issuer for which the security code is being entered.
 * @param onValueChange The callback triggered when the input value changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CardSecurityCodeInput(
    modifier: Modifier = Modifier,
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    cardIssuer: CardIssuerType = CardIssuerType.OTHER,
    onValueChange: (String) -> Unit
) {
    // Determine the type of security code based on the card issuer
    val securityCodeType = CardSecurityCodeValidator.detectSecurityCodeType(cardIssuer)

    // Check if the security code is valid
    val isValid = CardSecurityCodeValidator.isSecurityCodeValid(value, securityCodeType)

    // Parse the security code based on its type
    val securityCode = CreditCardInputValidator.parseSecurityCode(value, securityCodeType)

    // Determine the error message to display
    val errorMessage =
        if (value.isNotBlank() && !isValid) stringResource(id = R.string.error_security_code) else null

    // Create the visual representation of the security code input field
    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            // Parse the input value and invoke the callback
            CreditCardInputValidator.parseSecurityCode(it, securityCodeType)?.let { code ->
                onValueChange(code)
            }
        },
        label = when (securityCodeType) {
            SecurityCodeType.CVV -> stringResource(id = R.string.label_cvv)
            SecurityCodeType.CVC -> stringResource(id = R.string.label_cvc)
            SecurityCodeType.CSC -> stringResource(id = R.string.label_csc)
        },
        placeholder = buildString { repeat(securityCodeType.requiredDigits) { append("X") } },
        enabled = enabled,
        error = errorMessage,
        autofillType = AutofillType.CreditCardSecurityCode,
        // Show a success icon when the security code is valid and not blank
        trailingIcon = {
            if (!securityCode.isNullOrBlank()) {
                InputValidIcon()
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                // Request focus on the next input field
                nextFocus?.requestFocus()
            }
        )
    )
}

@LightDarkPreview
@Composable
private fun PreviewCardSecurityCodeCVVInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardSecurityCodeInput(value = "123", cardIssuer = CardIssuerType.VISA) {

            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewCardSecurityCodeCVCInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardSecurityCodeInput(value = "123", cardIssuer = CardIssuerType.MASTERCARD) {

            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewCardSecurityCodeCSCInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardSecurityCodeInput(value = "1234", cardIssuer = CardIssuerType.AMERICAN_EXPRESS) {

            }
        }
    }
}