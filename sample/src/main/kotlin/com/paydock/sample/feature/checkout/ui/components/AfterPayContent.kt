package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.afterpay.presentation.AfterPayWidget
import com.paydock.feature.afterpay.presentation.model.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOption
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOptionUpdate
import com.paydock.feature.wallet.domain.model.WalletType
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.feature.checkout.CheckoutViewModel
import java.util.Currency

@Composable
fun AfterPayContent(viewModel: CheckoutViewModel) {
    val configuration = AfterpaySDKConfig(
        config = AfterpaySDKConfig.AfterPayConfiguration(
            maximumAmount = "100",
            currency = AU_CURRENCY_CODE,
            language = "en",
            country = AU_COUNTRY_CODE
        ),
        options = AfterpaySDKConfig.CheckoutOptions(
            shippingOptionRequired = true,
            enableSingleShippingOptionUpdate = true
        )
    )
    AfterPayWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        token = viewModel.getWalletToken(WalletType.AFTER_PAY),
        config = configuration,
        selectAddress = { _, provideShippingOptions ->
            val currency = Currency.getInstance(configuration.config.currency)
            val shippingOptions = listOf(
                AfterpayShippingOption(
                    "standard",
                    "Standard",
                    "",
                    currency,
                    "0.00".toBigDecimal(),
                    "50.00".toBigDecimal(),
                    "0.00".toBigDecimal(),
                ),
                AfterpayShippingOption(
                    "priority",
                    "Priority",
                    "Next business day",
                    currency,
                    "10.00".toBigDecimal(),
                    "60.00".toBigDecimal(),
                    null,
                )
            )
            provideShippingOptions(shippingOptions)
        },
        selectShippingOption = { shippingOption, provideShippingOptionUpdateResult ->
            val currency = Currency.getInstance(configuration.config.currency)
            // if standard shipping was selected, update the amounts
            // otherwise leave as is by passing null
            val result: AfterpayShippingOptionUpdate? =
                if (shippingOption.id == "standard") {
                    AfterpayShippingOptionUpdate(
                        "standard",
                        currency,
                        "0.00".toBigDecimal(),
                        "50.00".toBigDecimal(),
                        "2.00".toBigDecimal(),
                    )
                } else {
                    null
                }
            provideShippingOptionUpdateResult(result)
        },
        completion = viewModel::handleChargeResult
    )
}