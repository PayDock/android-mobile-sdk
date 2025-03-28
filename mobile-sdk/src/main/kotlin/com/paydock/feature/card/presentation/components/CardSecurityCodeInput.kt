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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.input.InputValidIcon
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import com.paydock.feature.card.presentation.utils.errors.SecurityCodeError
import com.paydock.feature.card.presentation.utils.validators.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputParser
import kotlinx.coroutines.delay

/**
 * A composable input field for entering a credit card security code (CVV, CVC, or CID),
 * with automatic detection of the security code type based on the card scheme.
 *
 * This input handles user interactions, validation, formatting, and provides error messages if the input is invalid.
 *
 * @param modifier The [Modifier] to be applied to the input field for layout adjustments and styling.
 * @param value The current value of the input field, representing the security code entered by the user.
 * @param enabled Whether the input field is enabled for user input. Defaults to `true`.
 * @param cardCode The [CardCode] representing the type of the card (e.g., CVV, CVC, CID) along
 * with the required length.
 * @param nextFocus A [FocusRequester] used to shift focus to the next input field when the "Next" IME action is triggered.
 * @param onValueChange A callback invoked when the value of the input field changes.
 *                      Provides the parsed security code string as its parameter.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CardSecurityCodeInput(
    modifier: Modifier = Modifier,
    value: String = "",
    cardCode: CardCode? = null,
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
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

    // Determine the security code type based on the card scheme (e.g., CVV, CVC, CID)
    val securityCodeType = cardCode?.type ?: CodeType.CVV

    // Validate the input and check for possible errors
    val securityCodeError = CardSecurityCodeValidator.validateSecurityCodeInput(
        debouncedValue,
        cardCode,
        hasUserInteracted
    )

    // Determine the appropriate error message based on validation results
    val errorMessage = when (securityCodeError) {
        SecurityCodeError.Empty,
        SecurityCodeError.Invalid -> stringResource(id = R.string.error_security_code)

        SecurityCodeError.None -> null
    }

    val placeholder = remember(cardCode, securityCodeType) {
        buildString {
            val requiredDigits =
                cardCode?.size ?: MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
            repeat(requiredDigits) { append("X") }
        }
    }

    // Render the security code input field with appropriate properties
    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            hasUserInteracted = true
            // Format and parse the security code input before invoking the callback
            CreditCardInputParser.parseSecurityCode(
                it,
                cardCode?.size ?: MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
            )?.let { code ->
                onValueChange(code)
            }
        },
        // Display the label according to the detected security code type
        label = securityCodeType.name,
        // Show a placeholder with 'X' placeholders based on the required digits
        placeholder = placeholder,
        enabled = enabled,
        error = errorMessage,
        autofillType = AutofillType.CreditCardSecurityCode,
        // Display a success icon when the input is valid and non-blank
        trailingIcon = {
            if (debouncedValue.isNotBlank()) {
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

@PreviewLightDark
@Composable
internal fun PreviewCardSecurityCodeCVVInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardSecurityCodeInput(
                value = "123",
                cardCode = CardCode(
                    CodeType.CVV,
                    MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
                ),
                enabled = true,
                nextFocus = null
            ) {

            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewCardSecurityCodeCVCInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardSecurityCodeInput(
                value = "123",
                cardCode = CardCode(
                    CodeType.CVC,
                    MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
                ),
                enabled = true,
                nextFocus = null
            ) {

            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewCardSecurityCodeCIDInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardSecurityCodeInput(
                value = "1234",
                cardCode = CardCode(CodeType.CID, MobileSDKConstants.CardDetailsConfig.CID_LENGTH),
                enabled = true,
                nextFocus = null
            ) {

            }
        }
    }
}