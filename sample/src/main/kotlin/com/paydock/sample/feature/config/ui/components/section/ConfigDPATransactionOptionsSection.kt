package com.paydock.sample.feature.config.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayDPAOptions
import com.paydock.feature.src.domain.model.integration.meta.TransactionAmount
import com.paydock.feature.src.domain.model.integration.meta.enum.ClickToPayDPAShippingBillingPreference
import com.paydock.feature.src.domain.model.integration.meta.enum.ClickToPayOrderType
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.models.CurrencyCode
import com.paydock.sample.feature.config.ui.components.properties.clicktopay.ConfigPaymentOptionsField
import java.math.BigDecimal

@Composable
fun ConfigDPATransactionOptionsSection(
    currentOptions: ClickToPayDPAOptions?,
    onOptionsChange: (ClickToPayDPAOptions?) -> Unit
) {
    // Use current options or create empty one for editing
    val currentOptionsForEditing = currentOptions ?: ClickToPayDPAOptions()

    // Convert enum values with fallback
    val currentBillingPreference = remember(currentOptionsForEditing.dpaBillingPreference) {
        currentOptionsForEditing.dpaBillingPreference ?: ClickToPayDPAShippingBillingPreference.NONE
    }

    val currentOrderType = remember(currentOptionsForEditing.orderType) {
        currentOptionsForEditing.orderType ?: ClickToPayOrderType.SPLIT_SHIPMENT
    }

    // Transaction Amount - inherited from BaseDPAOptions, accessible but needs special handling
    val currentTransactionAmount = currentOptionsForEditing.transactionAmount
    val currentCurrency = remember(currentTransactionAmount?.transactionCurrencyCode) {
        currentTransactionAmount?.transactionCurrencyCode?.let { code ->
            CurrencyCode.fromCode(code)
        } ?: CurrencyCode.USD
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        EnumDropdown(
            label = stringResource(R.string.label_dpa_billing_preference_optional),
            options = ClickToPayDPAShippingBillingPreference.entries,
            selectedOption = currentBillingPreference,
            onOptionSelected = { newPreference ->
                val updatedOptions = currentOptionsForEditing.copy(
                    dpaBillingPreference = newPreference
                )
                onOptionsChange(updatedOptions)
            },
            displayText = { it.name }
        )

        HorizontalDivider()

        EnumDropdown(
            label = stringResource(R.string.label_order_type_optional),
            options = ClickToPayOrderType.entries,
            selectedOption = currentOrderType,
            onOptionSelected = { newOrderType ->
                val updatedOptions = currentOptionsForEditing.copy(
                    orderType = newOrderType
                )
                onOptionsChange(updatedOptions)
            },
            displayText = { it.name }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_3ds_preference_optional),
            value = currentOptionsForEditing.threeDSPreference ?: "",
            onValueChange = { newValue ->
                val updatedOptions = currentOptionsForEditing.copy(
                    threeDSPreference = newValue.ifEmpty { null }
                )
                onOptionsChange(updatedOptions)
            }
        )

        HorizontalDivider()

        BooleanField(
            label = stringResource(R.string.label_confirm_payment_optional),
            value = currentOptionsForEditing.confirmPayment ?: false,
            onValueChange = { newValue ->
                val updatedOptions = currentOptionsForEditing.copy(
                    confirmPayment = if (newValue) true else null
                )
                onOptionsChange(updatedOptions)
            }
        )

        HorizontalDivider()

        ConfigPaymentOptionsField(
            label = stringResource(R.string.label_payment_options_optional),
            paymentOptions = currentOptionsForEditing.paymentOptions,
            onPaymentOptionsChange = { newOptions ->
                // Filter out empty options before saving
                val filteredOptions = newOptions?.filter {
                    it.dynamicDataType?.isNotBlank() == true
                }
                val updatedOptions = currentOptionsForEditing.copy(
                    paymentOptions = if (filteredOptions.isNullOrEmpty()) null else filteredOptions
                )
                onOptionsChange(updatedOptions)
            }
        )

        HorizontalDivider()

        // Transaction Amount - Note: transactionAmount is inherited from BaseDPAOptions
        // We can read it but need to recreate the object to modify it
        TextField(
            label = stringResource(R.string.label_transaction_amount_optional),
            value = currentTransactionAmount?.transactionAmount?.toString() ?: "",
            onValueChange = { newValue ->
                val updatedAmount = if (newValue.isNotBlank()) {
                    try {
                        val amount = BigDecimal(newValue)
                        TransactionAmount(
                            transactionAmount = amount,
                            transactionCurrencyCode = currentTransactionAmount?.transactionCurrencyCode
                        )
                    } catch (e: NumberFormatException) {
                        currentTransactionAmount
                    }
                } else {
                    currentTransactionAmount?.transactionCurrencyCode?.let {
                        TransactionAmount(
                            transactionAmount = null,
                            transactionCurrencyCode = it
                        )
                    } ?: null
                }
                // Note: transactionAmount is inherited from BaseDPAOptions and can't be modified via .copy()
                // Updates should be done via individual property handlers (TRANSACTION_AMOUNT_VALUE)
                // For now, we'll keep the current options unchanged
                // The transactionAmount will be updated via ConfigViewModel handlers
                onOptionsChange(currentOptionsForEditing)
            }
        )

        EnumDropdown(
            label = stringResource(R.string.label_transaction_currency_code_optional),
            options = CurrencyCode.entries,
            selectedOption = currentCurrency,
            onOptionSelected = { newCurrency ->
                val updatedAmount = TransactionAmount(
                    transactionAmount = currentTransactionAmount?.transactionAmount,
                    transactionCurrencyCode = newCurrency.code
                )
                // Note: transactionAmount is inherited from BaseDPAOptions and can't be modified via .copy()
                // Updates should be done via individual property handlers (TRANSACTION_CURRENCY_CODE)
                // For now, we'll keep the current options unchanged
                // The transactionAmount will be updated via ConfigViewModel handlers
                onOptionsChange(currentOptionsForEditing)
            },
            displayText = { it.code }
        )
    }
}


