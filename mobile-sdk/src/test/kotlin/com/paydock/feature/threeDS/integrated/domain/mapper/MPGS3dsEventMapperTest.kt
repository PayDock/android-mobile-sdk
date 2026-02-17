package com.paydock.feature.threeDS.integrated.domain.mapper

import com.paydock.core.MobileSDKTestConstants
import com.paydock.feature.threeDS.integrated.domain.model.integration.MPGS3dsResult
import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.MPGS3dsEventType
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsChargeEventData
import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.MPGS3dsStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class MPGS3dsEventMapperTest {

    @Test
    fun `asEntity maps ChargeAuthSuccessEvent correctly`() {
        val event = MPGS3dsEvent.ChargeAuthSuccessEvent(
            data = MPGS3dsChargeEventData(
                MPGS3dsStatus.AUTHENTICATED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.CHARGE_AUTH_SUCCESS
            )
        )
        val expectedResult = MPGS3dsResult(
            MPGS3dsEventType.CHARGE_AUTH_SUCCESS,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthRejectEvent correctly`() {
        val event = MPGS3dsEvent.ChargeAuthRejectEvent(
            data = MPGS3dsChargeEventData(
                MPGS3dsStatus.NOT_AUTHENTICATED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.CHARGE_AUTH_REJECT
            )
        )
        val expectedResult = MPGS3dsResult(
            MPGS3dsEventType.CHARGE_AUTH_REJECT,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthCancelledEvent correctly`() {
        val event = MPGS3dsEvent.ChargeAuthCancelledEvent(
            data = MPGS3dsChargeEventData(
                MPGS3dsStatus.AUTHENTICATION_CANCELLED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.CHARGE_AUTH_CANCELLED
            )
        )
        val expectedResult = MPGS3dsResult(
            MPGS3dsEventType.CHARGE_AUTH_CANCELLATION,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps AdditionalDataCollectSuccessEvent correctly`() {
        val event = MPGS3dsEvent.AdditionalDataCollectSuccessEvent(
            data = MPGS3dsChargeEventData(
                MPGS3dsStatus.ADDITIONAL_DATA_COMPLETE,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS
            )
        )
        val expectedResult = MPGS3dsResult(
            MPGS3dsEventType.ADDITIONAL_DATA_COLLECT_SUCCESS,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps AdditionalDataCollectRejectEvent correctly`() {
        val event = MPGS3dsEvent.AdditionalDataCollectRejectEvent(
            data = MPGS3dsChargeEventData(
                MPGS3dsStatus.ADDITIONAL_DATA_FAILED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT
            )
        )
        val expectedResult = MPGS3dsResult(
            MPGS3dsEventType.ADDITIONAL_DATA_COLLECT_REJECT,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthEvent correctly`() {
        val event = MPGS3dsEvent.AdditionalDataCollectRejectEvent(
            data = MPGS3dsChargeEventData(
                MPGS3dsStatus.AUTHENTICATED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.CHARGE_AUTH
            )
        )
        val expectedResult = MPGS3dsResult(
            MPGS3dsEventType.ADDITIONAL_DATA_COLLECT_REJECT,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthSuccessEvent correctly when chargeId is null`() {
        val event = MPGS3dsEvent.ChargeAuthSuccessEvent(
            data = MPGS3dsChargeEventData(
                MPGS3dsStatus.AUTHENTICATED,
                null,
                IntegratedEvent.CHARGE_AUTH
            )
        )
        val expectedResult = MPGS3dsResult(
            MPGS3dsEventType.CHARGE_AUTH_SUCCESS,
            null
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

}