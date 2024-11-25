package com.paydock.api.charges.data.mapper

import com.paydock.api.charges.data.dto.CallbackChargeData
import com.paydock.api.charges.data.dto.CallbackData
import com.paydock.api.charges.data.dto.CaptureChargeResponse
import com.paydock.api.charges.data.dto.CaptureData
import com.paydock.api.charges.data.dto.WalletCallbackResponse
import com.paydock.core.BaseUnitTest
import com.paydock.core.network.dto.Resource
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

internal class MapperTest : BaseUnitTest() {

    @Test
    fun testChargeResponseMappingFromWalletCaptureResponse() {
        val mockResponse = mockk<CaptureChargeResponse>()
        val mockResource = mockk<Resource<CaptureData>>()
        val mockData = mockk<CaptureData>()

        every { mockResponse.status } returns 200
        every { mockResponse.resource } returns mockResource
        every { mockResource.type } returns "charge"
        every { mockResource.data } returns mockData
        every { mockData.status } returns "wallet_initialized"
        every { mockData.id } returns "659bec8f5f64b3a77113d51d"
        every { mockData.amount } returns BigDecimal(10)
        every { mockData.currency } returns "AUD"

        val chargeResponse = mockResponse.asEntity()

        assertEquals(mockResponse.status, chargeResponse.status)
        assertEquals(mockResource.type, chargeResponse.resource.type)
        assertEquals(mockData.status, chargeResponse.resource.data?.status)
        assertEquals(mockData.id, chargeResponse.resource.data?.id)
        assertEquals(mockData.amount, chargeResponse.resource.data?.amount)
        assertEquals(mockData.currency, chargeResponse.resource.data?.currency)
    }

    @Test
    fun testChargeResponseMappingFromWalletCallbackResponse() {
        val mockResponse = mockk<WalletCallbackResponse>()
        val mockResource = mockk<Resource<CallbackData>>()
        val mockData = mockk<CallbackData>()
        val mockCharge = mockk<CallbackChargeData>()

        every { mockResponse.status } returns 200
        every { mockResponse.resource } returns mockResource
        every { mockResource.type } returns "charge"
        every { mockResource.data } returns mockData
        every { mockData.status } returns "CREATED"
        every { mockData.id } returns "0AT311688E3149121"
        every { mockData.callbackMethod } returns "GET"
        every { mockData.callbackRel } returns "approve"
        every { mockData.callbackUrl } returns "https://www.sandbox.paypal.com/checkoutnow?token=0AT311688E3149121"
        every { mockData.refToken } returns null
        every { mockData.charge } returns mockCharge
        every { mockCharge.status } returns "wallet_initialized"

        val walletCallback = mockResponse.asEntity()

        assertEquals(mockData.id, walletCallback.callbackId)
        assertEquals(mockData.callbackUrl, walletCallback.callbackUrl)
        assertEquals(mockCharge.status, walletCallback.status)
    }

}