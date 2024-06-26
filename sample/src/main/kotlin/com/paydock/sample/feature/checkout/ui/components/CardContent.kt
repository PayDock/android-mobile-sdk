package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.sample.feature.checkout.CheckoutViewModel

@Composable
fun CardContent(viewModel: CheckoutViewModel) {
    CardDetailsWidget(
        modifier = Modifier.padding(vertical = 16.dp),
        completion = viewModel::handleCardResult
    )
}