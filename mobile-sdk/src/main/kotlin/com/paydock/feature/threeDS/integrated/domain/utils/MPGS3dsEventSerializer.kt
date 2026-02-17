package com.paydock.feature.threeDS.integrated.domain.utils

import com.paydock.feature.threeDS.integrated.domain.model.ui.MPGS3dsEvent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Custom serializer for deserializing JSON payloads into [MPGS3dsEvent] instances.
 *
 * This serializer extends [JsonContentPolymorphicSerializer] to dynamically select the appropriate
 * deserialization strategy based on the `event` field in the JSON payload.
 *
 * It ensures that incoming JSON messages from the WebView are mapped to the correct event type
 * in the MPGS 3DS flow.
 */
internal class MPGS3dsEventSerializer :
    JsonContentPolymorphicSerializer<MPGS3dsEvent>(MPGS3dsEvent::class) {

    /**
     * Determines the appropriate deserialization strategy based on the `event` field in the JSON.
     *
     * The function inspects the `event` field within the received JSON object and returns the
     * corresponding serializer for the specific [MPGS3dsEvent] type.
     *
     * @param element The JSON element containing the event data.
     * @return The deserialization strategy for the detected event type.
     * @throws SerializationException if the event type is unknown.
     */
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<MPGS3dsEvent> {
        return when (element.jsonObject["event"]?.jsonPrimitive?.content) {
            "chargeAuthSuccess" -> MPGS3dsEvent.ChargeAuthSuccessEvent.serializer()
            "chargeAuthReject" -> MPGS3dsEvent.ChargeAuthRejectEvent.serializer()
            "chargeAuthCancelled" -> MPGS3dsEvent.ChargeAuthRejectEvent.serializer()
            "additionalDataCollectSuccess" -> MPGS3dsEvent.AdditionalDataCollectSuccessEvent.serializer()
            "additionalDataCollectReject" -> MPGS3dsEvent.AdditionalDataCollectRejectEvent.serializer()
            "chargeAuth" -> MPGS3dsEvent.ChargeAuthEvent.serializer()
            else -> throw SerializationException("Unknown 3DS event type: [${element.jsonObject["event"]?.jsonPrimitive?.content}]")
        }
    }
}
