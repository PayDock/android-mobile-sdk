package com.paydock.feature.threeDS.standalone.domain.mapper

import com.paydock.core.MobileSDKTestConstants
import com.paydock.feature.threeDS.standalone.domain.model.integration.Standalone3DSResult
import com.paydock.feature.threeDS.standalone.domain.model.integration.enums.StandaloneEventType
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeError
import com.paydock.feature.threeDS.standalone.domain.model.ui.ChargeErrorEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import com.paydock.feature.threeDS.standalone.domain.model.ui.StandaloneChargeEventData
import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import kotlin.test.Test
import kotlin.test.assertEquals

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
}