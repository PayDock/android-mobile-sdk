package com.paydock.sample.feature.config.ui.properties.zip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.ui.components.section.ConfigZipAddressSection
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun ZipShippingAddressHeader(
    config: ZipWidgetConfig,
    widgetContext: WidgetType,
    configViewModel: ConfigViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        BooleanField(
            label = stringResource(R.string.label_use_default_shipping_address),
            value = config.shipping != null,
            onValueChange = { useDefault ->
                configViewModel.updateWidgetConfig(
                    widgetContext,
                    ConfigComponent.ZIP_USE_DEFAULT_SHIPPING_ADDRESS,
                    useDefault
                )
            }
        )
        if (config.shipping != null) {
            ConfigZipAddressSection(
                currentAddress = config.shipping,
                onAddressChange = { newAddress ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_ADDRESS,
                        newAddress
                    )
                },
                isShipping = true
            )
        }
    }
}
