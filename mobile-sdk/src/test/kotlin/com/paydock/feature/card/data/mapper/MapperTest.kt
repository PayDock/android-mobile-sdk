package com.paydock.feature.card.data.mapper

import com.paydock.core.BaseUnitTest
import com.paydock.core.network.dto.Resource
import com.paydock.feature.card.data.dto.CardTokenResponse
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

internal class MapperTest : BaseUnitTest() {

    @Test
    fun testTokeniseCardDetailsMappingFromTokeniseCardResponse() {
        val mockResponse = mockk<CardTokenResponse>()
        val mockResource = mockk<Resource<String>>()

        every { mockResponse.status } returns 201
        every { mockResponse.resource } returns mockResource
        every { mockResource.type } returns "token"
        every { mockResource.data } returns "fe0f6a4b-1c8f-4693-a185-0baa0745e673"

        val tokeniseCardDetails = mockResponse.asEntity()

        assertEquals(mockResource.type, tokeniseCardDetails.type)
        assertEquals(mockResource.data, tokeniseCardDetails.token)
    }
}
