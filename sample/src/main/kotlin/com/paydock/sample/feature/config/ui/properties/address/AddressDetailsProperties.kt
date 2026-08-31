package com.paydock.sample.feature.config.ui.properties.address

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.TextField
import com.paydock.sample.feature.config.ConfigViewModel
import com.paydock.sample.feature.config.models.ConfigComponent
import com.paydock.sample.feature.config.ui.components.section.ConfigAddressSection
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun AddressDetailsProperties(
    currentAddress: BillingAddress,
    currentActivePrimaryButton: Boolean,
    configItemName: ConfigComponent,
    widgetContext: WidgetType,
    configViewModel: ConfigViewModel
) {
    when (configItemName) {
        ConfigComponent.ACTIVE_PRIMARY_BUTTON -> {
            BooleanField(
                label = stringResource(R.string.label_active_primary_button),
                value = currentActivePrimaryButton,
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ACTIVE_PRIMARY_BUTTON,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.BILLING_ADDRESS -> {
            ConfigAddressSection(
                currentAddress = currentAddress,
                onAddressChange = { newAddress ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.BILLING_ADDRESS,
                        newAddress
                    )
                }
            )
        }

        // Handle sub-components (individual fields) for backward compatibility
        ConfigComponent.FIRST_NAME -> {
            TextField(
                label = stringResource(R.string.label_first_name_optional),
                value = currentAddress.firstName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.FIRST_NAME,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.LAST_NAME -> {
            TextField(
                label = stringResource(R.string.label_last_name_optional),
                value = currentAddress.lastName ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.LAST_NAME,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.NAME -> {
            TextField(
                label = stringResource(R.string.label_full_name_optional),
                value = currentAddress.name ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.NAME,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ADDRESS_LINE1 -> {
            TextField(
                label = stringResource(R.string.label_address_line_1_optional),
                value = currentAddress.addressLine1 ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ADDRESS_LINE1,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.ADDRESS_LINE2 -> {
            TextField(
                label = stringResource(R.string.label_address_line_2_optional),
                value = currentAddress.addressLine2 ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.ADDRESS_LINE2,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.CITY -> {
            TextField(
                label = stringResource(R.string.label_city_optional),
                value = currentAddress.city ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.CITY,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.STATE -> {
            TextField(
                label = stringResource(R.string.label_state_optional),
                value = currentAddress.state ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.STATE,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.POSTAL_CODE -> {
            TextField(
                label = stringResource(R.string.label_postal_code_optional),
                value = currentAddress.postalCode ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.POSTAL_CODE,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.COUNTRY -> {
            TextField(
                label = stringResource(R.string.label_country_optional),
                value = currentAddress.country ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.COUNTRY,
                        newValue
                    )
                }
            )
        }

        ConfigComponent.PHONE_NUMBER -> {
            TextField(
                label = stringResource(R.string.label_phone_number_optional),
                value = currentAddress.phoneNumber ?: "",
                onValueChange = { newValue ->
                    configViewModel.updateWidgetConfig(
                        widgetContext,
                        ConfigComponent.PHONE_NUMBER,
                        newValue
                    )
                }
            )
        }

        else -> {}
    }
}

