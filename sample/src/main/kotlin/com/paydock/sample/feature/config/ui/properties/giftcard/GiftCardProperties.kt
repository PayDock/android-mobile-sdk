package com.paydock.sample.feature.config.ui.properties.giftcard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.card.domain.model.integration.GiftCardWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun GiftCardProperties(
    config: GiftCardWidgetConfig,
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

        ConfigComponent.STORE_PIN -> {
            BooleanField(
                label = stringResource(R.string.label_store_pin),
                value = config.storePin,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.STORE_PIN,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ACTIVE_PRIMARY_BUTTON -> {
            BooleanField(
                label = stringResource(R.string.label_active_primary_button),
                value = config.activePrimaryButton,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ACTIVE_PRIMARY_BUTTON,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SHOW_SUBMIT_BUTTON -> {
            BooleanField(
                label = stringResource(R.string.label_show_submit_button),
                value = config.showSubmitButton,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHOW_SUBMIT_BUTTON,
                        newValue
                    )
                }
            )
        }

        else -> {}
    }
}

