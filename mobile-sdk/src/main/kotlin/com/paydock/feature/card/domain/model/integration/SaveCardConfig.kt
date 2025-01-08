package com.paydock.feature.card.domain.model.integration

import com.paydock.core.MobileSDKConstants

/**
 * Configuration settings for saving a card.
 *
 * @property consentText The text displayed to the user for card saving consent.
 * @property privacyPolicyConfig The configuration for the privacy policy, if applicable.
 */
data class SaveCardConfig(
    val consentText: String = MobileSDKConstants.CardDetailsConfig.DEFAULT_CONSENT_TEXT,
    val privacyPolicyConfig: PrivacyPolicyConfig? = null
) {
    /**
     * Configuration settings for the privacy policy.
     *
     * @property privacyPolicyText The text displayed to the user for the privacy policy link.
     * @property privacyPolicyURL The URL of the privacy policy document.
     */
    data class PrivacyPolicyConfig(
        val privacyPolicyText: String = MobileSDKConstants.CardDetailsConfig.DEFAULT_POLICY_TEXT,
        val privacyPolicyURL: String
    )
}
