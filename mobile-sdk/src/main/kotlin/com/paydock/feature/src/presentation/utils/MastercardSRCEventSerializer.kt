package com.paydock.feature.src.presentation.utils

import com.paydock.feature.src.presentation.model.MastercardSRCEvent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Custom JSON serializer for serializing and deserializing Mastercard SRC events with polymorphic behavior.
 * This serializer selects the appropriate deserializer based on the 'event' property in the JSON element.
 */
internal class MastercardSRCEventSerializer : JsonContentPolymorphicSerializer<MastercardSRCEvent>(
    MastercardSRCEvent::class
) {

    /**
     * Selects the appropriate deserializer based on the 'event' property in the JSON element.
     *
     * @param element The JSON element to be deserialized.
     * @return The deserialization strategy for the specified MastercardSRCEvent subtype.
     * @throws SerializationException if the 'event' property is unknown or missing.
     */
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<MastercardSRCEvent> {
        return when (element.jsonObject["event"]?.jsonPrimitive?.content) {
            "checkoutCompleted" -> MastercardSRCEvent.CheckoutCompletedEvent.serializer()
            "checkoutPopupOpen" -> MastercardSRCEvent.CheckoutPopupOpenEvent.serializer()
            "checkoutPopupClose" -> MastercardSRCEvent.CheckoutPopupClosedEvent.serializer()
            "checkoutError" -> MastercardSRCEvent.CheckoutErrorEvent.serializer()
            "iframeLoaded" -> MastercardSRCEvent.IframeLoadedEvent.serializer()
            "checkoutReady" -> MastercardSRCEvent.CheckoutReadyEvent.serializer()
            else -> throw SerializationException("Unknown Mastercard SRC event type")
        }
    }
}
