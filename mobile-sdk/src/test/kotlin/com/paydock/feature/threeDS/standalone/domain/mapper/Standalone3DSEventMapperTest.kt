package com.paydock.feature.threeDS.standalone.domain.mapper

import com.paydock.core.MobileSDKTestConstants
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSProgress
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeCompletedSource
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.ChallengeLoadedReason
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeError
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeErrorEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeResult
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.StandaloneChargeEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Standalone3DSEventMapperTest {

    @Test
    fun `asEntity maps ChargeAuthSuccessEvent correctly`() {
        val event = Standalone3DSEvent.ChargeAuthSuccessEvent(
            data = StandaloneChargeEventData(
                StandaloneStatus.SUCCESS,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )
        val expectedResult = Standalone3DSResult(
            StandaloneEventType.CHARGE_AUTH_SUCCESS,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthRejectEvent correctly`() {
        val event = Standalone3DSEvent.ChargeAuthRejectEvent(
            data = StandaloneChargeEventData(
                StandaloneStatus.ERROR,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )
        val expectedResult = Standalone3DSResult(
            StandaloneEventType.CHARGE_AUTH_REJECT,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthDecoupledEvent correctly`() {
        val event = Standalone3DSEvent.ChargeAuthDecoupledEvent(
            data = StandaloneChargeEventData(
                StandaloneStatus.PENDING,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )
        val expectedResult = Standalone3DSResult(
            StandaloneEventType.CHARGE_AUTH_DECOUPLED,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthInfoEvent correctly`() {
        val event = Standalone3DSEvent.ChargeAuthInfoEvent(
            data = StandaloneChargeEventData(
                StandaloneStatus.PENDING,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )
        val expectedResult = Standalone3DSResult(
            StandaloneEventType.CHARGE_AUTH_INFO,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthChallengeEvent correctly`() {
        val event = Standalone3DSEvent.ChargeAuthChallengeEvent(
            data = StandaloneChargeEventData(
                StandaloneStatus.PENDING,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )
        val expectedResult = Standalone3DSResult(
            StandaloneEventType.CHARGE_AUTH_CHALLENGE,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeErrorEvent correctly`() {
        val event = Standalone3DSEvent.ChargeErrorEvent(
            data = ChargeErrorEventData(
                ChargeError("Charge error occurred!"),
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )
        val expectedResult = Standalone3DSResult(
            StandaloneEventType.CHARGE_ERROR,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthSuccessEvent correctly when chargeId is null`() {
        val event = Standalone3DSEvent.ChargeAuthSuccessEvent(
            data = StandaloneChargeEventData(StandaloneStatus.SUCCESS, null)
        )
        val expectedResult = Standalone3DSResult(
            StandaloneEventType.CHARGE_AUTH_SUCCESS,
            null
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeErrorEvent correctly when chargeId is null`() {
        val event = Standalone3DSEvent.ChargeErrorEvent(
            data = ChargeErrorEventData(
                ChargeError("Charge error occurred!"),
                null
            )
        )
        val expectedResult = Standalone3DSResult(
            StandaloneEventType.CHARGE_ERROR,
            null
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity returns null for progress-only ChargeAuthChallengeLoadedEvent`() {
        val event = Standalone3DSEvent.ChargeAuthChallengeLoadedEvent(
            data = StandaloneChargeEventData(
                status = null,
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                reason = "load"
            )
        )

        assertNull(event.asEntity())
    }

    @Test
    fun `asEntity returns null for progress-only ChargeAuthChallengeCompletedEvent`() {
        val event = Standalone3DSEvent.ChargeAuthChallengeCompletedEvent(
            data = StandaloneChargeEventData(
                status = null,
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                source = "poll"
            )
        )

        assertNull(event.asEntity())
    }

    @Test
    fun `asProgress maps ChargeAuthChallengeEvent to ChallengeStarted`() {
        val event = Standalone3DSEvent.ChargeAuthChallengeEvent(
            data = StandaloneChargeEventData(
                StandaloneStatus.PENDING,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )

        assertEquals(
            Standalone3DSProgress.ChallengeStarted(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID),
            event.asProgress()
        )
    }

    @Test
    fun `asProgress maps ChargeAuthChallengeLoadedEvent reasons`() {
        val expected = mapOf(
            "load" to ChallengeLoadedReason.LOAD,
            "timeout" to ChallengeLoadedReason.TIMEOUT,
            "somethingElse" to ChallengeLoadedReason.UNKNOWN,
            "unknown" to ChallengeLoadedReason.UNKNOWN,
            null to ChallengeLoadedReason.UNKNOWN
        )
        expected.forEach { (rawReason, reason) ->
            val event = Standalone3DSEvent.ChargeAuthChallengeLoadedEvent(
                data = StandaloneChargeEventData(
                    status = null,
                    charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                    reason = rawReason
                )
            )

            assertEquals(
                Standalone3DSProgress.ChallengeLoaded(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID, reason),
                event.asProgress()
            )
        }
    }

    @Test
    fun `asProgress maps ChargeAuthChallengeCompletedEvent sources`() {
        val expected = mapOf(
            "poll" to ChallengeCompletedSource.POLL,
            "callback" to ChallengeCompletedSource.CALLBACK,
            "somethingElse" to ChallengeCompletedSource.UNKNOWN,
            "unknown" to ChallengeCompletedSource.UNKNOWN,
            null to ChallengeCompletedSource.UNKNOWN
        )
        expected.forEach { (rawSource, source) ->
            val event = Standalone3DSEvent.ChargeAuthChallengeCompletedEvent(
                data = StandaloneChargeEventData(
                    status = null,
                    charge3dsId = null,
                    source = rawSource
                )
            )

            assertEquals(Standalone3DSProgress.ChallengeCompleted(null, source), event.asProgress())
        }
    }

    @Test
    fun `asProgress maps ChargeAuthDecoupledEvent to Decoupled with description`() {
        val event = Standalone3DSEvent.ChargeAuthDecoupledEvent(
            data = StandaloneChargeEventData(
                StandaloneStatus.PENDING,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                ChargeResult("Approve in your banking app")
            )
        )

        assertEquals(
            Standalone3DSProgress.Decoupled(
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                "Approve in your banking app"
            ),
            event.asProgress()
        )
    }

    @Test
    fun `asProgress maps ChargeAuthDecoupledEvent without result to Decoupled with null description`() {
        val event = Standalone3DSEvent.ChargeAuthDecoupledEvent(
            data = StandaloneChargeEventData(StandaloneStatus.PENDING, null)
        )

        assertEquals(Standalone3DSProgress.Decoupled(null, null), event.asProgress())
    }

    @Test
    fun `asProgress returns null for non-progress events`() {
        val data = StandaloneChargeEventData(StandaloneStatus.SUCCESS, MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID)
        listOf(
            Standalone3DSEvent.ChargeAuthSuccessEvent(data = data),
            Standalone3DSEvent.ChargeAuthRejectEvent(data = data),
            Standalone3DSEvent.ChargeAuthInfoEvent(data = data),
            Standalone3DSEvent.ChargeErrorEvent(data = ChargeErrorEventData(ChargeError("error"), null))
        ).forEach { event ->
            assertNull(event.asProgress())
        }
    }

    @Test
    fun `asEntity exposes verbatim status and description on a frictionless success`() {
        val event = Standalone3DSEvent.ChargeAuthSuccessEvent(
            data = StandaloneChargeEventData(
                status = StandaloneStatus.SUCCESS,
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                result = ChargeResult(description = "frictionless"),
                rawStatus = "success"
            )
        )

        val actualResult = event.asEntity()

        assertEquals(
            Standalone3DSResult(
                event = StandaloneEventType.CHARGE_AUTH_SUCCESS,
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                status = "success",
                resultDescription = "frictionless"
            ),
            actualResult
        )
    }

    @Test
    fun `asEntity keeps statuses the internal enum cannot represent`() {
        val event = Standalone3DSEvent.ChargeAuthRejectEvent(
            data = StandaloneChargeEventData(
                status = null,
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                rawStatus = "rejected"
            )
        )

        val actualResult = event.asEntity()

        assertEquals("rejected", actualResult?.status)
        assertNull(actualResult?.resultDescription)
    }

    @Test
    fun `asEntity maps blank status and description to null`() {
        val event = Standalone3DSEvent.ChargeAuthSuccessEvent(
            data = StandaloneChargeEventData(
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                result = ChargeResult(description = " "),
                rawStatus = ""
            )
        )

        val actualResult = event.asEntity()

        assertNull(actualResult?.status)
        assertNull(actualResult?.resultDescription)
        assertEquals(MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID, actualResult?.charge3dsId)
    }

    @Test
    fun `asEntity leaves status and description null for error events`() {
        val event = Standalone3DSEvent.ChargeErrorEvent(
            data = ChargeErrorEventData(
                error = ChargeError(message = "boom"),
                charge3dsId = MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
            )
        )

        val actualResult = event.asEntity()

        assertEquals(StandaloneEventType.CHARGE_ERROR, actualResult?.event)
        assertNull(actualResult?.status)
        assertNull(actualResult?.resultDescription)
    }
}
