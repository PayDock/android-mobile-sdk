package com.paydock.feature.threeDS.standalone.domain.mapper

import com.paydock.core.extensions.safeCastAs
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeCompletedSource
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeLoadedReason
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.StandaloneChargeEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.asChargeIdProvider

/**
 * Converts a [Standalone3DSEvent] to a [Standalone3DSResult] entity.
 *
 * This function extracts relevant information from a [.Standalone3DSEvent]
 * and transforms it into a [Standalone3DSResult] object. It determines the event type
 * based on the specific subtype of the input event and extracts the charge ID
 * provider if available.
 *
 * Progress-only events ([Standalone3DSEvent.ChargeAuthChallengeLoadedEvent] and
 * [Standalone3DSEvent.ChargeAuthChallengeCompletedEvent]) have no [StandaloneEventType] and are only
 * delivered as [Standalone3DSProgress] (see [asProgress]), so they map to `null`.
 *
 * @receiver The [Standalone3DSEvent] instance to be converted.
 * @return A [Standalone3DSResult] entity containing the extracted event type, charge ID and — when the
 *         web SDK reported them — the verbatim status and result description, or `null`
 *         for progress-only events. The charge ID will be null if it cannot be extracted from the event data.
 *
 * @see Standalone3DSResult
 * @see Standalone3DSEvent
 * @see StandaloneEventType
 * @see StandaloneChargeEventData
 */
internal fun Standalone3DSEvent.asEntity(): Standalone3DSResult? {
    val chargeIdProvider = when (this) {
        is Standalone3DSEvent.ChargeErrorEvent -> this.data.asChargeIdProvider()
        else -> this.data.safeCastAs<StandaloneChargeEventData>()?.asChargeIdProvider()
    }
    val eventType = when (this) {
        is Standalone3DSEvent.ChargeAuthSuccessEvent -> StandaloneEventType.CHARGE_AUTH_SUCCESS
        is Standalone3DSEvent.ChargeAuthRejectEvent -> StandaloneEventType.CHARGE_AUTH_REJECT
        is Standalone3DSEvent.ChargeAuthDecoupledEvent -> StandaloneEventType.CHARGE_AUTH_DECOUPLED
        is Standalone3DSEvent.ChargeAuthInfoEvent -> StandaloneEventType.CHARGE_AUTH_INFO
        is Standalone3DSEvent.ChargeAuthChallengeEvent -> StandaloneEventType.CHARGE_AUTH_CHALLENGE
        is Standalone3DSEvent.ChargeErrorEvent -> StandaloneEventType.CHARGE_ERROR
        is Standalone3DSEvent.ChargeAuthChallengeLoadedEvent,
        is Standalone3DSEvent.ChargeAuthChallengeCompletedEvent -> return null
    }
    val chargeData = this.data.safeCastAs<StandaloneChargeEventData>()
    return Standalone3DSResult(
        event = eventType,
        charge3dsId = chargeIdProvider?.charge3dsId,
        status = chargeData?.rawStatus?.takeIf { it.isNotBlank() },
        resultDescription = chargeData?.result?.description?.takeIf { it.isNotBlank() }
    )
}

/**
 * Converts a [Standalone3DSEvent] to a [Standalone3DSProgress], if the event represents progress.
 *
 * - [Standalone3DSEvent.ChargeAuthChallengeEvent] → [Standalone3DSProgress.ChallengeStarted]
 * - [Standalone3DSEvent.ChargeAuthChallengeLoadedEvent] → [Standalone3DSProgress.ChallengeLoaded]
 * - [Standalone3DSEvent.ChargeAuthChallengeCompletedEvent] → [Standalone3DSProgress.ChallengeCompleted]
 * - [Standalone3DSEvent.ChargeAuthDecoupledEvent] → [Standalone3DSProgress.Decoupled]
 *
 * @receiver The [Standalone3DSEvent] instance to be converted.
 * @return The matching [Standalone3DSProgress], or `null` for events that are not progress
 *         (success, reject, info and error).
 */
internal fun Standalone3DSEvent.asProgress(): Standalone3DSProgress? = when (this) {
    is Standalone3DSEvent.ChargeAuthChallengeEvent ->
        Standalone3DSProgress.ChallengeStarted(data.charge3dsId)

    is Standalone3DSEvent.ChargeAuthChallengeLoadedEvent ->
        Standalone3DSProgress.ChallengeLoaded(
            charge3dsId = data.charge3dsId,
            reason = ChallengeLoadedReason.fromValue(data.reason)
        )

    is Standalone3DSEvent.ChargeAuthChallengeCompletedEvent ->
        Standalone3DSProgress.ChallengeCompleted(
            charge3dsId = data.charge3dsId,
            source = ChallengeCompletedSource.fromValue(data.source)
        )

    is Standalone3DSEvent.ChargeAuthDecoupledEvent ->
        Standalone3DSProgress.Decoupled(
            charge3dsId = data.charge3dsId,
            description = data.result?.description
        )

    is Standalone3DSEvent.ChargeAuthSuccessEvent,
    is Standalone3DSEvent.ChargeAuthRejectEvent,
    is Standalone3DSEvent.ChargeAuthInfoEvent,
    is Standalone3DSEvent.ChargeErrorEvent -> null
}
