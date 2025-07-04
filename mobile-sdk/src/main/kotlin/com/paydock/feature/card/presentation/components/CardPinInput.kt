package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.feature.card.presentation.utils.errors.CardPinError
import com.paydock.feature.card.presentation.utils.validators.CardPinValidator
import com.paydock.feature.card.presentation.utils.validators.GiftCardInputParser
import kotlinx.coroutines.delay

/**
 * A composable function that creates a card pin input field.
 *
 * @param modifier The modifier to apply to the input field.
 * @param value The current value of the input field.
 * @param enabled Flag to enable or disable user interaction with the input field.
 * @param nextFocus The focus requester for the next input field (optional).
 * @param onValueChange The callback triggered when the input value changes.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CardPinInput(
    modifier: Modifier = Modifier,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
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
    // Parse the card pin
    val cardPinError = CardPinValidator.validateCardPinInput(debouncedValue, hasUserInteracted)

    // Determine the error message to display
    val errorMessage = when (cardPinError) {
        CardPinError.Empty,
        CardPinError.Invalid -> stringResource(id = R.string.error_pin)
        CardPinError.None -> null
    }

    // Create the visual representation of the security code input field
    SdkTextField(
        modifier = modifier,
        appearance = appearance,
        value = value,
        onValueChange = {
            hasUserInteracted = true
            // Parse the input value and invoke the callback
            GiftCardInputParser.parseCardPin(it)?.let { code ->
                onValueChange(code)
            }
        },
        label = stringResource(id = R.string.label_pin),
        placeholder = stringResource(id = R.string.placeholder_card_pin),
        enabled = enabled,
        error = errorMessage,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                // Request focus on the next input field
                nextFocus?.requestFocus()
            }
        )
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardPinInputDefault() {
    CardPinInput {}
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCardPinInputValue() {
    CardPinInput(value = "1234") {

    }
}