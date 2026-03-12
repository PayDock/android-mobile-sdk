package com.paydock.sample.feature.config.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField

@Composable
fun ConfigAfterpayCheckoutOptionsSection(
    currentOptions: AfterpaySDKConfig.CheckoutOptions?,
    onOptionsChange: (AfterpaySDKConfig.CheckoutOptions?) -> Unit
) {
    // Use current options or create empty one for editing
    val currentOptionsForEditing = currentOptions ?: AfterpaySDKConfig.CheckoutOptions()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        BooleanField(
            label = stringResource(R.string.label_pickup_optional),
            value = currentOptionsForEditing.pickup ?: false,
            onValueChange = { newValue ->
                val updatedOptions = currentOptionsForEditing.copy(
                    pickup = if (newValue) true else null
                )
                // If all fields are null, set to null, otherwise keep the options object
                val finalOptions = if (
                    updatedOptions.pickup == null &&
                    updatedOptions.buyNow == null &&
                    updatedOptions.shippingOptionRequired == null &&
                    updatedOptions.enableSingleShippingOptionUpdate == null
                ) {
                    null
                } else {
                    updatedOptions
                }
                onOptionsChange(finalOptions)
            }
        )

        HorizontalDivider()

        BooleanField(
            label = stringResource(R.string.label_buy_now_optional),
            value = currentOptionsForEditing.buyNow ?: false,
            onValueChange = { newValue ->
                val updatedOptions = currentOptionsForEditing.copy(
                    buyNow = if (newValue) true else null
                )
                val finalOptions = if (
                    updatedOptions.pickup == null &&
                    updatedOptions.buyNow == null &&
                    updatedOptions.shippingOptionRequired == null &&
                    updatedOptions.enableSingleShippingOptionUpdate == null
                ) {
                    null
                } else {
                    updatedOptions
                }
                onOptionsChange(finalOptions)
            }
        )

        HorizontalDivider()

        BooleanField(
            label = stringResource(R.string.label_shipping_option_required_optional),
            value = currentOptionsForEditing.shippingOptionRequired ?: false,
            onValueChange = { newValue ->
                val updatedOptions = currentOptionsForEditing.copy(
                    shippingOptionRequired = if (newValue) true else null
                )
                val finalOptions = if (
                    updatedOptions.pickup == null &&
                    updatedOptions.buyNow == null &&
                    updatedOptions.shippingOptionRequired == null &&
                    updatedOptions.enableSingleShippingOptionUpdate == null
                ) {
                    null
                } else {
                    updatedOptions
                }
                onOptionsChange(finalOptions)
            }
        )

        HorizontalDivider()

        BooleanField(
            label = stringResource(R.string.label_enable_single_shipping_option_update_optional),
            value = currentOptionsForEditing.enableSingleShippingOptionUpdate ?: false,
            onValueChange = { newValue ->
                val updatedOptions = currentOptionsForEditing.copy(
                    enableSingleShippingOptionUpdate = if (newValue) true else null
                )
                val finalOptions = if (
                    updatedOptions.pickup == null &&
                    updatedOptions.buyNow == null &&
                    updatedOptions.shippingOptionRequired == null &&
                    updatedOptions.enableSingleShippingOptionUpdate == null
                ) {
                    null
                } else {
                    updatedOptions
                }
                onOptionsChange(finalOptions)
            }
        )
    }
}

