package com.paydock.feature.card.data.utils

import com.paydock.feature.card.data.dto.Schema
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for the [Schema] enum class, supporting nullable values.
 *
 * This serializer is responsible for converting [Schema] instances to their string representations
 * during serialization and mapping string values back to [Schema] instances during deserialization.
 * It supports nullable values, encoding them as `null` when necessary.
 */
internal object SchemaSerializer : KSerializer<Schema?> {

    /**
     * The serial descriptor for the [Schema] serializer.
     * Represents the primitive type [String] used to serialize and deserialize the enum.
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Schema", PrimitiveKind.STRING)

    /**
     * Deserializes a string value into a [Schema] instance.
     *
     * @param decoder The [Decoder] instance used to read the serialized string value.
     * @return The corresponding [Schema] instance if the value is valid, or `null` if no match is found.
     */
    override fun deserialize(decoder: Decoder): Schema? {
        val value = decoder.decodeString()
        return Schema.fromValue(value)
    }

    /**
     * Serializes a [Schema] instance into its string representation.
     * If the value is `null`, it encodes it as `null`.
     *
     * @param encoder The [Encoder] instance used to write the serialized string value.
     * @param value The [Schema] instance to serialize, or `null` if no value is present.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Schema?) {
        if (value != null) {
            encoder.encodeString(value.name)
        } else {
            encoder.encodeNull()
        }
    }
}