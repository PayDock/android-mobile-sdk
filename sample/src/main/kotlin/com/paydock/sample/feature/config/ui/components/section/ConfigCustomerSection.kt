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
import com.paydock.feature.src.domain.model.integration.meta.Customer
import com.paydock.feature.src.domain.model.integration.meta.Phone
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun ConfigCustomerSection(
    currentCustomer: Customer?,
    onCustomerChange: (Customer?) -> Unit
) {
    // Use current customer or create empty one for editing
    val currentCustomerForEditing = currentCustomer ?: Customer()

    // Phone number parts
    val currentPhoneCountryCode = currentCustomerForEditing.phone?.countryCode ?: ""
    val currentPhoneNumber = currentCustomerForEditing.phone?.phone ?: ""

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            label = stringResource(R.string.label_email_optional),
            value = currentCustomerForEditing.email ?: "",
            onValueChange = { newValue ->
                val updatedCustomer = currentCustomerForEditing.copy(
                    email = newValue.ifEmpty { null }
                )
                // Set to null if all fields are empty
                val finalCustomer = if (isCustomerEmpty(updatedCustomer)) null else updatedCustomer
                onCustomerChange(finalCustomer)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_first_name_optional),
            value = currentCustomerForEditing.firstName ?: "",
            onValueChange = { newValue ->
                val updatedCustomer = currentCustomerForEditing.copy(
                    firstName = newValue.ifEmpty { null }
                )
                val finalCustomer = if (isCustomerEmpty(updatedCustomer)) null else updatedCustomer
                onCustomerChange(finalCustomer)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_last_name_optional),
            value = currentCustomerForEditing.lastName ?: "",
            onValueChange = { newValue ->
                val updatedCustomer = currentCustomerForEditing.copy(
                    lastName = newValue.ifEmpty { null }
                )
                val finalCustomer = if (isCustomerEmpty(updatedCustomer)) null else updatedCustomer
                onCustomerChange(finalCustomer)
            }
        )

        HorizontalDivider()

        // Phone number fields
        TextField(
            label = stringResource(R.string.label_phone_country_code_optional),
            value = currentPhoneCountryCode,
            onValueChange = { newCountryCode ->
                val updatedPhone =
                    if (newCountryCode.isNotBlank() || currentPhoneNumber.isNotBlank()) {
                        Phone(
                            countryCode = newCountryCode.ifEmpty { null },
                            phone = currentPhoneNumber.ifEmpty { null }
                        )
                    } else {
                        null
                    }
                val updatedCustomer = currentCustomerForEditing.copy(phone = updatedPhone)
                val finalCustomer = if (isCustomerEmpty(updatedCustomer)) null else updatedCustomer
                onCustomerChange(finalCustomer)
            }
        )

        TextField(
            label = stringResource(R.string.label_phone_number_optional),
            value = currentPhoneNumber,
            onValueChange = { newPhoneNumber ->
                val updatedPhone =
                    if (currentPhoneCountryCode.isNotBlank() || newPhoneNumber.isNotBlank()) {
                        Phone(
                            countryCode = currentPhoneCountryCode.ifEmpty { null },
                            phone = newPhoneNumber.ifEmpty { null }
                        )
                    } else {
                        null
                    }
                val updatedCustomer = currentCustomerForEditing.copy(phone = updatedPhone)
                val finalCustomer = if (isCustomerEmpty(updatedCustomer)) null else updatedCustomer
                onCustomerChange(finalCustomer)
            }
        )
    }
}

private fun isCustomerEmpty(customer: Customer): Boolean {
    return customer.email.isNullOrEmpty() &&
            customer.firstName.isNullOrEmpty() &&
            customer.lastName.isNullOrEmpty() &&
            (customer.phone == null ||
                    (customer.phone?.countryCode.isNullOrEmpty() && customer.phone?.phone.isNullOrEmpty()))
}

