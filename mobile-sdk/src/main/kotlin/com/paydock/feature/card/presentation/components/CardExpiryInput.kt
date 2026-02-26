package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.feature.card.presentation.utils.errors.CardExpiryError
import com.paydock.feature.card.presentation.utils.transformations.ExpiryInputTransformation
import com.paydock.feature.card.presentation.utils.validators.CardExpiryValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputParser

/**
 * A composable function for inputting and validating a credit card expiry date.
 *
 * This input field allows users to enter the expiry date in MM/YY format, performs validation checks,
 * and provides feedback based on the input's correctness. It handles user interactions, debounced
 * input updates, and error messages dynamically.
 *
 * @param modifier [Modifier] to be applied to the text field.
 * @param value The current value of the expiry input as a [String].
 * @param enabled Boolean indicating whether the input field is enabled.
 * @param nextFocus Optional [FocusRequester] to handle focus movement when the "Next" action is triggered.
 * @param onValueChange Callback function to handle changes in the input value.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CardExpiryInput(
    modifier: Modifier = Modifier,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    var hasUserInteracted by remember { mutableStateOf(false) }
    var suppressErrorUntilNextInput by remember { mutableStateOf(false) }
    var focusedState by remember { mutableStateOf(false) }
    val previousFocus = remember { object { var value = false } }

    // Validate possible expiry errors (digit-phase: month on 2-3 digits, full on 4)
    // Pass focusedState to enable focus-aware validation (month while typing, full on defocus)
    val expiryError = CardExpiryValidator.validateExpiryInput(value, hasUserInteracted, focusedState)

    // Map the validation result to an error message
    // Empty -> null: no error when field is defocused if empty
    val mappedError = when (expiryError) {
        CardExpiryError.Empty,
        CardExpiryError.None -> null
        CardExpiryError.InvalidMonth -> stringResource(id = R.string.error_expiry_month)
        CardExpiryError.InvalidFormat -> stringResource(id = R.string.error_expiry_date)
        CardExpiryError.Expired -> stringResource(id = R.string.error_expiry_expired)
    }
    val errorMessage = if (suppressErrorUntilNextInput) null else mappedError

    // Input field configuration
    SdkTextField(
        modifier = modifier.onFocusChanged {
            focusedState = it.isFocused
            val justGainedFocus = it.isFocused && !previousFocus.value
            previousFocus.value = it.isFocused
            if (justGainedFocus) suppressErrorUntilNextInput = true
            if (!it.isFocused) suppressErrorUntilNextInput = false
        },
        appearance = appearance,
        value = value,
        onValueChange = {
            hasUserInteracted = true
            suppressErrorUntilNextInput = false
            // Format and validate expiry date
            val formattedExpiry = CardExpiryValidator.formatExpiry(it)
            CreditCardInputParser.parseExpiry(formattedExpiry)?.let { expiry ->
                onValueChange(expiry)
            }
        },
        label = stringResource(id = R.string.label_expiry), // Label for the input field
        placeholder = stringResource(id = R.string.placeholder_expiry), // Placeholder text
        enabled = enabled,
        error = errorMessage, // Dynamically show error messages if validation fails
        showValidIcon = expiryError == CardExpiryError.None &&
            value.length >= MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH &&
            !focusedState,
        autofillType = ContentType.CreditCardExpirationDate,
        visualTransformation = ExpiryInputTransformation(), // Format input as MM/YY
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number, // Input limited to numbers
            imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                nextFocus?.requestFocus() // Move focus to the next input field
            }
        )
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardExpiryInputDefault() {
    CardExpiryInput(nextFocus = null, onValueChange = {

    })
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardExpiryInputValue() {
    CardExpiryInput(value = "0823", nextFocus = null, onValueChange = {

    })
}