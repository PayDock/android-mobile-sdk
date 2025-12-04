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
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
import com.paydock.feature.src.domain.model.integration.meta.enum.CheckoutExperience
import com.paydock.feature.src.domain.model.integration.meta.enum.Services
import com.paydock.feature.src.domain.model.integration.meta.enum.UnacceptedCardType
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.feature.config.ui.components.properties.card.ConfigCardBrandsField
import com.paydock.sample.feature.config.ui.components.properties.clicktopay.ConfigCoBrandNamesField

@Composable
fun ConfigClickToPayMetaSection(
    currentMeta: ClickToPayMeta?,
    onMetaChange: (ClickToPayMeta?) -> Unit
) {
    // Use current meta or create empty one for editing
    val currentMetaForEditing = currentMeta ?: ClickToPayMeta()

    // Convert enum values with fallback
    val currentCheckoutExperience = remember(currentMetaForEditing.checkoutExperience) {
        currentMetaForEditing.checkoutExperience ?: CheckoutExperience.WITHIN_CHECKOUT
    }

    val currentServices = remember(currentMetaForEditing.services) {
        currentMetaForEditing.services ?: Services.INLINE_CHECKOUT
    }

    val currentUnacceptedCardType = remember(currentMetaForEditing.unacceptedCardType) {
        currentMetaForEditing.unacceptedCardType ?: UnacceptedCardType.CREDIT
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        BooleanField(
            label = stringResource(R.string.label_disable_summary_screen_optional),
            value = currentMetaForEditing.disableSummaryScreen ?: false,
            onValueChange = { newValue ->
                val updatedMeta = currentMetaForEditing.copy(
                    disableSummaryScreen = if (newValue) true else null
                )
                // If all fields are null/empty, set to null, otherwise keep the meta object
                val finalMeta = if (
                    updatedMeta.disableSummaryScreen == null &&
                    updatedMeta.checkoutExperience == null &&
                    updatedMeta.services == null &&
                    updatedMeta.unacceptedCardType == null &&
                    updatedMeta.cardBrands == null &&
                    updatedMeta.coBrandNames == null &&
                    updatedMeta.dpaData == null &&
                    updatedMeta.dpaTransactionOptions == null &&
                    updatedMeta.customer == null
                ) {
                    null
                } else {
                    updatedMeta
                }
                onMetaChange(finalMeta)
            }
        )

        HorizontalDivider()

        EnumDropdown(
            label = stringResource(R.string.label_checkout_experience_optional),
            options = CheckoutExperience.entries,
            selectedOption = currentCheckoutExperience,
            onOptionSelected = { newExperience ->
                val updatedMeta = currentMetaForEditing.copy(
                    checkoutExperience = newExperience
                )
                val finalMeta = if (
                    updatedMeta.disableSummaryScreen == null &&
                    updatedMeta.checkoutExperience == null &&
                    updatedMeta.services == null &&
                    updatedMeta.unacceptedCardType == null &&
                    updatedMeta.cardBrands == null &&
                    updatedMeta.coBrandNames == null &&
                    updatedMeta.dpaData == null &&
                    updatedMeta.dpaTransactionOptions == null &&
                    updatedMeta.customer == null
                ) {
                    null
                } else {
                    updatedMeta
                }
                onMetaChange(finalMeta)
            },
            displayText = { it.name }
        )

        HorizontalDivider()

        EnumDropdown(
            label = stringResource(R.string.label_services_optional),
            options = Services.entries,
            selectedOption = currentServices,
            onOptionSelected = { newServices ->
                val updatedMeta = currentMetaForEditing.copy(
                    services = newServices
                )
                val finalMeta = if (
                    updatedMeta.disableSummaryScreen == null &&
                    updatedMeta.checkoutExperience == null &&
                    updatedMeta.services == null &&
                    updatedMeta.unacceptedCardType == null &&
                    updatedMeta.cardBrands == null &&
                    updatedMeta.coBrandNames == null &&
                    updatedMeta.dpaData == null &&
                    updatedMeta.dpaTransactionOptions == null &&
                    updatedMeta.customer == null
                ) {
                    null
                } else {
                    updatedMeta
                }
                onMetaChange(finalMeta)
            },
            displayText = { it.name }
        )

        HorizontalDivider()

        EnumDropdown(
            label = stringResource(R.string.label_unaccepted_card_type_optional),
            options = UnacceptedCardType.entries,
            selectedOption = currentUnacceptedCardType,
            onOptionSelected = { newType ->
                val updatedMeta = currentMetaForEditing.copy(
                    unacceptedCardType = newType
                )
                val finalMeta = if (
                    updatedMeta.disableSummaryScreen == null &&
                    updatedMeta.checkoutExperience == null &&
                    updatedMeta.services == null &&
                    updatedMeta.unacceptedCardType == null &&
                    updatedMeta.cardBrands == null &&
                    updatedMeta.coBrandNames == null &&
                    updatedMeta.dpaData == null &&
                    updatedMeta.dpaTransactionOptions == null &&
                    updatedMeta.customer == null
                ) {
                    null
                } else {
                    updatedMeta
                }
                onMetaChange(finalMeta)
            },
            displayText = { it.name }
        )

        HorizontalDivider()

        ConfigCardBrandsField(
            label = stringResource(R.string.label_card_brands_optional),
            selectedBrands = currentMetaForEditing.cardBrands,
            onBrandsChange = { newBrands ->
                val updatedMeta = currentMetaForEditing.copy(
                    cardBrands = newBrands
                )
                val finalMeta = if (
                    updatedMeta.disableSummaryScreen == null &&
                    updatedMeta.checkoutExperience == null &&
                    updatedMeta.services == null &&
                    updatedMeta.unacceptedCardType == null &&
                    updatedMeta.cardBrands == null &&
                    updatedMeta.coBrandNames == null &&
                    updatedMeta.dpaData == null &&
                    updatedMeta.dpaTransactionOptions == null &&
                    updatedMeta.customer == null
                ) {
                    null
                } else {
                    updatedMeta
                }
                onMetaChange(finalMeta)
            }
        )

        HorizontalDivider()

        ConfigCoBrandNamesField(
            label = stringResource(R.string.label_co_brand_names_optional),
            coBrandNames = currentMetaForEditing.coBrandNames,
            onCoBrandNamesChange = { newNames ->
                val updatedMeta = currentMetaForEditing.copy(
                    coBrandNames = newNames
                )
                val finalMeta = if (
                    updatedMeta.disableSummaryScreen == null &&
                    updatedMeta.checkoutExperience == null &&
                    updatedMeta.services == null &&
                    updatedMeta.unacceptedCardType == null &&
                    updatedMeta.cardBrands == null &&
                    updatedMeta.coBrandNames == null &&
                    updatedMeta.dpaData == null &&
                    updatedMeta.dpaTransactionOptions == null &&
                    updatedMeta.customer == null
                ) {
                    null
                } else {
                    updatedMeta
                }
                onMetaChange(finalMeta)
            }
        )
    }
}

