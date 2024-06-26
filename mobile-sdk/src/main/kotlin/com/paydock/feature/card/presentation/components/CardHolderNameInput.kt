package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.InputValidIcon
import com.paydock.designsystems.components.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.utils.CreditCardInputValidator

/**
 * Composable that displays an input field for the cardholder name.
 *
 * @param modifier The modifier to be applied to the composable.
 * @param value The current value of the cardholder name.
 * @param nextFocus The focus requester for the next input field (optional).
 * @param onValueChange The callback function to be invoked when the value of the cardholder name changes.
 */
@Composable
fun CardHolderNameInput(
    modifier: Modifier = Modifier,
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    // Validate the cardholder name using CardInputValidator
    val cardHolder = CreditCardInputValidator.parseHolderName(value)

    // Card Number Luhn check: if it passes, it is likely to be a valid card number
    val isLuhnValid = CreditCardInputValidator.isLuhnValid(value)

    // Define the error message to be shown if the cardholder name is invalid
    val errorMessage =
        if (cardHolder == null || (value.isNotBlank() && isLuhnValid)) stringResource(id = R.string.error_card_holder_name) else null

    // Use AppTextField from the AppCompat library with the specified properties
    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = { newValue ->
            // Validate and parse the input when the value changes
            CreditCardInputValidator.parseHolderName(newValue)?.let { parsedName ->
                onValueChange(parsedName)
            }
        },
        label = stringResource(id = R.string.label_cardholder_name),
        enabled = enabled,
        error = errorMessage,
        // Show a success icon when the cardholder name is valid and not blank
        trailingIcon = if (!cardHolder.isNullOrBlank()) {
            {
                InputValidIcon()
            }
        } else {
            null
        },
        // Use keyboard options and actions for a more user-friendly input experience
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                nextFocus?.requestFocus() // Move focus to the next input field if available
            }
        )
    )
}

@LightDarkPreview
@Composable
private fun PreviewCardHolderNameInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardHolderNameInput(value = "J DOE") {

            }
        }
    }
}