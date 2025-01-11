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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.components.input.InputValidIcon
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.presentation.utils.errors.CardNumberError
import com.paydock.feature.card.presentation.utils.transformations.CardNumberInputTransformation
import com.paydock.feature.card.presentation.utils.validators.CardSchemeValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputValidator
import com.paydock.feature.card.presentation.utils.validators.CreditCardNumberValidator
import kotlinx.coroutines.delay

/**
 * A composable function for entering and validating a credit card number.
 *
 * This function provides an input field for credit card numbers with validation, error handling,
 * card scheme detection, and UI feedback. It supports user-friendly interactions such as autofill,
 * focus handling, and dynamic input validation.
 *
 * @param modifier Modifier to customize the layout or styling of the input field.
 * @param schemeConfig The configuration defining the supported card schemes and validation settings.
 * @param value The current value of the input field, representing the credit card number.
 * @param enabled Flag to enable or disable user interaction with the input field.
 * @param nextFocus An optional `FocusRequester` for moving focus to the next input field when the
 * 'Next' keyboard action is triggered.
 * @param onValueChange Callback function invoked when the input value changes. The callback receives
 * a parsed credit card number if the input is valid.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("CyclomaticComplexMethod")
@Composable
internal fun CreditCardNumberInput(
    modifier: Modifier = Modifier,
    schemeConfig: SupportedSchemeConfig,
    value: String = "",
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit,
) {
    // State to track the focus state of the input field
    var focusedState by remember { mutableStateOf(false) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    var debouncedValue by remember { mutableStateOf("") }
    LaunchedEffect(value) {
        delay(MobileSDKConstants.General.INPUT_DELAY)
        debouncedValue = value
    }

    // Parse the card number to determine its type and validity
    val cardNumber = CreditCardInputValidator.parseNumber(debouncedValue)
    // Detect the card scheme type to show the appropriate icon
    val cardScheme = CardSchemeValidator.detectCardScheme(value)
    // Validate possible card number errors
    val cardNumberError = CreditCardNumberValidator.validateCardNumberInput(
        debouncedValue,
        hasUserInteracted,
        cardScheme,
        schemeConfig
    )

    val errorMessage = when (cardNumberError) {
        CardNumberError.Empty,
        CardNumberError.InvalidLuhn -> stringResource(id = R.string.error_card_number)
        CardNumberError.UnsupportedCardScheme -> stringResource(id = R.string.error_unsupported_card_scheme)
        CardNumberError.None -> null
    }

    SdkTextField(
        modifier = modifier.onFocusChanged {
            focusedState = it.isFocused
        },
        value = value,
        onValueChange = {
            hasUserInteracted = true
            if (it.length <= MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH) {
                // Parse the input text to ensure it is a valid card number before invoking the callback
                CreditCardInputValidator.parseNumber(it)?.let { number ->
                    onValueChange(number)
                }
            }
        },
        placeholder = stringResource(id = R.string.placeholder_card_number),
        enabled = enabled,
        label = stringResource(id = R.string.label_card_number),
        autofillType = AutofillType.CreditCardNumber,
        leadingIcon = { CardSchemeIcon(cardScheme, focusedState) },
        error = errorMessage,
        visualTransformation = CardNumberInputTransformation(MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH),
        // Show a success icon when the card number is valid and not blank
        trailingIcon = {
            if (!cardNumber.isNullOrBlank()) {
                InputValidIcon()
            }
        },
        // Use keyboard options and actions for a more user-friendly input experience
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = when (nextFocus) {
                null -> ImeAction.Done
                else -> ImeAction.Next
            }
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
            CreditCardNumberInput(value = "4242424242424242", onValueChange = {

            }, schemeConfig = SupportedSchemeConfig())
        }
    }
}