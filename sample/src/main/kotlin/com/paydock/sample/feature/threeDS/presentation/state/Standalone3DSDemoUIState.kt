package com.paydock.sample.feature.threeDS.presentation.state

import com.paydock.sample.feature.checkout.models.Standalone3DSFlowState
import com.paydock.sample.feature.checkout.models.Standalone3DSPhase
import java.math.BigDecimal

/**
 * How the authentication reached its outcome, derived from the widget's progress events.
 */
enum class Standalone3DSFlowType(val label: String) {
    FRICTIONLESS("Frictionless"),
    CHALLENGE("Challenge"),
    DECOUPLED("Decoupled")
}

/**
 * Details of a finished standalone 3DS attempt, shown on the demo's result screen.
 *
 * @property charge3dsId The 3DS charge id reported by the widget.
 * @property status The verbatim status reported by the SDK ([com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult.status]).
 * @property description The outcome description reported by the SDK, if any.
 * @property flowType Frictionless, challenge or decoupled.
 * @property amount The authenticated amount.
 * @property currency The ISO currency code of [amount].
 * @property errorMessage The error that interrupted the flow, if any.
 */
data class Standalone3DSDemoOutcome(
    val charge3dsId: String?,
    val status: String?,
    val description: String?,
    val flowType: Standalone3DSFlowType,
    val amount: BigDecimal,
    val currency: String,
    val errorMessage: String? = null
)

/**
 * State of the Widgets > Standalone 3DS demo.
 *
 * @property flow The presentation phase (same phases as the checkout sheet and the web playground).
 * @property threeDSToken The token the widget is mounted with; `null` tears the widget (and its WebView) down.
 * @property outcome Details of the finished attempt, for the success / failure screens.
 * @property attempt Incremented on every run so the widget is recreated even when a token repeats.
 */
data class Standalone3DSDemoUIState(
    val flow: Standalone3DSFlowState = Standalone3DSFlowState(Standalone3DSPhase.PREPARING),
    val threeDSToken: String? = null,
    val outcome: Standalone3DSDemoOutcome? = null,
    val attempt: Int = 0
)
