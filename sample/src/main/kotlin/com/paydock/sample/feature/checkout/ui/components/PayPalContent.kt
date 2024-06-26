package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paydock.feature.paypal.presentation.PayPalWidget
import com.paydock.feature.wallet.domain.model.WalletType
import com.paydock.sample.feature.checkout.CheckoutViewModel

@Composable
fun PayPalContent(viewModel: CheckoutViewModel) {
    PayPalWidget(
        modifier = Modifier.fillMaxWidth(),
        token = viewModel.getWalletToken(WalletType.PAY_PAL),
        requestShipping = false,
        completion = viewModel::handleChargeResult
    )
}