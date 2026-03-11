package com.paydock.core.utils.serializer

import com.paydock.core.utils.toSafeAmount
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal

@OptIn(ExperimentalSerializationApi::class)
internal object BigDecimalSerializer : KSerializer<BigDecimal> {

    override val descriptor =
        PrimitiveSerialDescriptor("java.math.BigDecimal", PrimitiveKind.DOUBLE)

    /**
     * If decoding JSON uses [JsonDecoder.decodeJsonElement] to get the raw content,
     * otherwise decodes using [Decoder.decodeString].
     * Normalizes to 2 decimal places to avoid floating-point precision issues from JSON numbers.
     */
    override fun deserialize(decoder: Decoder): BigDecimal =
        when (decoder) {
            is JsonDecoder -> decoder.decodeJsonElement().jsonPrimitive.content.toBigDecimal().toSafeAmount()
            else -> decoder.decodeString().toBigDecimal().toSafeAmount()
        }

    /**
     * If encoding JSON uses [JsonUnquotedLiteral] to encode the [BigDecimal] value.
     * Normalizes to 2 decimal places before encoding to avoid floating-point precision issues.
     *
     * Otherwise, [value] is encoded using [Encoder.encodeString].
     */
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        val normalized = value.toSafeAmount().toPlainString()
        when (encoder) {
            is JsonEncoder -> encoder.encodeJsonElement(JsonUnquotedLiteral(normalized))
            else -> encoder.encodeString(normalized)
        }
    }
}