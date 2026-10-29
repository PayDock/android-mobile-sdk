package com.paydock.sample.feature.threeDS.presentation.state

import com.paydock.sample.feature.card.data.api.dto.VaultTokenRequest

/** The card the demo used before scenarios existed (it triggers a TestLabs challenge). */
private val DEFAULT_TEST_CARD = VaultTokenRequest.CreateCardVaultTokenRequest().cardNumber

/**
 * GPayments TestLabs scenarios the Widgets > Standalone 3DS demo can run, one test card each.
 *
 * Fill in [cardNumber] from the TestLabs test card list (https://docs.activeserver.cloud/en/guides/testlabs/).
 * A scenario without a card number is shown disabled. Test cards only — never a real PAN.
 *
 * @property label The scenario name shown on the picker.
 * @property cardNumber The TestLabs card number that triggers this scenario.
 */
enum class Standalone3DSScenario(val label: String, val cardNumber: String) {
    FRICTIONLESS("Authentication success - frictionless", "4100000000000100"),
    FRICTIONLESS_WITH_3DS_METHOD("Authentication success - frictionless (with 3DS Method)", "4100000000600008"),
    CHALLENGE("Authentication success - challenge", DEFAULT_TEST_CARD),
    CHALLENGE_DECOUPLED("Authentication success - challenge (decoupled)", "4100000000030008"),
    ATTEMPT("Authentication attempt", "4100000000100009"),
    FAILED("Authentication failed", "4100000000300005"),
    UNAVAILABLE("Authentication unavailable", "4100000000400003"),
    REJECTED("Authentication rejected", "4100000000500000");

    /** Whether a test card is configured for this scenario. */
    val isConfigured: Boolean get() = cardNumber.isNotBlank()
}
