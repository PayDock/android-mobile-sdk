package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.feature.card.presentation.model.CardResult

@Composable
fun CardContent(accessToken: String, resultHandler: (Result<CardResult>) -> Unit) {
    CardDetailsWidget(
        modifier = Modifier.padding(vertical = 16.dp),
        accessToken = accessToken,
        completion = resultHandler
    )
}