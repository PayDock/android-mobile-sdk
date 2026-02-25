package com.paydock.feature.src.presentation.utils

import com.paydock.feature.src.domain.model.ui.ErrorData
import com.paydock.feature.src.domain.model.ui.enums.EventDataType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
            "UserError" -> UserErrorDataFlexibleSerializer
            "CriticalError" -> ErrorData.CriticalErrorData.serializer()
            else -> throw SerializationException("Unknown error type: $type")
        }
    }
}

/**
 * Custom serializer for [ErrorData.UserErrorData] that accepts both formats for the `data` field:
 * - String (legacy): `{"type":"UserError","data":"Request failed due to CARD_MISSING"}`
 * - Array (Web SDK): `{"type":"UserError","data":[{"field":"unknown","message":"...","originalError":{...}}]}`
 *
 * When `data` is an array, extracts the first error's `message` or `originalError.message`.
 */
private object UserErrorDataFlexibleSerializer : KSerializer<ErrorData.UserErrorData> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("UserErrorData") {
        element<String>("type")
        element<String>("data")
    }

    override fun deserialize(decoder: Decoder): ErrorData.UserErrorData {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        val jsonObject = element.jsonObject
        val dataElement = jsonObject["data"] ?: throw SerializationException("Missing 'data' in UserError")
        val dataString = when (dataElement) {
            is JsonPrimitive -> dataElement.content
            is JsonArray -> {
                val firstObj = dataElement.firstOrNull() as? JsonObject
                    ?: throw SerializationException("UserError data array is empty or invalid")
                firstObj["message"]?.jsonPrimitive?.contentOrNull
                    ?: (firstObj["originalError"] as? JsonObject)?.get("message")?.jsonPrimitive?.contentOrNull
                    ?: firstObj.toString()
            }
            else -> throw SerializationException("UserError 'data' must be string or array, got ${dataElement::class.simpleName}")
        }
        return ErrorData.UserErrorData(
            type = EventDataType.USER_ERROR,
            data = dataString
        )
    }

    override fun serialize(encoder: Encoder, value: ErrorData.UserErrorData) {
        ErrorData.UserErrorData.serializer().serialize(encoder, value)
    }
}
