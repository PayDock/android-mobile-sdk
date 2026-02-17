package com.paydock.feature.threeDS.integrated.domain.utils

import com.paydock.feature.threeDS.integrated.domain.model.ui.enums.MPGS3dsStatus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull

/**
 * Serializer for the [MPGS3dsStatus] enum.
 *
 * This object provides the logic for serializing and deserializing [MPGS3dsStatus] objects
 * to and from their string representations. It handles both valid [MPGS3dsStatus] values
 * and `null` values.
 */
internal object MPGS3dsStatusSerializer : KSerializer<MPGS3dsStatus?> {

    /**
     * The serial descriptor for the [MPGS3dsStatus] serializer.
     * Represents the primitive type [MPGS3dsStatus] used to serialize and deserialize the enum.
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Status", PrimitiveKind.STRING).nullable

    /**
     * Deserializes a string value into a [MPGS3dsStatus] instance.
     *
     * @param decoder The [Decoder] instance used to read the serialized string value.
     * @return The corresponding [MPGS3dsStatus] instance if the value is valid, or `null` if no match is found.
     */
    override fun deserialize(decoder: Decoder): MPGS3dsStatus? {
        return if (decoder is JsonDecoder && decoder.decodeJsonElement() is JsonNull) {
            null
        } else {
            val value = decoder.decodeString()
            MPGS3dsStatus.fromValue(value)
        }
    }

    /**
     * Serializes a [MPGS3dsStatus] instance into its string representation.
     * If the value is `null`, it encodes it as `null`.
     *
     * @param encoder The [Encoder] instance used to write the serialized string value.
     * @param value The [MPGS3dsStatus] instance to serialize, or `null` if no value is present.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: MPGS3dsStatus?) {
        if (value != null) {
            encoder.encodeString(value.name)
        } else {
            encoder.encodeNull()
        }
    }
}