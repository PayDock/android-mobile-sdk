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
import com.paydock.feature.card.domain.model.integration.enums.CardType.Companion.displayLabel
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
 * @param forceShowErrors Flag to force showing validation errors even if the user hasn't interacted with the field yet.
 * @param nextFocus An optional `FocusRequester` for moving focus to the next input field when the
 * 'Next' keyboard action is triggered.
 * @param a11yFocus An optional [Boolean] that allows programmatically moving accessibility focus to this
 *  input field in a form.
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
    forceShowErrors: Boolean = false,
    nextFocus: FocusRequester? = null,
    a11yFocus: Boolean = false,
    onValueChange: (String) -> Unit
) {
    // State to track the focus state of the input field
    var focusedState by remember { mutableStateOf(false) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    // Validate using live value so we don't show Empty/Invalid during debounce (e.g. "4" → validator still saw "" → false error)
    val cardNumberError = CreditCardNumberValidator.validateCardNumberInput(
        cardNumber = value,
        cardScheme = cardScheme,
        schemeConfig = schemeConfig,
        hasUserInteracted = hasUserInteracted || forceShowErrors,
        isCardNumberFocused = if (forceShowErrors) false else focusedState
    )

    val errorMessage = when (cardNumberError) {
        CardNumberError.Empty -> if (forceShowErrors) stringResource(id = R.string.error_card_number_required) else null
        CardNumberError.InvalidLuhn,
        CardNumberError.InvalidLength -> stringResource(id = R.string.error_card_number)
        CardNumberError.UnsupportedCardScheme -> stringResource(id = R.string.error_unsupported_card_scheme)
        CardNumberError.None -> null
    }

    val placeholder = appearance.placeholderText ?: stringResource(id = R.string.placeholder_card_number)
    val hint = appearance.hintText ?: stringResource(id = R.string.hint_card_number)

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
            if (shouldAcceptCardNumberInput(
                    newDigits = digitsOnly,
                    currentLength = value.length,
                    schemeMaxLength = cardScheme?.lengths?.maxOrNull()
                )
            ) {
                CreditCardInputParser.parseNumber(digitsOnly)?.let { number ->
                    onValueChange(number)
                }
            }
        },
        enabled = enabled,
        label = stringResource(id = R.string.label_card_number),
        a11yFocus = a11yFocus,
        autofillType = ContentType.CreditCardNumber,
        leadingIcon = { CardSchemeIcon(cardScheme?.type, focusedState, hideFromAccessibility = true) },
        leadingIconDescription = cardScheme?.type?.displayLabel(),
        showValidIcon = !focusedState && cardNumberError == CardNumberError.None && value.isNotBlank(),
        error = errorMessage,
        hint = hint,
        placeholder = placeholder,
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

/**
 * Decides whether a card-number edit should be accepted, capping single typed digits at the
 * detected scheme's maximum length so the user can't enter more digits than the scheme allows
 * (e.g. a 17th digit on a 16-digit Mastercard).
 *
 * - Deletions are always accepted (prevents an "unable to delete" bug after paste/format operations).
 * - Single typed digits (incremental entry) are capped at [schemeMaxLength] when a scheme is known,
 *   otherwise at [MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH].
 * - Bulk changes (e.g. paste) may switch the scheme (Amex 15 → Visa 16), so they're allowed up to
 *   the absolute max and the validator flags any over-length input.
 *
 * @param newDigits The proposed value's digits only (non-digits already stripped).
 * @param currentLength The length of the current (raw, digits-only) value.
 * @param schemeMaxLength The detected scheme's maximum length, or null when no scheme is detected.
 * @return true if the edit should be applied; false if it should be rejected.
 */
internal fun shouldAcceptCardNumberInput(
    newDigits: String,
    currentLength: Int,
    schemeMaxLength: Int?
): Boolean {
    val isDeletion = newDigits.length < currentLength
    val isIncrementalEntry = newDigits.length == currentLength + 1
    val maxLength = if (isIncrementalEntry) {
        schemeMaxLength ?: MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH
    } else {
        MobileSDKConstants.CardDetailsConfig.MAX_CREDIT_CARD_LENGTH
    }
    return isDeletion || newDigits.length <= maxLength
}

/**
 * Preview for the credit card number input with default settings.
 */
@SdkLightDarkPreviews
@Composable
internal fun PreviewCreditCardNumberInputDefault() {
    CreditCardNumberInput(
        schemeConfig = SupportedSchemeConfig(),
        onValueChange = {}
    )
}

/**
 * Preview for the credit card number input with a pre-filled valid card number.
 */
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