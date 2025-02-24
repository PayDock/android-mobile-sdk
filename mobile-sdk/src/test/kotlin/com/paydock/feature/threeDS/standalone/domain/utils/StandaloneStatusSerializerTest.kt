package com.paydock.feature.threeDS.standalone.domain.utils

import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class StandaloneStatusSerializerTest {

    private val serializer = StandaloneStatusSerializer

    @Test
    fun `descriptor should have correct name and kind`() {
        val descriptor: SerialDescriptor = serializer.descriptor
        assertEquals("Status?", descriptor.serialName)
        assertEquals(true, descriptor.isNullable)
        assertEquals(PrimitiveKind.STRING, descriptor.kind)
    }

    @Test
    fun `deserialize should return correct StandaloneStatus for valid string`() {
        val decoder: Decoder = mock()
        whenever(decoder.decodeString()).thenReturn("pending")

        val result = serializer.deserialize(decoder)

        assertEquals(StandaloneStatus.PENDING, result)
        verify(decoder).decodeString()
    }

    @Test
    fun `deserialize should return null for invalid string`() {
        val decoder: Decoder = mock()
        whenever(decoder.decodeString()).thenReturn("INVALID")

        val result = serializer.deserialize(decoder)

        assertEquals(null, result)
        verify(decoder).decodeString()
    }

    @Test
    fun `deserialize should return null for empty string`() {
        val decoder: Decoder = mock()
        whenever(decoder.decodeString()).thenReturn("")

        val result = serializer.deserialize(decoder)

        assertEquals(null, result)
        verify(decoder).decodeString()
    }

    @Test
    fun `serialize should encode StandaloneStatus name`() {
        val encoder: Encoder = mock()
        val status = StandaloneStatus.SUCCESS

        serializer.serialize(encoder, status)

        verify(encoder).encodeString("SUCCESS")
    }

    @Test
    fun `serialize should encode null when value is null`() {
        val encoder: Encoder = mock()

        serializer.serialize(encoder, null)

        verify(encoder).encodeNull()
    }

    @Test
    fun `serialize and deserialize with Json should work correctly for non-null value`() {
        val status = StandaloneStatus.PENDING
        val json = Json.encodeToJsonElement(serializer, status)
        val decodedStatus = Json.decodeFromJsonElement(serializer, json)

        assertEquals(StandaloneStatus.PENDING, decodedStatus)
        assertEquals(JsonPrimitive("PENDING"), json)
    }

    @Test
    fun `serialize and deserialize with Json should work correctly for null value`() {
        val status: StandaloneStatus? = null
        val json = Json.encodeToJsonElement(serializer, status)
        val decodedStatus = Json.decodeFromJsonElement(serializer, json)

        assertEquals(null, decodedStatus)
        assertEquals(JsonNull, json)
    }

}