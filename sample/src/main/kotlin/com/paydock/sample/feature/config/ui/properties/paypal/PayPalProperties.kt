package com.paydock.sample.feature.config.ui.properties.paypal

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.paypal.checkout.domain.model.integration.PayPalWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun PayPalProperties(
    config: PayPalWidgetConfig,
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

        ConfigComponent.REQUEST_SHIPPING -> {
            BooleanField(
                label = stringResource(R.string.label_request_shipping),
                value = config.requestShipping,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.REQUEST_SHIPPING,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.FUNDING_SOURCE -> {
            EnumDropdown(
                label = stringResource(R.string.label_funding_source),
                options = PayPalWidgetConfig.PayPalFundingSource.entries,
                selectedOption = config.fundingSource,
                onOptionSelected = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.FUNDING_SOURCE,
                        newValue
                    )
                }
            )
        }

        else -> {}
    }
}

