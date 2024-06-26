package com.paydock.sample.designsystems.components.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.sample.designsystems.theme.SampleTheme
import com.paydock.sample.designsystems.theme.Theme

@Composable
fun NumberCounter(
    modifier: Modifier = Modifier,
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val currentValue = remember { mutableStateOf(value) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = Theme.typography.label,
            color = Theme.colors.onBackground
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = currentValue.value.toString(),
                onValueChange = {
                    val newValue = it.toIntOrNull() ?: 0
                    currentValue.value = newValue
                    onValueChange(newValue)
                },
                readOnly = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = { /* Handle keyboard done action if needed */ }),
                modifier = Modifier.weight(1f),
                textStyle = Theme.typography.body,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Theme.colors.onBackground,
                    unfocusedTextColor = Theme.colors.onBackground,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Column(
                modifier = Modifier.padding(end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
            ) {
                Icon(
                    modifier = Modifier
                        .clickable {
                            currentValue.value++
                            onValueChange(currentValue.value)
                        },
                    imageVector = Icons.Filled.ArrowDropUp,
                    contentDescription = "Increase",
                )
                Icon(
                    modifier = Modifier
                        .clickable {
                            currentValue.value--
                            onValueChange(currentValue.value)
                        },
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Decrease",
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewCopyTextField() {
    SampleTheme {
        NumberCounter(
            modifier = Modifier.fillMaxWidth(),
            title = "Number Field",
            value = 1,
            onValueChange = {})
    }
}