package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.presentation.CardDetailsWidget

@Composable
fun CardContent(accessToken: String, resultHandler: (Result<CardResult>) -> Unit) {
    CardDetailsWidget(
        modifier = Modifier.padding(vertical = 16.dp),
        accessToken = accessToken,
        actionText = "Pay",
        showCardTitle = false,
        collectCardholderName = false,
        allowSaveCard = SaveCardConfig(
            consentText = "Save payment details",
            privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                privacyPolicyURL = "https://www.google.com"
            )
        ),
        completion = resultHandler
    )
}