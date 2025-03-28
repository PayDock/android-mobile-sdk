package com.paydock.feature.threeDS.standalone.domain.utils

import com.paydock.feature.threeDS.standalone.domain.model.ui.enums.StandaloneStatus
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
 * Serializer for the [StandaloneStatus] enum.
 *
 * This object provides the logic for serializing and deserializing [StandaloneStatus] objects
 * to and from their string representations. It handles both valid [StandaloneStatus] values
 * and `null` values.
 */
internal object StandaloneStatusSerializer : KSerializer<StandaloneStatus?> {

    /**
     * The serial descriptor for the [StandaloneStatus] serializer.
     * Represents the primitive type [StandaloneStatus] used to serialize and deserialize the enum.
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Status", PrimitiveKind.STRING).nullable

    /**
     * Deserializes a string value into a [StandaloneStatus] instance.
     *
     * @param decoder The [Decoder] instance used to read the serialized string value.
     * @return The corresponding [StandaloneStatus] instance if the value is valid, or `null` if no match is found.
     */
    override fun deserialize(decoder: Decoder): StandaloneStatus? {
        return if (decoder is JsonDecoder && decoder.decodeJsonElement() is JsonNull) {
            null
        } else {
            val value = decoder.decodeString()
            StandaloneStatus.fromValue(value)
        }
    }

    /**
     * Serializes a [StandaloneStatus] instance into its string representation.
     * If the value is `null`, it encodes it as `null`.
     *
     * @param encoder The [Encoder] instance used to write the serialized string value.
     * @param value The [StandaloneStatus] instance to serialize, or `null` if no value is present.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: StandaloneStatus?) {
        if (value != null) {
            encoder.encodeString(value.name)
        } else {
            encoder.encodeNull()
        }
    }
}