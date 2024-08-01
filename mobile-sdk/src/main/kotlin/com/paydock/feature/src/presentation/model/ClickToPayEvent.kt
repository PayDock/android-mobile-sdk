package com.paydock.feature.src.presentation.model

import com.paydock.feature.src.presentation.model.enum.Event
import com.paydock.feature.src.presentation.utils.ClickToPayEventSerializer
import kotlinx.serialization.Serializable

/**
 * Represents events related to ClickToPay interactions.
 *
 * This sealed class encapsulates various types of events that can occur during ClickToPay interactions.
 * Each subclass represents a specific type of event, such as iframe loaded, checkout ready, checkout completed,
 * and checkout error.
 */
@Serializable(with = ClickToPayEventSerializer::class)
internal sealed class ClickToPayEvent {
    /**
     * The type of the event.
     */
    abstract val event: Event

    /**
     * Represents an event indicating that the iframe has been loaded.
     *
     * @property event The type of the event.
     * @property data Additional data associated with the event.
     */
    @Serializable
    data class IframeLoadedEvent(
        override val event: Event = Event.IFRAME_LOADED,
        val data: EventData
    ) : ClickToPayEvent()

    /**
     * Represents an event indicating that checkout is ready.
     *
     * @property event The type of the event.
     * @property data Additional data associated with the event. Can be null if no additional data is available.
     */
    @Serializable
    data class CheckoutReadyEvent(
        override val event: Event = Event.CHECKOUT_READY,
        val data: EventData?
    ) : ClickToPayEvent()

    /**
     * Represents an event indicating that checkout has been completed successfully.
     *
     * @property event The type of the event.
     * @property data Additional data associated with the event, containing information about the checkout.
     */
    @Serializable
    data class CheckoutCompletedEvent(
        override val event: Event = Event.CHECKOUT_COMPLETED,
        val data: SuccessEventData
    ) : ClickToPayEvent()

    /**
     * Represents an event sent when SRC Checkout flow is started, regardless of embedded or windowed mode.
     *
     * @property event The type of the event.
     * @property data Additional data associated with the event, containing information about the checkout.
     */
    @Serializable
    data class CheckoutPopupOpenEvent(
        override val event: Event = Event.CHECKOUT_POPUP_OPEN,
        val data: EventData?
    ) : ClickToPayEvent()

    /**
     * Represents an event sent when SRC Checkout flow is closed, regardless of embedded or windowed mode.
     *
     * @property event The type of the event.
     * @property data Additional data associated with the event, containing information about the checkout.
     */
    @Serializable
    data class CheckoutPopupClosedEvent(
        override val event: Event = Event.CHECKOUT_POPUP_CLOSE,
        val data: EventData?
    ) : ClickToPayEvent()

    /**
     * Represents an event indicating that an error occurred during checkout.
     *
     * @property event The type of the event.
     * @property data Additional data associated with the event, containing information about the error.
     */
    @Serializable
    data class CheckoutErrorEvent(
        override val event: Event = Event.CHECKOUT_ERROR,
        val data: ErrorData
    ) : ClickToPayEvent()
}
