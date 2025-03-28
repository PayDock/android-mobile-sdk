package com.paydock.feature.threeDS.standalone.domain.utils

import com.paydock.feature.threeDS.standalone.domain.model.ui.Standalone3DSEvent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A custom JSON polymorphic serializer for deserializing different types of 3D Secure (3DS) standalone events.
 *
 * This serializer inspects the "event" field in the JSON payload and dynamically selects the appropriate
 * deserialization strategy for a specific [Standalone3DSEvent] subtype.
 *
 * It ensures that the correct event type (e.g., `ChargeAuthSuccessEvent`, `ChargeAuthRejectEvent`)
 * is deserialized based on the provided JSON content.
 */
internal class Standalone3DSEventSerializer :
    JsonContentPolymorphicSerializer<Standalone3DSEvent>(Standalone3DSEvent::class) {

    /**
     * Determines the appropriate deserialization strategy based on the "event" field in the JSON object.
     *
     * This function extracts the value of the "event" field and maps it to the corresponding event serializer.
     * If the event type is unknown or missing, a [SerializationException] is thrown.
     *
     * @param element The JSON element containing the event data.
     * @return The [DeserializationStrategy] for the matching [Standalone3DSEvent] subtype.
     * @throws SerializationException If the "event" field is missing or contains an unrecognized value.
     */
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Standalone3DSEvent> {
        return when (element.jsonObject["event"]?.jsonPrimitive?.content) {
            "chargeAuthSuccess" -> Standalone3DSEvent.ChargeAuthSuccessEvent.serializer()
            "chargeAuthReject" -> Standalone3DSEvent.ChargeAuthRejectEvent.serializer()
            "chargeAuthChallenge" -> Standalone3DSEvent.ChargeAuthChallengeEvent.serializer()
            "chargeAuthDecoupled" -> Standalone3DSEvent.ChargeAuthDecoupledEvent.serializer()
            "chargeAuthInfo" -> Standalone3DSEvent.ChargeAuthInfoEvent.serializer()
            "error" -> Standalone3DSEvent.ChargeErrorEvent.serializer()
            else -> throw SerializationException("Unknown 3DS event type")
        }
    }
}
