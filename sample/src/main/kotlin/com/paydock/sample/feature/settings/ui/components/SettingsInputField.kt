package com.paydock.sample.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.PaydockBlackLight
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun SettingsInputField(
    modifier: Modifier = Modifier,
    label: String? = null,
    value: String,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start
    ) {
        if (!label.isNullOrBlank()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = label,
                style = Theme.typography.label,
                color = PaydockBlackLight,
                fontWeight = FontWeight(400)
            )
        }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            textStyle = Theme.typography.body,
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
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
    }
}
