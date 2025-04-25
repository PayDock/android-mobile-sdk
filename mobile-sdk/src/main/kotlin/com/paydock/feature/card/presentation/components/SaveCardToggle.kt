package com.paydock.feature.card.presentation.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.link.SdkLinkText
import com.paydock.designsystems.components.toggle.SdkSwitch
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
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            // Consent Label
            Text(
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                text = config.consentText
            )
            // Privacy Policy Label
            if (config.privacyPolicyConfig != null) {
                SdkLinkText(linkText = config.privacyPolicyConfig.privacyPolicyText) {
                    if (enabled) {
                        val uri = config.privacyPolicyConfig.privacyPolicyURL.toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                }
            }
        }

        SdkSwitch(
            modifier = Modifier.align(Alignment.CenterVertically),
            enabled = enabled,
            isChecked = saveCard,
            onCheckedChange = onToggle
        )
    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSaveCardToggleOff() {
    SaveCardToggle(
        saveCard = false,
        config = SaveCardConfig(
            privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(privacyPolicyURL = "https://google.com")
        )
    ) {

    }
}

@SdkLightDarkPreviews
@Composable
internal fun PreviewSaveCardToggleOn() {
    SaveCardToggle(
        saveCard = true,
        config = SaveCardConfig(
            privacyPolicyConfig = SaveCardConfig.PrivacyPolicyConfig(privacyPolicyURL = "https://google.com")
        )
    ) {

    }
}