package com.paydock.sample.feature.config.ui.properties.afterpay

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.AfterpayLocaleOption
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.ui.components.section.ConfigAfterpayCheckoutOptionsSection
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun AfterpayProperties(
    config: AfterpaySDKConfig,
    configItemName: ConfigComponent,
    widgetContext: WidgetType,
    configViewModel: ConfigViewModel
) {
    when (configItemName) {
        ConfigComponent.AFTERPAY_LOCALE -> {
            val selectedOption = AfterpayLocaleOption.fromLocale(config.locale)
            EnumDropdown(
                label = stringResource(R.string.label_afterpay_locale),
                options = AfterpayLocaleOption.entries,
                selectedOption = selectedOption,
                onOptionSelected = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.AFTERPAY_LOCALE,
                        newValue.toLocale()
                    )
                },
                displayText = { it.displayName }
            )
        }

        ConfigComponent.AFTERPAY_CHECKOUT_OPTIONS -> {
            ConfigAfterpayCheckoutOptionsSection(
                currentOptions = config.options,
                onOptionsChange = { newOptions ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.AFTERPAY_CHECKOUT_OPTIONS,
                        newOptions
                    )
                }
            )
        }

        ConfigComponent.PICKUP -> {
            BooleanField(
                label = stringResource(R.string.label_pickup_optional),
                value = config.options?.pickup ?: false,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.PICKUP,
                        newValue.takeIf { it }
                    )
                }
            )
        }

        ConfigComponent.BUY_NOW -> {
            BooleanField(
                label = stringResource(R.string.label_buy_now_optional),
                value = config.options?.buyNow ?: false,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.BUY_NOW,
                        newValue.takeIf { it }
                    )
                }
            )
        }

        ConfigComponent.SHIPPING_OPTION_REQUIRED -> {
            BooleanField(
                label = stringResource(R.string.label_shipping_option_required_optional),
                value = config.options?.shippingOptionRequired ?: false,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHIPPING_OPTION_REQUIRED,
                        newValue.takeIf { it }
                    )
                }
            )
        }

        ConfigComponent.ENABLE_SINGLE_SHIPPING_OPTION_UPDATE -> {
            BooleanField(
                label = stringResource(R.string.label_enable_single_shipping_option_update_optional),
                value = config.options?.enableSingleShippingOptionUpdate ?: false,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ENABLE_SINGLE_SHIPPING_OPTION_UPDATE,
                        newValue.takeIf { it }
                    )
                }
            )
        }

        else -> {}
    }
}

