package com.paydock.feature.paypal.data.mapper

import com.paydock.core.BaseUnitTest
import com.paydock.core.MobileSDKTestConstants
import com.paydock.core.network.dto.Resource
import com.paydock.feature.paypal.core.data.dto.PayPalPaymentTokenData
import com.paydock.feature.paypal.core.data.dto.PayPalVaultTokenResponse
import com.paydock.feature.paypal.core.data.mapper.asEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

internal class MapperTest : BaseUnitTest() {

    @Test
    fun testPayPalPaymentTokenDetailsMappingFromPayPalVaultTokenResponse() {
        val mockResponse = mockk<PayPalVaultTokenResponse>()
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