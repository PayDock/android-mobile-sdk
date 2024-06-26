package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.InputValidIcon
import com.paydock.designsystems.components.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.utils.GiftCardInputValidator

/**
 * A composable function that creates a card pin input field.
 *
 * @param modifier The modifier to apply to the input field.
 * @param value The current value of the input field.
 * @param nextFocus The focus requester for the next input field (optional).
 * @param onValueChange The callback triggered when the input value changes.
 */
@Composable
internal fun CardPinInput(
    modifier: Modifier = Modifier,
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    // Parse the card pin
    val cardPin = GiftCardInputValidator.parseCardPin(value)

    // Determine the error message to display
    val errorMessage =
        if (cardPin == null) stringResource(id = R.string.error_security_code) else null

    // Create the visual representation of the security code input field
    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            // Parse the input value and invoke the callback
            GiftCardInputValidator.parseCardPin(it)?.let { code ->
                onValueChange(code)
            }
        },
        label = stringResource(id = R.string.label_pin),
        placeholder = stringResource(id = R.string.placeholder_card_pin),
        enabled = enabled,
        error = errorMessage,
        // Show a success icon when the security code is valid and not blank
        trailingIcon = {
            if (!cardPin.isNullOrBlank()) {
                InputValidIcon()
            }
        },
        singleLine = true,
        maxLines = 1,
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

@LightDarkPreview
@Composable
private fun PreviewCardPinInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardPinInput(value = "1234") {

            }
        }
    }
}