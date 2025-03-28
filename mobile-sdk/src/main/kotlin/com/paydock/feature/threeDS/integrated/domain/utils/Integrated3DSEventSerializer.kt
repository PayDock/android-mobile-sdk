package com.paydock.feature.threeDS.integrated.domain.utils

import com.paydock.feature.threeDS.integrated.domain.model.ui.Integrated3DSEvent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Custom serializer for deserializing JSON payloads into [Integrated3DSEvent] instances.
 *
 * This serializer extends [JsonContentPolymorphicSerializer] to dynamically select the appropriate
 * deserialization strategy based on the `event` field in the JSON payload.
 *
 * It ensures that incoming JSON messages from the WebView are mapped to the correct event type
 * in the integrated 3DS flow.
 */
internal class Integrated3DSEventSerializer :
    JsonContentPolymorphicSerializer<Integrated3DSEvent>(Integrated3DSEvent::class) {

    /**
     * Determines the appropriate deserialization strategy based on the `event` field in the JSON.
     *
     * The function inspects the `event` field within the received JSON object and returns the
     * corresponding serializer for the specific [Integrated3DSEvent] type.
     *
     * @param element The JSON element containing the event data.
     * @return The deserialization strategy for the detected event type.
     * @throws SerializationException if the event type is unknown.
     */
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Integrated3DSEvent> {
        return when (element.jsonObject["event"]?.jsonPrimitive?.content) {
            "chargeAuthSuccess" -> Integrated3DSEvent.ChargeAuthSuccessEvent.serializer()
            "chargeAuthReject" -> Integrated3DSEvent.ChargeAuthRejectEvent.serializer()
            "chargeAuthCancelled" -> Integrated3DSEvent.ChargeAuthRejectEvent.serializer()
            "additionalDataCollectSuccess" -> Integrated3DSEvent.AdditionalDataCollectSuccessEvent.serializer()
            "additionalDataCollectReject" -> Integrated3DSEvent.AdditionalDataCollectRejectEvent.serializer()
            "chargeAuth" -> Integrated3DSEvent.ChargeAuthEvent.serializer()
            else -> throw SerializationException("Unknown 3DS event type: [${element.jsonObject["event"]?.jsonPrimitive?.content}]")
        }
    }
}
