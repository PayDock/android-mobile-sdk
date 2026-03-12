package com.paydock.sample.feature.config.ui.properties.paypalvault

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.paypal.vault.domain.model.integration.PayPalVaultConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun PayPalVaultProperties(
    config: PayPalVaultConfig,
    configItemName: ConfigComponent,
    widgetContext: WidgetType,
    configViewModel: ConfigViewModel
) {
    when (configItemName) {
        ConfigComponent.ACCESS_TOKEN -> {
            TextField(
                label = stringResource(R.string.label_access_token),
                value = config.accessToken,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ACCESS_TOKEN,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.GATEWAY_ID -> {
            TextField(
                label = stringResource(R.string.label_gateway_id),
                value = config.gatewayId,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.GATEWAY_ID,
                        newValue
                    )
                }
            )
        }

        else -> {}
    }
}

