package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paydock.feature.flypay.presentation.FlyPayWidget
import com.paydock.feature.wallet.domain.model.WalletType
import com.paydock.sample.feature.checkout.CheckoutViewModel

@Composable
fun FlyPayContent(viewModel: CheckoutViewModel) {
    FlyPayWidget(
        modifier = Modifier.fillMaxWidth(),
        token = viewModel.getWalletToken(WalletType.FLY_PAY),
        completion = viewModel::handleFlyPayResult
    )
}