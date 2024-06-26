package com.paydock.sample.feature.style.ui.components.picker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.sample.core.extensions.color
import com.paydock.sample.core.extensions.isValidHexCode
import com.paydock.sample.core.extensions.toHexCode
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun ColorInputField(
    color: String,
    readOnly: Boolean,
    showCounter: Boolean = false,
    onResultColorChanged: (Color) -> Unit,
    onTextColorChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val isValid = remember {
        mutableStateOf(true)
    }
    val maxChar = 6
    var colorInputValue by rememberSaveable { mutableStateOf(color.replace("#", "")) }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !readOnly,
        readOnly = readOnly,
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color.LightGray
        ),
        textStyle = Theme.typography.label.copy(color = Theme.colors.onBackground),
        value = color.replace("#", ""),
        supportingText = {
            if (showCounter) {
                Text(
                    text = "${color.replace("#", "").length} / $maxChar",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
        },
        onValueChange = { hexValue ->
            if ("#$hexValue".isValidHexCode()) {
                "#${hexValue.take(maxChar)}".color?.let { color ->
                    onResultColorChanged(color)
                    isValid.value = true
                } ?: run {
                    isValid.value = false
                }
            }
            colorInputValue = hexValue
            onTextColorChanged(hexValue)
        },
        prefix = {
            Text(text = "#", style = Theme.typography.label)
        },
        trailingIcon = {
            if (isValid.value) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Color Valid",
                    tint = Color.Green
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Color Invalid",
                    tint = Theme.colors.error
                )
            }
        },
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
    )

    LaunchedEffect(colorInputValue) {
        if (!readOnly) {
            "#${colorInputValue.take(maxChar)}".color?.let { color ->
                isValid.value = true
                onResultColorChanged(color)
            }
            isValid.value = true
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewColorInputField() {
    val color = Color.Blue
    SampleTheme {
        ColorInputField(
            color.toHexCode(),
            readOnly = true,
            showCounter = true,
            onResultColorChanged = {},
            onTextColorChanged = {}
        )
    }
}