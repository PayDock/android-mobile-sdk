package com.paydock.sample.feature.config.ui.components.properties.clicktopay

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.feature.src.domain.model.integration.meta.PaymentOption
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun ConfigPaymentOptionsField(
    label: String,
    paymentOptions: List<PaymentOption>?,
    onPaymentOptionsChange: (List<PaymentOption>?) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOptions = paymentOptions ?: emptyList()
    val editableOptions = remember(currentOptions) {
        mutableStateListOf<PaymentOption>().apply {
            addAll(if (currentOptions.isEmpty()) listOf(PaymentOption(null)) else currentOptions)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        editableOptions.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    label = stringResource(R.string.label_dynamic_data_type),
                    value = option.dynamicDataType ?: "",
                    onValueChange = { newValue ->
                        editableOptions[index] = PaymentOption(
                            dynamicDataType = newValue.ifEmpty { null }
                        )
                        // Filter out empty options before saving
                        val filteredOptions = editableOptions.filter {
                            it.dynamicDataType?.isNotBlank() == true
                        }
                        onPaymentOptionsChange(if (filteredOptions.isEmpty()) null else filteredOptions)
                    },
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        editableOptions.removeAt(index)
                        // Filter out empty options before saving
                        val filteredOptions = editableOptions.filter {
                            it.dynamicDataType?.isNotBlank() == true
                        }
                        onPaymentOptionsChange(if (filteredOptions.isEmpty()) null else filteredOptions)
                    }
                ) {
                    Text(stringResource(R.string.button_remove))
                }
            }
        }

        TextButton(
            onClick = {
                editableOptions.add(PaymentOption(null))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.button_add_payment_option))
        }
    }
}

