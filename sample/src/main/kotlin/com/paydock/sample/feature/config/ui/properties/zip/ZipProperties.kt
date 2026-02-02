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
import com.paydock.sample.designsystems.components.fields.StringDropdown
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.ui.components.section.ConfigZipAddressSection
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

        // Direct shopper fields
        ConfigComponent.ZIP_FIRST_NAME -> {
            TextField(
                label = stringResource(R.string.label_first_name),
                value = config.firstName,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_FIRST_NAME,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ZIP_LAST_NAME -> {
            TextField(
                label = stringResource(R.string.label_last_name),
                value = config.lastName,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_LAST_NAME,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ZIP_EMAIL -> {
            TextField(
                label = stringResource(R.string.label_email),
                value = config.email,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_EMAIL,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ZIP_PHONE -> {
            TextField(
                label = stringResource(R.string.label_phone_number_optional),
                value = config.phone ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_PHONE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_TOKENIZE -> {
            BooleanField(
                label = stringResource(R.string.label_tokenize),
                value = config.tokenize,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_TOKENIZE,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ZIP_GENDER -> {
            val genderOptions = listOf("male", "female", "other", "prefer_not_to_say")
            StringDropdown(
                title = stringResource(R.string.label_gender_optional),
                options = genderOptions,
                selectedOption = config.gender ?: genderOptions.first(),
                onOptionSelected = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_GENDER,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ZIP_DATE_OF_BIRTH -> {
            TextField(
                label = stringResource(R.string.label_date_of_birth_optional),
                value = config.dateOfBirth ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_DATE_OF_BIRTH,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_SHIPPING_TYPE -> {
            val shippingTypeOptions = listOf("delivery", "pickup")
            StringDropdown(
                title = stringResource(R.string.label_shipping_type_optional),
                options = shippingTypeOptions,
                selectedOption = config.shippingType ?: "delivery",
                onOptionSelected = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_TYPE,
                        newValue
                    )
                }
            )
        }

        // Billing Address Section
        ConfigComponent.ZIP_BILLING_ADDRESS -> {
            ConfigZipAddressSection(
                currentAddress = config.billing,
                onAddressChange = { newAddress ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_ADDRESS,
                        newAddress
                    )
                },
                isShipping = false
            )
        }

        // Individual billing address fields
        ConfigComponent.ZIP_BILLING_FIRST_NAME -> {
            TextField(
                label = stringResource(R.string.label_first_name_optional),
                value = config.billing?.firstName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_FIRST_NAME,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_BILLING_LAST_NAME -> {
            TextField(
                label = stringResource(R.string.label_last_name_optional),
                value = config.billing?.lastName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_LAST_NAME,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_BILLING_LINE1 -> {
            TextField(
                label = stringResource(R.string.label_address_line_1_optional),
                value = config.billing?.line1 ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_LINE1,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_BILLING_LINE2 -> {
            TextField(
                label = stringResource(R.string.label_address_line_2_optional),
                value = config.billing?.line2 ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_LINE2,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_BILLING_CITY -> {
            TextField(
                label = stringResource(R.string.label_city_optional),
                value = config.billing?.city ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_CITY,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_BILLING_STATE -> {
            TextField(
                label = stringResource(R.string.label_state_optional),
                value = config.billing?.state ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_STATE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_BILLING_POSTCODE -> {
            TextField(
                label = stringResource(R.string.label_postcode_optional),
                value = config.billing?.postcode ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_POSTCODE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_BILLING_COUNTRY -> {
            TextField(
                label = stringResource(R.string.label_country_optional),
                value = config.billing?.country ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_BILLING_COUNTRY,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        // Shipping Address Section
        ConfigComponent.ZIP_SHIPPING_ADDRESS -> {
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

        // Individual shipping address fields
        ConfigComponent.ZIP_SHIPPING_FIRST_NAME -> {
            TextField(
                label = stringResource(R.string.label_first_name_optional),
                value = config.shipping?.firstName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_FIRST_NAME,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_SHIPPING_LAST_NAME -> {
            TextField(
                label = stringResource(R.string.label_last_name_optional),
                value = config.shipping?.lastName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_LAST_NAME,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_SHIPPING_LINE1 -> {
            TextField(
                label = stringResource(R.string.label_address_line_1_optional),
                value = config.shipping?.line1 ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_LINE1,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_SHIPPING_LINE2 -> {
            TextField(
                label = stringResource(R.string.label_address_line_2_optional),
                value = config.shipping?.line2 ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_LINE2,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_SHIPPING_CITY -> {
            TextField(
                label = stringResource(R.string.label_city_optional),
                value = config.shipping?.city ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_CITY,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_SHIPPING_STATE -> {
            TextField(
                label = stringResource(R.string.label_state_optional),
                value = config.shipping?.state ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_STATE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_SHIPPING_POSTCODE -> {
            TextField(
                label = stringResource(R.string.label_postcode_optional),
                value = config.shipping?.postcode ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_POSTCODE,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        ConfigComponent.ZIP_SHIPPING_COUNTRY -> {
            TextField(
                label = stringResource(R.string.label_country_optional),
                value = config.shipping?.country ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ZIP_SHIPPING_COUNTRY,
                        newValue.ifEmpty { null }
                    )
                }
            )
        }

        else -> {}
    }
}
