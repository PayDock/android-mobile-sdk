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

package com.paydock.designsystems.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.paydock.R
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme

@Suppress("LongMethod")
@Composable
internal fun SdkTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    label: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    error: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    var focusedState by remember { mutableStateOf(false) }
    val focusedAndHasText = focusedState || value.isNotEmpty()
    // Set the current label text style based on the focused state and whether there's text
    val labelTextStyle = if (focusedAndHasText) {
        Theme.typography.label
    } else {
        Theme.typography.label.copy(fontSize = Theme.typography.body1.fontSize)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    focusedState = it.isFocused
                }
                .testTag("sdkInput"),
            enabled = enabled,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            maxLines = maxLines,
            interactionSource = interactionSource,
            trailingIcon = if (error != null) {
                {
                    Icon(
                        modifier = Modifier.testTag("errorIcon"),
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = stringResource(id = R.string.content_desc_error_icon),
                        tint = Theme.colors.error
                    )
                }
            } else {
                trailingIcon
            },
            leadingIcon = leadingIcon,
            textStyle = Theme.typography.body1,
            label = {
                Text(
                    modifier = Modifier.testTag("sdkLabel"),
                    maxLines = maxLines,
                    style = labelTextStyle,
                    text = label,
                    color = Theme.colors.onBackground
                )
            },
            placeholder = {
                Text(
                    modifier = Modifier.testTag("sdkPlaceholder"),
                    maxLines = maxLines,
                    style = Theme.typography.body1,
                    text = placeholder,
                    color = Theme.colors.onBackground
                )
            },
            isError = error != null,
            shape = Theme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                // Error Colors
                errorLabelColor = Theme.colors.onSurfaceVariant,
                errorBorderColor = Theme.colors.outline,
                errorCursorColor = Theme.colors.primary,
            ),
        )

        // Theme.shapes.small.copy(all = CornerSize(if (focusedState) 5.dp else 1.dp))

        AnimatedVisibility(visible = error != null) {
            Text(
                modifier = Modifier
                    .padding(start = 15.dp)
                    .testTag("errorLabel"),
                text = error ?: "",
                color = Theme.colors.error,
                style = Theme.typography.label
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewSdkTextFieldEmptyState() {
    var value by remember { mutableStateOf("") }
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            SdkTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "Placeholder",
                label = "Label",
                error = if (value == "error") "This is error" else null
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewSdkTextFieldWithInput() {
    var value by remember { mutableStateOf("Input") }
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            SdkTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "Placeholder",
                label = "Label"
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewSdkTextFieldError() {
    var value by remember { mutableStateOf("xxxx") }
    SdkTheme {
        Surface(color = Theme.colors.surface) {
            SdkTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = "Placeholder",
                label = "Label",
                error = "Enter valid input",
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = "Error Icon",
                    )
                }
            )
        }
    }
}
