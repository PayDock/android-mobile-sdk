package com.paydock.feature.address.presentation.components

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
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.paydock.R
import com.paydock.core.MobileSDKConstants
import com.paydock.designsystems.components.input.SdkTextField
import com.paydock.designsystems.components.input.TextFieldAppearance
import com.paydock.designsystems.components.input.TextFieldAppearanceDefaults
import com.paydock.feature.address.utils.errors.AddressInputError
import com.paydock.feature.address.utils.validators.AddressValidator
import kotlinx.coroutines.delay

/**
 * A composable function representing an input field for entering an address component.
 * This field can be customized with a label, supports autofill, and automatically moves
 * the focus to the next input field if specified.
 *
 * @param modifier The [Modifier] to be applied to the composable. It allows customization
 * of the layout, drawing, and interaction behavior.
 * @param appearance The [TextFieldAppearance] to customize the visual style of the input field.
 * @param value The current text value of the address input field. This is a state-managed
 * property that should be updated when the user enters new text.
 * @param label The label to be displayed above the input field, providing context about
 * what the input field represents (e.g., "Street Address" or "City").
 * @param onValueUpdated A callback function that is called whenever the text value of the
 * input field is updated. This function receives the new value as a parameter, allowing
 * the parent composable to handle the state change.
 * @param isMandatory A boolean indicating whether the input field is mandatory.
 * Defaults to `true`. If `true`, an error message will be displayed if the field is empty
 * after user interaction.
 * @param nextFocus An optional [FocusRequester] to specify the next input field that should
 * receive focus when the user completes editing this field. This allows for smooth focus
 * transitions between multiple input fields.
 * @param autofillType An optional [ContentType] indicating the type of data that can be
 * autofilled for this input field (e.g., address, postal code). If provided, it enables
 * autofill support.
 * @param keyboardOptions Optional [KeyboardOptions] to customize the software keyboard,
 * such as the IME action. Defaults to `ImeAction.Next`.
 * @param keyboardActions Optional [KeyboardActions] to specify actions to be performed
 * when IME actions are triggered (e.g., 'Next', 'Done').
 * @param enabled A boolean indicating whether the input field is enabled for user interaction.
 * Defaults to `true`.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun AddressInputField(
    modifier: Modifier = Modifier,
    appearance: TextFieldAppearance = TextFieldAppearanceDefaults.appearance(),
    value: String,
    label: String,
    onValueUpdated: (String) -> Unit,
    isMandatory: Boolean = true,
    nextFocus: FocusRequester? = null,
    autofillType: ContentType? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    var hasUserInteracted by remember { mutableStateOf(false) }

    var debouncedValue by remember { mutableStateOf("") }
    LaunchedEffect(value) {
        delay(MobileSDKConstants.General.INPUT_DELAY)
        debouncedValue = value
    }
    val errorMessage = if (isMandatory) {
        val inputError = AddressValidator.validateInput(debouncedValue, hasUserInteracted)
        when (inputError) {
            AddressInputError.Empty -> stringResource(id = R.string.error_mandatory_field)
            AddressInputError.None -> null
        }
    } else {
        null
    }

    val focusManager = LocalFocusManager.current

    SdkTextField(
        modifier = modifier,
        appearance = appearance,
        value = value,
        onValueChange = { newValue ->
            hasUserInteracted = true
            onValueUpdated(newValue)
        },
        label = label,
        error = errorMessage,
        enabled = enabled,
        autofillType = autofillType,
        keyboardOptions = keyboardOptions.copy(
            capitalization = KeyboardCapitalization.Words,
            imeAction = if (keyboardOptions.imeAction == ImeAction.Default && nextFocus != null) {
                ImeAction.Next
            } else {
                keyboardOptions.imeAction
            }
        ),
        keyboardActions = if (keyboardActions == KeyboardActions.Default && nextFocus != null) {
            KeyboardActions(
                onNext = {
                    nextFocus.requestFocus()
                }
            )
        } else if (keyboardActions == KeyboardActions.Default && nextFocus == null) {
            KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )
        } else {
            keyboardActions
        }
    )
}