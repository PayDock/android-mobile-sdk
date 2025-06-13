package com.paydock.sample.feature.style.ui.components.core.counter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

@Composable
fun FloatCounter(
    modifier: Modifier = Modifier,
    title: String,
    value: Float, // Changed to Float
    onValueChange: (Float) -> Unit, // Changed to Float
    minValue: Float = 0.0f,
    maxValue: Float = 1.0f,
    step: Float = 0.1f,
    decimalFormatPattern: String = "0.0" // For display formatting
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
                value = decimalFormat.format(currentValue), // Format for display
                onValueChange = {
                    val newValue = it.toFloatOrNull() ?: currentValue // Fallback to current
                    val coercedValue = newValue.coerceIn(minValue, maxValue)
                    if (currentValue != coercedValue) {
                        currentValue = coercedValue
                        onValueChange(coercedValue)
                    }
                },
                readOnly = true, // Keeping it readOnly as per your original
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // KeyboardType.Decimal might be better if not readOnly
                keyboardActions = KeyboardActions(onDone = { /* Usually for text input submission */ }),
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
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top), // Adjusted spacing if needed
                horizontalAlignment = Alignment.CenterHorizontally, // Center icons
            ) {
                Icon(
                    modifier = Modifier
                        .clickable {
                            val newValue = (currentValue + step).coerceIn(minValue, maxValue)
                            // Round to avoid precision errors if needed, e.g., for 0.1 + 0.2
                            val roundedNewValue = (newValue * (1 / step).toInt()).toInt() / (1 / step).toFloat()
                            if (currentValue != roundedNewValue) {
                                currentValue = roundedNewValue
                                onValueChange(roundedNewValue)
                            }
                        },
                    imageVector = Icons.Filled.ArrowDropUp,
                    contentDescription = "Increase",
                )
                Icon(
                    modifier = Modifier
                        .clickable {
                            val newValue = (currentValue - step).coerceIn(minValue, maxValue)
                            val roundedNewValue = (newValue * (1 / step).toInt()).toInt() / (1 / step).toFloat()
                            if (currentValue != roundedNewValue) {
                                currentValue = roundedNewValue
                                onValueChange(roundedNewValue)
                            }
                        },
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Decrease",
                )
            }
        }
    }
}