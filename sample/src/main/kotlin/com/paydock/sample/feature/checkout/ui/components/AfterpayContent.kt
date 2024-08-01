package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paydock.feature.afterpay.presentation.AfterpayWidget
import com.paydock.feature.afterpay.presentation.model.AfterpaySDKConfig
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOption
import com.paydock.feature.afterpay.presentation.model.AfterpayShippingOptionUpdate
import com.paydock.feature.charge.domain.model.ChargeResponse
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import com.paydock.sample.designsystems.theme.SampleTheme
import java.util.Currency

@Composable
fun AfterpayContent(
    tokenHandler: (onTokenReceived: (String) -> Unit) -> Unit,
    resultHandler: (Result<ChargeResponse>) -> Unit
) {
    val configuration = AfterpaySDKConfig(
        config = AfterpaySDKConfig.AfterpayConfiguration(
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
    AfterpayWidget(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        token = tokenHandler,
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
        completion = resultHandler
    )
}

@Composable
@Preview
private fun AfterpayContentDefault() {
    SampleTheme {
        AfterpayContent({}, {})
    }
}