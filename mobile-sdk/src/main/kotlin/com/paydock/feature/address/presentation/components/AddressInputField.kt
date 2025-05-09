package com.paydock.feature.address.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults

/**
 * A composable function representing an input field for entering an address component.
 * This field can be customized with a label, supports autofill, and automatically moves
 * the focus to the next input field if specified.
 *
 * @param modifier The [Modifier] to be applied to the composable. It allows customization
 * of the layout, drawing, and interaction behavior.
 * @param value The current text value of the address input field. This is a state-managed
 * property that should be updated when the user enters new text.
 * @param label The label to be displayed above the input field, providing context about
 * what the input field represents (e.g., "Street Address" or "City").
 * @param nextFocus An optional [FocusRequester] to specify the next input field that should
 * receive focus when the user completes editing this field. This allows for smooth focus
 * transitions between multiple input fields.
 * @param autofillType An optional [AutofillType] indicating the type of data that can be
 * autofilled for this input field (e.g., address, postal code). If provided, it enables
 * autofill support.
 * @param onValueUpdated A callback function that is called whenever the text value of the
 * input field is updated. This function receives the new value as a parameter, allowing
 * the parent composable to handle the state change.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AddressInputField(
    modifier: Modifier = Modifier,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    value: String,
    label: String,
    nextFocus: FocusRequester? = null,
    autofillType: AutofillType? = null,
    onValueUpdated: (String) -> Unit
) {
    SdkTextField(
        modifier = modifier,
        appearance = appearance,
        value = value,
        onValueChange = { newValue ->
            onValueUpdated(newValue)
        },
        label = label,
        autofillType = autofillType,
        // Use keyboard options and actions for a more user-friendly input experience
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                nextFocus?.requestFocus() // Move focus to the next input field if available
            }
        )
    )
}