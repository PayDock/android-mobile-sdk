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
import com.paydock.feature.card.domain.model.integration.SaveCardConfig
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.fields.BooleanField
import com.paydock.sample.designsystems.components.fields.TextField

@Composable
fun ConfigSaveCardSection(
    currentSaveCardConfig: SaveCardConfig?,
    onSaveCardConfigChange: (SaveCardConfig?) -> Unit
) {
    // Use current config or create default one for editing
    val currentConfigForEditing = currentSaveCardConfig ?: SaveCardConfig()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Enable Save Card Toggle
        BooleanField(
            label = stringResource(R.string.label_enable_save_card),
            value = currentSaveCardConfig != null,
            onValueChange = { isEnabled ->
                onSaveCardConfigChange(
                    if (isEnabled) {
                        // Create default SaveCardConfig with privacy policy URL when re-enabled
                        SaveCardConfig(
                            privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(
                                privacyPolicyURL = "https://www.paydock.com/privacy"
                            )
                        )
                    } else {
                        null
                    }
                )
            }
        )

        // Only show the rest of the fields if Save Card is enabled
        if (currentSaveCardConfig != null) {
            HorizontalDivider()

            // Consent Text
            TextField(
                label = stringResource(R.string.label_consent_text),
                value = currentConfigForEditing.consentText,
                onValueChange = { newValue ->
                    onSaveCardConfigChange(
                        currentConfigForEditing.copy(consentText = newValue)
                    )
                }
            )

            HorizontalDivider()

            // Privacy Policy Text
            TextField(
                label = stringResource(R.string.label_privacy_policy_text),
                value = currentConfigForEditing.privacyPolicyConfig?.privacyPolicyText ?: "",
                onValueChange = { newValue ->
                    val currentPrivacyConfig = currentConfigForEditing.privacyPolicyConfig
                    val updatedPrivacyConfig = if (currentPrivacyConfig != null) {
                        currentPrivacyConfig.copy(privacyPolicyText = newValue)
                    } else {
                        SaveCardConfig.PrivacyPolicyConfig(
                            privacyPolicyText = newValue,
                            privacyPolicyURL = ""
                        )
                    }
                    onSaveCardConfigChange(
                        currentConfigForEditing.copy(privacyPolicyConfig = updatedPrivacyConfig)
                    )
                }
            )

            HorizontalDivider()

            // Privacy Policy URL
            TextField(
                label = stringResource(R.string.label_privacy_policy_url),
                value = currentConfigForEditing.privacyPolicyConfig?.privacyPolicyURL ?: "",
                onValueChange = { newValue ->
                    val currentPrivacyConfig = currentConfigForEditing.privacyPolicyConfig
                    val updatedPrivacyConfig = if (currentPrivacyConfig != null) {
                        currentPrivacyConfig.copy(privacyPolicyURL = newValue)
                    } else {
                        SaveCardConfig.PrivacyPolicyConfig(
                            privacyPolicyText = "",
                            privacyPolicyURL = newValue
                        )
                    }
                    onSaveCardConfigChange(
                        currentConfigForEditing.copy(privacyPolicyConfig = updatedPrivacyConfig)
                    )
                }
            )
        }
    }
}

