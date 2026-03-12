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
import androidx.compose.ui.text.input.KeyboardType
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.feature.card.domain.model.integration.enums.CardType
import com.paydock.feature.card.domain.model.ui.CardCode
import com.paydock.feature.card.domain.model.ui.CardScheme
import com.paydock.feature.card.domain.model.ui.enums.CodeType
import com.paydock.feature.card.presentation.utils.errors.CardNumberError
import com.paydock.feature.card.presentation.utils.transformations.CardNumberInputTransformation
import com.paydock.feature.card.presentation.utils.validators.CreditCardInputParser
import com.paydock.feature.card.presentation.utils.validators.CreditCardNumberValidator

/**
 * A composable function for entering and validating a credit card number.
 *
 * This function provides an input field for credit card numbers with validation, error handling,
 * card scheme detection, and UI feedback. It supports user-friendly interactions such as autofill,
 * focus handling, and dynamic input validation.
 *
 * @param modifier Modifier to customize the layout or styling of the input field.
 * @param appearance Defines the visual appearance of the text field, including colors, typography, etc.
 * @param schemeConfig The configuration defining the supported card schemes and validation settings.]
 * @param value The current value of the input field, representing the credit card number.
 * @param cardScheme The detected [CardScheme] based on the card number input.
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
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    schemeConfig: SupportedSchemeConfig,
    value: String = "",
    cardScheme: CardScheme? = null,
    enabled: Boolean = true,
    nextFocus: FocusRequester? = null,
    onValueChange: (String) -> Unit,
) {
    // State to track the focus state of the input field
    var focusedState by remember { mutableStateOf(false) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    // Validate using live value so we don't show Empty/Invalid during debounce (e.g. "4" → validator still saw "" → false error)
    val cardNumberError = CreditCardNumberValidator.validateCardNumberInput(
        cardNumber = value,
        cardScheme = cardScheme,
        schemeConfig = schemeConfig,
        hasUserInteracted = hasUserInteracted,
        isCardNumberFocused = focusedState
    )

    val errorMessage = when (cardNumberError) {
        CardNumberError.Empty -> null // Empty field should show no error (neutral state)
        CardNumberError.InvalidLuhn,
        CardNumberError.InvalidLength -> stringResource(id = R.string.error_card_number)

        CardNumberError.UnsupportedCardScheme -> stringResource(id = R.string.error_unsupported_card_scheme)
        CardNumberError.None -> null
    }

    SdkTextField(
        modifier = modifier.onFocusChanged {
            focusedState = it.isFocused
        },
        appearance = appearance,
        value = value,
        onValueChange = { newText ->
            hasUserInteracted = true
            // Strip spaces and other non-digits so pasted values (e.g. "6334 9000 0000 0005") are accepted
            val digitsOnly = newText.replace(Regex("\\D"), "")
            // Use MAX so paste across schemes works (e.g. Amex 15→Visa 16). Scheme length enforced by validator.
            val maxLength = MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH
            // Always accept deletions to prevent "unable to delete" bug after paste/format operations
            val isDeletion = digitsOnly.length < value.length
            val withinLength = digitsOnly.length <= maxLength
            if (isDeletion || withinLength) {
                CreditCardInputParser.parseNumber(digitsOnly)?.let { number ->
                    onValueChange(number)
                }
            }
        },
        placeholder = stringResource(id = R.string.placeholder_card_number),
        enabled = enabled,
        label = stringResource(id = R.string.label_card_number),
        autofillType = ContentType.CreditCardNumber,
        leadingIcon = { CardSchemeIcon(cardScheme?.type, focusedState, hideFromAccessibility = true) },
        showValidIcon = !focusedState && cardNumberError == CardNumberError.None && value.isNotBlank(),
        error = errorMessage,
        visualTransformation = cardScheme?.let {
            CardNumberInputTransformation(
                subSectionSizes = it.gaps
            )
        } ?: CardNumberInputTransformation(),
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

@SdkLightDarkPreviews
@Composable
internal fun PreviewCreditCardNumberInputDefault() {
    CreditCardNumberInput(
        schemeConfig = SupportedSchemeConfig(),
        onValueChange = {}
    )
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewCreditCardNumberInput() {
    CreditCardNumberInput(
        schemeConfig = SupportedSchemeConfig(),
        value = "4242424242424242",
        cardScheme = CardScheme(
            type = CardType.VISA,
            code = CardCode(
                CodeType.CVV,
                MobileSDKConstants.CardDetailsConfig.CVV_CVC_LENGTH
            )
        ),
        enabled = true,
        nextFocus = null,
        onValueChange = {}
    )
}