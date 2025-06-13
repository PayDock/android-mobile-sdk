package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.presentation.CardDetailsAppearanceDefaults
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.sample.BuildConfig
import com.paydock.sample.feature.style.StylingViewModel

@Composable
fun CardContent(
    stylingViewModel: StylingViewModel,
    enabled: Boolean = true,
    loadingDelegate: WidgetLoadingDelegate?,
    resultHandler: (Result<CardResult>) -> Unit
) {
    val cardDetailsAppearance by stylingViewModel.cardDetailsWidgetAppearance.collectAsState()
    val currentOrDefaultAppearance =
        cardDetailsAppearance ?: CardDetailsAppearanceDefaults.appearance()
    CardDetailsWidget(
        modifier = Modifier.padding(vertical = 16.dp),
        enabled = enabled,
        appearance = currentOrDefaultAppearance,
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