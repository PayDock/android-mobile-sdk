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
import com.paydock.feature.zip.domain.model.integration.ZipWidgetConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun ConfigZipAddressSection(
    currentAddress: ZipWidgetConfig.Address?,
    onAddressChange: (ZipWidgetConfig.Address?) -> Unit
) {
    // Use current address or create empty one for editing
    val currentAddressForEditing = currentAddress ?: ZipWidgetConfig.Address()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            label = stringResource(R.string.label_first_name_optional),
            value = currentAddressForEditing.firstName ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(
                    firstName = newValue.ifEmpty { null }
                )
                // Set to null if all fields are empty
                val finalAddress = if (isAddressEmpty(updatedAddress)) null else updatedAddress
                onAddressChange(finalAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_last_name_optional),
            value = currentAddressForEditing.lastName ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(
                    lastName = newValue.ifEmpty { null }
                )
                val finalAddress = if (isAddressEmpty(updatedAddress)) null else updatedAddress
                onAddressChange(finalAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_address_line_1_optional),
            value = currentAddressForEditing.line1 ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(
                    line1 = newValue.ifEmpty { null }
                )
                val finalAddress = if (isAddressEmpty(updatedAddress)) null else updatedAddress
                onAddressChange(finalAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_address_line_2_optional),
            value = currentAddressForEditing.line2 ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(
                    line2 = newValue.ifEmpty { null }
                )
                val finalAddress = if (isAddressEmpty(updatedAddress)) null else updatedAddress
                onAddressChange(finalAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_city_optional),
            value = currentAddressForEditing.city ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(
                    city = newValue.ifEmpty { null }
                )
                val finalAddress = if (isAddressEmpty(updatedAddress)) null else updatedAddress
                onAddressChange(finalAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_state_optional),
            value = currentAddressForEditing.state ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(
                    state = newValue.ifEmpty { null }
                )
                val finalAddress = if (isAddressEmpty(updatedAddress)) null else updatedAddress
                onAddressChange(finalAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_postal_code_optional),
            value = currentAddressForEditing.postalCode ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(
                    postalCode = newValue.ifEmpty { null }
                )
                val finalAddress = if (isAddressEmpty(updatedAddress)) null else updatedAddress
                onAddressChange(finalAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_country_code_optional),
            value = currentAddressForEditing.countryCode ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(
                    countryCode = newValue.ifEmpty { null }
                )
                val finalAddress = if (isAddressEmpty(updatedAddress)) null else updatedAddress
                onAddressChange(finalAddress)
            }
        )
    }
}

private fun isAddressEmpty(address: ZipWidgetConfig.Address): Boolean {
    return address.firstName.isNullOrEmpty() &&
            address.lastName.isNullOrEmpty() &&
            address.line1.isNullOrEmpty() &&
            address.line2.isNullOrEmpty() &&
            address.city.isNullOrEmpty() &&
            address.state.isNullOrEmpty() &&
            address.postalCode.isNullOrEmpty() &&
            address.countryCode.isNullOrEmpty()
}


