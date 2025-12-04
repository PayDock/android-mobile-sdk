package com.paydock.sample.feature.config.ui.components.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayDPAData
import com.paydock.feature.src.domain.model.integration.meta.PhoneNumber
import com.paydock.feature.src.domain.model.integration.meta.enum.ApplicationType
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun ConfigDPADataSection(
    currentDPAData: ClickToPayDPAData?,
    onDPADataChange: (ClickToPayDPAData?) -> Unit
) {
    // Use current DPA data or create empty one for editing
    val currentDPADataForEditing = currentDPAData ?: ClickToPayDPAData()

    // Convert enum values with fallback
    val currentApplicationType = remember(currentDPADataForEditing.applicationType) {
        currentDPADataForEditing.applicationType ?: ApplicationType.WEB_BROWSER
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // dpaPresentationName is from BaseDPAData and cannot be modified via copy()
        // It's read-only since ClickToPayDPAData constructor doesn't accept it
        TextField(
            label = stringResource(R.string.label_dpa_presentation_name_optional_readonly),
            value = currentDPADataForEditing.dpaPresentationName ?: "",
            onValueChange = { }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_dpa_address_optional),
            value = currentDPADataForEditing.dpaAddress ?: "",
            onValueChange = { newValue ->
                val updatedDPAData = currentDPADataForEditing.copy(
                    dpaAddress = newValue.ifEmpty { null }
                )
                onDPADataChange(updatedDPAData)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_dpa_email_address_optional),
            value = currentDPADataForEditing.dpaEmailAddress ?: "",
            onValueChange = { newValue ->
                val updatedDPAData = currentDPADataForEditing.copy(
                    dpaEmailAddress = newValue.ifEmpty { null }
                )
                onDPADataChange(updatedDPAData)
            }
        )

        HorizontalDivider()

        // DPA Phone Number
        TextField(
            label = stringResource(R.string.label_dpa_phone_number_country_code_optional),
            value = currentDPADataForEditing.dpaPhoneNumber?.countryCode ?: "",
            onValueChange = { newCountryCode ->
                val currentPhone = currentDPADataForEditing.dpaPhoneNumber
                val currentPhoneNumber = currentPhone?.phoneNumber ?: ""

                val updatedDPAData =
                    if (newCountryCode.isNotBlank() && currentPhoneNumber.isNotBlank()) {
                        // Both fields are filled, create PhoneNumber
                        currentDPADataForEditing.copy(
                            dpaPhoneNumber = PhoneNumber(
                                countryCode = newCountryCode,
                                phoneNumber = currentPhoneNumber
                            )
                        )
                    } else {
                        // One or both fields are empty, set to null
                        currentDPADataForEditing.copy(dpaPhoneNumber = null)
                    }
                onDPADataChange(updatedDPAData)
            }
        )

        TextField(
            label = stringResource(R.string.label_dpa_phone_number_phone_number_optional),
            value = currentDPADataForEditing.dpaPhoneNumber?.phoneNumber ?: "",
            onValueChange = { newPhoneNumber ->
                val currentPhone = currentDPADataForEditing.dpaPhoneNumber
                val currentCountryCode = currentPhone?.countryCode ?: ""

                val updatedDPAData =
                    if (currentCountryCode.isNotBlank() && newPhoneNumber.isNotBlank()) {
                        // Both fields are filled, create PhoneNumber
                        currentDPADataForEditing.copy(
                            dpaPhoneNumber = PhoneNumber(
                                countryCode = currentCountryCode,
                                phoneNumber = newPhoneNumber
                            )
                        )
                    } else {
                        // One or both fields are empty, set to null
                        currentDPADataForEditing.copy(dpaPhoneNumber = null)
                    }
                onDPADataChange(updatedDPAData)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_dpa_logo_uri_optional),
            value = currentDPADataForEditing.dpaLogoUri ?: "",
            onValueChange = { newValue ->
                val updatedDPAData = currentDPADataForEditing.copy(
                    dpaLogoUri = newValue.ifEmpty { null }
                )
                onDPADataChange(updatedDPAData)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_dpa_supported_email_address_optional),
            value = currentDPADataForEditing.dpaSupportedEmailAddress ?: "",
            onValueChange = { newValue ->
                val updatedDPAData = currentDPADataForEditing.copy(
                    dpaSupportedEmailAddress = newValue.ifEmpty { null }
                )
                onDPADataChange(updatedDPAData)
            }
        )

        HorizontalDivider()

        // DPA Supported Phone Number
        TextField(
            label = stringResource(R.string.label_dpa_supported_phone_number_country_code_optional),
            value = currentDPADataForEditing.dpaSupportedPhoneNumber?.countryCode ?: "",
            onValueChange = { newCountryCode ->
                val currentPhone = currentDPADataForEditing.dpaSupportedPhoneNumber
                val currentPhoneNumber = currentPhone?.phoneNumber ?: ""

                val updatedDPAData =
                    if (newCountryCode.isNotBlank() && currentPhoneNumber.isNotBlank()) {
                        // Both fields are filled, create PhoneNumber
                        currentDPADataForEditing.copy(
                            dpaSupportedPhoneNumber = PhoneNumber(
                                countryCode = newCountryCode,
                                phoneNumber = currentPhoneNumber
                            )
                        )
                    } else {
                        // One or both fields are empty, set to null
                        currentDPADataForEditing.copy(dpaSupportedPhoneNumber = null)
                    }
                onDPADataChange(updatedDPAData)
            }
        )

        TextField(
            label = stringResource(R.string.label_dpa_supported_phone_number_phone_number_optional),
            value = currentDPADataForEditing.dpaSupportedPhoneNumber?.phoneNumber ?: "",
            onValueChange = { newPhoneNumber ->
                val currentPhone = currentDPADataForEditing.dpaSupportedPhoneNumber
                val currentCountryCode = currentPhone?.countryCode ?: ""

                val updatedDPAData =
                    if (currentCountryCode.isNotBlank() && newPhoneNumber.isNotBlank()) {
                        // Both fields are filled, create PhoneNumber
                        currentDPADataForEditing.copy(
                            dpaSupportedPhoneNumber = PhoneNumber(
                                countryCode = currentCountryCode,
                                phoneNumber = newPhoneNumber
                            )
                        )
                    } else {
                        // One or both fields are empty, set to null
                        currentDPADataForEditing.copy(dpaSupportedPhoneNumber = null)
                    }
                onDPADataChange(updatedDPAData)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_dpa_uri_optional),
            value = currentDPADataForEditing.dpaUri ?: "",
            onValueChange = { newValue ->
                val updatedDPAData = currentDPADataForEditing.copy(
                    dpaUri = newValue.ifEmpty { null }
                )
                onDPADataChange(updatedDPAData)
            }
        )

        HorizontalDivider()

        TextField(
            label = stringResource(R.string.label_dpa_support_uri_optional),
            value = currentDPADataForEditing.dpaSupportUri ?: "",
            onValueChange = { newValue ->
                val updatedDPAData = currentDPADataForEditing.copy(
                    dpaSupportUri = newValue.ifEmpty { null }
                )
                onDPADataChange(updatedDPAData)
            }
        )

        HorizontalDivider()

        EnumDropdown(
            label = stringResource(R.string.label_dpa_application_type_optional),
            options = ApplicationType.entries,
            selectedOption = currentApplicationType,
            onOptionSelected = { newType ->
                val updatedDPAData = currentDPADataForEditing.copy(
                    applicationType = newType
                )
                onDPADataChange(updatedDPAData)
            },
            displayText = { it.name }
        )
    }
}

