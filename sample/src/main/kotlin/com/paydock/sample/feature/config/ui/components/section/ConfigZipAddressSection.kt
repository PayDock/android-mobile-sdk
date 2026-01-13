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
    onAddressChange: (ZipWidgetConfig.Address?) -> Unit,
    isShipping: Boolean = false
) {
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
                val updatedAddress =
                    currentAddressForEditing.copy(firstName = newValue.ifEmpty { null })
                onAddressChange(if (isZipAddressEmpty(updatedAddress)) null else updatedAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_last_name_optional),
            value = currentAddressForEditing.lastName ?: "",
            onValueChange = { newValue ->
                val updatedAddress =
                    currentAddressForEditing.copy(lastName = newValue.ifEmpty { null })
                onAddressChange(if (isZipAddressEmpty(updatedAddress)) null else updatedAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_address_line_1_optional),
            value = currentAddressForEditing.line1 ?: "",
            onValueChange = { newValue ->
                val updatedAddress =
                    currentAddressForEditing.copy(line1 = newValue.ifEmpty { null })
                onAddressChange(if (isZipAddressEmpty(updatedAddress)) null else updatedAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_address_line_2_optional),
            value = currentAddressForEditing.line2 ?: "",
            onValueChange = { newValue ->
                val updatedAddress =
                    currentAddressForEditing.copy(line2 = newValue.ifEmpty { null })
                onAddressChange(if (isZipAddressEmpty(updatedAddress)) null else updatedAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_city_optional),
            value = currentAddressForEditing.city ?: "",
            onValueChange = { newValue ->
                val updatedAddress = currentAddressForEditing.copy(city = newValue.ifEmpty { null })
                onAddressChange(if (isZipAddressEmpty(updatedAddress)) null else updatedAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_state_optional),
            value = currentAddressForEditing.state ?: "",
            onValueChange = { newValue ->
                val updatedAddress =
                    currentAddressForEditing.copy(state = newValue.ifEmpty { null })
                onAddressChange(if (isZipAddressEmpty(updatedAddress)) null else updatedAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_postcode_optional),
            value = currentAddressForEditing.postcode ?: "",
            onValueChange = { newValue ->
                val updatedAddress =
                    currentAddressForEditing.copy(postcode = newValue.ifEmpty { null })
                onAddressChange(if (isZipAddressEmpty(updatedAddress)) null else updatedAddress)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_country_optional),
            value = currentAddressForEditing.country ?: "",
            onValueChange = { newValue ->
                val updatedAddress =
                    currentAddressForEditing.copy(country = newValue.ifEmpty { null })
                onAddressChange(if (isZipAddressEmpty(updatedAddress)) null else updatedAddress)
            }
        )
    }
}

private fun isZipAddressEmpty(address: ZipWidgetConfig.Address): Boolean {
    return address.firstName.isNullOrEmpty() &&
            address.lastName.isNullOrEmpty() &&
            address.line1.isNullOrEmpty() &&
            address.line2.isNullOrEmpty() &&
            address.city.isNullOrEmpty() &&
            address.state.isNullOrEmpty() &&
            address.postcode.isNullOrEmpty() &&
            address.country.isNullOrEmpty()
}
