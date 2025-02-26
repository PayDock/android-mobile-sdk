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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.input.InputValidIcon
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.utils.errors.CardHolderNameError
import com.paydock.feature.card.presentation.utils.validators.CardHolderNameValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputParser
import kotlinx.coroutines.delay

/**
 * Composable that displays an input field for the cardholder name.
 *
 * @param modifier The modifier to be applied to the composable.
 * @param value The current value of the cardholder name.
 * @param enabled Controls the enabled state of this Widget
 * @param nextFocus The focus requester for the next input field (optional).
 * @param onValueChange The callback function to be invoked when the value of the cardholder name changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CardHolderNameInput(
    modifier: Modifier = Modifier,
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    var hasUserInteracted by remember { mutableStateOf(false) }

    var debouncedValue by remember { mutableStateOf("") }
    LaunchedEffect(value) {
        delay(MobileSDKConstants.General.INPUT_DELAY)
        debouncedValue = value
    }

    // Validate the cardholder name using CardInputValidator
    val cardHolder = CreditCardInputParser.parseHolderName(debouncedValue)
    // Validate possible cardholder errors
    val cardHolderError = CardHolderNameValidator.validateHolderNameInput(debouncedValue, hasUserInteracted)

    // Define the error message to be shown if the cardholder name is invalid
    val errorMessage = when (cardHolderError) {
        CardHolderNameError.Empty -> stringResource(id = R.string.error_card_holder_name)
        CardHolderNameError.InvalidLuhn -> stringResource(id = R.string.error_luhn_card_holder_name)
        CardHolderNameError.None -> null
    }

    // Use AppTextField from the AppCompat library with the specified properties
    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = { newValue ->
            hasUserInteracted = true
            // Validate and parse the input when the value changes
            CreditCardInputParser.parseHolderName(newValue)?.let { parsedName ->
                onValueChange(parsedName)
            }
        },
        label = stringResource(id = R.string.label_cardholder_name),
        enabled = enabled,
        error = errorMessage,
        autofillType = AutofillType.PersonFullName,
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

@PreviewLightDark
@Composable
internal fun PreviewCardHolderNameInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardHolderNameInput(value = "J DOE") {

            }
        }
    }
}