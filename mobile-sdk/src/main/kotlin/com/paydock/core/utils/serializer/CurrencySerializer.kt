package com.paydock.core.utils.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Currency

/**
 * Serializer for Currency objects.
 */
internal object CurrencySerializer : KSerializer<Currency> {

    /**
     * The descriptor for the serializer.
     */
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "Currency",
        kind = PrimitiveKind.STRING,
    )

    /**
     * Deserializes a Currency object from the provided decoder.
     *
     * @param decoder The decoder used for deserialization.
     * @return The deserialized Currency object.
     */
    override fun deserialize(decoder: Decoder): Currency =
        Currency.getInstance(decoder.decodeString())

    /**
     * Serializes the given Currency object using the provided encoder.
     *
     * @param encoder The encoder used for serialization.
     * @param value The Currency object to be serialized.
     */
    override fun serialize(encoder: Encoder, value: Currency) =
        encoder.encodeString(value.currencyCode)
}
