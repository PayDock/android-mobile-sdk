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
import com.paydock.feature.card.domain.model.integration.SupportedSchemeConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.feature.config.ui.components.properties.card.ConfigCardSchemesField

@Composable
fun ConfigSchemeSupportSection(
    currentSchemeSupport: SupportedSchemeConfig,
    onSchemeSupportChange: (SupportedSchemeConfig) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        BooleanField(
            label = stringResource(R.string.label_enable_scheme_validation),
            value = currentSchemeSupport.enableValidation,
            onValueChange = { newValue ->
                onSchemeSupportChange(
                    currentSchemeSupport.copy(enableValidation = newValue)
                )
            }
        )

        HorizontalDivider()

        ConfigCardSchemesField(
            label = stringResource(R.string.label_supported_card_schemes),
            selectedSchemes = currentSchemeSupport.supportedSchemes,
            onSchemesChange = { newSchemes ->
                onSchemeSupportChange(
                    currentSchemeSupport.copy(supportedSchemes = newSchemes)
                )
            }
        )
    }
}

