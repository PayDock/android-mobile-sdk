package com.paydock.feature.threeDS.presentation.utils

import com.paydock.feature.threeDS.presentation.model.ThreeDSEvent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A custom JSON serializer for the [ThreeDSEvent] sealed class, used for polymorphic serialization/deserialization.
 */
internal class ThreeDSEventSerializer :
    JsonContentPolymorphicSerializer<ThreeDSEvent>(ThreeDSEvent::class) {

    /**
     * Selects the appropriate deserializer based on the event type specified in the JSON element.
     *
     * @param element The JSON element to deserialize.
     * @return The deserialization strategy for the specified event type.
     * @throws SerializationException if the event type is unknown.
     */
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ThreeDSEvent> {
        return when (element.jsonObject["event"]?.jsonPrimitive?.content) {
            "chargeAuthSuccess" -> ThreeDSEvent.ChargeAuthSuccessEvent.serializer()
            "chargeAuthReject" -> ThreeDSEvent.ChargeAuthRejectEvent.serializer()
            "chargeAuthChallenge" -> ThreeDSEvent.ChargeAuthChallengeEvent.serializer()
            "chargeAuthDecoupled" -> ThreeDSEvent.ChargeAuthDecoupledEvent.serializer()
            "chargeAuthInfo" -> ThreeDSEvent.ChargeAuthInfoEvent.serializer()
            "error" -> ThreeDSEvent.ChargeErrorEvent.serializer()
            else -> throw SerializationException("Unknown 3DS event type")
        }
    }
}
