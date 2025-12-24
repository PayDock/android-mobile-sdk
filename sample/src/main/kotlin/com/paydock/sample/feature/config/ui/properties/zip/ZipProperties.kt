package com.paydock.sample.feature.config.ui.properties.zip

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.ui.components.section.ConfigZipAddressSection
import com.paydock.sample.feature.config.ui.components.section.ConfigZipShopperSection
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun ZipProperties(
    config: ZipWidgetConfig,
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

        ConfigComponent.ZIP_SHOPPER -> {
            ConfigZipShopperSection(
                currentShopper = config.shopper,
                onShopperChange = { newShopper ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHOPPER,
                        newShopper
                    )
                }
            )
        }

        ConfigComponent.ZIP_BILLING_ADDRESS -> {
            ConfigZipAddressSection(
                currentAddress = config.billing,
                onAddressChange = { newAddress ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_ADDRESS,
                        newAddress
                    )
                }
            )
        }

        // Handle individual shopper fields
        ConfigComponent.SHOPPER_FIRST_NAME -> {
            TextField(
                label = stringResource(R.string.label_first_name),
                value = config.shopper.firstName,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHOPPER_FIRST_NAME,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SHOPPER_LAST_NAME -> {
            TextField(
                label = stringResource(R.string.label_last_name),
                value = config.shopper.lastName,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHOPPER_LAST_NAME,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SHOPPER_EMAIL -> {
            TextField(
                label = stringResource(R.string.label_email),
                value = config.shopper.email,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHOPPER_EMAIL,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.SHOPPER_PHONE -> {
            TextField(
                label = stringResource(R.string.label_phone_number_optional),
                value = config.shopper.phone ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.SHOPPER_PHONE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        // Handle individual billing address fields
        ConfigComponent.ZIP_ADDRESS_FIRST_NAME -> {
            TextField(
                label = stringResource(R.string.label_first_name_optional),
                value = config.billing?.firstName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_ADDRESS_FIRST_NAME,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_ADDRESS_LAST_NAME -> {
            TextField(
                label = stringResource(R.string.label_last_name_optional),
                value = config.billing?.lastName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_ADDRESS_LAST_NAME,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_ADDRESS_LINE1 -> {
            TextField(
                label = stringResource(R.string.label_address_line_1_optional),
                value = config.billing?.line1 ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_ADDRESS_LINE1,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_ADDRESS_LINE2 -> {
            TextField(
                label = stringResource(R.string.label_address_line_2_optional),
                value = config.billing?.line2 ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_ADDRESS_LINE2,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_ADDRESS_CITY -> {
            TextField(
                label = stringResource(R.string.label_city_optional),
                value = config.billing?.city ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_ADDRESS_CITY,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_ADDRESS_STATE -> {
            TextField(
                label = stringResource(R.string.label_state_optional),
                value = config.billing?.state ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_ADDRESS_STATE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_ADDRESS_POSTAL_CODE -> {
            TextField(
                label = stringResource(R.string.label_postal_code_optional),
                value = config.billing?.postalCode ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_ADDRESS_POSTAL_CODE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_ADDRESS_COUNTRY_CODE -> {
            TextField(
                label = stringResource(R.string.label_country_code_optional),
                value = config.billing?.countryCode ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_ADDRESS_COUNTRY_CODE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        else -> {}
    }
}

