package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.input.InputValidIcon
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.enums.CardIssuerType
import com.paydock.feature.card.domain.model.integration.enums.SecurityCodeType
import com.paydock.feature.card.presentation.utils.errors.SecurityCodeError
import com.paydock.feature.card.presentation.utils.validators.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputValidator
import kotlinx.coroutines.delay

/**
 * A composable input field for entering a credit card security code (CVV, CVC, or CSC),
 * with automatic detection of the security code type based on the card issuer.
 *
 * This input handles user interactions, validation, formatting, and provides error messages if the input is invalid.
 *
 * @param modifier The [Modifier] to be applied to the input field for layout adjustments and styling.
 * @param value The current value of the input field, representing the security code entered by the user.
 * @param enabled Whether the input field is enabled for user input. Defaults to `true`.
 * @param nextFocus A [FocusRequester] used to shift focus to the next input field when the "Next" IME action is triggered.
 * @param cardIssuer The [CardIssuerType] representing the issuer of the card (e.g., AMERICAN_EXPRESS, MASTERCARD, VISA).
 *                   This is used to determine the security code type and the required input length.
 * @param onValueChange A callback invoked when the value of the input field changes.
 *                      Provides the parsed security code string as its parameter.
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
    // Tracks whether the user has interacted with the input field
    var hasUserInteracted by remember { mutableStateOf(false) }

    // Debounced value to prevent rapid input updates from causing unnecessary processing
    var debouncedValue by remember { mutableStateOf("") }
    LaunchedEffect(value) {
        delay(MobileSDKConstants.General.INPUT_DELAY)
        debouncedValue = value
    }

    // Determine the security code type based on the card issuer (e.g., CVV, CVC, CSC)
    val securityCodeType = CardSecurityCodeValidator.detectSecurityCodeType(cardIssuer)

    // Parse the security code based on its type
    val securityCode = CreditCardInputValidator.parseSecurityCode(debouncedValue, securityCodeType)

    // Validate the input and check for possible errors
    val securityCodeError = CardSecurityCodeValidator.validateSecurityCodeInput(
        debouncedValue,
        securityCodeType,
        hasUserInteracted
    )

    // Determine the appropriate error message based on validation results
    val errorMessage = when (securityCodeError) {
        SecurityCodeError.Empty,
        SecurityCodeError.Invalid -> stringResource(id = R.string.error_security_code)
        SecurityCodeError.None -> null
    }

    // Render the security code input field with appropriate properties
    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            hasUserInteracted = true
            // Format and parse the security code input before invoking the callback
            CreditCardInputValidator.parseSecurityCode(it, securityCodeType)?.let { code ->
                onValueChange(code)
            }
        },
        // Display the label according to the detected security code type
        label = when (securityCodeType) {
            SecurityCodeType.CVV -> stringResource(id = R.string.label_cvv)
            SecurityCodeType.CVC -> stringResource(id = R.string.label_cvc)
            SecurityCodeType.CSC -> stringResource(id = R.string.label_csc)
        },
        // Show a placeholder with 'X' placeholders based on the required digits
        placeholder = buildString { repeat(securityCodeType.requiredDigits) { append("X") } },
        enabled = enabled,
        error = errorMessage,
        autofillType = AutofillType.CreditCardSecurityCode,
        // Display a success icon when the input is valid and non-blank
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
                // Request focus on the next input field, if provided
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