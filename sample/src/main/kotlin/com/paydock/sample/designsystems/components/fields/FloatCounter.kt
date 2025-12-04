package com.paydock.sample.designsystems.components.fields

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.paydock.sample.R
import java.text.DecimalFormat

/**
 * Reusable float counter component.
 * Used in Style screens.
 */
@Composable
fun FloatCounter(
    modifier: Modifier = Modifier,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    minValue: Float = 0.0f,
    maxValue: Float = 1.0f,
    step: Float = 0.1f,
    decimalFormatPattern: String = "0.0"
) {
    var currentValue by remember { mutableFloatStateOf(value.coerceIn(minValue, maxValue)) }

    LaunchedEffect(value) {
        if (value != currentValue) {
            currentValue = value.coerceIn(minValue, maxValue)
        }
    }

    val decimalFormat = remember { DecimalFormat(decimalFormatPattern) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = decimalFormat.format(currentValue),
                onValueChange = {
                    val newValue = it.toFloatOrNull() ?: currentValue
                    val coercedValue = newValue.coerceIn(minValue, maxValue)
                    if (currentValue != coercedValue) {
                        currentValue = coercedValue
                        onValueChange(coercedValue)
                    }
                },
                readOnly = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = {}),
                modifier = Modifier.weight(1f),
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.inverseOnSurface),
                verticalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.Top
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    modifier = Modifier
                        .clickable {
                            val newValue = (currentValue + step).coerceIn(minValue, maxValue)
                            val roundedNewValue =
                                (newValue * (1 / step).toInt()).toInt() / (1 / step).toFloat()
                            if (currentValue != roundedNewValue) {
                                currentValue = roundedNewValue
                                onValueChange(roundedNewValue)
                            }
                        },
                    imageVector = Icons.Filled.ArrowDropUp,
                    contentDescription = stringResource(R.string.content_desc_increase),
                )
                Icon(
                    modifier = Modifier
                        .clickable {
                            val newValue = (currentValue - step).coerceIn(minValue, maxValue)
                            val roundedNewValue =
                                (newValue * (1 / step).toInt()).toInt() / (1 / step).toFloat()
                            if (currentValue != roundedNewValue) {
                                currentValue = roundedNewValue
                                onValueChange(roundedNewValue)
                            }
                        },
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.content_desc_decrease),
                )
            }
        }
    }
}


