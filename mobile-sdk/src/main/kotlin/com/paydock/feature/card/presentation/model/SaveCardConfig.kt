package com.paydock.feature.card.presentation.model

/**
 * Configuration settings for saving a card.
 *
 * @property consentText The text displayed to the user for card saving consent.
 * @property privacyPolicyConfig The configuration for the privacy policy, if applicable.
 */
data class SaveCardConfig(
    val consentText: String = "Remember this card for next time.",
    val privacyPolicyConfig: PrivacyPolicyConfig? = null
) {
    /**
     * Configuration settings for the privacy policy.
     *
     * @property privacyPolicyText The text displayed to the user for the privacy policy link.
     * @property privacyPolicyURL The URL of the privacy policy document.
     */
    data class PrivacyPolicyConfig(
        val privacyPolicyText: String = "Read our privacy policy",
        val privacyPolicyURL: String
    )
}
