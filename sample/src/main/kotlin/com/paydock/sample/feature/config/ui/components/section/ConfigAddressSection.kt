package com.paydock.sample.feature.config.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.feature.address.domain.model.integration.BillingAddress
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun ConfigAddressSection(
    currentAddress: BillingAddress,
    onAddressChange: (BillingAddress) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            label = stringResource(R.string.label_first_name_optional),
            value = currentAddress.firstName ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(firstName = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_last_name_optional),
            value = currentAddress.lastName ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(lastName = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_full_name_optional),
            value = currentAddress.name ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(name = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_address_line_1_optional),
            value = currentAddress.addressLine1 ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(addressLine1 = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_address_line_2_optional),
            value = currentAddress.addressLine2 ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(addressLine2 = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_city_optional),
            value = currentAddress.city ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(city = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_state_optional),
            value = currentAddress.state ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(state = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_postal_code_optional),
            value = currentAddress.postalCode ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(postalCode = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_country_optional),
            value = currentAddress.country ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(country = newValue.ifEmpty { null }))
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_phone_number_optional),
            value = currentAddress.phoneNumber ?: "",
            onValueChange = { newValue ->
                onAddressChange(currentAddress.copy(phoneNumber = newValue.ifEmpty { null }))
            }
        )
    }
}

