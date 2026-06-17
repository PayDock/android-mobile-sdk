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
 * @param appearance Customization options for the input field's appearance. Defaults to [TextFieldAppearanceDefaults.appearance].
 * @param value The current value of the input field, representing the security code entered by the user.
 * @param cardCode The [CardCode] representing the type of the card (e.g., CVV, CVC, CID) along
 * with the required length.
 * @param enabled Whether the input field is enabled for user input. Defaults to `true`.
 * @param forceShowErrors A [Boolean] that determines whether to show error messages regardless of user interaction.
 * @param nextFocus A [FocusRequester] used to shift focus to the next input field when the "Next" IME action is triggered.
 * @param a11yFocus An optional [Boolean] that allows programmatically moving accessibility focus to this
 *  input field in a form.
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
    forceShowErrors: Boolean = false,
    nextFocus: FocusRequester? = null,
    a11yFocus: Boolean = false,
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
        hasUserInteracted || forceShowErrors,
        isSecurityCodeFocused = if (forceShowErrors) false else focusedState
    )

    // Determine the appropriate error message: Empty on defocus shows no error; Invalid shows error
    val errorMessage = when (securityCodeError) {
        SecurityCodeError.Empty -> if (forceShowErrors) stringResource(id = R.string.error_security_code_required) else null
        SecurityCodeError.None -> null
        SecurityCodeError.Invalid -> stringResource(id = R.string.error_security_code)
    }

    val processPlaceholder = remember(cardCode, securityCodeType) {
        buildString {
            // Default to the standard 3-digit mask ("XXX") and only widen to 4 ("XXXX") when a
            // recognised scheme requires it (e.g. Amex). When no scheme is recognised, cardCode
            // is null so we keep "XXX" rather than the absolute max length.
            val maxDigits =
                cardCode?.size ?: MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
            repeat(maxDigits) { append("X") }
        }
    }

    // A configured placeholder consisting solely of the default "X" mask (e.g. the seeded
    // "XXX" default) is treated as scheme-driven, so it still widens to "XXXX" for Amex.
    // Any other custom placeholder is kept verbatim and never changes. The
    // placeholder only swaps between "XXX"/"XXXX" and a custom override stays fixed.
    val configuredPlaceholder = appearance.placeholderText
    val placeholder = when {
        configuredPlaceholder == null -> processPlaceholder
        configuredPlaceholder.isNotEmpty() && configuredPlaceholder.all { it == 'X' } -> processPlaceholder
        else -> configuredPlaceholder
    }
    val hint = appearance.hintText ?: stringResource(id = R.string.hint_cvv)

    // Get the display label for the security code type
    val securityCodeLabel = when (securityCodeType) {
        CodeType.CVV_CVC -> "CVV / CVC"
        else -> securityCodeType.name
    }

    val dynamicHint = remember(cardCode, hint) {
        if (cardCode != null) {
            val size = cardCode.size.toString()
            // Replace patterns like "3 or 4", "3/4", "3-4" with the actual size
            hint.replace(Regex("3\\s*(?:or|/|and|-)\\s*4"), size)
                .replace(Regex("4\\s*(?:or|/|and|-)\\s*3"), size)
                // Also handle cases where a specific single digit was provided but needs to change
                // e.g., if hint was "3 digits" but card is Amex, it becomes "4 digits"
                .let { updated ->
                    if (updated == hint) {
                        val otherSize = if (size == "3") "4" else "3"
                        hint.replace(otherSize, size)
                    } else {
                        updated
                    }
                }
        } else {
            hint
        }
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
        placeholder = placeholder,
        enabled = enabled,
        error = errorMessage,
        hint = dynamicHint,
        showValidIcon = securityCodeError == SecurityCodeError.None &&
            value.length >= (cardCode?.size ?: MobileSDKConstants.CardDetailsConfig.MAX_SECURITY_CODE_LENGTH) &&
            !focusedState,
        a11yFocus = a11yFocus,
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

/**
 * Preview for the card security code input with default settings.
 */
@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSecurityCodeInputDefault() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        CardSecurityCodeInput(
            onValueChange = {}
        )
    }
}

/**
 * Preview for the card security code input specifically for CVV.
 */
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
            nextFocus = null,
            onValueChange = {}
        )
    }
}

/**
 * Preview for the card security code input specifically for CVC.
 */
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
            nextFocus = null,
            onValueChange = {}
        )
    }
}

/**
 * Preview for the card security code input specifically for CID.
 */
@SdkLightDarkPreviews
@Composable
internal fun PreviewCardSecurityCodeCIDInput() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        CardSecurityCodeInput(
            value = "1234",
            cardCode = CardCode(CodeType.CID, MobileSDKConstants.CardDetailsConfig.CID_LENGTH),
            enabled = true,
            nextFocus = null,
            onValueChange = {}
        )
    }
}

/**
 * Preview for the card security code input supporting both CVV and CVC.
 */
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
            nextFocus = null,
            onValueChange = {}
        )
    }
}