package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.presentation.CardDetailsAppearanceDefaults
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.sample.BuildConfig

@Composable
fun CardContent(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loadingDelegate: WidgetLoadingDelegate?,
    resultHandler: (Result<CardResult>) -> Unit
) {
    CardDetailsWidget(
        modifier = modifier,
        enabled = enabled,
        appearance = CardDetailsAppearanceDefaults.appearance(),
        config = CardDetailsWidgetConfig(
            accessToken = BuildConfig.WIDGET_ACCESS_TOKEN,
            actionText = "Pay",
            showCardTitle = false,
            collectCardholderName = false,
            allowSaveCard = SaveCardConfig(
                consentText = "Save payment details",
                privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                    privacyPolicyURL = "https://www.google.com"
                )
            ),
        ),
        loadingDelegate = loadingDelegate,
        completion = resultHandler
    )
}