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
import com.paydock.feature.card.presentation.utils.errors.CardExpiryError
import com.paydock.feature.card.presentation.utils.transformations.ExpiryInputTransformation
import com.paydock.feature.card.presentation.utils.validators.CardExpiryValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputParser
import kotlinx.coroutines.delay

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
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    // Tracks if the user has interacted with the field
    var hasUserInteracted by remember { mutableStateOf(false) }

    // Debounced value to prevent rapid updates
    var debouncedValue by remember { mutableStateOf("") }
    LaunchedEffect(value) {
        delay(MobileSDKConstants.General.INPUT_DELAY)
        debouncedValue = value
    }

    // Parse the expiry value for display purposes and validate it
    val expiry = CreditCardInputParser.parseExpiry(debouncedValue)
    // Validate possible expiry errors
    val expiryError = CardExpiryValidator.validateExpiryInput(debouncedValue, hasUserInteracted)

    // Map the validation result to an error message
    val errorMessage = when (expiryError) {
        CardExpiryError.Empty,
        CardExpiryError.InvalidFormat -> stringResource(id = R.string.error_expiry_date)
        CardExpiryError.Expired -> stringResource(id = R.string.error_expiry_expired)
        CardExpiryError.None -> null
    }

    // Input field configuration
    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            hasUserInteracted = true
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
        autofillType = AutofillType.CreditCardExpirationDate,
        visualTransformation = ExpiryInputTransformation(), // Format input as MM/YY
        trailingIcon = {
            // Display a success icon when the expiry input is valid and non-blank
            if (!expiry.isNullOrBlank()) {
                InputValidIcon()
            }
        },
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

@PreviewLightDark
@Composable
internal fun PreviewCardExpiryInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardExpiryInput(value = "0823", enabled = true, nextFocus = null) {

            }
        }
    }
}