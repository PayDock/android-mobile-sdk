package com.paydock.sample.feature.config.ui.properties.colespay

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.colespay.integration.ColesPayWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun ColesPayProperties(
    config: ColesPayWidgetConfig,
    configItemName: ConfigComponent,
    widgetContext: WidgetType,
    configViewModel: ConfigViewModel
) {
    when (configItemName) {
        ConfigComponent.CLIENT_ID -> {
            TextField(
                label = stringResource(R.string.label_client_id),
                value = config.clientId,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CLIENT_ID,
                        newValue
                    )
                }
            )
        }

        else -> {}
    }
}

