package com.paydock.sample.designsystems.components.fields

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import com.paydock.core.presentation.ui.extensions.toast
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun CopyTextField(
    modifier: Modifier = Modifier,
    value: String,
    readOnly: Boolean = true,
    onValueChange: (String) -> Unit = { /* Read-only default, do nothing */ },
    textStyle: TextStyle = Theme.typography.body,
    trailingIconColor: Color = Color.Black,
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(ClipboardManager::class.java)
    val focusManager = LocalFocusManager.current

    TextField(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChange(value) },
        readOnly = readOnly,
        textStyle = textStyle,
        colors = TextFieldDefaults.colors(
            focusedTextColor = Theme.colors.onBackground,
            unfocusedTextColor = Theme.colors.onBackground,
            disabledTextColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            IconButton(onClick = {
                clipboardManager?.setText(AnnotatedString((value)))
                context.toast("Copied!")
            }) {
                Icon(
                    imageVector = Icons.Default.CopyAll,
                    contentDescription = "Copy Icon",
                    tint = trailingIconColor
                )
            }
        },
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
    )
}

@LightDarkPreview
@Composable
private fun PreviewCopyTextField() {
    SampleTheme {
        CopyTextField(value = "1e092408bsdsh4343232js")
    }
}