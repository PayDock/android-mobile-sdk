package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.input.InputValidIcon
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.utils.errors.GiftCardNumberError
import com.paydock.feature.card.presentation.utils.transformations.CardNumberInputTransformation
import com.paydock.feature.card.presentation.utils.validators.GiftCardInputParser
import com.paydock.feature.card.presentation.utils.validators.GiftCardNumberValidator
import kotlinx.coroutines.delay

/**
 * A composable that displays an input field for entering a gift card number.
 *
 * @param modifier The modifier to be applied to the composable.
 * @param value The current value of the input field.
 * @param nextFocus The focus requester for the next input field. If provided, pressing 'Next' on the keyboard will
 *                  move focus to the next input field.
 * @param onValueChange The callback to be invoked when the value of the input field changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongMethod")
@Composable
internal fun GiftCardNumberInput(
    modifier: Modifier = Modifier,
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    // State to track the focus state of the input field
    val focusedState = remember { mutableStateOf(false) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    var debouncedValue by remember { mutableStateOf("") }
    LaunchedEffect(value) {
        delay(MobileSDKConstants.General.INPUT_DELAY)
        debouncedValue = value
    }

    // Parse the card number to determine its type and validity
    val cardNumber = GiftCardInputParser.parseNumber(debouncedValue)

    // Check if the card number is valid
    val cardNumberError = GiftCardNumberValidator.validateCardNumberInput(debouncedValue, hasUserInteracted)

    // Define the error message to be shown if the card number is invalid
    val errorMessage = when (cardNumberError) {
        GiftCardNumberError.Empty,
        GiftCardNumberError.Invalid -> stringResource(id = R.string.error_card_number)
        GiftCardNumberError.None -> null
    }

    SdkTextField(
        modifier = modifier.onFocusChanged {
            focusedState.value = it.isFocused
        },
        value = value,
        onValueChange = {
            hasUserInteracted = true
            if (it.length <= MobileSDKConstants.CardDetailsConfig.MAX_GIFT_CARD_LENGTH) {
                // Parse the input text to ensure it is a valid card number before invoking the callback
                GiftCardInputParser.parseNumber(it)?.let { number ->
                    onValueChange(number)
                }
            }
        },
        placeholder = stringResource(id = R.string.placeholder_card_number),
        enabled = enabled,
        label = stringResource(id = R.string.label_card_number),
        leadingIcon = {
            // Display the card scheme icon as a leading icon in the input field
            Icon(
                modifier = Modifier.testTag("cardIcon"),
                painter = painterResource(id = R.drawable.ic_credit_card),
                contentDescription = null,
                tint = if (focusedState.value) {
                    Theme.colors.onSurface // Use primary color when focused
                } else {
                    Theme.colors.onSurfaceVariant // Use secondary color when not focused
                }
            )
        },
        error = errorMessage,
        visualTransformation = CardNumberInputTransformation(),
        // Show a success icon when the card number is valid and not blank
        trailingIcon = {
            if (!cardNumber.isNullOrBlank()) {
                InputValidIcon()
            }
        },
        singleLine = true,
        maxLines = 1,
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

@PreviewLightDark
@Composable
internal fun PreviewGiftCardNumberInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            GiftCardNumberInput(value = "4242424242424242") {

            }
        }
    }
}