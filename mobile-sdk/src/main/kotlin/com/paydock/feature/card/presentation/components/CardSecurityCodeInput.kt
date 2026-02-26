package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import com.paydock.feature.card.presentation.utils.errors.SecurityCodeError
import com.paydock.feature.card.presentation.utils.validators.CardSecurityCodeValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputParser

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
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    value: String = "",
    cardCode: CardCode? = null,
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    // Tracks whether the user has interacted with the input field
    var hasUserInteracted by remember { mutableStateOf(false) }
    var focusedState by remember { mutableStateOf(false) }

    // Determine the security code type based on the card scheme (e.g., CVV, CVC, CID)
    val securityCodeType = cardCode?.type ?: CodeType.CVV

    // Validate the input and check for possible errors (only on defocus; no inline feedback while typing)
    val securityCodeError = CardSecurityCodeValidator.validateSecurityCodeInput(
        value,
        cardCode,
        hasUserInteracted,
        isSecurityCodeFocused = focusedState
    )

    // Determine the appropriate error message: Empty on defocus shows no error; Invalid shows error
    val errorMessage = when (securityCodeError) {
        SecurityCodeError.Empty,
        SecurityCodeError.None -> null
        SecurityCodeError.Invalid -> stringResource(id = R.string.error_security_code)
    }

    val placeholder = remember(cardCode, securityCodeType) {
        buildString {
            val maxDigits =
                cardCode?.size ?: MobileSDKConstants.CardDetailsConfig.MAX_SECURITY_CODE_LENGTH
            repeat(maxDigits) { append("X") }
        }
    }

    // Get the display label for the security code type
    val securityCodeLabel = when (securityCodeType) {
        CodeType.CVV_CVC -> "CVV / CVC"
        else -> securityCodeType.name
    }

    // Render the security code input field with appropriate properties
    SdkTextField(
        modifier = modifier.onFocusChanged { focusedState = it.isFocused },
        appearance = appearance,
        value = value,
        onValueChange = {
            hasUserInteracted = true
            // Format and parse the security code input before invoking the callback
            // When no cardCode is provided, allow up to MAX_SECURITY_CODE_LENGTH (4) digits
            CreditCardInputParser.parseSecurityCode(
                it,
                cardCode?.size ?: MobileSDKConstants.CardDetailsConfig.MAX_SECURITY_CODE_LENGTH
            )?.let { code ->
                onValueChange(code)
            }
        },
        // Display the label according to the detected security code type
        label = securityCodeLabel,
        // Show a placeholder with 'X' placeholders based on the required digits
        placeholder = placeholder,
        enabled = enabled,
        error = errorMessage,
        showValidIcon = securityCodeError == SecurityCodeError.None &&
            value.length >= (cardCode?.size ?: MobileSDKConstants.CardDetailsConfig.MAX_SECURITY_CODE_LENGTH) &&
            !focusedState,
        autofillType = ContentType.CreditCardSecurityCode,
        visualTransformation = PasswordVisualTransformation(),
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

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSecurityCodeInputDefault() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        CardSecurityCodeInput {}
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSecurityCodeCVVInput() {
    Surface(color = MaterialTheme.colorScheme.surface) {
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

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSecurityCodeCVCInput() {
    Surface(color = MaterialTheme.colorScheme.surface) {
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

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSecurityCodeCIDInput() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        CardSecurityCodeInput(
            value = "1234",
            cardCode = CardCode(CodeType.CID, MobileSDKConstants.CardDetailsConfig.CID_LENGTH),
            enabled = true,
            nextFocus = null
        ) {

        }
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSecurityCodeCVV_CVCInput() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        CardSecurityCodeInput(
            value = "123",
            cardCode = CardCode(
                CodeType.CVV_CVC,
                MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
            ),
            enabled = true,
            nextFocus = null
        ) {

        }
    }
}