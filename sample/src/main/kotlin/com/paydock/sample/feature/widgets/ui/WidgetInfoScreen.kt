package com.paydock.sample.feature.widgets.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.paydock.sample.feature.style.StylingViewModel
import com.paydock.sample.feature.widgets.ui.components.AddressDetailsItem
import com.paydock.sample.feature.widgets.ui.components.AfterpayItem
import com.paydock.sample.feature.widgets.ui.components.CardDetailsItem
import com.paydock.sample.feature.widgets.ui.components.ClickToPayItem
import com.paydock.sample.feature.widgets.ui.components.ColesPayItem
import com.paydock.sample.feature.widgets.ui.components.GiftCardItem
import com.paydock.sample.feature.widgets.ui.components.GooglePayItem
import com.paydock.sample.feature.widgets.ui.components.Integrated3DSItem
import com.paydock.sample.feature.widgets.ui.components.PayPalItem
import com.paydock.sample.feature.widgets.ui.components.PayPalVaultItem
import com.paydock.sample.feature.widgets.ui.components.StandaloneThreeDSItem
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun WidgetInfoScreen(
    widgetType: WidgetType,
    stylingViewModel: StylingViewModel
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxWidth()
    ) {
        val context = LocalContext.current
        when (widgetType) {
            WidgetType.ADDRESS_DETAILS -> {
                AddressDetailsItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.AFTER_PAY -> {
                AfterpayItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.CARD_DETAILS -> {
                CardDetailsItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.CLICK_TO_PAY -> {
                ClickToPayItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.COLES_PAY -> {
                ColesPayItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.GIFT_CARD -> {
                GiftCardItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.GOOGLE_PAY -> {
                GooglePayItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.INTEGRATED_3DS -> {
                Integrated3DSItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.PAY_PAL -> {
                PayPalItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.PAY_PAL_VAULT -> {
                PayPalVaultItem(context = context, stylingViewModel = stylingViewModel)
            }

            WidgetType.STANDALONE_3DS -> {
                StandaloneThreeDSItem(context = context, stylingViewModel = stylingViewModel)
            }
        }
    }
}