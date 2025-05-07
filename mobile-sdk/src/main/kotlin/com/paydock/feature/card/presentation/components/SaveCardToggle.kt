package com.paydock.feature.card.presentation.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.paydock.core.presentation.ui.previews.SdkLightDarkPreviews
import com.paydock.designsystems.components.link.SdkLinkText
import com.paydock.designsystems.components.text.SdkText
import com.paydock.designsystems.components.text.TextAppearance
import com.paydock.designsystems.components.text.TextAppearanceDefaults
import com.paydock.designsystems.components.toggle.SdkToggle
import com.paydock.designsystems.components.toggle.ToggleAppearance
import com.paydock.designsystems.components.toggle.ToggleAppearanceDefaults
import com.paydock.feature.card.domain.model.integration.SaveCardConfig

/**
 * A composable for displaying a toggle switch to save card details, along with associated consent and privacy policy links.
 *
 * @param enabled Controls the enabled state of this section, affecting the toggle and clickable links.
 * @param saveCard The current state of the save card toggle switch (true if card details are to be saved, false otherwise).
 * @param config The configuration for the save card toggle, including the consent text and details for the privacy policy link.
 * @param linkTextAppearance The appearance for the consent text.
 * @param linkAppearance The appearance for the privacy policy link.
 * @param toggleAppearance The appearance for the toggle switch.
 * @param onToggle A callback invoked when the toggle switch is changed. The boolean parameter indicates the new checked state.
 */
@Composable
internal fun SaveCardToggle(
    enabled: Boolean = true,
    saveCard: Boolean,
    config: SaveCardConfig,
    linkTextAppearance: TextAppearance = TextAppearanceDefaults.appearance(),
    linkAppearance: TextAppearance = TextAppearanceDefaults.appearance(),
    toggleAppearance: ToggleAppearance = ToggleAppearanceDefaults.appearance(),
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
            SdkText(
                modifier = Modifier.fillMaxWidth(),
                text = config.consentText,
                appearance = linkTextAppearance
            )
            // Privacy Policy Label
            if (config.privacyPolicyConfig != null) {
                SdkLinkText(linkText = config.privacyPolicyConfig.privacyPolicyText, appearance = linkAppearance) {
                    if (enabled) {
                        val uri = config.privacyPolicyConfig.privacyPolicyURL.toUri()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                }
            }
        }

        SdkToggle(
            modifier = Modifier.align(Alignment.CenterVertically),
            enabled = enabled,
            isChecked = saveCard,
            appearance = toggleAppearance,
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