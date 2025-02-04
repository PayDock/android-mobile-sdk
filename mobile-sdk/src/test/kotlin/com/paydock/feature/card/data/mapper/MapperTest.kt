package com.paydock.feature.card.data.mapper

import com.paydock.core.BaseUnitTest
import com.paydock.core.network.dto.Resource
import com.paydock.core.network.extensions.convertToDataClass
import com.paydock.feature.card.data.dto.CardSchemasResponse
import com.paydock.feature.card.data.dto.CardTokenResponse
import com.paydock.feature.card.domain.model.integration.enums.CardType
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.test.fail

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

    @Test
    fun `asEntity should map valid JSON to CardSchema TreeMap`() {
        // Arrange
        val json = """
        {
          "card_schemas": [
            {"bin": "420412", "schema": "visa"},
            { "bin": "2221~2720", "schema": "mastercard" },
            { "bin": "324000", "schema": "amex" },
            { "bin": "309", "schema": "diners" },
            { "bin": "6011", "schema": "discover" },
            { "bin": "180000~180099", "schema": "japcb" },
            { "bin": "633454", "schema": "solo" },
            { "bin": "5610", "schema": "ausbc" }
          ]
        }
        """.trimIndent()
        val response = json.convertToDataClass<CardSchemasResponse>()
        // Act
        val cardSchemas = response.asEntity()
        // Assert
        assertEquals(8, cardSchemas.size)
        // Assert that the keys are sorted correctly
        val sortedKeys = cardSchemas.keys.sorted()
        assertEquals(listOf(309, 5610, 6011, 420412, 2221, 180000, 633454, 324000).sorted(), sortedKeys)

        // Assert that the values are correct
        cardSchemas.forEach { (_, value) ->
            when (value.schema) {
                CardType.VISA -> {
                    assertEquals("420412", value.bin)
                    assertEquals(CardType.VISA, value.schema)
                }

                CardType.MASTERCARD -> {
                    assertEquals("2221~2720", value.bin)
                    assertEquals(CardType.MASTERCARD, value.schema)
                }

                CardType.DINERS -> {
                    assertEquals("309", value.bin)
                    assertEquals(CardType.DINERS, value.schema)
                }

                CardType.AUSBC -> {
                    assertEquals("5610", value.bin)
                    assertEquals(CardType.AUSBC, value.schema)
                }

                CardType.JAPCB -> {
                    assertEquals("180000~180099", value.bin)
                    assertEquals(CardType.JAPCB, value.schema)
                }

                CardType.AMEX -> {
                    assertEquals("324000", value.bin)
                    assertEquals(CardType.AMEX, value.schema)
                }

                CardType.SOLO -> {
                    assertEquals("633454", value.bin)
                    assertEquals(CardType.SOLO, value.schema)
                }

                CardType.DISCOVER -> {
                    assertEquals("6011", value.bin)
                    assertEquals(CardType.DISCOVER, value.schema)
                }

                null -> fail("Unknown schema: bin: ${value.bin}")
            }
        }
    }

    @Test
    fun `asEntity should handle empty card_schemas array`() {
        // Arrange
        val json = """
            {
              "card_schemas": []
            }
        """.trimIndent()
        val response = json.convertToDataClass<CardSchemasResponse>()

        // Act
        val cardSchemas = response.asEntity()

        // Assert
        assertEquals(0, cardSchemas.size)
    }

    @Test
    fun `asEntity should handle unknown schema`() {
        // Arrange
        val json = """
            {
              "card_schemas": [
                {
                  "bin": "123456",
                  "schema": "unknown_schema"
                }
              ]
            }
        """.trimIndent()
        val response = json.convertToDataClass<CardSchemasResponse>()

        // Act
        val cardSchemas = response.asEntity()

        // Assert
        assertEquals(1, cardSchemas.size)
        assertEquals("123456", cardSchemas.firstEntry()?.value?.bin)
        assertNull(cardSchemas.firstEntry()?.value?.schema) // Assuming NULL is your default
    }
}