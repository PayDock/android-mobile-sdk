package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.icon.IconAppearanceDefaults
import com.paydock.designsystems.components.icon.SdkIcon
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.feature.card.presentation.utils.errors.GiftCardNumberError
import com.paydock.feature.card.presentation.utils.transformations.CardNumberInputTransformation
import com.paydock.feature.card.presentation.utils.validators.GiftCardInputParser
import com.paydock.feature.card.presentation.utils.validators.GiftCardNumberValidator

/**
 * A composable that displays an input field for entering a gift card number.
 *
 * @param modifier The modifier to be applied to the composable.
 * @param value The current value of the input field.
 * @param nextFocus The focus requester for the next input field. If provided, pressing 'Next' on the keyboard will
 *                  move focus to the next input field.
 * @param forceShowErrors Flag to force showing validation errors even if the user hasn't interacted with the field
 *                  yet, and even while the field is focused (used on submit).
 * @param onValueChange The callback to be invoked when the value of the input field changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongMethod")
@Composable
internal fun GiftCardNumberInput(
    modifier: Modifier = Modifier,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    forceShowErrors: Boolean = false,
    a11yFocus: Boolean = false,
    onValueChange: (String) -> Unit
) {
    // State to track the focus state of the input field
    var focusedState by remember { mutableStateOf(false) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    // Validate the live value; the focus flag (not a debounce timer) is what suppresses inline
    // errors while the user is still typing, matching CardDetailsWidget's field behaviour.
    val cardNumberError = GiftCardNumberValidator.validateCardNumberInput(
        value,
        hasUserInteracted || forceShowErrors,
        isCardNumberFocused = if (forceShowErrors) false else focusedState
    )

    // Define the error message to be shown if the card number is invalid
    val errorMessage = when (cardNumberError) {
        // On submit (forceShowErrors) an empty field must surface a "required" error like other fields.
        GiftCardNumberError.Empty -> if (forceShowErrors) stringResource(id = R.string.error_card_number_required) else null
        GiftCardNumberError.Invalid -> stringResource(id = R.string.error_card_number)
        GiftCardNumberError.None -> null
    }

    // Source placeholder/hint from the appearance so they are customizable per field, falling back
    // to the built-in defaults when not overridden.
    val placeholder = appearance.placeholderText ?: stringResource(id = R.string.placeholder_card_number)
    val hint = appearance.hintText ?: stringResource(id = R.string.hint_gift_card_number)

    SdkTextField(
        modifier = modifier.onFocusChanged {
            focusedState = it.isFocused
        },
        appearance = appearance,
        value = value,
        onValueChange = { newText ->
            hasUserInteracted = true
            // Strip spaces and other non-digits so pasted values (e.g. "1234 5678 9012 3456") are accepted
            val digitsOnly = newText.replace(Regex("\\D"), "")
            val maxLength = MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH
            // take(maxLength) handles truncation when pasting exceeds max digits
            val valueToUse = digitsOnly.take(maxLength)
            GiftCardInputParser.parseNumber(valueToUse)?.let { number ->
                onValueChange(number)
            }
        },
        placeholder = placeholder,
        hint = hint,
        a11yFocus = a11yFocus,
        enabled = enabled,
        label = stringResource(id = R.string.label_card_number),
        leadingIcon = {
            // Display the card scheme icon as a leading icon in the input field
            SdkIcon(
                modifier = Modifier.testTag("cardIcon"),
                painter = painterResource(id = R.drawable.ic_credit_card),
                contentDescription = null,
                appearance = IconAppearanceDefaults.appearance().copy(
                    tint = if (focusedState) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                ),
            )
        },
        error = errorMessage,
        visualTransformation = CardNumberInputTransformation(CardNumberInputTransformation.GIFT_CARD_SIZES),
        // Use keyboard options and actions for a more user-friendly input experience
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                nextFocus?.requestFocus() // Move focus to the next input field when 'Next' is pressed
            }
        )
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewGiftCardNumberInputDefault() {
    GiftCardNumberInput {}
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewGiftCardNumberInput() {
    GiftCardNumberInput(value = "4242424242424242") {

    }
}