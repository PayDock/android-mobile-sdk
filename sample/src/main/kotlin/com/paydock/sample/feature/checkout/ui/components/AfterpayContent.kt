package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOption
import com.paydock.feature.afterpay.domain.model.integration.AfterpayShippingOptionUpdate
import com.paydock.feature.afterpay.presentation.AfterpayAppearanceDefaults
import com.paydock.feature.afterpay.presentation.AfterpayWidget
import com.paydock.feature.wallet.domain.model.integration.ChargeResponse
import com.paydock.feature.wallet.domain.model.integration.WalletTokenResult
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.shop.data.CartManager
import java.util.Currency
import java.util.Locale

@Composable
fun AfterpayContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tokenHandler: (onTokenReceived: (Result<WalletTokenResult>) -> Unit) -> Unit,
    loadingDelegate: WidgetLoadingDelegate? = null,
    resultHandler: (Result<ChargeResponse>) -> Unit,
    configViewModel: ConfigViewModel = hiltViewModel(),
) {
    val globalConfig by configViewModel.globalConfig.collectAsState()
    val cartManager = remember { CartManager.shared }
    val cartTotal = cartManager.totalPrice

    val configuration = AfterpaySDKConfig(
        locale = Locale("en", "AU"),
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
            val currency = Currency.getInstance(Locale("en", "AU"))
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
            val currency = Currency.getInstance(Locale("en", "AU"))
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