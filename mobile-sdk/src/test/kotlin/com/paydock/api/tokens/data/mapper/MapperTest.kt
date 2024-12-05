package com.paydock.api.tokens.data.mapper

import com.paydock.api.tokens.data.dto.PayPalPaymentTokenData
import com.paydock.api.tokens.data.dto.PaymentTokenResponse
import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.network.dto.Resource
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

internal class MapperTest : BaseUnitTest() {

    @Test
    fun testTokeniseCardDetailsMappingFromTokeniseCardResponse() {
        val mockResponse = mockk<PaymentTokenResponse.CardTokenResponse>()
        val mockResource = mockk<Resource<String>>()

        every { mockResponse.status } returns 201
        every { mockResponse.resource } returns mockResource
        every { mockResource.type } returns "token"
        every { mockResource.data } returns "fe0f6a4b-1c8f-4693-a185-0baa0745e673"

        val tokeniseCardDetails = mockResponse.asEntity()

        assertEquals(mockResource.type, tokeniseCardDetails.type)
        assertEquals(mockResource.data, tokeniseCardDetails.token)
    }

    @Test
    fun testPayPalPaymentTokenDetailsMappingFromPayPalVaultTokenResponse() {
        val mockResponse = mockk<PaymentTokenResponse.PayPalVaultTokenResponse>()
        val mockResource = mockk<Resource<PayPalPaymentTokenData>>()
        val mockData = mockk<PayPalPaymentTokenData>()

        every { mockResponse.status } returns 201
        every { mockResponse.resource } returns mockResource
        every { mockResource.type } returns "token"
        every { mockResource.data } returns mockData
        every { mockData.token } returns MobileSDKTestConstants.PayPalVault.MOCK_PAYMENT_TOKEN
        every { mockData.email } returns MobileSDKTestConstants.PayPalVault.MOCK_EMAIL

        val payPalTokenDetails = mockResponse.asEntity()

        assertEquals(mockData.email, payPalTokenDetails.email)
        assertEquals(mockData.token, payPalTokenDetails.token)
    }
}