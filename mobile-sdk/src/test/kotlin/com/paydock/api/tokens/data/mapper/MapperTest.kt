package com.paydock.api.tokens.data.mapper

import com.paydock.api.tokens.data.dto.AuthTokenData
import com.paydock.api.tokens.data.dto.PaymentTokenResponse
import com.paydock.api.tokens.data.dto.SessionAuthTokenResponse
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
    fun testPayPalSessionAuthMappingFromPayPalSessionAuthResponse() {
        val mockResponse = mockk<SessionAuthTokenResponse>()
        val mockResource = mockk<Resource<AuthTokenData>>()
        val mockData = mockk<AuthTokenData>()

        every { mockResponse.status } returns 201
        every { mockResponse.resource } returns mockResource
        every { mockResource.type } returns "oauth-token"
        every { mockResource.data } returns mockData
        every {
            mockData.idToken
        } returns MobileSDKTestConstants.PayPalVault.MOCK_ID_TOKEN
        every {
            mockData.accessToken
        } returns MobileSDKTestConstants.PayPalVault.MOCK_ACCESS_TOKEN

        val payPalSessionAuth = mockResponse.asEntity()

        assertEquals(mockData.accessToken, payPalSessionAuth.accessToken)
        assertEquals(mockData.idToken, payPalSessionAuth.idToken)
    }

}