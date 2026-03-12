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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import java.math.BigDecimal

/**
 * Reusable BigDecimal counter component.
 * Used in both Style and Config screens.
 */
@Composable
fun BigDecimalCounter(
    modifier: Modifier = Modifier,
    label: String,
    value: BigDecimal?,
    onValueChange: (BigDecimal?) -> Unit,
    step: BigDecimal = BigDecimal("1.0")
) {
    // Remember the step to ensure it's stable across recompositions
    val rememberedStep = remember(step) { step }

    var currentValue by remember { mutableStateOf(value?.toPlainString() ?: "") }

    // Synchronize internal currentValue with the value prop when the prop changes
    LaunchedEffect(value) {
        val newValueString = value?.toPlainString() ?: ""
        if (currentValue != newValueString) {
            currentValue = newValueString
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = currentValue,
                onValueChange = { newValue ->
                    // Allow empty string or valid decimal number
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        currentValue = newValue
                        val parsedValue = newValue.toBigDecimalOrNull()
                        onValueChange(parsedValue)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val parsedValue = currentValue.toBigDecimalOrNull()
                        onValueChange(parsedValue)
                    }
                ),
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
                            val currentAmount = currentValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            val newValue = currentAmount.add(rememberedStep)
                            currentValue = newValue.toPlainString()
                            onValueChange(newValue)
                        },
                    imageVector = Icons.Filled.ArrowDropUp,
                    contentDescription = stringResource(R.string.content_desc_increase),
                )
                Icon(
                    modifier = Modifier
                        .clickable {
                            val currentAmount = currentValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            val newValue =
                                (currentAmount.subtract(rememberedStep)).coerceAtLeast(BigDecimal.ZERO)
                            currentValue = newValue.toPlainString()
                            onValueChange(newValue)
                        },
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.content_desc_decrease),
                )
            }
        }
    }
}


