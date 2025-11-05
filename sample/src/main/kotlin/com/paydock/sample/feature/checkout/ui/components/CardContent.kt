package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.paydock.core.presentation.util.WidgetLoadingDelegate
import com.paydock.designsystems.components.button.ButtonAppearanceDefaults
import com.paydock.feature.card.domain.model.integration.CardDetailsWidgetConfig
import com.paydock.feature.card.domain.model.integration.CardResult
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.feature.card.presentation.CardDetailsAppearanceDefaults
import com.paydock.feature.card.presentation.CardDetailsWidget
import com.paydock.feature.paypal.vault.domain.model.integration.ButtonIcon
import com.paydock.sample.BuildConfig
import com.paydock.sample.R

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
        appearance = CardDetailsAppearanceDefaults.appearance().copy(
            actionButton = ButtonAppearanceDefaults.outlineButtonAppearance().copy(
                text = stringResource(R.string.button_submit),
                icon = ButtonIcon.Vector(Icons.Filled.CreditCard)
            )
        ),
        config = CardDetailsWidgetConfig(
            accessToken = BuildConfig.ACCESS_TOKEN_WIDGET,
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