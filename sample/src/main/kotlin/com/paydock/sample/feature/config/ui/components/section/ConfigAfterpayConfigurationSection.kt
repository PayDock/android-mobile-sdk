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
import com.paydock.feature.afterpay.domain.model.integration.AfterpaySDKConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BigDecimalCounter
import com.paydock.sample.designsystems.components.fields.EnumDropdown
import com.paydock.sample.feature.config.models.CountryCode
import com.paydock.sample.feature.config.models.CurrencyCode
import com.paydock.sample.feature.config.models.LanguageCode
import java.math.BigDecimal

@Composable
fun ConfigAfterpayConfigurationSection(
    currentConfiguration: AfterpaySDKConfig.AfterpayConfiguration,
    onConfigurationChange: (AfterpaySDKConfig.AfterpayConfiguration) -> Unit
) {
    // Convert string values to enum values, with fallback to first option
    val currentCurrency = remember(currentConfiguration.currency) {
        CurrencyCode.fromCode(currentConfiguration.currency) ?: CurrencyCode.USD
    }

    val currentLanguage = remember(currentConfiguration.language) {
        LanguageCode.fromCode(currentConfiguration.language) ?: LanguageCode.ENGLISH
    }

    val currentCountry = remember(currentConfiguration.country) {
        CountryCode.fromCode(currentConfiguration.country) ?: CountryCode.UNITED_STATES
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        BigDecimalCounter(
            label = stringResource(R.string.label_minimum_amount_optional),
            value = currentConfiguration.minimumAmount?.toBigDecimalOrNull(),
            onValueChange = { newValue ->
                onConfigurationChange(
                    currentConfiguration.copy(
                        minimumAmount = newValue?.toPlainString()
                    )
                )
            },
            step = BigDecimal("0.01"),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        BigDecimalCounter(
            label = stringResource(R.string.label_maximum_amount),
            value = currentConfiguration.maximumAmount.toBigDecimalOrNull(),
            onValueChange = { newValue ->
                onConfigurationChange(
                    currentConfiguration.copy(
                        maximumAmount = newValue?.toPlainString() ?: "0.00"
                    )
                )
            },
            step = BigDecimal("0.01"),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        EnumDropdown(
            label = stringResource(R.string.label_currency),
            options = CurrencyCode.entries,
            selectedOption = currentCurrency,
            onOptionSelected = { newCurrency ->
                onConfigurationChange(
                    currentConfiguration.copy(
                        currency = newCurrency.code
                    )
                )
            },
            displayText = { it.code }
        )

        HorizontalDivider()

        EnumDropdown(
            label = stringResource(R.string.label_language),
            options = LanguageCode.entries,
            selectedOption = currentLanguage,
            onOptionSelected = { newLanguage ->
                onConfigurationChange(
                    currentConfiguration.copy(
                        language = newLanguage.code
                    )
                )
            },
            displayText = { "${it.displayName} (${it.code})" }
        )

        HorizontalDivider()

        EnumDropdown(
            label = stringResource(R.string.label_country),
            options = CountryCode.entries,
            selectedOption = currentCountry,
            onOptionSelected = { newCountry ->
                onConfigurationChange(
                    currentConfiguration.copy(
                        country = newCountry.code
                    )
                )
            },
            displayText = { "${it.displayName} (${it.code})" }
        )
    }
}

