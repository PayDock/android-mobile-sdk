package com.paydock.feature.src.presentation.utils

import com.paydock.feature.src.presentation.model.ErrorData
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Custom JSON serializer for serializing and deserializing ErrorData objects with polymorphic behavior.
 * This serializer selects the appropriate deserializer based on the 'type' property in the JSON element.
 */
internal class ClickToPayErrorDataSerializer :
    JsonContentPolymorphicSerializer<ErrorData>(ErrorData::class) {

    /**
     * Selects the appropriate deserializer based on the 'type' property in the JSON element.
     *
     * @param element The JSON element to be deserialized.
     * @return The deserialization strategy for the specified ErrorData subtype.
     * @throws SerializationException if the 'type' property is unknown or missing.
     */
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ErrorData> {
        val jsonObject = element.jsonObject
        return when (val type = jsonObject["type"]?.jsonPrimitive?.contentOrNull) {
            "UserError" -> ErrorData.UserErrorData.serializer()
            "CriticalError" -> ErrorData.CriticalErrorData.serializer()
            else -> throw SerializationException("Unknown error type: $type")
        }
    }
}
