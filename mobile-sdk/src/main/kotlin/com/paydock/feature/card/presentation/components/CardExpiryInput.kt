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
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.InputValidIcon
import com.paydock.designsystems.components.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.utils.CardExpiryValidator
import com.paydock.feature.card.presentation.utils.CreditCardInputValidator
import com.paydock.feature.card.presentation.utils.ExpiryInputTransformation

/**
 * Composable that displays an input field for the card expiry date (MM/YY).
 *
 * @param modifier The modifier to be applied to the composable.
 * @param value The current value of the expiry.
 * @param nextFocus The focus requester for the next input field (optional).
 * @param onValueChange The callback function to be invoked when the value of the expiry changes.
 */
@Composable
internal fun CardExpiryInput(
    modifier: Modifier = Modifier,
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    // Parse the expiry value for display purposes
    val expiry = CreditCardInputValidator.parseExpiry(value)

    // Check if the expiry is valid and if the card is expired
    val isValid = CardExpiryValidator.isExpiryValid(value)
    val isExpired = CardExpiryValidator.isCardExpired(value)

    // Determine the appropriate error message based on the conditions
    val errorMessage = when {
        value.isNotBlank() && isExpired && value.length == MobileSDKConstants.CardDetailsConfig.MAX_EXPIRY_LENGTH ->
            stringResource(id = R.string.error_expiry_expired)

        value.isNotBlank() && !isValid -> stringResource(id = R.string.error_expiry_date)
        else -> null
    }

    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            CreditCardInputValidator.parseExpiry(it)?.let { expiry ->
                onValueChange(expiry)
            }
        },
        label = stringResource(id = R.string.label_expiry),
        placeholder = stringResource(id = R.string.placeholder_expiry),
        enabled = enabled,
        error = errorMessage,
        visualTransformation = ExpiryInputTransformation(), // Apply visual transformation for MM/YY format
        trailingIcon = {
            // Show a success icon if the input is valid and not blank
            if (!expiry.isNullOrBlank()) {
                InputValidIcon()
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                nextFocus?.requestFocus()
            }
        )
    )
}

@LightDarkPreview
@Composable
private fun PreviewCardExpiryInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CardExpiryInput(value = "0823") {

            }
        }
    }
}