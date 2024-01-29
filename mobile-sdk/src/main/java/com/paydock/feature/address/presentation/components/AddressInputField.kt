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

package com.paydock.feature.address.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import com.paydock.designsystems.components.InputValidIcon
import com.paydock.designsystems.components.SdkTextField

/**
 * A composable representing an input field for an address component.
 *
 * @param modifier The modifier to be applied to the composable.
 * @param value The current value of the address component.
 * @param label The label to be displayed above the input field.
 * @param nextFocus The [FocusRequester] of the next input field to move focus to.
 * @param onValueUpdated The callback function to be called when the value of the input field is updated.
 */
@Composable
fun AddressInputField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    nextFocus: FocusRequester? = null,
    onValueUpdated: (String) -> Unit
) {
    SdkTextField(
        modifier = modifier,
        value = value,
        onValueChange = { newValue ->
            onValueUpdated(newValue)
        },
        label = label,
        // Show a success icon when the address component is valid and not blank
        trailingIcon = if (value.isNotBlank()) {
            {
                InputValidIcon()
            }
        } else {
            null
        },
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