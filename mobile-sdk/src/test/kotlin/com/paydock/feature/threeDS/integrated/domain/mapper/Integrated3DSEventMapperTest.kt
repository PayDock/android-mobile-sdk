package com.paydock.feature.threeDS.integrated.domain.mapper

import com.paydock.core.MobileSDKTestConstants
import com.paydock.feature.threeDS.integrated.domain.model.integration.Integrated3DSResult
import com.paydock.feature.threeDS.integrated.domain.model.integration.enums.IntegratedEventType
import com.paydock.feature.threeDS.integrated.domain.model.ui.Integrated3DSEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.IntegratedChargeEventData
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedEvent
import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.IntegratedStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class Integrated3DSEventMapperTest {

    @Test
    fun `asEntity maps ChargeAuthSuccessEvent correctly`() {
        val event = Integrated3DSEvent.ChargeAuthSuccessEvent(
            data = IntegratedChargeEventData(
                IntegratedStatus.AUTHENTICATED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.CHARGE_AUTH_SUCCESS
            )
        )
        val expectedResult = Integrated3DSResult(
            IntegratedEventType.CHARGE_AUTH_SUCCESS,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthRejectEvent correctly`() {
        val event = Integrated3DSEvent.ChargeAuthRejectEvent(
            data = IntegratedChargeEventData(
                IntegratedStatus.NOT_AUTHENTICATED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.CHARGE_AUTH_REJECT
            )
        )
        val expectedResult = Integrated3DSResult(
            IntegratedEventType.CHARGE_AUTH_REJECT,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthCancelledEvent correctly`() {
        val event = Integrated3DSEvent.ChargeAuthCancelledEvent(
            data = IntegratedChargeEventData(
                IntegratedStatus.AUTHENTICATION_CANCELLED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.CHARGE_AUTH_CANCELLED
            )
        )
        val expectedResult = Integrated3DSResult(
            IntegratedEventType.CHARGE_AUTH_CANCELLATION,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps AdditionalDataCollectSuccessEvent correctly`() {
        val event = Integrated3DSEvent.AdditionalDataCollectSuccessEvent(
            data = IntegratedChargeEventData(
                IntegratedStatus.ADDITIONAL_DATA_COMPLETE,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.ADDITIONAL_DATA_COLLECT_SUCCESS
            )
        )
        val expectedResult = Integrated3DSResult(
            IntegratedEventType.ADDITIONAL_DATA_COLLECT_SUCCESS,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps AdditionalDataCollectRejectEvent correctly`() {
        val event = Integrated3DSEvent.AdditionalDataCollectRejectEvent(
            data = IntegratedChargeEventData(
                IntegratedStatus.ADDITIONAL_DATA_FAILED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.ADDITIONAL_DATA_COLLECT_REJECT
            )
        )
        val expectedResult = Integrated3DSResult(
            IntegratedEventType.ADDITIONAL_DATA_COLLECT_REJECT,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthEvent correctly`() {
        val event = Integrated3DSEvent.AdditionalDataCollectRejectEvent(
            data = IntegratedChargeEventData(
                IntegratedStatus.AUTHENTICATED,
                MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID,
                IntegratedEvent.CHARGE_AUTH
            )
        )
        val expectedResult = Integrated3DSResult(
            IntegratedEventType.ADDITIONAL_DATA_COLLECT_REJECT,
            MobileSDKTestConstants.ThreeDS.MOCK_CHARGE_ID
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `asEntity maps ChargeAuthSuccessEvent correctly when chargeId is null`() {
        val event = Integrated3DSEvent.ChargeAuthSuccessEvent(
            data = IntegratedChargeEventData(
                IntegratedStatus.AUTHENTICATED,
                null,
                IntegratedEvent.CHARGE_AUTH
            )
        )
        val expectedResult = Integrated3DSResult(
            IntegratedEventType.CHARGE_AUTH_SUCCESS,
            null
        )

        val actualResult = event.asEntity()

        assertEquals(expectedResult, actualResult)
    }

}