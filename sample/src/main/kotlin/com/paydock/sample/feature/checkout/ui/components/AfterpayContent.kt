package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOption
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOptionUpdate
import com.paydock.feature.afterpay.presentation.AfterpayAppearanceDefaults
import com.paydock.feature.afterpay.presentation.AfterpayWidget
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.sample.core.AU_COUNTRY_CODE
import com.paydock.sample.core.AU_CURRENCY_CODE
import java.util.Currency

@Composable
fun AfterpayContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tokenHandler: (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<ChargeResponse>) -> Unit,
) {
    val configuration = AfterpaySDKConfig(
        config = AfterpaySDKConfig.AfterpayConfiguration(
            maximumAmount = "0.50",
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
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        tokenRequest = tokenHandler,
        config = configuration,
        appearance = AfterpayAppearanceDefaults.appearance(),
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
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}