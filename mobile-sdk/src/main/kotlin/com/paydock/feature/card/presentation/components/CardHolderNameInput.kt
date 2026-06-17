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
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.paydock.R
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.feature.card.presentation.utils.errors.CardHolderNameError
import com.paydock.feature.card.presentation.utils.validators.CardHolderNameValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputParser

/**
 * Composable that displays an input field for the cardholder name.
 *
 * This composable provides a text field specifically designed for entering the cardholder's full name.
 * It handles basic validation, autofill suggestions, and keyboard actions for a smooth user experience.
 * The input is automatically formatted to capitalize words.
 *
 * @param modifier The modifier to be applied to the composable.
 * @param appearance Defines the visual appearance of the text field, including colors, typography, etc.
 * @param value The current value displayed in the cardholder name input field.
 * @param enabled Controls the enabled state of this Widget. When disabled, the input is not editable
 * and appears grayed out.
 * @param forceShowErrors A [Boolean] that determines whether to show error messages regardless of user interaction.
 * @param nextFocus An optional [FocusRequester] that allows programmatically moving focus to the next
 * input field in a form when the user presses the "Next" button on the keyboard.
 * @param a11yFocus An optional [Boolean] that allows programmatically moving accessibility focus to this
 *  input field in a form.
 * @param onValueChange The callback function to be invoked when the value of the cardholder name
 * changes due to user input. This function receives the updated string value after parsing and formatting.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CardHolderNameInput(
    modifier: Modifier = Modifier,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    value: String = "",
    enabled: Boolean = true,
    forceShowErrors: Boolean = false,
    nextFocus: FocusRequester? = null,
    a11yFocus: Boolean = false,
    onValueChange: (String) -> Unit
) {
    var hasUserInteracted by remember { mutableStateOf(false) }
    var focusedState by remember { mutableStateOf(false) }

    // Validate the current input value and determine the appropriate error state
    val cardHolderError = CardHolderNameValidator.validateHolderNameInput(value, hasUserInteracted || forceShowErrors)
    val mappedError = when (cardHolderError) {
        CardHolderNameError.Empty -> if (forceShowErrors) stringResource(id = R.string.error_cardholder_name_required) else null
        CardHolderNameError.None -> null
        CardHolderNameError.InvalidLuhn -> stringResource(id = R.string.error_luhn_card_holder_name)
        CardHolderNameError.InvalidFormat -> stringResource(id = R.string.error_card_holder_name)
    }
    // Resolve placeholder and hint from appearance or defaults
    val placeholder = appearance.placeholderText ?: ""
    val hint = appearance.hintText ?: stringResource(id = R.string.hint_card_name)

    SdkTextField(
        modifier = modifier.onFocusChanged {
            focusedState = it.isFocused
        },
        appearance = appearance,
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
        error = mappedError,
        hint = hint,
        placeholder = placeholder,
        showValidIcon = cardHolderError == CardHolderNameError.None && value.isNotBlank() && !focusedState,
        a11yFocus = a11yFocus,
        autofillType = ContentType.PersonFullName,
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

/**
 * Preview for the cardholder name input with a default state.
 */
@SdkLightDarkPreviews
@Composable
internal fun PreviewCardHolderNameInputDefault() {
    CardHolderNameInput(
        onValueChange = {}
    )
}

/**
 * Preview for the cardholder name input with a pre-filled value.
 */
@SdkLightDarkPreviews
@Composable
internal fun PreviewCardHolderNameInputValue() {
    CardHolderNameInput(
        value = "J DOE",
        onValueChange = {}
    )
}