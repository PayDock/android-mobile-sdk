package com.paydock.sample.feature.config.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.ui.properties.address.AddressDetailsProperties
import com.paydock.sample.feature.config.ui.properties.afterpay.AfterpayProperties
import com.paydock.sample.feature.config.ui.properties.card.CardDetailsProperties
import com.paydock.sample.feature.config.ui.properties.clicktopay.ClickToPayProperties
import com.paydock.sample.feature.config.ui.properties.colespay.ColesPayProperties
import com.paydock.sample.feature.config.ui.properties.giftcard.GiftCardProperties
import com.paydock.sample.feature.config.ui.properties.paypal.PayPalProperties
import com.paydock.sample.feature.config.ui.properties.paypalvault.PayPalVaultProperties
import com.paydock.sample.feature.config.ui.properties.zip.ZipProperties
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun ConfigPropertiesScreen(
    widgetContext: WidgetType,
    configItemName: ConfigComponent,
    configViewModel: ConfigViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (widgetContext) {
            WidgetType.CARD_DETAILS -> {
                val config by configViewModel.cardDetailsWidgetConfig.collectAsState()
                CardDetailsProperties(
                    config = config,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            WidgetType.GIFT_CARD -> {
                val config by configViewModel.giftCardWidgetConfig.collectAsState()
                GiftCardProperties(
                    config = config,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            WidgetType.PAY_PAL -> {
                val config by configViewModel.paypalWidgetConfig.collectAsState()
                PayPalProperties(
                    config = config,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            WidgetType.COLES_PAY -> {
                val config by configViewModel.colesPayWidgetConfig.collectAsState()
                ColesPayProperties(
                    config = config,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            WidgetType.CLICK_TO_PAY -> {
                val config by configViewModel.clickToPayWidgetConfig.collectAsState()
                ClickToPayProperties(
                    config = config,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            WidgetType.AFTER_PAY -> {
                val config by configViewModel.afterpayWidgetConfig.collectAsState()
                AfterpayProperties(
                    config = config,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            WidgetType.PAY_PAL_VAULT -> {
                val config by configViewModel.paypalVaultWidgetConfig.collectAsState()
                PayPalVaultProperties(
                    config = config,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            WidgetType.ADDRESS_DETAILS -> {
                val address by configViewModel.addressConfig.collectAsState()
                // Create a default empty address if null for editing
                val currentAddress = address ?: BillingAddress()

                AddressDetailsProperties(
                    currentAddress = currentAddress,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            WidgetType.ZIP -> {
                val config by configViewModel.zipWidgetConfig.collectAsState()
                ZipProperties(
                    config = config,
                    configItemName = configItemName,
                    widgetContext = widgetContext,
                    configViewModel = configViewModel
                )
            }

            else -> {
                // No config properties for other widgets
            }
        }

        HorizontalDivider()
    }
}

