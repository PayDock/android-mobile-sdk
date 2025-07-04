package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.paydock.sample.feature.style.StylingViewModel
import java.util.Currency

@Composable
fun AfterpayContent(
    stylingViewModel: StylingViewModel,
    tokenHandler: (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<ChargeResponse>) -> Unit,
) {
    val afterpayAppearance by stylingViewModel.afterpayWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance = afterpayAppearance ?: AfterpayAppearanceDefaults.appearance()
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        tokenRequest = tokenHandler,
        config = configuration,
        appearance = currentOrDefaultAppearance,
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