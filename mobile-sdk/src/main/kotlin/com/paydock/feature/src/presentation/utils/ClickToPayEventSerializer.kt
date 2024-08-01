package com.paydock.feature.src.presentation.utils

import com.paydock.feature.src.presentation.model.ClickToPayEvent
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Custom JSON serializer for serializing and deserializing Click to Pay events with polymorphic behavior.
 * This serializer selects the appropriate deserializer based on the 'event' property in the JSON element.
 */
internal class ClickToPayEventSerializer : JsonContentPolymorphicSerializer<ClickToPayEvent>(
    ClickToPayEvent::class
) {

    /**
     * Selects the appropriate deserializer based on the 'event' property in the JSON element.
     *
     * @param element The JSON element to be deserialized.
     * @return The deserialization strategy for the specified ClickToPayEvent subtype.
     * @throws SerializationException if the 'event' property is unknown or missing.
     */
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ClickToPayEvent> {
        return when (element.jsonObject["event"]?.jsonPrimitive?.content) {
            "checkoutCompleted" -> ClickToPayEvent.CheckoutCompletedEvent.serializer()
            "checkoutPopupOpen" -> ClickToPayEvent.CheckoutPopupOpenEvent.serializer()
            "checkoutPopupClose" -> ClickToPayEvent.CheckoutPopupClosedEvent.serializer()
            "checkoutError" -> ClickToPayEvent.CheckoutErrorEvent.serializer()
            "iframeLoaded" -> ClickToPayEvent.IframeLoadedEvent.serializer()
            "checkoutReady" -> ClickToPayEvent.CheckoutReadyEvent.serializer()
            else -> throw SerializationException("Unknown ClickToPay event type")
        }
    }
}
