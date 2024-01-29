/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.paydock.R
import com.paydock.core.MAX_CREDIT_CARD_LENGTH
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.InputValidIcon
import com.paydock.designsystems.components.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.presentation.model.CardIssuerType
import com.paydock.feature.card.presentation.utils.CardIssuerValidator
import com.paydock.feature.card.presentation.utils.CardNumberInputTransformation
import com.paydock.feature.card.presentation.utils.CreditCardInputValidator

/**
 * A composable that displays an input field for entering a credit card number.
 *
 * @param modifier The modifier to be applied to the composable.
 * @param value The current value of the input field.
 * @param nextFocus The focus requester for the next input field. If provided, pressing 'Next' on the keyboard will
 *                  move focus to the next input field.
 * @param onValueChange The callback to be invoked when the value of the input field changes.
 */
@Composable
internal fun CreditCardNumberInput(
    modifier: Modifier = Modifier,
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit
) {
    // State to track the focus state of the input field
    val focusedState = remember { mutableStateOf(false) }

    // Parse the card number to determine its type and validity
    val cardNumber = CreditCardInputValidator.parseNumber(value)

    // Cardholder Name Luhn check: if it passes, the customer has accidentally put their PAN in the wrong field
    val isLuhnValid = CreditCardInputValidator.isLuhnValid(value)

    // Define the error message to be shown if the card number is invalid
    val errorMessage =
        if (cardNumber == null || (value.isNotBlank() && !isLuhnValid)) stringResource(id = R.string.error_card_number) else null

    // Detect the card issuer type to show the appropriate icon
    val cardIssuer = CardIssuerValidator.detectCardIssuer(value)

    SdkTextField(
        modifier = modifier.onFocusChanged {
            focusedState.value = it.isFocused
        },
        value = value,
        onValueChange = {
            if (it.length <= MAX_CREDIT_CARD_LENGTH) {
                // Parse the input text to ensure it is a valid card number before invoking the callback
                CreditCardInputValidator.parseNumber(it)?.let { number ->
                    onValueChange(number)
                }
            }
        },
        placeholder = stringResource(id = R.string.placeholder_card_number),
        enabled = enabled,
        label = stringResource(id = R.string.label_card_number),
        leadingIcon = {
            // Display the card issuer icon as a leading icon in the input field
            Icon(
                modifier = Modifier.testTag("cardIcon"),
                painter = painterResource(id = cardIssuer.imageResId),
                contentDescription = null,
                // Tint the icon based on whether it's a recognized card issuer or not
                tint = if (cardIssuer == CardIssuerType.OTHER) {
                    if (focusedState.value) {
                        Theme.colors.onSurface // Use primary color when focused
                    } else Theme.colors.onSurfaceVariant // Use secondary color when not focused
                } else Color.Unspecified // Use the default icon color for recognized issuers
            )
        },
        error = errorMessage,
        visualTransformation = CardNumberInputTransformation(MAX_CREDIT_CARD_LENGTH),
        // Show a success icon when the card number is valid and not blank
        trailingIcon = {
            if (!cardNumber.isNullOrBlank()) {
                InputValidIcon()
            }
        },
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

@LightDarkPreview
@Composable
private fun PreviewCardNumberInput() {
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            CreditCardNumberInput(value = "4242424242424242") {

            }
        }
    }
}