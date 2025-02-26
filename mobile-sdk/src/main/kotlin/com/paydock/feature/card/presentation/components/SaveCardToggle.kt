package com.paydock.feature.card.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.paydock.core.presentation.extensions.alpha20
import com.paydock.designsystems.components.link.HyperlinkText
import com.paydock.designsystems.theme.SdkTheme
import com.paydock.designsystems.theme.Theme
import com.paydock.feature.card.domain.model.integration.SaveCardConfig

/**
 * A composable for displaying a toggle switch to save card details.
 *
 * @param enabled Controls the enabled state of this section.
 * @param saveCard The current state of the save card toggle switch.
 * @param config The configuration for the save card toggle, including consent text and privacy policy.
 * @param onToggle A callback invoked when the toggle switch is changed.
 */
@Composable
internal fun SaveCardToggle(
    enabled: Boolean = true,
    saveCard: Boolean,
    config: SaveCardConfig,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Theme.dimensions.textSpacing, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            // Consent Label
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = Theme.typography.body1,
                text = config.consentText,
                color = Theme.colors.onSurface
            )
            // Privacy Policy Label
            if (config.privacyPolicyConfig != null) {
                HyperlinkText(
                    enabled = enabled,
                    text = config.privacyPolicyConfig.privacyPolicyText,
                    url = config.privacyPolicyConfig.privacyPolicyURL
                )
            }
        }

        Switch(
            modifier = Modifier.align(Alignment.CenterVertically),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Theme.colors.onPrimary,
                checkedTrackColor = Theme.colors.primary,
                checkedBorderColor = Theme.colors.outline,
                uncheckedBorderColor = Theme.colors.outline,
                uncheckedThumbColor = Theme.colors.outline,
                uncheckedTrackColor = Theme.colors.primary.alpha20,
            ),
            enabled = enabled,
            checked = saveCard,
            onCheckedChange = onToggle
        )
    }
}

@PreviewLightDark
@Composable
internal fun PreviewSaveCardToggleOff() {
    SdkTheme {
        SaveCardToggle(saveCard = false, config = SaveCardConfig()) {

        }
    }
}

@PreviewLightDark
@Composable
internal fun PreviewSaveCardToggleOn() {
    SdkTheme {
        SaveCardToggle(saveCard = true, config = SaveCardConfig()) {

        }
    }
}