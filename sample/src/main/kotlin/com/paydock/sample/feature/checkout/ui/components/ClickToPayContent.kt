package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.presentation.ClickToPayWidget
import com.paydock.sample.BuildConfig

@Composable
fun ClickToPayComponent(
    isLoading: Boolean,
    accessToken: String,
    resultHandler: (Result<String>) -> Unit,
) {
    val modifier = remember {
        Modifier
            .fillMaxWidth()
            .height(750.dp)
    }
    if (!isLoading) {
        ClickToPayWidget(
            modifier = modifier,
            accessToken = accessToken,
            serviceId = BuildConfig.GATEWAY_ID_MASTERCARD_SRC,
            meta = ClickToPayMeta(
                disableSummaryScreen = true
            ),
            completion = resultHandler
        )
    } else {
        Spacer(modifier = modifier)
    }
}